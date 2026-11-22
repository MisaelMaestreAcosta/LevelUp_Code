-- LevelUp Code | Instalación o reconstrucción del entorno de demostración
-- Ejecuta con F5 en SQL Developer como LEVELUP_ADMIN desde la carpeta database.
-- Si falta el usuario, créalo por separado con 00_setup_user.sql.
-- Conserva juntos los archivos de esta carpeta para que @@ resuelva sus rutas.
-- Cambia la confirmación a YES solo en un esquema vacío o que puedas reconstruir.
-- La reconstrucción elimina las tablas de LevelUp Code y sus datos.
-- Para actualizar una base existente, sigue database/README.md.

SET ECHO OFF;
SET FEEDBACK ON;
SET SERVEROUTPUT ON SIZE UNLIMITED;
SET DEFINE ON;
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

DEFINE levelup_confirm_rebuild = "NO"

DECLARE
    v_confirmation VARCHAR2(10) := UPPER(TRIM('&levelup_confirm_rebuild'));
BEGIN
    IF v_confirmation <> 'YES' THEN
        RAISE_APPLICATION_ERROR(
            -20090,
            'Ejecución cancelada. Cambia levelup_confirm_rebuild de NO a YES para reconstruir el esquema.'
        );
    END IF;
END;
/

PROMPT [1/20] LIMPIEZA DE TABLAS DE LEVELUP CODE
@@00_drop.sql

PROMPT [2/20] ESQUEMA BASE
@@01_schema.sql

PROMPT [3/20] CATÁLOGO INICIAL
@@02_seed.sql

PROMPT [4/20] DATOS DE DEMOSTRACIÓN
@@02b_seed_demo_users.sql

PROMPT [5/20] CUADERNO Y FAVORITOS
@@05_student_notebook.sql

PROMPT [6/20] METAS DE APRENDIZAJE
@@06_learning_goals.sql

PROMPT [7/20] PREFERENCIAS DE USUARIO
@@07_user_preferences.sql

PROMPT [8/20] PUBLICACIÓN DE RETOS
@@08_content_publication.sql

PROMPT [9/20] VERSIONADO E HISTORIAL
@@09_content_versions.sql

PROMPT [10/20] TIPOS DE RETO
@@10_challenge_types.sql

PROMPT [11/20] GAMIFICACIÓN
@@03_plsql_gamificacion.sql

PROMPT [12/20] PLANTILLAS Y SOLUCIONES
@@12_seed_starter_templates.sql

PROMPT [13/20] GUÍAS DIDÁCTICAS
@@13_challenge_guidance.sql

PROMPT [14/20] FINALES POR MÓDULO
@@14_module_finals.sql

PROMPT [15/20] AMPLIACIÓN DEL CATÁLOGO
@@15_expanded_curriculum.sql

PROMPT [16/20] LECTURAS AMPLIADAS
@@16_enriched_lessons.sql

PROMPT [17/20] ESQUEMA DE MISIONES
@@15_professional_missions.sql

PROMPT [18/20] MISIÓN DE DEMOSTRACIÓN
@@16_seed_discount_mission.sql

PROMPT [19/20] ACCESO A DATOS DESDE JAVA
@@17_data_access.sql

PROMPT [20/20] PRUEBAS PL/SQL
@@04_test_plsql.sql

COMMIT;

PROMPT LEVELUP CODE - RECONSTRUCCION Y VALIDACION FINALIZADAS
