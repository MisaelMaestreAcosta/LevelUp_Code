-- Acceso a datos de LevelUp Code | Ejecutar después de instalar el esquema.
-- Conecta como LEVELUP_ADMIN y ejecuta este archivo con F5 en SQL Developer.
-- Conserva la subcarpeta packages junto a este archivo.
-- Los procedimientos conservan la transacción del llamador.

SET DEFINE OFF;
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

@@packages/pkg_diagnostico.sql
@@packages/pkg_usuarios.sql
@@packages/pkg_catalogo.sql
@@packages/pkg_retos.sql
@@packages/pkg_seguimiento.sql
@@packages/pkg_espacio_estudiante.sql

DECLARE
    v_detalle VARCHAR2(1000);
BEGIN
    SELECT MIN(name || '/' || type || ':' || line || ' ' || SUBSTR(text, 1, 300))
    INTO v_detalle
    FROM user_errors
    WHERE name IN (
        'PKG_DIAGNOSTICO', 'PKG_USUARIOS', 'PKG_CATALOGO',
        'PKG_RETOS', 'PKG_SEGUIMIENTO', 'PKG_ESPACIO_ESTUDIANTE'
    ) AND attribute = 'ERROR';

    IF v_detalle IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20095, 'Error al compilar el acceso a datos: ' || v_detalle);
    END IF;

    -- Una referencia de archivo ausente también se detecta aunque el cliente
    -- continúe tras un error al abrir un archivo de comandos.
    FOR paquete IN (
        SELECT column_value AS nombre FROM TABLE(SYS.ODCIVARCHAR2LIST(
            'PKG_DIAGNOSTICO', 'PKG_USUARIOS', 'PKG_CATALOGO',
            'PKG_RETOS', 'PKG_SEGUIMIENTO', 'PKG_ESPACIO_ESTUDIANTE'
        ))
        MINUS
        SELECT object_name FROM user_objects
        WHERE object_type IN ('PACKAGE', 'PACKAGE BODY') AND status = 'VALID'
        GROUP BY object_name
        HAVING COUNT(*) = 2
    ) LOOP
        RAISE_APPLICATION_ERROR(-20096, 'Falta un paquete válido: ' || paquete.nombre);
    END LOOP;
END;
/
