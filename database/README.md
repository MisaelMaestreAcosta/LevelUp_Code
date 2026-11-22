# Base de datos de LevelUp Code

| Situación | Archivo que debes abrir |
|---|---|
| Quieres una instalación completa para una demostración | [11_master_database.sql](11_master_database.sql) |
| Ya tienes el esquema actualizado y necesitas las consultas que llama Java | [17_data_access.sql](17_data_access.sql) |
| Quieres comprobar las reglas de gamificación con los datos de demostración | [04_test_plsql.sql](04_test_plsql.sql) |

## Instalación para demostración

1. Si falta `LEVELUP_ADMIN`, créalo con [00_setup_user.sql](00_setup_user.sql). Conéctate como `SYS AS SYSDBA` o `SYSTEM` al servicio del contenedor de la aplicación, normalmente `XEPDB1`, y configura una contraseña propia antes de ejecutar el script.
2. Conéctate a ese servicio como `LEVELUP_ADMIN`. Abre [11_master_database.sql](11_master_database.sql) desde la carpeta `database` del repositorio completo. El maestro llama a los demás archivos mediante rutas relativas con `@@`.
3. En un esquema vacío o que puedas reconstruir, cambia `levelup_confirm_rebuild` de `NO` a `YES` y ejecuta el maestro con **F5 (Run Script)**. El maestro elimina las tablas de LevelUp Code que encuentre, crea el esquema, carga el catálogo y las cuentas de demostración, compila los paquetes y ejecuta las pruebas PL/SQL.

En una instalación manual para uso real, omite `02b_seed_demo_users.sql` y la prueba que depende de esas cuentas. El orden de los demás archivos se ve en el maestro.

## Actualizar una base con datos

- Aplica únicamente los archivos pendientes según los objetos instalados, siguiendo el orden del maestro a partir de las migraciones que falten. Los scripts `05` a `10` son de aplicación única.
- Si tu esquema ya tiene las migraciones hasta misiones y el paquete de gamificación vigente, ejecuta [17_data_access.sql](17_data_access.sql) con F5 como `LEVELUP_ADMIN`. El archivo instala los paquetes que usa Java y llama a cada uno desde la subcarpeta `packages/`.
- Para actualizar desde la versión anterior de gamificación, ejecuta [03_plsql_gamificacion.sql](03_plsql_gamificacion.sql) y luego [16_enriched_lessons.sql](16_enriched_lessons.sql) con F5. El primero instala la sincronización de rachas y la recarga diaria de vidas. El segundo amplía las lecturas conservando los identificadores de las lecciones y el progreso.
- Reserva `00_drop.sql`, `01_schema.sql` y `02_seed.sql` para la preparación inicial o la reconstrucción. El maestro es una instalación completa y su fase de limpieza elimina los datos del proyecto.

## Qué contiene cada grupo

| Grupo | Archivos | Qué hacen |
|---|---|---|
| Usuario y esquema | [00_setup_user.sql](00_setup_user.sql), [01_schema.sql](01_schema.sql) | Crean el propietario de la aplicación y sus tablas base. |
| Carga inicial | [02_seed.sql](02_seed.sql), [02b_seed_demo_users.sql](02b_seed_demo_users.sql) | Cargan el catálogo inicial y, por separado, las cuentas de demostración. |
| Herramientas del estudiante | [05_student_notebook.sql](05_student_notebook.sql), [06_learning_goals.sql](06_learning_goals.sql), [07_user_preferences.sql](07_user_preferences.sql) | Añaden cuaderno, favoritos, metas y preferencias. |
| Publicación y evaluación | [08_content_publication.sql](08_content_publication.sql), [09_content_versions.sql](09_content_versions.sql), [10_challenge_types.sql](10_challenge_types.sql) | Añaden publicación, versiones de retos, vínculo histórico de los intentos y modalidades complementarias. |
| Gamificación y consultas | [03_plsql_gamificacion.sql](03_plsql_gamificacion.sql), [17_data_access.sql](17_data_access.sql) | Aplican las reglas de XP, vidas y progreso. Exponen las consultas a Java mediante procedimientos. |
| Catálogo ampliado | [12_seed_starter_templates.sql](12_seed_starter_templates.sql), [13_challenge_guidance.sql](13_challenge_guidance.sql), [14_module_finals.sql](14_module_finals.sql), [15_expanded_curriculum.sql](15_expanded_curriculum.sql), [16_enriched_lessons.sql](16_enriched_lessons.sql) | Incorporan plantillas, soluciones de referencia, pistas, finales, módulos adicionales y lecturas ampliadas. |
| Misiones | [15_professional_missions.sql](15_professional_missions.sql), [16_seed_discount_mission.sql](16_seed_discount_mission.sql) | Crean la estructura de misiones y cargan Tienda Horizonte. |
| Operación y pruebas | [00_drop.sql](00_drop.sql), [11_master_database.sql](11_master_database.sql), [04_test_plsql.sql](04_test_plsql.sql) | Limpian, coordinan la instalación y comprueban las reglas PL/SQL. |

Los números son identificadores históricos. Para el orden de ejecución usa el maestro.

## Paquetes instalados

| Paquete | Responsabilidad |
|---|---|
| [`PKG_USUARIOS`](packages/pkg_usuarios.sql) | Búsqueda de usuarios, autorización administrativa, ranking e inscripciones. |
| [`PKG_CATALOGO`](packages/pkg_catalogo.sql) | Lenguajes, módulos, lecciones y su orden. |
| [`PKG_RETOS`](packages/pkg_retos.sql) | Retos, casos de prueba, soluciones, versiones y misiones profesionales. |
| [`PKG_SEGUIMIENTO`](packages/pkg_seguimiento.sql) | Historial de intentos, estados de lecciones, logros y estadísticas de XP. |
| [`PKG_ESPACIO_ESTUDIANTE`](packages/pkg_espacio_estudiante.sql) | Cuaderno, favoritos, metas y preferencias. |
| [`PKG_DIAGNOSTICO`](packages/pkg_diagnostico.sql) | Disponibilidad del esquema y comprobación de objetos instalados. |
| [`PKG_GAMIFICACION`](03_plsql_gamificacion.sql) | Reglas de experiencia, vidas, racha y progreso. |
