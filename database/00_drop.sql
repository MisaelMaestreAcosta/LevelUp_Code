-- =============================================================================
-- LEVELUP CODE - SCRIPT DE LIMPIEZA DE ESQUEMA (ORACLE SQL)
-- Elimina las tablas base y las extensiones de aprendizaje. Esta operación borra sus datos.
-- Ejecutar conectado con la conexión: level_up_admin
-- =============================================================================

BEGIN
    FOR t IN (
        SELECT table_name FROM user_tables WHERE table_name IN (
            'VERSION_CASO_PRUEBA',
            'VERSION_RETO',
            'SOLUCION_REFERENCIA',
            'RETO_PREDICCION',
            'RETO_ORDENAMIENTO',
            'PREFERENCIA_USUARIO',
            'META_USUARIO',
            'NOTA_ESTUDIO',
            'RETO_FAVORITO',
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

