-- =============================================================================
-- LEVELUP CODE - CREACIÓN DE USUARIO Y PERMISOS SEGUROS (ORACLE SQL)
-- Ejecutar conectado como SYS o SYSTEM en el contenedor pluggable (XEPDB1):
--   ALTER SESSION SET CONTAINER = XEPDB1;
-- =============================================================================

-- 1. Crear el usuario del esquema con cuota acotada en el tablespace USERS.
-- IMPORTANTE: Reemplaza la contraseña local antes de ejecutar y regístrala en db.properties.
DEFINE levelup_db_password = "Cambiar_Clave_Local_2026"

CREATE USER LEVELUP_ADMIN IDENTIFIED BY "&levelup_db_password"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 100M ON USERS;

-- 2. Principio de Menor Privilegio: conceder únicamente permisos específicos del esquema
-- (Sin GRANT DBA, sin RESOURCE heredado y sin UNLIMITED TABLESPACE).
GRANT CREATE SESSION   TO LEVELUP_ADMIN;
GRANT CREATE TABLE     TO LEVELUP_ADMIN;
GRANT CREATE VIEW      TO LEVELUP_ADMIN;
GRANT CREATE SEQUENCE  TO LEVELUP_ADMIN;
GRANT CREATE PROCEDURE TO LEVELUP_ADMIN;
GRANT CREATE TRIGGER   TO LEVELUP_ADMIN;

-- =============================================================================
-- Paso siguiente:
-- Abrir una conexión en Oracle SQL Developer con el usuario LEVELUP_ADMIN
-- y ejecutar en orden:
--   1. database/01_schema.sql
--   2. database/02_seed.sql
-- =============================================================================
