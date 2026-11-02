# LevelUp Code

Plataforma interactiva para el aprendizaje comparativo y resolución de retos algorítmicos en Java, Python y C++.  
**Universidad Popular del Cesar** — Ingeniería de Sistemas  
**Asignatura:** Programación III (Prof. Amilkar Sierra)  
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

### 2. Configuración de Credenciales de la Aplicación
1. Copia la plantilla de configuración:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
2. Abre `src/main/resources/db.properties` y verifica la URL, usuario y contraseña de tu instancia local.  
   *(Nota: `db.properties` se encuentra ignorado en Git para prevenir fuga accidental de credenciales).*

### 3. Compilación y Ejecución

```bash
# Compilar el proyecto con Java 21
mvn clean compile

# Ejecutar la aplicación de escritorio Swing/JavaFX
mvn exec:java
```

---

## 🔐 Perfiles y Roles del Sistema

- **Estudiante:** Acceso al mapa curricular, comparador tri-lenguaje, lecciones interactivas, evaluador dinámico de retos y logros.
- **Administrador:** Acceso completo a la gestión y auditoría de retos en Oracle Database, validado a nivel de servicio (`ContentService`) e interfaz gráfica.
