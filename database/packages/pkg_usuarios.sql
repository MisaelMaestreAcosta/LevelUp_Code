-- Consultas de usuarios, permisos de administración e inscripciones.
CREATE OR REPLACE PACKAGE PKG_USUARIOS AS
    PROCEDURE administrador(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE esta_inscrito(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_lenguaje IN NUMBER
    );
    PROCEDURE inscribir(p_id_usuario IN NUMBER, p_id_lenguaje IN NUMBER);
    PROCEDURE inscripciones_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
    PROCEDURE ranking_usuarios(p_filas OUT SYS_REFCURSOR);
    PROCEDURE usuario_por_correo(p_filas OUT SYS_REFCURSOR, p_correo IN VARCHAR2);
    PROCEDURE usuario_por_id(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER);
END PKG_USUARIOS;
/

CREATE OR REPLACE PACKAGE BODY PKG_USUARIOS AS
    PROCEDURE administrador(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Usuarios u JOIN Roles r ON r.id_rol=u.id_rol WHERE u.id_usuario=p_id_usuario AND
                UPPER(TRIM(r.nombre_rol))= 'ADMINISTRADOR' ;
    END administrador;

    PROCEDURE esta_inscrito(
        p_filas OUT SYS_REFCURSOR,
        p_id_usuario IN NUMBER,
        p_id_lenguaje IN NUMBER
    ) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Inscripciones WHERE id_usuario = p_id_usuario AND id_lenguaje = p_id_lenguaje;
    END esta_inscrito;

    PROCEDURE inscribir(p_id_usuario IN NUMBER, p_id_lenguaje IN NUMBER) IS
    BEGIN
        MERGE INTO Inscripciones dst USING (SELECT p_id_usuario AS id_usuario, p_id_lenguaje AS id_lenguaje FROM
            dual) src ON (dst.id_usuario = src.id_usuario AND dst.id_lenguaje = src.id_lenguaje) WHEN NOT MATCHED
            THEN INSERT (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (p_id_usuario, p_id_lenguaje, SYSDATE);
    END inscribir;

    PROCEDURE inscripciones_usuario(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_lenguaje FROM Inscripciones WHERE id_usuario = p_id_usuario ORDER BY fecha_inscripcion ASC;
    END inscripciones_usuario;

    PROCEDURE ranking_usuarios(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT u.id_usuario, u.id_rol, u.primer_nombre, u.segundo_nombre, u.primer_apellido,
                u.segundo_apellido, u.correo_electronico, u.password_hash, u.exp_total, u.nivel,
                PKG_GAMIFICACION.FN_CALCULAR_RACHA_ACTIVA(u.id_usuario) AS racha_actual, u.racha_maxima, u.vidas,
                u.fecha_registro, u.ultima_actividad, r.nombre_rol, r.descripcion AS rol_descripcion FROM Usuarios
                u JOIN Roles r ON r.id_rol = u.id_rol ORDER BY u.exp_total DESC, u.id_usuario ASC;
    END ranking_usuarios;

    PROCEDURE usuario_por_correo(p_filas OUT SYS_REFCURSOR, p_correo IN VARCHAR2) IS
    BEGIN
        OPEN p_filas FOR
            SELECT u.id_usuario, u.id_rol, u.primer_nombre, u.segundo_nombre, u.primer_apellido,
                u.segundo_apellido, u.correo_electronico, u.password_hash, u.exp_total, u.nivel,
                PKG_GAMIFICACION.FN_CALCULAR_RACHA_ACTIVA(u.id_usuario) AS racha_actual, u.racha_maxima, u.vidas,
                u.fecha_registro, u.ultima_actividad, r.nombre_rol, r.descripcion AS rol_descripcion FROM Usuarios
                u JOIN Roles r ON r.id_rol = u.id_rol WHERE LOWER(u.correo_electronico) = p_correo;
    END usuario_por_correo;

    PROCEDURE usuario_por_id(p_filas OUT SYS_REFCURSOR, p_id_usuario IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT u.id_usuario, u.id_rol, u.primer_nombre, u.segundo_nombre, u.primer_apellido,
                u.segundo_apellido, u.correo_electronico, u.password_hash, u.exp_total, u.nivel,
                PKG_GAMIFICACION.FN_CALCULAR_RACHA_ACTIVA(u.id_usuario) AS racha_actual, u.racha_maxima, u.vidas,
                u.fecha_registro, u.ultima_actividad, r.nombre_rol, r.descripcion AS rol_descripcion FROM Usuarios
                u JOIN Roles r ON r.id_rol = u.id_rol WHERE u.id_usuario = p_id_usuario;
    END usuario_por_id;
END PKG_USUARIOS;
/
