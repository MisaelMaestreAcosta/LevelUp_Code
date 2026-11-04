# LevelUp Code

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
2. Ejecuta el script de creación de usuario con privilegios mínimos seguros:
   - [`database/00_setup_user.sql`](database/00_setup_user.sql)
3. Crea una nueva conexión en SQL Developer con el usuario creado (`LEVELUP_ADMIN`) y ejecuta en orden:
   - [`database/01_schema.sql`](database/01_schema.sql) (crea las 18 tablas normalizadas)
   - [`database/02_seed.sql`](database/02_seed.sql) (datos iniciales de roles, lenguajes, módulos, retos y logros)
   - [`database/03_plsql_gamificacion.sql`](database/03_plsql_gamificacion.sql) (compila el disparador `TRG_AUDIT_USUARIOS` y el paquete `PKG_GAMIFICACION`)

### 2. Pruebas Unitarias de PL/SQL en Oracle SQL Developer
Para demostrar el funcionamiento de la lógica de negocio en base de datos al docente:
1. Abre [`database/04_test_plsql.sql`](database/04_test_plsql.sql) en SQL Developer o DBeaver.
2. Asegúrate de tener activa la salida de consola (`SET SERVEROUTPUT ON;`).
3. Ejecuta el bloque anónimo completo para correr las 8 pruebas unitarias automatizadas con reporte visual de resultados (`[PASS]`).

### 3. Configuración de Credenciales de la Aplicación
1. Copia la plantilla de configuración:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
2. Abre `src/main/resources/db.properties` y verifica la URL, usuario y contraseña de tu instancia local.  
   *(Nota: `db.properties` se encuentra ignorado en Git para prevenir fuga accidental de credenciales).*

### 4. Compilación y Ejecución de la Aplicación

```bash
# Compilar el proyecto con Java 21 LTS
mvn clean compile

# Ejecutar la aplicación de escritorio en JavaFX 21 + CSS
mvn exec:java
```

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
