-- =============================================================================
-- LEVELUP CODE - CREACIÓN DE USUARIO Y PERMISOS SEGUROS (ORACLE SQL)
-- Ejecutar conectado como SYS o SYSTEM en el contenedor pluggable (XEPDB1):
--   ALTER SESSION SET CONTAINER = XEPDB1;
-- =============================================================================

-- 1. Crear el usuario del esquema (ajustar contraseña según requerimiento local)
CREATE USER LEVELUP_ADMIN IDENTIFIED BY "password123"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA UNLIMITED ON USERS;

-- 2. Asignar únicamente los privilegios estrictamente necesarios (Principio de Menor Privilegio)
-- NOTA DE SEGURIDAD: NO utilizar 'GRANT DBA'. Solo se conceden roles de conectividad y recursos.
GRANT CONNECT, RESOURCE TO LEVELUP_ADMIN;
GRANT CREATE VIEW TO LEVELUP_ADMIN;
GRANT UNLIMITED TABLESPACE TO LEVELUP_ADMIN;

-- =============================================================================
-- Paso siguiente:
-- Abrir una conexión en Oracle SQL Developer con el usuario LEVELUP_ADMIN
-- y ejecutar en orden:
--   1. database/01_schema.sql
--   2. database/02_seed.sql
-- =============================================================================
