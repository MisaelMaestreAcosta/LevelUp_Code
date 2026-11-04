-- =============================================================================
-- LEVELUP CODE — BATERÍA DE PRUEBAS PL/SQL EN BLOQUE ANÓNIMO
-- Asignatura: Bases de Datos & Programación III — Universidad Popular del Cesar
-- Autor: Misael Andrés Maestre Acosta (Tech Lead)
--
-- Propósito:
--   Ejecutar una suite completa de pruebas unitarias sobre el paquete
--   PKG_GAMIFICACION y el disparador TRG_AUDIT_USUARIOS, demostrando el uso de:
--     - Bloques anónimos (DECLARE ... BEGIN ... EXCEPTION ... END;)
--     - Cursores explícitos y estructuras de control (IF, CASE, FOR LOOP)
--     - Invocación de funciones y procedimientos con parámetros IN y OUT
--     - Control transaccional (SAVEPOINT / ROLLBACK)
--     - Captura y validación de excepciones (RAISE_APPLICATION_ERROR)
--     - Salida formateada por consola con DBMS_OUTPUT
--
-- Ejecución:
--   En Oracle SQL Developer o DBeaver, asegúrate de activar la consola:
--     SET SERVEROUTPUT ON SIZE UNLIMITED;
--   Y ejecutar este script con la conexión: LEVELUP_ADMIN
-- =============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;

DECLARE
    -- Variables para almacenar resultados de funciones y parámetros OUT
    v_nivel_calc       NUMBER;
    v_racha_calc       NUMBER;
    v_porcentaje_calc  NUMBER;

    v_nuevo_nivel      NUMBER;
    v_nueva_exp        NUMBER;
    v_vidas_restantes  NUMBER;
    v_exp_otorgada     NUMBER;
    v_id_intento       NUMBER;
    v_vidas_rest       NUMBER;

    -- Contadores de pruebas
    v_total_pruebas    NUMBER := 0;
    v_pruebas_exito    NUMBER := 0;

    -- Cursor explícito para auditar estudiantes de prueba
    CURSOR c_estudiantes IS
        SELECT u.id_usuario, u.primer_nombre, u.correo_electronico, u.exp_total, u.nivel, u.vidas
        FROM Usuarios u
        WHERE u.id_rol = 1
        FETCH FIRST 3 ROWS ONLY;

    -- Procedimiento local auxiliar dentro del bloque anónimo para registrar resultados
    PROCEDURE REGISTRAR_TEST(
        p_nombre_test IN VARCHAR2,
        p_condicion   IN BOOLEAN,
        p_detalle     IN VARCHAR2
    ) IS
    BEGIN
        v_total_pruebas := v_total_pruebas + 1;
        IF p_condicion THEN
            v_pruebas_exito := v_pruebas_exito + 1;
            DBMS_OUTPUT.PUT_LINE('  [PASS] ' || RPAD(p_nombre_test, 38) || ' | ' || p_detalle);
        ELSE
            DBMS_OUTPUT.PUT_LINE('  [FAIL] ' || RPAD(p_nombre_test, 38) || ' | ' || p_detalle);
        END IF;
    END REGISTRAR_TEST;

BEGIN
    DBMS_OUTPUT.PUT_LINE('================================================================================');
    DBMS_OUTPUT.PUT_LINE('     LEVELUP CODE — SUITE DE PRUEBAS UNITARIAS PL/SQL (ORACLE 18c XE)          ');
    DBMS_OUTPUT.PUT_LINE('================================================================================');
    DBMS_OUTPUT.PUT_LINE('Fecha de ejecución: ' || TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    -- Punto de restauración para dejar la base de datos intacta tras las pruebas
    SAVEPOINT sp_inicio_pruebas;

    -- =========================================================================
    -- CASO 1: PRUEBAS UNITARIAS DE LA FUNCIÓN FN_CALCULAR_NIVEL
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('1. PRUEBAS DE LA FUNCIÓN: PKG_GAMIFICACION.FN_CALCULAR_NIVEL');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(0);
    REGISTRAR_TEST('Nivel con 0 XP', v_nivel_calc = 1, 'Esperado: 1, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(299);
    REGISTRAR_TEST('Nivel con 299 XP (umbral)', v_nivel_calc = 1, 'Esperado: 1, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(300);
    REGISTRAR_TEST('Nivel con 300 XP (salto)', v_nivel_calc = 2, 'Esperado: 2, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(1450);
    REGISTRAR_TEST('Nivel con 1450 XP', v_nivel_calc = 5, 'Esperado: 5, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(NULL);
    REGISTRAR_TEST('Manejo de valor NULL', v_nivel_calc = 1, 'Esperado: 1, Obtenido: ' || v_nivel_calc);

    -- =========================================================================
    -- CASO 2: PRUEBAS DE LA FUNCIÓN FN_PORCENTAJE_PROGRESO
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('2. PRUEBAS DE LA FUNCIÓN: PKG_GAMIFICACION.FN_PORCENTAJE_PROGRESO');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    -- Lenguaje 1 = Java
    v_porcentaje_calc := PKG_GAMIFICACION.FN_PORCENTAJE_PROGRESO(1, 1);
    REGISTRAR_TEST('Porcentaje Java Estudiante #1', v_porcentaje_calc >= 0 AND v_porcentaje_calc <= 100,
                   'Porcentaje obtenido: ' || v_porcentaje_calc || '%');

    -- Lenguaje inexistente
    v_porcentaje_calc := PKG_GAMIFICACION.FN_PORCENTAJE_PROGRESO(1, 999);
    REGISTRAR_TEST('Lenguaje inexistente (999)', v_porcentaje_calc = 0,
                   'Esperado: 0%, Obtenido: ' || v_porcentaje_calc || '%');

    -- =========================================================================
    -- CASO 3: PROCEDIMIENTO SP_REGISTRAR_INTENTO_RETO (INTENTO EXITOSO)
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('3. PROCEDIMIENTO: PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO (EXITOSO)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         => 1,
        p_id_reto            => 101,
        p_es_correcto        => 1,
        p_codigo_enviado     => 'public class SolucionPrueba { /* código correcto */ }',
        p_salida_obtenida    => 'Aprobado: 3/3 casos',
        p_tiempo_ms          => 32,
        p_nuevo_nivel        => v_nuevo_nivel,
        p_nueva_exp          => v_nueva_exp,
        p_vidas_restantes    => v_vidas_restantes,
        p_exp_otorgada       => v_exp_otorgada,
        p_id_intento_creado  => v_id_intento
    );

    REGISTRAR_TEST('Generación de ID Intento', v_id_intento > 0, 'ID Intento generado: #' || v_id_intento);
    REGISTRAR_TEST('Integridad de Vidas', v_vidas_restantes = 5, 'Vidas tras acierto: ' || v_vidas_restantes);
    REGISTRAR_TEST('Cálculo reactivo de nivel', v_nuevo_nivel >= 5, 'Nivel tras envío: ' || v_nuevo_nivel);

    -- =========================================================================
    -- CASO 4: PROCEDIMIENTO SP_REGISTRAR_INTENTO_RETO (INTENTO FALLIDO)
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('4. PROCEDIMIENTO: PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO (FALLIDO)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         => 1,
        p_id_reto            => 101,
        p_es_correcto        => 0,
        p_codigo_enviado     => 'public class SolucionErronea { /* error */ }',
        p_salida_obtenida    => 'Error: caso 2 falló',
        p_tiempo_ms          => 18,
        p_nuevo_nivel        => v_nuevo_nivel,
        p_nueva_exp          => v_nueva_exp,
        p_vidas_restantes    => v_vidas_restantes,
        p_exp_otorgada       => v_exp_otorgada,
        p_id_intento_creado  => v_id_intento
    );

    REGISTRAR_TEST('Descuento de 1 vida por fallo', v_vidas_restantes = 4, 'Vidas tras fallo: ' || v_vidas_restantes);
    REGISTRAR_TEST('Cero XP otorgada en fallo', v_exp_otorgada = 0, 'XP otorgada: ' || v_exp_otorgada);

    -- =========================================================================
    -- CASO 5: PROCEDIMIENTO SP_RECUPERAR_VIDAS
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('5. PROCEDIMIENTO: PKG_GAMIFICACION.SP_RECUPERAR_VIDAS');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_RECUPERAR_VIDAS(
        p_id_usuario        => 1,
        p_vidas_restauradas => v_vidas_rest
    );

    REGISTRAR_TEST('Restauración a 5 vidas máximas', v_vidas_rest = 5, 'Vidas restauradas: ' || v_vidas_rest);

    -- =========================================================================
    -- CASO 6: PROCEDIMIENTO SP_COMPLETAR_LECCION
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('6. PROCEDIMIENTO: PKG_GAMIFICACION.SP_COMPLETAR_LECCION');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_COMPLETAR_LECCION(
        p_id_usuario  => 1,
        p_id_leccion  => 2,
        p_xp_otorgada => v_exp_otorgada,
        p_nuevo_nivel => v_nuevo_nivel
    );

    REGISTRAR_TEST('Lección completada con éxito', v_nuevo_nivel >= 5,
                   'XP otorgada: +' || v_exp_otorgada || ' | Nivel actual: ' || v_nuevo_nivel);

    -- =========================================================================
    -- CASO 7: MANEJO ROBUSTO DE EXCEPCIONES CONTROLADAS
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('7. PRUEBA DE DISPARO Y CAPTURA DE EXCEPCIONES PERSONALIZADAS');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    BEGIN
        -- Intentar registrar intento con un usuario inexistente (-999)
        PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
            p_id_usuario         => -999,
            p_id_reto            => 101,
            p_es_correcto        => 1,
            p_codigo_enviado     => 'test',
            p_salida_obtenida    => 'test',
            p_tiempo_ms          => 10,
            p_nuevo_nivel        => v_nuevo_nivel,
            p_nueva_exp          => v_nueva_exp,
            p_vidas_restantes    => v_vidas_restantes,
            p_exp_otorgada       => v_exp_otorgada,
            p_id_intento_creado  => v_id_intento
        );
        REGISTRAR_TEST('Excepción Usuario Inexistente', FALSE, 'No debió permitir la operación');
    EXCEPTION
        WHEN OTHERS THEN
            REGISTRAR_TEST('Excepción Usuario Inexistente', SQLCODE = -20002,
                           'Código esperado: -20002 | Error: ' || SQLERRM);
    END;

    -- =========================================================================
    -- CASO 8: RECORRIDO DE CURSOR EXPLÍCITO Y AUDITORÍA
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('8. AUDITORÍA MEDIANTE CURSOR EXPLÍCITO (ESTUDIANTES REGISTRADOS)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    FOR est IN c_estudiantes LOOP
        DBMS_OUTPUT.PUT_LINE('  ID: #' || LPAD(est.id_usuario, 2, '0')
                             || ' | Nombre: ' || RPAD(est.primer_nombre, 14)
                             || ' | Correo: ' || RPAD(est.correo_electronico, 26)
                             || ' | Nivel: ' || est.nivel
                             || ' | XP: ' || est.exp_total
                             || ' | Vidas: ' || est.vidas);
    END LOOP;

    -- Deshacer cambios de la prueba para mantener la base de datos en su estado inicial
    ROLLBACK TO sp_inicio_pruebas;

    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('RESUMEN DE PRUEBAS PL/SQL: ' || v_pruebas_exito || ' de ' || v_total_pruebas || ' pruebas superadas.');
    IF v_pruebas_exito = v_total_pruebas THEN
        DBMS_OUTPUT.PUT_LINE('ESTADO FINAL: [APROBADO] — Todos los objetos y procedimientos operan al 100%.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('ESTADO FINAL: [REVISAR] — Algunas pruebas fallaron.');
    END IF;
    DBMS_OUTPUT.PUT_LINE('================================================================================');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO sp_inicio_pruebas;
        DBMS_OUTPUT.PUT_LINE('ERROR CRÍTICO NO CONTROLADO EN EL BLOQUE ANÓNIMO: ' || SQLERRM);
END;
/

