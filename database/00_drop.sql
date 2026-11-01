-- =============================================================================
-- LEVELUP CODE - SCRIPT DE LIMPIEZA DE ESQUEMA (ORACLE SQL)
-- Elimina las 18 tablas en orden inverso de dependencias referenciales.
-- Ejecutar conectado con la conexión: level_up_admin
-- =============================================================================

BEGIN
    FOR t IN (
        SELECT table_name FROM user_tables WHERE table_name IN (
            'USUARIO_LOGRO',
            'LOGRO_RETOS',
            'LOGRO_EXP',
            'LOGRO_RACHA',
            'LOGROS',
            'INTENTOS',
            'RETO_QUIZ',
            'CASOS_PRUEBA',
            'RETO_CODIGO',
            'RETOS',
            'PROGRESO_LECCION',
            'LECCIONES',
            'MODULOS',
            'INSCRIPCIONES',
            'LENGUAJES',
            'ACTIVIDAD_DIARIA',
            'USUARIOS',
            'ROLES'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
    END LOOP;
END;
/
