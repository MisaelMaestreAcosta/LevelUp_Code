# LevelUp Code

## Actualizaciones de aprendizaje

La rama `feature/learning-experience` añade el centro de práctica y estadísticas reales por lenguaje.
Para activar el cuaderno y los favoritos, ejecuta **una sola vez**, conectado como propietario
del esquema, `database/05_student_notebook.sql` después de crear las tablas base de `01_schema.sql`.
Es necesario tanto para instalaciones nuevas como para bases existentes; no borra datos previos.
Las notas y favoritos son privados por usuario y se guardan en Oracle. En modo demo se muestran
como funciones que requieren una cuenta conectada. Las estadísticas no utilizan actividad demo.

Ejecuta también **una sola vez** `database/06_learning_goals.sql` para activar las metas personales.
Permite objetivos diarios o de siete días de retos superados, lecturas, XP o días con XP.
Su avance se consulta desde los registros existentes y no entrega recompensas adicionales;
no requiere nuevos procedimientos PL/SQL. Instala ambos scripts con la aplicación cerrada.

El laboratorio guarda manualmente un experimento por cuenta, modo y lenguaje en
`.levelup-code/laboratory` dentro del directorio del usuario. Java utiliza el motor real
existente y Python/C++ continúan mostrando resultados simulados. El comparador incorpora
actividades de predicción con explicaciones, copia de código y controles de letra.

Ejecuta **una sola vez** `database/07_user_preferences.sql` después del esquema base para guardar
lenguaje inicial, tamaño del editor, indentación y reducción de animaciones. Las preferencias
se recuperan al abrir el panel. Si la migración todavía no está instalada, el panel utiliza
los valores predeterminados. El perfil permite editar nombre y apellido y cambiar la contraseña
verificando la actual. El cambio de contraseña usa una actualización condicional para detectar
modificaciones concurrentes. Las preferencias demo solo duran la sesión.

Para la administración avanzada, aplica **una vez y en orden** `database/08_content_publication.sql`
y `database/09_content_versions.sql` antes de ejecutar esta versión de la aplicación, tanto en
bases nuevas como existentes. Los retos anteriores quedan publicados y se crea una copia inicial
de su contenido actual. Las versiones de ediciones anteriores a esta migración no se pueden reconstruir.
El panel permite guardar borradores, validar una solución de referencia privada, publicar, archivar,
previsualizar, editar quizzes y administrar casos de prueba visibles u ocultos. Cada guardado crea
una revisión y el historial de intentos conserva el enunciado de la versión evaluada. La XP se sigue
otorgando una vez por reto, aunque haya varias versiones. Los envíos comprueban la revisión antes
de evaluar y antes de confirmar la transacción. El trigger `TRG_INTENTO_VERSION` vincula los intentos
creados por el paquete PL/SQL existente con la versión vigente.

Después aplica **una vez** `database/10_challenge_types.sql` para habilitar predicción de salida,
depuración y ordenamiento de fragmentos. La predicción amplía los quizzes con un fragmento de código,
la depuración reutiliza plantillas y casos de `Reto_Codigo`, y el ordenamiento guarda fragmentos y
su permutación correcta. El catálogo permite filtrar por estos tipos y el administrador puede
crearlos, previsualizarlos y publicarlos. El modo demo incluye un ejemplo de cada tipo.
Los ejercicios de ordenamiento usan Subir/Bajar, conservan borrador y registran resultados con
el mismo servicio de XP e intentos. La vista previa evalúa de forma local.

Antes de utilizar los envíos, recompila `database/03_plsql_gamificacion.sql` si la base aún usa
la versión del paquete con `COMMIT` internos. Las pruebas incluyen una integración con rollback
para las entradas/salidas vacías y otra para comprobar snapshots e impedir envíos a retos archivados.
La segunda se omite hasta instalar las migraciones 08–10. Los scripts de este bloque se entregan
para su instalación y no se ejecutan automáticamente al arrancar la aplicación.

Plataforma interactiva para el aprendizaje comparativo y resolución de retos algorítmicos en Java, Python y C++.  
**Universidad Popular del Cesar** — Ingeniería de Sistemas  
**Asignaturas:** Programación III (Prof. Alfredo Bautista) · Bases de Datos (Prof. Amilkar Sierra)  
**Líder Técnico:** Misael Andrés Maestre Acosta

---

## 🛠 Requisitos del Entorno

- **Java Development Kit (JDK):** Versión 21 LTS (o superior)
- **Apache Maven:** 3.8+ (o el Maven integrado en Apache NetBeans / IntelliJ IDEA)
- **Base de Datos:** Oracle Database 18c XE / 21c XE / 23ai Free (servicio pluggable `XEPDB1`)
- **Herramienta SQL:** Oracle SQL Developer, DBeaver o IntelliJ Database Tools

---

## 🚀 Puesta en Marcha Rápida

### 1. Configuración de Base de Datos en Oracle
1. Conéctate como administrador (`SYS AS SYSDBA` o `SYSTEM`) en la base de datos conectable `XEPDB1`:
   ```sql
   ALTER SESSION SET CONTAINER = XEPDB1;
   ```
2. Ejecuta el script de creación de usuario con privilegios mínimos explícitos y cuota acotada (`100M`):
   - [`database/00_setup_user.sql`](database/00_setup_user.sql) *(solicita la contraseña local mediante `&levelup_db_password`)*
3. Crea una nueva conexión en SQL Developer con el usuario creado (`LEVELUP_ADMIN`) y ejecuta en orden:
   - [`database/01_schema.sql`](database/01_schema.sql) (crea las 18 tablas normalizadas)
   - [`database/02_seed.sql`](database/02_seed.sql) (catálogo limpio de despliegue: roles, lenguajes, módulos, lecciones, retos y logros)
   - *(Opcional — solo entornos de demostración/pruebas)* [`database/02b_seed_demo_users.sql`](database/02b_seed_demo_users.sql) (cuentas e historiales de prueba)
   - [`database/03_plsql_gamificacion.sql`](database/03_plsql_gamificacion.sql) (compila el disparador `TRG_AUDIT_USUARIOS` y el paquete `PKG_GAMIFICACION`)
   - [`database/05_student_notebook.sql`](database/05_student_notebook.sql) (crea notas y retos favoritos por usuario)
   - [`database/06_learning_goals.sql`](database/06_learning_goals.sql) (crea metas personales)
   - [`database/07_user_preferences.sql`](database/07_user_preferences.sql) (crea preferencias del editor)
   - [`database/08_content_publication.sql`](database/08_content_publication.sql) (añade estados de publicación y soluciones de referencia)
   - [`database/09_content_versions.sql`](database/09_content_versions.sql) (conserva versiones del contenido y vincula intentos)
   - [`database/10_challenge_types.sql`](database/10_challenge_types.sql) (añade predicción, depuración y ordenamiento)

   En una base existente, aplica una vez los scripts 05–10 que falten y recompila
   `database/03_plsql_gamificacion.sql` para actualizar la gamificación y los reintentos idempotentes.
   Comprueba las tablas instaladas antes de ejecutar cada script de creación.

### 2. Pruebas de PL/SQL en Oracle SQL Developer
Para demostrar el funcionamiento de la lógica de negocio en base de datos sin alterar datos persistidos:
1. Abre [`database/04_test_plsql.sql`](database/04_test_plsql.sql) en SQL Developer o DBeaver (requiere haber cargado los datos de prueba de `02b_seed_demo_users.sql`).
2. Asegúrate de tener activa la salida de consola (`SET SERVEROUTPUT ON;`).
3. Ejecuta el bloque anónimo completo: valida funciones y procedimientos y revierte todos los cambios al final mediante `ROLLBACK TO sp_inicio_pruebas`.

### 3. Configuración Obligatoria de Credenciales Locales
La aplicación no incluye credenciales por defecto en el código fuente. Configúralas mediante `db.properties` o variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`):
1. Copia la plantilla de configuración:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
2. Edita `src/main/resources/db.properties` con el usuario y contraseña definidos al ejecutar `00_setup_user.sql`.  
   *(Nota: `db.properties` está ignorado en Git para prevenir exposición de credenciales).*

La plantilla también define límites de conexión y lectura. Puedes ajustarlos con
`db.connect.timeout.ms` y `db.read.timeout.ms`, o con las variables de entorno
`DB_CONNECT_TIMEOUT_MS` y `DB_READ_TIMEOUT_MS`.

### 4. Compilación Reproducible, Pruebas Automatizadas y Ejecución (Maven Wrapper)

El repositorio incluye **Maven Wrapper** (`mvnw` / `mvnw.cmd`), por lo que no requiere tener `mvn` instalado globalmente en el `PATH`:

```powershell
# En Windows (PowerShell / CMD):
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run

# En Linux / macOS:
./mvnw clean test
./mvnw javafx:run
```

`OracleLearningStorageIntegrationTest` comprueba notas, favoritos, metas y preferencias
mediante los repositorios reales. Necesita Oracle configurado y las migraciones instaladas.
Crea cuentas temporales y elimina sus datos al finalizar cada prueba. Verifica también
que una cuenta solo pueda consultar y modificar sus propios registros.

---

## 🏛️ Arquitectura de Base de Datos y PL/SQL

El sistema implementa una arquitectura híbrida de alta fidelidad donde las transacciones críticas de gamificación se ejecutan directamente en el motor Oracle mediante **PL/SQL**:

| Componente PL/SQL | Tipo de Objeto | Descripción y Regla de Negocio |
| :--- | :--- | :--- |
| `TRG_AUDIT_USUARIOS` | **Trigger a nivel de fila** | Registra automáticamente `ultima_actividad = SYSTIMESTAMP` y previene inconsistencias de vidas (`vidas < 0`) o pérdida de XP. |
| `FN_CALCULAR_NIVEL` | **Función Determinista** | Centraliza la fórmula matemática de nivelación: `GREATEST(1, FLOOR(1 + (exp_total / 300)))`. |
| `FN_CALCULAR_RACHA_ACTIVA` | **Función con Cursor** | Recorre en orden descendente `Actividad_Diaria` mediante un cursor explícito para calcular días consecutivos reales. |
| `FN_PORCENTAJE_PROGRESO` | **Función de Agregación** | Calcula el ratio de lecciones completadas vs. registradas para un lenguaje y usuario específico. |
| `SP_REGISTRAR_INTENTO_RETO` | **Procedimiento Atómico** | Gestiona el ciclo completo de envío de un reto en una sola transacción `ACID`: guarda el intento, actualiza XP una sola vez si es correcto, recalcula racha, actualiza actividad diaria (`MERGE`), descuenta vidas si falló y desbloquea logros en `Usuario_Logro`. Retorna parámetros `OUT` consumidos en Java mediante `CallableStatement`. |
| `SP_COMPLETAR_LECCION` | **Procedimiento Almacenado** | Finaliza lecciones en `Progreso_Leccion`, asigna XP y sincroniza la telemetría del estudiante. |
| `SP_RECUPERAR_VIDAS` | **Procedimiento Almacenado** | Restaura las vidas del estudiante a su capacidad máxima (5 vidas). |
| `04_test_plsql.sql` | **Bloque Anónimo** | Script de prueba con transacciones controladas (`SAVEPOINT` / `ROLLBACK`), manejo de excepciones (`RAISE_APPLICATION_ERROR`) y reporte vía `DBMS_OUTPUT`. |

---

## 🔐 Perfiles y Roles del Sistema

- **Estudiante:** Acceso al mapa curricular, comparador tri-lenguaje, lecciones interactivas, evaluador dinámico de retos y logros.
- **Administrador:** Acceso completo a la gestión y auditoría de retos en Oracle Database, validado a nivel de servicio (`ContentService`) e interfaz gráfica.
