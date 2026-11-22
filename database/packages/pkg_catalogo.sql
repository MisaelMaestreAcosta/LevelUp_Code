-- Consulta y ordenación de lenguajes, módulos y lecciones.
CREATE OR REPLACE PACKAGE PKG_CATALOGO AS
    PROCEDURE bloquear_curso_activo(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER);
    PROCEDURE bloquear_leccion(p_filas OUT SYS_REFCURSOR, p_id_leccion IN NUMBER);
    PROCEDURE bloquear_modulo_activo(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER);
    PROCEDURE cursos_editor(p_filas OUT SYS_REFCURSOR);
    PROCEDURE existe_lenguaje(p_filas OUT SYS_REFCURSOR, p_nombre IN VARCHAR2);
    PROCEDURE leccion_por_id(p_filas OUT SYS_REFCURSOR, p_id_leccion IN NUMBER);
    PROCEDURE lecciones_activas(p_filas OUT SYS_REFCURSOR);
    PROCEDURE lecciones_modulo(p_filas OUT SYS_REFCURSOR, p_id_modulo IN NUMBER);
    PROCEDURE lenguaje_por_id(p_filas OUT SYS_REFCURSOR, p_id_lenguaje IN NUMBER);
    PROCEDURE lenguajes_activos(p_filas OUT SYS_REFCURSOR);
    PROCEDURE modulo_por_id(p_filas OUT SYS_REFCURSOR, p_id_modulo IN NUMBER);
    PROCEDURE modulos_activos(p_filas OUT SYS_REFCURSOR);
    PROCEDURE modulos_lenguaje(p_filas OUT SYS_REFCURSOR, p_id_lenguaje IN NUMBER);
    PROCEDURE orden_lecciones(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER, p_bloquear IN NUMBER);
    PROCEDURE orden_modulos(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER, p_bloquear IN NUMBER);
    PROCEDURE otro_lenguaje(
        p_filas OUT SYS_REFCURSOR,
        p_nombre IN VARCHAR2,
        p_id_excluido IN NUMBER
    );
END PKG_CATALOGO;
/

CREATE OR REPLACE PACKAGE BODY PKG_CATALOGO AS
    PROCEDURE bloquear_curso_activo(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_lenguaje FROM Lenguajes WHERE id_lenguaje=p_padre AND activo=1 FOR UPDATE;
    END bloquear_curso_activo;

    PROCEDURE bloquear_leccion(p_filas OUT SYS_REFCURSOR, p_id_leccion IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_leccion FROM Lecciones WHERE id_leccion=p_id_leccion FOR UPDATE;
    END bloquear_leccion;

    PROCEDURE bloquear_modulo_activo(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT m.id_modulo FROM Modulos m JOIN Lenguajes l ON l.id_lenguaje=m.id_lenguaje WHERE
                m.id_modulo=p_padre AND l.activo=1 FOR UPDATE OF m.id_modulo;
    END bloquear_modulo_activo;

    PROCEDURE cursos_editor(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_lenguaje,nombre FROM Lenguajes WHERE activo=1 ORDER BY id_lenguaje;
    END cursos_editor;

    PROCEDURE existe_lenguaje(p_filas OUT SYS_REFCURSOR, p_nombre IN VARCHAR2) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Lenguajes WHERE LOWER(nombre) = p_nombre;
    END existe_lenguaje;

    PROCEDURE leccion_por_id(p_filas OUT SYS_REFCURSOR, p_id_leccion IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT le.id_leccion, le.id_modulo, le.titulo, le.contenido, le.orden_secuencia, le.exp_recompensa,
                m.nombre_modulo, m.descripcion AS mod_desc, m.orden_secuencia AS mod_orden, lg.id_lenguaje,
                lg.nombre AS lang_nombre, lg.descripcion AS lang_desc, lg.icono_url, lg.activo FROM Lecciones le
                JOIN Modulos m ON m.id_modulo = le.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje
                WHERE le.id_leccion = p_id_leccion;
    END leccion_por_id;

    PROCEDURE lecciones_activas(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT le.id_leccion, le.id_modulo, le.titulo, le.contenido, le.orden_secuencia, le.exp_recompensa FROM
                Lecciones le JOIN Modulos m ON m.id_modulo = le.id_modulo JOIN Lenguajes l ON l.id_lenguaje =
                m.id_lenguaje WHERE l.activo = 1 ORDER BY m.id_lenguaje, m.orden_secuencia, le.orden_secuencia;
    END lecciones_activas;

    PROCEDURE lecciones_modulo(p_filas OUT SYS_REFCURSOR, p_id_modulo IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_leccion, titulo, contenido, orden_secuencia, exp_recompensa FROM Lecciones WHERE id_modulo =
                p_id_modulo ORDER BY orden_secuencia ASC;
    END lecciones_modulo;

    PROCEDURE lenguaje_por_id(p_filas OUT SYS_REFCURSOR, p_id_lenguaje IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_lenguaje, nombre, descripcion, icono_url, activo FROM Lenguajes WHERE id_lenguaje =
                p_id_lenguaje;
    END lenguaje_por_id;

    PROCEDURE lenguajes_activos(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_lenguaje, nombre, descripcion, icono_url, activo FROM Lenguajes WHERE activo = 1 ORDER BY
                id_lenguaje ASC;
    END lenguajes_activos;

    PROCEDURE modulo_por_id(p_filas OUT SYS_REFCURSOR, p_id_modulo IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT m.id_modulo, m.id_lenguaje, m.nombre_modulo, m.descripcion, m.orden_secuencia, l.nombre AS
                lang_nombre, l.descripcion AS lang_desc, l.icono_url, l.activo FROM Modulos m JOIN Lenguajes l ON
                l.id_lenguaje = m.id_lenguaje WHERE m.id_modulo = p_id_modulo;
    END modulo_por_id;

    PROCEDURE modulos_activos(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT m.id_modulo, m.id_lenguaje, m.nombre_modulo, m.descripcion, m.orden_secuencia FROM Modulos m
                JOIN Lenguajes l ON l.id_lenguaje = m.id_lenguaje WHERE l.activo = 1 ORDER BY m.id_lenguaje,
                m.orden_secuencia;
    END modulos_activos;

    PROCEDURE modulos_lenguaje(p_filas OUT SYS_REFCURSOR, p_id_lenguaje IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_modulo, nombre_modulo, descripcion, orden_secuencia FROM Modulos WHERE id_lenguaje =
                p_id_lenguaje ORDER BY orden_secuencia ASC;
    END modulos_lenguaje;

    PROCEDURE orden_lecciones(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER, p_bloquear IN NUMBER) IS
    BEGIN
        IF p_bloquear = 1 THEN
                OPEN p_filas FOR SELECT id_leccion,titulo,contenido,orden_secuencia,exp_recompensa FROM Lecciones
                    WHERE id_modulo=p_padre ORDER BY orden_secuencia FOR UPDATE;
            ELSE
                OPEN p_filas FOR SELECT id_leccion,titulo,contenido,orden_secuencia,exp_recompensa FROM Lecciones
                    WHERE id_modulo=p_padre ORDER BY orden_secuencia;
            END IF;
    END orden_lecciones;

    PROCEDURE orden_modulos(p_filas OUT SYS_REFCURSOR, p_padre IN NUMBER, p_bloquear IN NUMBER) IS
    BEGIN
        IF p_bloquear = 1 THEN
                OPEN p_filas FOR SELECT id_modulo,nombre_modulo,descripcion,orden_secuencia,0 FROM Modulos WHERE
                    id_lenguaje=p_padre ORDER BY orden_secuencia FOR UPDATE;
            ELSE
                OPEN p_filas FOR SELECT id_modulo,nombre_modulo,descripcion,orden_secuencia,0 FROM Modulos WHERE
                    id_lenguaje=p_padre ORDER BY orden_secuencia;
            END IF;
    END orden_modulos;

    PROCEDURE otro_lenguaje(
        p_filas OUT SYS_REFCURSOR,
        p_nombre IN VARCHAR2,
        p_id_excluido IN NUMBER
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Lenguajes WHERE LOWER(nombre) = p_nombre AND id_lenguaje <> p_id_excluido;
    END otro_lenguaje;
END PKG_CATALOGO;
/
