-- Progreso, actividad, intentos, logros y estadísticas de aprendizaje.
CREATE OR REPLACE PACKAGE PKG_SEGUIMIENTO AS
    PROCEDURE actividad_en_fecha(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_fecha IN DATE
    );
    PROCEDURE actividad_reciente(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_dias IN NUMBER
    );
    PROCEDURE catalogo_logros(p_filas OUT SYS_REFCURSOR);
    PROCEDURE estadisticas_logros(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE estados_lecciones(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE guardar_actividad(p_id_usuario IN NUMBER, p_fecha IN DATE, p_xp IN NUMBER);
    PROCEDURE guardar_estado_leccion(
        p_id_usuario IN NUMBER,
        p_id_leccion IN NUMBER,
        p_estado IN VARCHAR2,
        p_fecha IN TIMESTAMP
    );
    PROCEDURE intento_por_solicitud(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_reto IN NUMBER,
        p_solicitud IN VARCHAR2
    );
    PROCEDURE intentos_recientes(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_reto IN NUMBER,
        p_limite IN NUMBER
    );
    PROCEDURE logros_obtenidos(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE logros_pendientes(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE otorgar_logro(
        p_id_usuario IN NUMBER,
        p_id_logro IN NUMBER,
        p_fecha IN TIMESTAMP,
        p_filas OUT NUMBER
    );
    PROCEDURE progreso_leccion(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_leccion IN NUMBER
    );
    PROCEDURE progreso_retos(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE resumen_lenguajes(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE reto_aprobado(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_reto IN NUMBER
    );
    PROCEDURE retos_para_repasar(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE ultimas_practicas(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE xp_diaria(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE xp_semanal(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
END PKG_SEGUIMIENTO;
/

CREATE OR REPLACE PACKAGE BODY PKG_SEGUIMIENTO AS
    PROCEDURE actividad_en_fecha(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_fecha IN DATE
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_actividad, id_usuario, fecha, exp_ganada FROM Actividad_Diaria WHERE id_usuario =
                p_id_usuario AND TRUNC(fecha) = TRUNC(p_fecha);
    END actividad_en_fecha;

    PROCEDURE actividad_reciente(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_dias IN NUMBER
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_actividad, id_usuario, fecha, exp_ganada FROM Actividad_Diaria WHERE id_usuario =
                p_id_usuario AND fecha >= TRUNC(SYSDATE) - p_dias ORDER BY fecha ASC;
    END actividad_reciente;

    PROCEDURE catalogo_logros(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT l.id_logro, l.nombre_insignia, l.descripcion, l.tipo_condicion, lr.dias_requeridos,
                le.exp_requerida, lc.retos_requeridos FROM Logros l LEFT JOIN Logro_Racha lr ON lr.id_logro =
                l.id_logro LEFT JOIN Logro_EXP le ON le.id_logro = l.id_logro LEFT JOIN Logro_Retos lc ON
                lc.id_logro = l.id_logro ORDER BY l.id_logro ASC;
    END catalogo_logros;

    PROCEDURE estadisticas_logros(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT PKG_GAMIFICACION.FN_CALCULAR_RACHA_ACTIVA(u.id_usuario) AS racha_actual, u.racha_maxima,
                u.exp_total, (SELECT COUNT(DISTINCT i.id_reto) FROM Intentos i WHERE i.id_usuario = u.id_usuario
                AND i.es_correcto = 1) AS retos_aprobados FROM Usuarios u WHERE u.id_usuario = p_id_usuario;
    END estadisticas_logros;

    PROCEDURE estados_lecciones(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_leccion, estado FROM Progreso_Leccion WHERE id_usuario = p_id_usuario;
    END estados_lecciones;

    PROCEDURE guardar_actividad(p_id_usuario IN NUMBER, p_fecha IN DATE, p_xp IN NUMBER) IS
    BEGIN
        MERGE INTO Actividad_Diaria dst USING (SELECT p_id_usuario AS id_usuario, TRUNC(p_fecha) AS fecha FROM
            dual) src ON (dst.id_usuario = src.id_usuario AND TRUNC(dst.fecha) = src.fecha) WHEN MATCHED THEN
            UPDATE SET dst.exp_ganada = p_xp WHEN NOT MATCHED THEN INSERT (id_usuario, fecha, exp_ganada) VALUES
            (p_id_usuario, TRUNC(p_fecha), p_xp);
    END guardar_actividad;

    PROCEDURE guardar_estado_leccion(
        p_id_usuario IN NUMBER,
        p_id_leccion IN NUMBER,
        p_estado IN VARCHAR2,
        p_fecha IN TIMESTAMP
    ) IS
    BEGIN
        MERGE INTO Progreso_Leccion dst USING (SELECT p_id_usuario AS id_usuario, p_id_leccion AS id_leccion FROM
            dual) src ON (dst.id_usuario = src.id_usuario AND dst.id_leccion = src.id_leccion) WHEN MATCHED THEN
            UPDATE SET dst.estado = p_estado, dst.fecha_completado = p_fecha WHERE dst.estado <> 'COMPLETADA' WHEN
            NOT MATCHED THEN INSERT (id_usuario, id_leccion, estado, fecha_completado) VALUES (p_id_usuario,
            p_id_leccion, p_estado, p_fecha);
    END guardar_estado_leccion;

    PROCEDURE intento_por_solicitud(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_reto IN NUMBER,
        p_solicitud IN VARCHAR2
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT i.*,v.revision AS version_reto,v.enunciado AS enunciado_version FROM Intentos i JOIN
                Version_Reto v ON v.id_version=i.id_version_reto WHERE i.id_usuario=p_id_usuario AND
                i.id_reto=p_id_reto AND i.id_solicitud=p_solicitud;
    END intento_por_solicitud;

    PROCEDURE intentos_recientes(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_reto IN NUMBER,
        p_limite IN NUMBER
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT * FROM (SELECT i.*,v.revision AS version_reto,v.enunciado AS enunciado_version FROM Intentos i
                JOIN Version_Reto v ON v.id_version=i.id_version_reto WHERE i.id_usuario = p_id_usuario AND
                i.id_reto = p_id_reto ORDER BY i.fecha_intento DESC,i.id_intento DESC) WHERE ROWNUM <= p_limite;
    END intentos_recientes;

    PROCEDURE logros_obtenidos(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT ul.id_usuario, ul.id_logro, ul.fecha_desbloqueado, l.nombre_insignia, l.descripcion,
                l.tipo_condicion, lr.dias_requeridos, le.exp_requerida, lc.retos_requeridos FROM Usuario_Logro ul
                JOIN Logros l ON l.id_logro = ul.id_logro LEFT JOIN Logro_Racha lr ON lr.id_logro = l.id_logro LEFT
                JOIN Logro_EXP le ON le.id_logro = l.id_logro LEFT JOIN Logro_Retos lc ON lc.id_logro = l.id_logro
                WHERE ul.id_usuario = p_id_usuario ORDER BY ul.fecha_desbloqueado DESC;
    END logros_obtenidos;

    PROCEDURE logros_pendientes(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT l.id_logro, l.nombre_insignia, l.descripcion, l.tipo_condicion, lr.dias_requeridos,
                le.exp_requerida, lc.retos_requeridos FROM Logros l LEFT JOIN Logro_Racha lr ON lr.id_logro =
                l.id_logro LEFT JOIN Logro_EXP le ON le.id_logro = l.id_logro LEFT JOIN Logro_Retos lc ON
                lc.id_logro = l.id_logro WHERE NOT EXISTS ( SELECT 1 FROM Usuario_Logro ul WHERE ul.id_usuario =
                p_id_usuario AND ul.id_logro = l.id_logro) ORDER BY l.id_logro ASC;
    END logros_pendientes;

    PROCEDURE otorgar_logro(
        p_id_usuario IN NUMBER,
        p_id_logro IN NUMBER,
        p_fecha IN TIMESTAMP,
        p_filas OUT NUMBER
    ) IS
    BEGIN
        INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado) SELECT p_id_usuario, p_id_logro,
            p_fecha FROM dual WHERE NOT EXISTS ( SELECT 1 FROM Usuario_Logro WHERE id_usuario = p_id_usuario AND
            id_logro = p_id_logro);
        p_filas := SQL%ROWCOUNT;
    END otorgar_logro;

    PROCEDURE progreso_leccion(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_leccion IN NUMBER
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_usuario, id_leccion, estado, fecha_completado FROM Progreso_Leccion WHERE id_usuario =
                p_id_usuario AND id_leccion = p_id_leccion;
    END progreso_leccion;

    PROCEDURE progreso_retos(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_reto, MAX(es_correcto) AS aprobado FROM Intentos WHERE id_usuario = p_id_usuario GROUP BY
                id_reto;
    END progreso_retos;

    PROCEDURE resumen_lenguajes(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT lg.nombre, (SELECT COUNT(*) FROM Intentos i JOIN Retos r ON r.id_reto=i.id_reto JOIN Lecciones
                le ON le.id_leccion=r.id_leccion JOIN Modulos m ON m.id_modulo=le.id_modulo WHERE
                i.id_usuario=p_id_usuario AND m.id_lenguaje=lg.id_lenguaje) intentos, (SELECT COUNT(*) FROM
                Intentos i JOIN Retos r ON r.id_reto=i.id_reto JOIN Lecciones le ON le.id_leccion=r.id_leccion JOIN
                Modulos m ON m.id_modulo=le.id_modulo WHERE i.id_usuario=p_id_usuario AND i.es_correcto=1 AND
                m.id_lenguaje=lg.id_lenguaje) aciertos, (SELECT COUNT(DISTINCT i.id_reto) FROM Intentos i JOIN
                Retos r ON r.id_reto=i.id_reto JOIN Lecciones le ON le.id_leccion=r.id_leccion JOIN Modulos m ON
                m.id_modulo=le.id_modulo WHERE i.id_usuario=p_id_usuario AND i.es_correcto=1 AND r.estado=
                'PUBLICADO' AND m.id_lenguaje=lg.id_lenguaje) superados, (SELECT COUNT(*) FROM Retos r JOIN
                Lecciones le ON le.id_leccion=r.id_leccion JOIN Modulos m ON m.id_modulo=le.id_modulo WHERE
                r.estado= 'PUBLICADO' AND m.id_lenguaje=lg.id_lenguaje) retos, (SELECT COUNT(*) FROM
                Progreso_Leccion p JOIN Lecciones le ON le.id_leccion=p.id_leccion JOIN Modulos m ON
                m.id_modulo=le.id_modulo WHERE p.id_usuario=p_id_usuario AND p.estado= 'COMPLETADA' AND
                m.id_lenguaje=lg.id_lenguaje) lecturas, (SELECT COUNT(*) FROM Lecciones le JOIN Modulos m ON
                m.id_modulo=le.id_modulo WHERE m.id_lenguaje=lg.id_lenguaje) lecciones FROM Lenguajes lg WHERE
                lg.activo=1 ORDER BY lg.id_lenguaje;
    END resumen_lenguajes;

    PROCEDURE reto_aprobado(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_reto IN NUMBER
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Intentos WHERE id_usuario = p_id_usuario AND id_reto = p_id_reto AND es_correcto = 1
                FETCH FIRST 1 ROWS ONLY;
    END reto_aprobado;

    PROCEDURE retos_para_repasar(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT r.id_reto,r.titulo_reto,lg.nombre FROM (SELECT i.*,ROW_NUMBER() OVER (PARTITION BY i.id_reto
                ORDER BY i.fecha_intento DESC,i.id_intento DESC) posicion FROM Intentos i WHERE
                i.id_usuario=p_id_usuario) ultimo JOIN Retos r ON r.id_reto=ultimo.id_reto JOIN Lecciones le ON
                le.id_leccion=r.id_leccion JOIN Modulos m ON m.id_modulo=le.id_modulo JOIN Lenguajes lg ON
                lg.id_lenguaje=m.id_lenguaje WHERE ultimo.posicion=1 AND ultimo.es_correcto=0 AND r.estado=
                'PUBLICADO' AND lg.activo=1 AND NOT EXISTS (SELECT 1 FROM Intentos aprobado WHERE
                aprobado.id_usuario=p_id_usuario AND aprobado.id_reto=r.id_reto AND aprobado.es_correcto=1) ORDER
                BY ultimo.fecha_intento DESC,ultimo.id_intento DESC FETCH FIRST 5 ROWS ONLY;
    END retos_para_repasar;

    PROCEDURE ultimas_practicas(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_reto,TRUNC(MAX(fecha_intento)) FROM Intentos WHERE id_usuario=p_id_usuario GROUP BY id_reto;
    END ultimas_practicas;

    PROCEDURE xp_diaria(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            WITH dias AS (SELECT TRUNC(SYSDATE)-LEVEL+1 fecha FROM dual CONNECT BY LEVEL<=28) SELECT
                d.fecha,NVL(SUM(a.exp_ganada),0) FROM dias d LEFT JOIN Actividad_Diaria a ON
                a.id_usuario=p_id_usuario AND a.fecha>=d.fecha AND a.fecha<d.fecha+1 GROUP BY d.fecha ORDER BY
                d.fecha;
    END xp_diaria;

    PROCEDURE xp_semanal(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT NVL(SUM(exp_ganada),0), COUNT(DISTINCT CASE WHEN exp_ganada>0 THEN TRUNC(fecha) END) FROM
                Actividad_Diaria WHERE id_usuario=p_id_usuario AND fecha>=TRUNC(SYSDATE)-6 AND
                fecha<TRUNC(SYSDATE)+1;
    END xp_semanal;
END PKG_SEGUIMIENTO;
/
