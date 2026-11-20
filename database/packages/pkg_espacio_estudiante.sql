-- Cuaderno, favoritos, metas y preferencias del estudiante.
CREATE OR REPLACE PACKAGE PKG_ESPACIO_ESTUDIANTE AS
    PROCEDURE es_favorito(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER, p_id_reto IN NUMBER);
    PROCEDURE favoritos_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE guardar_favorito(p_id_usuario IN NUMBER, p_id_reto IN NUMBER, p_guardar IN NUMBER);
    PROCEDURE guardar_preferencias(
        p_id_usuario IN NUMBER,
        p_lenguaje IN VARCHAR2,
        p_fuente IN NUMBER,
        p_indentacion IN NUMBER,
        p_reducir IN NUMBER
    );
    PROCEDURE metas_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE notas_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE preferencias_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
END PKG_ESPACIO_ESTUDIANTE;
/

CREATE OR REPLACE PACKAGE BODY PKG_ESPACIO_ESTUDIANTE AS
    PROCEDURE es_favorito(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Reto_Favorito WHERE id_usuario=p_id_usuario AND id_reto=p_id_reto;
    END es_favorito;

    PROCEDURE favoritos_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_reto FROM Reto_Favorito WHERE id_usuario=p_id_usuario;
    END favoritos_usuario;

    PROCEDURE guardar_favorito(p_id_usuario IN NUMBER, p_id_reto IN NUMBER, p_guardar IN NUMBER) IS
    BEGIN
        IF p_guardar = 1 THEN
                MERGE INTO Reto_Favorito f USING (SELECT p_id_usuario id_usuario, p_id_reto id_reto FROM dual) v ON
                    (f.id_usuario=v.id_usuario AND f.id_reto=v.id_reto) WHEN NOT MATCHED THEN
                    INSERT(id_usuario,id_reto) VALUES(v.id_usuario,v.id_reto);
            ELSE
                DELETE FROM Reto_Favorito WHERE id_usuario=p_id_usuario AND id_reto=p_id_reto;
            END IF;
    END guardar_favorito;

    PROCEDURE guardar_preferencias(
        p_id_usuario IN NUMBER,
        p_lenguaje IN VARCHAR2,
        p_fuente IN NUMBER,
        p_indentacion IN NUMBER,
        p_reducir IN NUMBER
    ) IS
    BEGIN
        MERGE INTO Preferencia_Usuario p USING (SELECT p_id_usuario id_usuario FROM dual) v ON
            (p.id_usuario=v.id_usuario) WHEN MATCHED THEN UPDATE SET
            lenguaje_preferido=p_lenguaje,tamano_editor=p_fuente,espacios_indentacion=p_indentacion,reducir_animaciones=p_reducir
            WHEN NOT MATCHED THEN
            INSERT(id_usuario,lenguaje_preferido,tamano_editor,espacios_indentacion,reducir_animaciones)
            VALUES(v.id_usuario,p_lenguaje,p_fuente,p_indentacion,p_reducir);
    END guardar_preferencias;

    PROCEDURE metas_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT g.id_meta,g.tipo_meta,g.valor_objetivo,g.fecha_inicio,g.fecha_fin, CASE g.tipo_meta WHEN 'RETOS'
                THEN (SELECT COUNT(DISTINCT i.id_reto) FROM Intentos i WHERE i.id_usuario=g.id_usuario AND
                i.es_correcto=1 AND i.fecha_intento>=g.fecha_inicio AND i.fecha_intento<g.fecha_fin+1) WHEN
                'LECCIONES' THEN (SELECT COUNT(*) FROM Progreso_Leccion p WHERE p.id_usuario=g.id_usuario AND
                p.estado= 'COMPLETADA' AND p.fecha_completado>=g.fecha_inicio AND p.fecha_completado<g.fecha_fin+1)
                WHEN 'XP' THEN (SELECT NVL(SUM(a.exp_ganada),0) FROM Actividad_Diaria a WHERE
                a.id_usuario=g.id_usuario AND a.fecha>=g.fecha_inicio AND a.fecha<g.fecha_fin+1) WHEN 'DIAS' THEN
                (SELECT COUNT(DISTINCT TRUNC(a.fecha)) FROM Actividad_Diaria a WHERE a.id_usuario=g.id_usuario AND
                a.exp_ganada>0 AND a.fecha>=g.fecha_inicio AND a.fecha<g.fecha_fin+1) END avance FROM Meta_Usuario
                g WHERE g.id_usuario=p_id_usuario ORDER BY g.fecha_fin DESC,g.id_meta DESC;
    END metas_usuario;

    PROCEDURE notas_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_nota,id_reto,titulo,contenido FROM Nota_Estudio WHERE id_usuario=p_id_usuario ORDER BY
                fecha_edicion DESC;
    END notas_usuario;

    PROCEDURE preferencias_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT lenguaje_preferido,tamano_editor,espacios_indentacion,reducir_animaciones FROM
                Preferencia_Usuario WHERE id_usuario=p_id_usuario;
    END preferencias_usuario;
END PKG_ESPACIO_ESTUDIANTE;
/
