-- =============================================================================
-- LEVELUP CODE — PAQUETE Y COMPONENTES PL/SQL (ORACLE 18c XE / 21c / 23ai)
-- Asignatura: Bases de Datos & Programación III — Universidad Popular del Cesar
-- Autor: Misael Andrés Maestre Acosta (Tech Lead)
--
-- Contenido del archivo:
--   1. TRIGGER: TRG_AUDIT_USUARIOS (Auditoría reactiva de actividad y vidas)
--   2. PACKAGE SPEC: PKG_GAMIFICACION (Definición de funciones y procedimientos)
--   3. PACKAGE BODY: PKG_GAMIFICACION (Implementación transaccional y algoritmos)
--
-- Ejecutar conectado en Oracle SQL Developer con el usuario: LEVELUP_ADMIN
-- =============================================================================

-- =============================================================================
-- 1. TRIGGER A NIVEL DE FILA: TRG_AUDIT_USUARIOS
-- Actualiza automáticamente la estampa de última actividad y previene vidas < 0.
-- =============================================================================
CREATE OR REPLACE TRIGGER TRG_AUDIT_USUARIOS
BEFORE UPDATE ON Usuarios
FOR EACH ROW
BEGIN
    -- Actualizar estampa de última actividad automáticamente en cada actualización
    :NEW.ultima_actividad := SYSTIMESTAMP;

    -- Validar regla de integridad de vidas
    IF :NEW.vidas < 0 THEN
        :NEW.vidas := 0;
    END IF;

    -- Prevenir que la experiencia total se reduzca de manera inconsistente
    IF :NEW.exp_total < :OLD.exp_total THEN
        :NEW.exp_total := :OLD.exp_total;
    END IF;
END TRG_AUDIT_USUARIOS;
/

-- =============================================================================
-- 2. ESPECIFICACIÓN DEL PAQUETE: PKG_GAMIFICACION
-- =============================================================================
CREATE OR REPLACE PACKAGE PKG_GAMIFICACION AS

    -- Constantes de negocio
    C_XP_POR_NIVEL     CONSTANT NUMBER := 300;
    C_VIDAS_MAXIMAS    CONSTANT NUMBER := 5;
    C_ESTADO_COMPLETADA CONSTANT VARCHAR2(15) := 'COMPLETADA';
    C_ESTADO_ENCURSO    CONSTANT VARCHAR2(15) := 'EN_CURSO';

    -- -------------------------------------------------------------------------
    -- FUNCIONES
    -- -------------------------------------------------------------------------

    /**
     * Calcula determinísticamente el nivel del estudiante según su experiencia acumulada.
     * Fórmula: Nivel base 1 + (exp_total / 300).
     */
    FUNCTION FN_CALCULAR_NIVEL(
        p_exp_total IN NUMBER
    ) RETURN NUMBER;

    /**
     * Calcula la racha continua de días activos del estudiante analizando la tabla
     * Actividad_Diaria en orden descendente.
     */
    FUNCTION FN_CALCULAR_RACHA_ACTIVA(
        p_id_usuario IN NUMBER
    ) RETURN NUMBER;

    /**
     * Calcula el porcentaje de avance (0 a 100%) en un lenguaje de programación específico,
     * comparando lecciones completadas frente al total de lecciones de sus módulos.
     */
    FUNCTION FN_PORCENTAJE_PROGRESO(
        p_id_usuario  IN NUMBER,
        p_id_lenguaje IN NUMBER
    ) RETURN NUMBER;

    -- -------------------------------------------------------------------------
    -- PROCEDIMIENTOS ALMACENADOS
    -- -------------------------------------------------------------------------

    /**
     * Transacción atómica que procesa el envío de un reto:
     *   1. Inserta en Intentos.
     *   2. Si es correcto y es la primera vez que se supera:
     *      - Otorga XP.
     *      - Actualiza nivel en Usuarios.
     *      - Registra/acumula en Actividad_Diaria (MERGE).
     *      - Evalúa y actualiza la racha actual y máxima.
     *      - Evalúa y desbloquea logros en Usuario_Logro si se cumplen condiciones.
     *   3. Si es incorrecto:
     *      - Resta 1 vida en Usuarios (mínimo 0).
     *   4. Retorna los estados resultantes en parámetros OUT para el cliente Java.
     */
    PROCEDURE SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         IN  NUMBER,
        p_id_reto            IN  NUMBER,
        p_es_correcto        IN  NUMBER,
        p_codigo_enviado     IN  CLOB,
        p_salida_obtenida    IN  CLOB,
        p_tiempo_ms          IN  NUMBER,
        p_nuevo_nivel        OUT NUMBER,
        p_nueva_exp          OUT NUMBER,
        p_vidas_restantes    OUT NUMBER,
        p_exp_otorgada       OUT NUMBER,
        p_id_intento_creado  OUT NUMBER
    );

    /**
     * Registra la finalización de una lección en Progreso_Leccion:
     *   - Cambia estado a 'COMPLETADA' con estampa de tiempo.
     *   - Si no estaba completada previamente, otorga la XP de la lección y actualiza nivel.
     */
    PROCEDURE SP_COMPLETAR_LECCION(
        p_id_usuario     IN  NUMBER,
        p_id_leccion     IN  NUMBER,
        p_xp_otorgada    OUT NUMBER,
        p_nuevo_nivel    OUT NUMBER
    );

    /**
     * Restaura las vidas del estudiante al valor máximo permitido (5 vidas).
     */
    PROCEDURE SP_RECUPERAR_VIDAS(
        p_id_usuario          IN  NUMBER,
        p_vidas_restauradas   OUT NUMBER
    );

END PKG_GAMIFICACION;
/

-- =============================================================================
-- 3. CUERPO DEL PAQUETE: PKG_GAMIFICACION (IMPLEMENTACIÓN)
-- =============================================================================
CREATE OR REPLACE PACKAGE BODY PKG_GAMIFICACION AS

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: FN_CALCULAR_NIVEL
    -- -------------------------------------------------------------------------
    FUNCTION FN_CALCULAR_NIVEL(
        p_exp_total IN NUMBER
    ) RETURN NUMBER IS
        v_nivel NUMBER;
    BEGIN
        IF p_exp_total IS NULL OR p_exp_total <= 0 THEN
            RETURN 1;
        END IF;

        v_nivel := GREATEST(1, FLOOR(1 + (p_exp_total / C_XP_POR_NIVEL)));
        RETURN v_nivel;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 1;
    END FN_CALCULAR_NIVEL;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: FN_CALCULAR_RACHA_ACTIVA
    -- -------------------------------------------------------------------------
    FUNCTION FN_CALCULAR_RACHA_ACTIVA(
        p_id_usuario IN NUMBER
    ) RETURN NUMBER IS
        v_racha     NUMBER := 0;
        v_fecha_esp DATE := TRUNC(SYSDATE);
        v_primera   BOOLEAN := TRUE;

        -- Cursor explícito ordenado por fecha descendente
        CURSOR c_actividades IS
            SELECT fecha
            FROM Actividad_Diaria
            WHERE id_usuario = p_id_usuario
            ORDER BY fecha DESC;
    BEGIN
        FOR r IN c_actividades LOOP
            IF v_primera THEN
                -- Permite que la racha siga viva si la última actividad fue hoy o ayer
                IF r.fecha = v_fecha_esp THEN
                    v_racha := v_racha + 1;
                    v_fecha_esp := v_fecha_esp - 1;
                ELSIF r.fecha = (v_fecha_esp - 1) THEN
                    v_racha := v_racha + 1;
                    v_fecha_esp := r.fecha - 1;
                ELSE
                    EXIT; -- Se rompió la continuidad
                END IF;
                v_primera := FALSE;
            ELSE
                IF r.fecha = v_fecha_esp THEN
                    v_racha := v_racha + 1;
                    v_fecha_esp := v_fecha_esp - 1;
                ELSE
                    EXIT; -- Día faltante en la secuencia consecutiva
                END IF;
            END IF;
        END LOOP;

        RETURN v_racha;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END FN_CALCULAR_RACHA_ACTIVA;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: FN_PORCENTAJE_PROGRESO
    -- -------------------------------------------------------------------------
    FUNCTION FN_PORCENTAJE_PROGRESO(
        p_id_usuario  IN NUMBER,
        p_id_lenguaje IN NUMBER
    ) RETURN NUMBER IS
        v_total_lecciones     NUMBER := 0;
        v_completadas_usuario NUMBER := 0;
    BEGIN
        -- Conteo total de lecciones del lenguaje a través de sus módulos
        SELECT COUNT(l.id_leccion)
        INTO v_total_lecciones
        FROM Lecciones l
        JOIN Modulos m ON m.id_modulo = l.id_modulo
        WHERE m.id_lenguaje = p_id_lenguaje;

        IF v_total_lecciones = 0 THEN
            RETURN 0;
        END IF;

        -- Conteo de lecciones completadas por el usuario en dicho lenguaje
        SELECT COUNT(pl.id_leccion)
        INTO v_completadas_usuario
        FROM Progreso_Leccion pl
        JOIN Lecciones l ON l.id_leccion = pl.id_leccion
        JOIN Modulos m   ON m.id_modulo = l.id_modulo
        WHERE pl.id_usuario = p_id_usuario
          AND m.id_lenguaje = p_id_lenguaje
          AND pl.estado = C_ESTADO_COMPLETADA;

        RETURN ROUND((v_completadas_usuario / v_total_lecciones) * 100, 1);
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END FN_PORCENTAJE_PROGRESO;

    -- -------------------------------------------------------------------------
    -- PROCEDIMIENTO PRIVADO DE EVALUACIÓN DE LOGROS
    -- -------------------------------------------------------------------------
    PROCEDURE EVALUAR_LOGROS_USUARIO(
        p_id_usuario IN NUMBER,
        p_exp_total  IN NUMBER,
        p_racha      IN NUMBER
    ) IS
        v_retos_aprobados NUMBER := 0;

        -- Cursor de logros no desbloqueados aún por el usuario
        CURSOR c_logros_pendientes IS
            SELECT l.id_logro, l.tipo_condicion,
                   lr.dias_requeridos,
                   le.exp_requerida,
                   lc.retos_requeridos
            FROM Logros l
            LEFT JOIN Logro_Racha lr ON lr.id_logro = l.id_logro
            LEFT JOIN Logro_EXP le   ON le.id_logro = l.id_logro
            LEFT JOIN Logro_Retos lc ON lc.id_logro = l.id_logro
            WHERE NOT EXISTS (
                SELECT 1 FROM Usuario_Logro ul
                WHERE ul.id_usuario = p_id_usuario AND ul.id_logro = l.id_logro
            );
    BEGIN
        SELECT COUNT(DISTINCT id_reto)
        INTO v_retos_aprobados
        FROM Intentos
        WHERE id_usuario = p_id_usuario AND es_correcto = 1;

        FOR log IN c_logros_pendientes LOOP
            DECLARE
                v_cumple BOOLEAN := FALSE;
            BEGIN
                IF log.tipo_condicion = 'RACHA' AND p_racha >= NVL(log.dias_requeridos, 999999) THEN
                    v_cumple := TRUE;
                ELSIF log.tipo_condicion = 'EXP' AND p_exp_total >= NVL(log.exp_requerida, 999999) THEN
                    v_cumple := TRUE;
                ELSIF log.tipo_condicion = 'RETOS' AND v_retos_aprobados >= NVL(log.retos_requeridos, 999999) THEN
                    v_cumple := TRUE;
                END IF;

                IF v_cumple THEN
                    INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
                    VALUES (p_id_usuario, log.id_logro, SYSTIMESTAMP);
                END IF;
            EXCEPTION
                WHEN DUP_VAL_ON_INDEX THEN
                    NULL; -- Ya estaba insertado
            END;
        END LOOP;
    END EVALUAR_LOGROS_USUARIO;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_REGISTRAR_INTENTO_RETO
    -- -------------------------------------------------------------------------
    PROCEDURE SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         IN  NUMBER,
        p_id_reto            IN  NUMBER,
        p_es_correcto        IN  NUMBER,
        p_codigo_enviado     IN  CLOB,
        p_salida_obtenida    IN  CLOB,
        p_tiempo_ms          IN  NUMBER,
        p_nuevo_nivel        OUT NUMBER,
        p_nueva_exp          OUT NUMBER,
        p_vidas_restantes    OUT NUMBER,
        p_exp_otorgada       OUT NUMBER,
        p_id_intento_creado  OUT NUMBER
    ) IS
        v_exp_reto           NUMBER := 0;
        v_exp_actual         NUMBER := 0;
        v_vidas_actuales     NUMBER := C_VIDAS_MAXIMAS;
        v_racha_actual       NUMBER := 0;
        v_racha_max          NUMBER := 0;
        v_ya_aprobado_antes  NUMBER := 0;
        v_id_intento         NUMBER;
    BEGIN
        -- 1. Validar parámetros requeridos
        IF p_id_usuario IS NULL OR p_id_reto IS NULL THEN
            RAISE_APPLICATION_ERROR(-20001, 'Los identificadores de usuario y reto son obligatorios.');
        END IF;

        -- 2. Consultar y bloquear fila del usuario para concurrencia segura
        BEGIN
            SELECT exp_total, vidas, racha_actual, racha_maxima
            INTO v_exp_actual, v_vidas_actuales, v_racha_actual, v_racha_max
            FROM Usuarios
            WHERE id_usuario = p_id_usuario
            FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20002, 'El usuario #' || p_id_usuario || ' no existe en la base de datos.');
        END;

        -- 3. Consultar datos y recompensa del reto
        BEGIN
            SELECT exp_recompensa
            INTO v_exp_reto
            FROM Retos
            WHERE id_reto = p_id_reto;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20003, 'El reto #' || p_id_reto || ' no existe en el catálogo.');
        END;

        -- 4. Verificar si el usuario ya había aprobado este reto previamente
        SELECT COUNT(*)
        INTO v_ya_aprobado_antes
        FROM Intentos
        WHERE id_usuario = p_id_usuario
          AND id_reto = p_id_reto
          AND es_correcto = 1;

        -- La experiencia solo se concede una única vez por reto
        IF p_es_correcto = 1 AND v_ya_aprobado_antes = 0 THEN
            p_exp_otorgada := v_exp_reto;
        ELSE
            p_exp_otorgada := 0;
        END IF;

        -- 5. Insertar en la tabla Intentos
        INSERT INTO Intentos (
            id_usuario, id_reto, codigo_enviado, salida_obtenida,
            es_correcto, tiempo_ejecucion_ms, exp_obtenida, fecha_intento
        ) VALUES (
            p_id_usuario, p_id_reto, p_codigo_enviado, p_salida_obtenida,
            p_es_correcto, p_tiempo_ms, p_exp_otorgada, SYSTIMESTAMP
        ) RETURNING id_intento INTO v_id_intento;

        p_id_intento_creado := v_id_intento;

        -- 6. Procesar consecuencias del intento
        IF p_es_correcto = 1 THEN
            p_nueva_exp := v_exp_actual + p_exp_otorgada;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(p_nueva_exp);
            p_vidas_restantes := v_vidas_actuales;

            -- Registrar actividad diaria del día actual con MERGE
            MERGE INTO Actividad_Diaria dst
            USING (SELECT p_id_usuario AS id_usr, TRUNC(SYSDATE) AS fec FROM dual) src
            ON (dst.id_usuario = src.id_usr AND dst.fecha = src.fec)
            WHEN MATCHED THEN
                UPDATE SET dst.exp_ganada = dst.exp_ganada + p_exp_otorgada
            WHEN NOT MATCHED THEN
                INSERT (id_usuario, fecha, exp_ganada)
                VALUES (p_id_usuario, TRUNC(SYSDATE), p_exp_otorgada);

            -- Calcular nueva racha activa
            v_racha_actual := FN_CALCULAR_RACHA_ACTIVA(p_id_usuario);
            v_racha_max := GREATEST(v_racha_max, v_racha_actual);

            -- Actualizar usuario
            UPDATE Usuarios
            SET exp_total = p_nueva_exp,
                nivel = p_nuevo_nivel,
                racha_actual = v_racha_actual,
                racha_maxima = v_racha_max,
                ultima_actividad = SYSTIMESTAMP
            WHERE id_usuario = p_id_usuario;

            -- Evaluar si desbloqueó logros
            EVALUAR_LOGROS_USUARIO(p_id_usuario, p_nueva_exp, v_racha_actual);

        ELSE
            -- Si falló, restar 1 vida
            p_nueva_exp := v_exp_actual;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(p_nueva_exp);
            p_vidas_restantes := GREATEST(0, v_vidas_actuales - 1);

            UPDATE Usuarios
            SET vidas = p_vidas_restantes,
                ultima_actividad = SYSTIMESTAMP
            WHERE id_usuario = p_id_usuario;
        END IF;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END SP_REGISTRAR_INTENTO_RETO;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_COMPLETAR_LECCION
    -- -------------------------------------------------------------------------
    PROCEDURE SP_COMPLETAR_LECCION(
        p_id_usuario     IN  NUMBER,
        p_id_leccion     IN  NUMBER,
        p_xp_otorgada    OUT NUMBER,
        p_nuevo_nivel    OUT NUMBER
    ) IS
        v_estado_actual VARCHAR2(15);
        v_exp_leccion   NUMBER := 0;
        v_exp_usuario   NUMBER := 0;
        v_racha         NUMBER := 0;
        v_racha_max     NUMBER := 0;
    BEGIN
        IF p_id_usuario IS NULL OR p_id_leccion IS NULL THEN
            RAISE_APPLICATION_ERROR(-20010, 'Usuario y lección son requeridos.');
        END IF;

        -- Consultar recompensa de la lección
        SELECT exp_recompensa
        INTO v_exp_leccion
        FROM Lecciones
        WHERE id_leccion = p_id_leccion;

        -- Consultar usuario
        SELECT exp_total, racha_actual, racha_maxima
        INTO v_exp_usuario, v_racha, v_racha_max
        FROM Usuarios
        WHERE id_usuario = p_id_usuario
        FOR UPDATE;

        -- Verificar estado previo en Progreso_Leccion
        BEGIN
            SELECT estado
            INTO v_estado_actual
            FROM Progreso_Leccion
            WHERE id_usuario = p_id_usuario AND id_leccion = p_id_leccion;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_estado_actual := 'NO_INICIADA';
        END;

        -- Solo otorgar experiencia si no estaba previamente completada
        IF v_estado_actual = C_ESTADO_COMPLETADA THEN
            p_xp_otorgada := 0;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(v_exp_usuario);
            RETURN;
        END IF;

        p_xp_otorgada := v_exp_leccion;
        v_exp_usuario := v_exp_usuario + p_xp_otorgada;
        p_nuevo_nivel := FN_CALCULAR_NIVEL(v_exp_usuario);

        -- Guardar estado completado con MERGE
        MERGE INTO Progreso_Leccion dst
        USING (SELECT p_id_usuario AS id_usr, p_id_leccion AS id_lec FROM dual) src
        ON (dst.id_usuario = src.id_usr AND dst.id_leccion = src.id_lec)
        WHEN MATCHED THEN
            UPDATE SET dst.estado = C_ESTADO_COMPLETADA, dst.fecha_completado = SYSTIMESTAMP
        WHEN NOT MATCHED THEN
            INSERT (id_usuario, id_leccion, estado, fecha_completado)
            VALUES (p_id_usuario, p_id_leccion, C_ESTADO_COMPLETADA, SYSTIMESTAMP);

        -- Registrar actividad diaria
        MERGE INTO Actividad_Diaria dst
        USING (SELECT p_id_usuario AS id_usr, TRUNC(SYSDATE) AS fec FROM dual) src
        ON (dst.id_usuario = src.id_usr AND dst.fecha = src.fec)
        WHEN MATCHED THEN
            UPDATE SET dst.exp_ganada = dst.exp_ganada + p_xp_otorgada
        WHEN NOT MATCHED THEN
            INSERT (id_usuario, fecha, exp_ganada)
            VALUES (p_id_usuario, TRUNC(SYSDATE), p_xp_otorgada);

        -- Actualizar racha y usuario
        v_racha := FN_CALCULAR_RACHA_ACTIVA(p_id_usuario);
        v_racha_max := GREATEST(v_racha_max, v_racha);

        UPDATE Usuarios
        SET exp_total = v_exp_usuario,
            nivel = p_nuevo_nivel,
            racha_actual = v_racha,
            racha_maxima = v_racha_max,
            ultima_actividad = SYSTIMESTAMP
        WHERE id_usuario = p_id_usuario;

        EVALUAR_LOGROS_USUARIO(p_id_usuario, v_exp_usuario, v_racha);

        COMMIT;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR(-20011, 'Lección o usuario no encontrado en la base de datos.');
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END SP_COMPLETAR_LECCION;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_RECUPERAR_VIDAS
    -- -------------------------------------------------------------------------
    PROCEDURE SP_RECUPERAR_VIDAS(
        p_id_usuario          IN  NUMBER,
        p_vidas_restauradas   OUT NUMBER
    ) IS
    BEGIN
        IF p_id_usuario IS NULL THEN
            RAISE_APPLICATION_ERROR(-20020, 'El ID de usuario es obligatorio.');
        END IF;

        UPDATE Usuarios
        SET vidas = C_VIDAS_MAXIMAS,
            ultima_actividad = SYSTIMESTAMP
        WHERE id_usuario = p_id_usuario;

        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20021, 'Usuario #' || p_id_usuario || ' no encontrado.');
        END IF;

        p_vidas_restauradas := C_VIDAS_MAXIMAS;
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END SP_RECUPERAR_VIDAS;

END PKG_GAMIFICACION;
/

