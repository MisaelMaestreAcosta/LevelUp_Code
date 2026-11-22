-- Consultas de disponibilidad y objetos instalados.
CREATE OR REPLACE PACKAGE PKG_DIAGNOSTICO AS
    PROCEDURE columnas_guias(p_filas OUT SYS_REFCURSOR);
    PROCEDURE columnas_instaladas(p_filas OUT SYS_REFCURSOR);
    PROCEDURE comprobar_usuarios(p_filas OUT SYS_REFCURSOR);
    PROCEDURE objetos_validos(p_filas OUT SYS_REFCURSOR);
    PROCEDURE tabla_casos_mision(p_filas OUT SYS_REFCURSOR);
    PROCEDURE tabla_final_modulo(p_filas OUT SYS_REFCURSOR);
END PKG_DIAGNOSTICO;
/

CREATE OR REPLACE PACKAGE BODY PKG_DIAGNOSTICO AS
    PROCEDURE columnas_guias(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT COUNT(*) FROM user_tab_columns WHERE table_name IN ( 'RETOS' , 'VERSION_RETO' ) AND column_name=
                'GUIA_DIDACTICA' ;
    END columnas_guias;

    PROCEDURE columnas_instaladas(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT table_name,column_name FROM user_tab_columns;
    END columnas_instaladas;

    PROCEDURE comprobar_usuarios(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM Usuarios FETCH FIRST 1 ROWS ONLY;
    END comprobar_usuarios;

    PROCEDURE objetos_validos(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT object_name,object_type FROM user_objects WHERE status= 'VALID' ;
    END objetos_validos;

    PROCEDURE tabla_casos_mision(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT 1 FROM user_tables WHERE table_name = 'REQUISITO_CASO_PRUEBA' ;
    END tabla_casos_mision;

    PROCEDURE tabla_final_modulo(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT COUNT(*) FROM user_tables WHERE table_name= 'FINAL_MODULO' ;
    END tabla_final_modulo;

END PKG_DIAGNOSTICO;
/
