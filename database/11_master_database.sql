-- =============================================================================
-- LEVELUP CODE - SCRIPT MAESTRO AUTOCONTENIDO (ORACLE SQL)
-- Generado a partir de todos los scripts de la carpeta database.
--
-- OBJETIVO
--   Reconstruir y validar por completo la base de datos académica de LevelUp Code
--   desde un único archivo, sin depender de llamadas @@ a otros archivos.
--
-- EJECUCIÓN
--   1. El usuario LEVELUP_ADMIN debe existir. Si todavía no existe, copia y
--      ejecuta primero la FASE 0 incluida como comentario al final de este
--      encabezado, conectado como SYS o SYSTEM en XEPDB1.
--   2. Conéctate como LEVELUP_ADMIN.
--   3. Cambia levelup_confirm_rebuild de NO a YES.
--   4. Ejecuta el archivo completo con "Run Script" (F5) en SQL Developer.
--
-- ADVERTENCIAS
--   * La ejecución elimina todas las tablas y los datos actuales del esquema.
--   * Incluye cuentas y actividad de demostración académica.
--   * Al final ejecuta la batería de pruebas PL/SQL con DBMS_OUTPUT.
--   * No contiene contraseñas reales. La contraseña de la FASE 0 es un ejemplo.
--
-- ORDEN RESUELTO POR DEPENDENCIAS
--   limpieza -> esquema -> catálogo -> demo -> extensiones -> PL/SQL -> pruebas
-- =============================================================================

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

PROMPT ============================================================
PROMPT LEVELUP CODE - INICIO DE RECONSTRUCCION COMPLETA
PROMPT ============================================================

-- =============================================================================
-- FASE 0 - CREACIÓN DEL USUARIO (REFERENCIA, NO SE EJECUTA AUTOMÁTICAMENTE)
--
-- Esta fase exige una conexión SYS/SYSTEM y por eso se conserva comentada.
-- Copia su contenido en otra hoja SQL y ejecútalo solo si LEVELUP_ADMIN no existe.
-- =============================================================================
/*
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
*/

-- =============================================================================
-- FASE 1 - LIMPIEZA DE OBJETOS EXISTENTES
-- Fuente integrada: database/00_drop.sql
-- =============================================================================
PROMPT FASE 1 - LIMPIEZA DE OBJETOS EXISTENTES

-- =============================================================================
-- LEVELUP CODE - SCRIPT DE LIMPIEZA DE ESQUEMA (ORACLE SQL)
-- Elimina las tablas base y las extensiones de aprendizaje. Esta operación borra sus datos.
-- Ejecutar conectado con la conexión: level_up_admin
-- =============================================================================

BEGIN
    FOR t IN (
        SELECT table_name FROM user_tables WHERE table_name IN (
            'FINAL_MODULO',
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

-- =============================================================================
-- FASE 2 - ESQUEMA RELACIONAL BASE
-- Fuente integrada: database/01_schema.sql
-- =============================================================================
PROMPT FASE 2 - ESQUEMA RELACIONAL BASE

-- =============================================================================
-- LEVELUP CODE - ESQUEMA RELACIONAL COMPLETO (18 TABLAS - ORACLE SQL)
-- Basado 100% en el Diccionario de Datos del documento de arquitectura.
-- Equivalencias Oracle aplicadas:
--   INT      -> NUMBER
--   DATETIME -> TIMESTAMP
--   TEXT     -> CLOB
--   BOOLEAN  -> NUMBER(1) CHECK (columna IN (0, 1))
-- Ejecutar conectado en SQL Developer con la conexión: level_up_admin
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. TABLA: Roles
-- -----------------------------------------------------------------------------
CREATE TABLE Roles (
    id_rol      NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    nombre_rol  VARCHAR2(30)  NOT NULL,
    descripcion VARCHAR2(150),
    CONSTRAINT pk_roles PRIMARY KEY (id_rol),
    CONSTRAINT uq_roles_nombre UNIQUE (nombre_rol)
);

-- -----------------------------------------------------------------------------
-- 2. TABLA: Usuarios
-- -----------------------------------------------------------------------------
CREATE TABLE Usuarios (
    id_usuario         NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_rol             NUMBER        NOT NULL,
    primer_nombre      VARCHAR2(50)  NOT NULL,
    segundo_nombre     VARCHAR2(50),
    primer_apellido    VARCHAR2(50)  NOT NULL,
    segundo_apellido   VARCHAR2(50),
    correo_electronico VARCHAR2(120) NOT NULL,
    password_hash      VARCHAR2(255) NOT NULL,
    exp_total          NUMBER        DEFAULT 0 NOT NULL,
    nivel              NUMBER        DEFAULT 1 NOT NULL,
    racha_actual       NUMBER        DEFAULT 0 NOT NULL,
    racha_maxima       NUMBER        DEFAULT 0 NOT NULL,
    vidas              NUMBER        DEFAULT 5 NOT NULL,
    fecha_registro     DATE          DEFAULT SYSDATE NOT NULL,
    ultima_actividad   TIMESTAMP,
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo_electronico),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol)
        REFERENCES Roles (id_rol),
    CONSTRAINT ck_usuarios_exp CHECK (exp_total >= 0),
    CONSTRAINT ck_usuarios_nivel CHECK (nivel >= 1),
    CONSTRAINT ck_usuarios_vidas CHECK (vidas >= 0)
);

-- -----------------------------------------------------------------------------
-- 3. TABLA: Actividad_Diaria
-- -----------------------------------------------------------------------------
CREATE TABLE Actividad_Diaria (
    id_actividad NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_usuario   NUMBER NOT NULL,
    fecha        DATE   NOT NULL,
    exp_ganada   NUMBER DEFAULT 0 NOT NULL,
    CONSTRAINT pk_actividad_diaria PRIMARY KEY (id_actividad),
    CONSTRAINT uq_actividad_usuario_fecha UNIQUE (id_usuario, fecha),
    CONSTRAINT fk_actividad_usuario FOREIGN KEY (id_usuario)
        REFERENCES Usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT ck_actividad_exp CHECK (exp_ganada >= 0)
);

-- -----------------------------------------------------------------------------
-- 4. TABLA: Lenguajes
-- -----------------------------------------------------------------------------
CREATE TABLE Lenguajes (
    id_lenguaje NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    nombre      VARCHAR2(40)  NOT NULL,
    descripcion VARCHAR2(200),
    icono_url   VARCHAR2(255),
    activo      NUMBER(1)     DEFAULT 1 NOT NULL,
    CONSTRAINT pk_lenguajes PRIMARY KEY (id_lenguaje),
    CONSTRAINT uq_lenguajes_nombre UNIQUE (nombre),
    CONSTRAINT ck_lenguajes_activo CHECK (activo IN (0, 1))
);

-- -----------------------------------------------------------------------------
-- 5. TABLA: Inscripciones
-- -----------------------------------------------------------------------------
CREATE TABLE Inscripciones (
    id_usuario        NUMBER NOT NULL,
    id_lenguaje       NUMBER NOT NULL,
    fecha_inscripcion DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_inscripciones PRIMARY KEY (id_usuario, id_lenguaje),
    CONSTRAINT fk_inscripciones_usuario FOREIGN KEY (id_usuario)
        REFERENCES Usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_inscripciones_lenguaje FOREIGN KEY (id_lenguaje)
        REFERENCES Lenguajes (id_lenguaje)
);

-- -----------------------------------------------------------------------------
-- 6. TABLA: Modulos
-- -----------------------------------------------------------------------------
CREATE TABLE Modulos (
    id_modulo       NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_lenguaje     NUMBER        NOT NULL,
    nombre_modulo   VARCHAR2(80)  NOT NULL,
    descripcion     VARCHAR2(200),
    orden_secuencia NUMBER        NOT NULL,
    CONSTRAINT pk_modulos PRIMARY KEY (id_modulo),
    CONSTRAINT uq_modulos_orden UNIQUE (id_lenguaje, orden_secuencia),
    CONSTRAINT fk_modulos_lenguaje FOREIGN KEY (id_lenguaje)
        REFERENCES Lenguajes (id_lenguaje) ON DELETE CASCADE,
    CONSTRAINT ck_modulos_orden CHECK (orden_secuencia >= 1)
);

-- -----------------------------------------------------------------------------
-- 7. TABLA: Lecciones
-- -----------------------------------------------------------------------------
CREATE TABLE Lecciones (
    id_leccion      NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_modulo       NUMBER        NOT NULL,
    titulo          VARCHAR2(100) NOT NULL,
    contenido       CLOB          NOT NULL,
    orden_secuencia NUMBER        NOT NULL,
    exp_recompensa  NUMBER        NOT NULL,
    CONSTRAINT pk_lecciones PRIMARY KEY (id_leccion),
    CONSTRAINT uq_lecciones_orden UNIQUE (id_modulo, orden_secuencia),
    CONSTRAINT fk_lecciones_modulo FOREIGN KEY (id_modulo)
        REFERENCES Modulos (id_modulo) ON DELETE CASCADE,
    CONSTRAINT ck_lecciones_orden CHECK (orden_secuencia >= 1),
    CONSTRAINT ck_lecciones_exp CHECK (exp_recompensa >= 0)
);

-- -----------------------------------------------------------------------------
-- 8. TABLA: Progreso_Leccion
-- -----------------------------------------------------------------------------
CREATE TABLE Progreso_Leccion (
    id_usuario       NUMBER       NOT NULL,
    id_leccion       NUMBER       NOT NULL,
    estado           VARCHAR2(15) DEFAULT 'PENDIENTE' NOT NULL,
    fecha_completado TIMESTAMP,
    CONSTRAINT pk_progreso_leccion PRIMARY KEY (id_usuario, id_leccion),
    CONSTRAINT fk_progreso_usuario FOREIGN KEY (id_usuario)
        REFERENCES Usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_progreso_leccion FOREIGN KEY (id_leccion)
        REFERENCES Lecciones (id_leccion) ON DELETE CASCADE,
    CONSTRAINT ck_progreso_estado CHECK (estado IN ('PENDIENTE', 'EN_CURSO', 'COMPLETADA'))
);

-- -----------------------------------------------------------------------------
-- 9. TABLA: Retos
-- -----------------------------------------------------------------------------
CREATE TABLE Retos (
    id_reto         NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_leccion      NUMBER        NOT NULL,
    titulo_reto     VARCHAR2(100) NOT NULL,
    enunciado       CLOB          NOT NULL,
    tipo_reto       VARCHAR2(20)  NOT NULL,
    dificultad      VARCHAR2(15)  DEFAULT 'FACIL' NOT NULL,
    exp_recompensa  NUMBER        NOT NULL,
    orden_secuencia NUMBER        NOT NULL,
    CONSTRAINT pk_retos PRIMARY KEY (id_reto),
    CONSTRAINT uq_retos_orden UNIQUE (id_leccion, orden_secuencia),
    CONSTRAINT fk_retos_leccion FOREIGN KEY (id_leccion)
        REFERENCES Lecciones (id_leccion) ON DELETE CASCADE,
    CONSTRAINT ck_retos_tipo CHECK (tipo_reto IN ('CODIGO', 'QUIZ')),
    CONSTRAINT ck_retos_dificultad CHECK (dificultad IN ('FACIL', 'MEDIO', 'DIFICIL')),
    CONSTRAINT ck_retos_exp CHECK (exp_recompensa >= 0)
);

-- -----------------------------------------------------------------------------
-- 10. TABLA: Reto_Codigo
-- -----------------------------------------------------------------------------
CREATE TABLE Reto_Codigo (
    id_reto          NUMBER NOT NULL,
    codigo_inicial   CLOB,
    limite_tiempo_ms NUMBER NOT NULL,
    CONSTRAINT pk_reto_codigo PRIMARY KEY (id_reto),
    CONSTRAINT fk_reto_codigo_base FOREIGN KEY (id_reto)
        REFERENCES Retos (id_reto) ON DELETE CASCADE,
    CONSTRAINT ck_reto_codigo_tiempo CHECK (limite_tiempo_ms > 0)
);

-- -----------------------------------------------------------------------------
-- 11. TABLA: Casos_Prueba
-- -----------------------------------------------------------------------------
CREATE TABLE Casos_Prueba (
    id_caso         NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_reto         NUMBER    NOT NULL,
    entrada         CLOB      NOT NULL,
    salida_esperada CLOB      NOT NULL,
    es_oculto       NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_casos_prueba PRIMARY KEY (id_caso),
    CONSTRAINT fk_casos_reto_codigo FOREIGN KEY (id_reto)
        REFERENCES Reto_Codigo (id_reto) ON DELETE CASCADE,
    CONSTRAINT ck_casos_oculto CHECK (es_oculto IN (0, 1))
);

-- -----------------------------------------------------------------------------
-- 12. TABLA: Reto_Quiz
-- -----------------------------------------------------------------------------
CREATE TABLE Reto_Quiz (
    id_reto            NUMBER        NOT NULL,
    opciones           CLOB          NOT NULL,
    respuesta_correcta VARCHAR2(200) NOT NULL,
    CONSTRAINT pk_reto_quiz PRIMARY KEY (id_reto),
    CONSTRAINT fk_reto_quiz_base FOREIGN KEY (id_reto)
        REFERENCES Retos (id_reto) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 13. TABLA: Intentos
-- -----------------------------------------------------------------------------
CREATE TABLE Intentos (
    id_intento          NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    id_usuario          NUMBER    NOT NULL,
    id_reto             NUMBER    NOT NULL,
    codigo_enviado      CLOB      NOT NULL,
    salida_obtenida     CLOB,
    es_correcto         NUMBER(1) DEFAULT 0 NOT NULL,
    tiempo_ejecucion_ms NUMBER,
    exp_obtenida        NUMBER    DEFAULT 0 NOT NULL,
    id_solicitud        VARCHAR2(64) DEFAULT RAWTOHEX(SYS_GUID()) NOT NULL,
    fecha_intento       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_intentos PRIMARY KEY (id_intento),
    CONSTRAINT fk_intentos_usuario FOREIGN KEY (id_usuario)
        REFERENCES Usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_intentos_reto FOREIGN KEY (id_reto)
        REFERENCES Retos (id_reto) ON DELETE CASCADE,
    CONSTRAINT ck_intentos_correcto CHECK (es_correcto IN (0, 1)),
    CONSTRAINT ck_intentos_exp CHECK (exp_obtenida >= 0),
    CONSTRAINT uq_intentos_solicitud UNIQUE (id_solicitud)
);

-- -----------------------------------------------------------------------------
-- 14. TABLA: Logros
-- -----------------------------------------------------------------------------
CREATE TABLE Logros (
    id_logro        NUMBER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    nombre_insignia VARCHAR2(60)  NOT NULL,
    descripcion     VARCHAR2(200),
    tipo_condicion  VARCHAR2(30)  NOT NULL,
    CONSTRAINT pk_logros PRIMARY KEY (id_logro),
    CONSTRAINT ck_logros_tipo CHECK (tipo_condicion IN ('RACHA', 'EXP', 'RETOS'))
);

-- -----------------------------------------------------------------------------
-- 15. TABLA: Logro_Racha
-- -----------------------------------------------------------------------------
CREATE TABLE Logro_Racha (
    id_logro        NUMBER NOT NULL,
    dias_requeridos NUMBER NOT NULL,
    CONSTRAINT pk_logro_racha PRIMARY KEY (id_logro),
    CONSTRAINT fk_logro_racha_base FOREIGN KEY (id_logro)
        REFERENCES Logros (id_logro) ON DELETE CASCADE,
    CONSTRAINT ck_logro_racha_dias CHECK (dias_requeridos > 0)
);

-- -----------------------------------------------------------------------------
-- 16. TABLA: Logro_EXP
-- -----------------------------------------------------------------------------
CREATE TABLE Logro_EXP (
    id_logro      NUMBER NOT NULL,
    exp_requerida NUMBER NOT NULL,
    CONSTRAINT pk_logro_exp PRIMARY KEY (id_logro),
    CONSTRAINT fk_logro_exp_base FOREIGN KEY (id_logro)
        REFERENCES Logros (id_logro) ON DELETE CASCADE,
    CONSTRAINT ck_logro_exp_req CHECK (exp_requerida > 0)
);

-- -----------------------------------------------------------------------------
-- 17. TABLA: Logro_Retos
-- -----------------------------------------------------------------------------
CREATE TABLE Logro_Retos (
    id_logro         NUMBER NOT NULL,
    retos_requeridos NUMBER NOT NULL,
    CONSTRAINT pk_logro_retos PRIMARY KEY (id_logro),
    CONSTRAINT fk_logro_retos_base FOREIGN KEY (id_logro)
        REFERENCES Logros (id_logro) ON DELETE CASCADE,
    CONSTRAINT ck_logro_retos_req CHECK (retos_requeridos > 0)
);

-- -----------------------------------------------------------------------------
-- 18. TABLA: Usuario_Logro
-- -----------------------------------------------------------------------------
CREATE TABLE Usuario_Logro (
    id_usuario         NUMBER    NOT NULL,
    id_logro           NUMBER    NOT NULL,
    fecha_desbloqueado TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_usuario_logro PRIMARY KEY (id_usuario, id_logro),
    CONSTRAINT fk_usuario_logro_usr FOREIGN KEY (id_usuario)
        REFERENCES Usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_logro_log FOREIGN KEY (id_logro)
        REFERENCES Logros (id_logro) ON DELETE CASCADE
);

-- =============================================================================
-- ÍNDICES DE RENDIMIENTO SOBRE LLAVES FORÁNEAS Y RANKING (RNF-03)
-- =============================================================================
CREATE INDEX idx_usuarios_rol         ON Usuarios (id_rol);
CREATE INDEX idx_usuarios_ranking     ON Usuarios (exp_total DESC, nivel DESC);
CREATE INDEX idx_modulos_lenguaje     ON Modulos (id_lenguaje);
CREATE INDEX idx_lecciones_modulo     ON Lecciones (id_modulo);
CREATE INDEX idx_retos_leccion        ON Retos (id_leccion);
CREATE INDEX idx_casos_reto           ON Casos_Prueba (id_reto);
CREATE INDEX idx_intentos_usuario     ON Intentos (id_usuario, id_reto, fecha_intento DESC);
CREATE INDEX idx_progreso_usuario     ON Progreso_Leccion (id_usuario, estado);
CREATE INDEX idx_actividad_usuario    ON Actividad_Diaria (id_usuario, fecha DESC);

-- =============================================================================
-- FASE 3 - CATÁLOGO, CURSOS, RETOS Y LOGROS
-- Fuente integrada: database/02_seed.sql
-- =============================================================================
PROMPT FASE 3 - CATÁLOGO, CURSOS, RETOS Y LOGROS

-- =============================================================================
-- LEVELUP CODE - CATÁLOGO COMPLETO DE DESPLIEGUE (ROLES, CURSOS, RETOS Y LOGROS)
-- Incluye:
--   • 2 Roles (Estudiante, Administrador)
--   • 3 Lenguajes (Java 21 LTS, Python 3.12, ISO C++ 20)
--   • 9 Módulos (3 módulos progresivos por lenguaje)
--   • 18 Lecciones (6 lecciones por lenguaje con teoría estructurada)
--   • 36 Retos (18 Retos de Código + 18 Quizzes conceptuales: 2 por lección)
--   • 54 Casos de Prueba (2 visibles + 1 oculto por cada reto de código)
--   • 8 Logros de gamificación (Racha, Experiencia y Retos resueltos)
-- Ejecutar después de 01_schema.sql en la conexión: LEVELUP_ADMIN
-- =============================================================================

SET DEFINE OFF;

-- Limpieza idempotente del catálogo para permitir re-ejecución directa (F5)
DELETE FROM Modulos;
DELETE FROM Logros;

-- 1. ROLES
MERGE INTO Roles dst
USING (SELECT 1 AS id_rol, 'Estudiante' AS nombre_rol,
              'Acceso al catálogo de lenguajes, lecciones comparativas, laboratorio de retos y logros.' AS descripcion FROM dual) src
ON (dst.id_rol = src.id_rol)
WHEN MATCHED THEN UPDATE SET dst.nombre_rol = src.nombre_rol, dst.descripcion = src.descripcion
WHEN NOT MATCHED THEN INSERT (id_rol, nombre_rol, descripcion) VALUES (src.id_rol, src.nombre_rol, src.descripcion);

MERGE INTO Roles dst
USING (SELECT 2 AS id_rol, 'Administrador' AS nombre_rol,
              'Gestión integral de roles, lenguajes, módulos, lecciones, retos y auditoría.' AS descripcion FROM dual) src
ON (dst.id_rol = src.id_rol)
WHEN MATCHED THEN UPDATE SET dst.nombre_rol = src.nombre_rol, dst.descripcion = src.descripcion
WHEN NOT MATCHED THEN INSERT (id_rol, nombre_rol, descripcion) VALUES (src.id_rol, src.nombre_rol, src.descripcion);

-- 2. LENGUAJES
MERGE INTO Lenguajes dst
USING (SELECT 1 AS id_lenguaje, 'Java' AS nombre,
              'POO, Jerarquías ES-UN, Principios SOLID, Algoritmos y Colecciones Tipadas en JVM (Java 21 LTS).' AS descripcion,
              'java-icon' AS icono_url, 1 AS activo FROM dual) src
ON (dst.id_lenguaje = src.id_lenguaje)
WHEN MATCHED THEN UPDATE SET dst.nombre = src.nombre, dst.descripcion = src.descripcion, dst.icono_url = src.icono_url, dst.activo = src.activo
WHEN NOT MATCHED THEN INSERT (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (src.id_lenguaje, src.nombre, src.descripcion, src.icono_url, src.activo);

MERGE INTO Lenguajes dst
USING (SELECT 2 AS id_lenguaje, 'Python' AS nombre,
              'Protocolos ABC, Métodos Dunder, Slicing, Comprensiones y Estructuras Hash (Python 3.12).' AS descripcion,
              'python-icon' AS icono_url, 1 AS activo FROM dual) src
ON (dst.id_lenguaje = src.id_lenguaje)
WHEN MATCHED THEN UPDATE SET dst.nombre = src.nombre, dst.descripcion = src.descripcion, dst.icono_url = src.icono_url, dst.activo = src.activo
WHEN NOT MATCHED THEN INSERT (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (src.id_lenguaje, src.nombre, src.descripcion, src.icono_url, src.activo);

MERGE INTO Lenguajes dst
USING (SELECT 3 AS id_lenguaje, 'C++' AS nombre,
              'Polimorfismo Virtual, Gestión Determinista RAII, Smart Pointers y Contenedores STL (ISO C++ 20).' AS descripcion,
              'cpp-icon' AS icono_url, 1 AS activo FROM dual) src
ON (dst.id_lenguaje = src.id_lenguaje)
WHEN MATCHED THEN UPDATE SET dst.nombre = src.nombre, dst.descripcion = src.descripcion, dst.icono_url = src.icono_url, dst.activo = src.activo
WHEN NOT MATCHED THEN INSERT (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (src.id_lenguaje, src.nombre, src.descripcion, src.icono_url, src.activo);

-- =============================================================================
-- 3. MODULOS (3 Módulos progresivos por cada lenguaje = 9 Módulos)
-- =============================================================================
-- Módulos de JAVA (id_lenguaje = 1)
INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (1, 1, 'Módulo 1: POO, Polimorfismo y SOLID en Java', 'Clases abstractas, despacho dinámico en JVM, interfaces y sustitución de Liskov.', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (4, 1, 'Módulo 2: Algoritmos de Búsqueda y Dos Punteros', 'Búsqueda binaria O(log n) libre de overflow y técnica de dos punteros sobre cadenas.', 2);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (5, 1, 'Módulo 3: Colecciones Dinámicas y Tablas Hash', 'Complejidad amortizada en ArrayList y detección de duplicados en O(n) con HashSet.', 3);

-- Módulos de PYTHON (id_lenguaje = 2)
INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (2, 2, 'Módulo 1: Protocolos Abstractos y Modelo de Datos', 'Clases abstractas abc.ABC, Duck Typing y métodos especiales (__str__, __eq__).', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (6, 2, 'Módulo 2: Algoritmos Eficientes y Secuencias', 'Búsqueda binaria con división entera (//), slicing e inversión de secuencias.', 2);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (7, 2, 'Módulo 3: Listas Dinámicas, Comprensiones y Sets', 'Sobre-asignación en list, filtrado expresivo y búsqueda O(1) con set y dict.', 3);

-- Módulos de C++ (id_lenguaje = 3)
INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (3, 3, 'Módulo 1: Polimorfismo Virtual y Memoria RAII', 'Funciones virtuales puras (= 0), vtable, destructores virtuales y std::unique_ptr.', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (8, 3, 'Módulo 2: Algoritmos STL y Paso por Referencia', 'Búsqueda binaria sobre const std::vector<int>& e iteradores bidireccionales.', 2);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (9, 3, 'Módulo 3: Contenedores Secuenciales y Asociativos STL', 'Gestión de capacidad en std::vector y tablas hash con std::unordered_set.', 3);

-- =============================================================================
-- 4. LECCIONES (6 Lecciones por lenguaje = 18 Lecciones)
-- =============================================================================
-- ------------------------- LECCIONES DE JAVA ---------------------------------
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    1, 1,
    'Herencia, Clases Abstractas y Despacho Dinámico',
    '1. CONTRATOS ABSTRACTOS Y DESPACHO DINÁMICO EN JAVA' || CHR(10) ||
    'En Java, todos los métodos de instancia no marcados como final, static ni private se enlazan dinámicamente en tiempo de ejecución mediante la tabla de métodos virtuales (vtable) de la JVM.' || CHR(10) || CHR(10) ||
    '2. REGLAS CLAVE DE DISEÑO POO' || CHR(10) ||
    '• Una clase abstracta no puede instanciarse directamente; define estado común y métodos abstractos.' || CHR(10) ||
    '• La anotación @Override instruye al compilador javac para verificar que la firma coincida exactamente con la superclase.' || CHR(10) || CHR(10) ||
    '3. COMPARACIÓN CON PYTHON Y C++' || CHR(10) ||
    '• Java: despacho dinámico por defecto + @Override.' || CHR(10) ||
    '• Python: módulo abc + @abstractmethod.' || CHR(10) ||
    '• C++: exige la palabra clave virtual explícita y destructor virtual.',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    10, 1,
    'Interfaces, Contratos Múltiples y Sustitución de Liskov (SOLID)',
    '1. DESACOPLAMIENTO MEDIANTE INTERFACES' || CHR(10) ||
    'Mientras que la herencia de clases en Java es simple (extends), una clase puede implementar múltiples interfaces (implements), separando el QUÉ hace un componente de CÓMO lo ejecuta.' || CHR(10) || CHR(10) ||
    '2. PRINCIPIO DE SUSTITUCIÓN DE LISKOV (LSP)' || CHR(10) ||
    'Cualquier instancia de una subclase o implementación debe poder sustituir a su abstracción sin alterar la correctitud del programa ni lanzar excepciones inesperadas.' || CHR(10) || CHR(10) ||
    '3. APLICACIÓN PRÁCTICA' || CHR(10) ||
    'En sistemas de control de acceso, modelar políticas mediante contratos claros evita cadenas frágiles de acoplamiento y facilita las pruebas unitarias con dobles de prueba.',
    2, 70
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    2, 4,
    'Búsqueda Binaria O(log n) y Aritmética Segura',
    '1. DIVISIÓN DEL ESPACIO DE BÚSQUEDA' || CHR(10) ||
    'La búsqueda binaria reduce a la mitad el intervalo [low, high] en cada iteración sobre un arreglo ordenado. Para 1,000,000 de elementos requiere a lo sumo 20 comparaciones.' || CHR(10) || CHR(10) ||
    '2. PREVENCIÓN DE OVERFLOW EN ENTEROS DE 32 BITS' || CHR(10) ||
    'En Java y C++, el tipo int tiene 32 bits con signo (máximo 2,147,483,647). Calcular (low + high) / 2 desborda a negativo si ambos índices son grandes; la fórmula segura es:' || CHR(10) ||
    '    int mid = low + (high - low) / 2;',
    1, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    11, 4,
    'Técnica de Dos Punteros (Two Pointers) sobre Secuencias',
    '1. OPTIMIZACIÓN DE TIEMPO Y MEMORIA' || CHR(10) ||
    'La técnica de dos punteros coloca un índice al inicio (left = 0) y otro al final (right = n - 1), avanzando hacia el centro en tiempo O(n) y espacio auxiliar O(1).' || CHR(10) || CHR(10) ||
    '2. VERIFICACIÓN DE PALÍNDROMOS SIN COPIAS' || CHR(10) ||
    'En lugar de crear objetos adicionales invirtiendo toda la cadena en el Heap, comparar caracteres extremos permite abortar en la primera discrepancia encontrada.',
    2, 80
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    3, 5,
    'Gestión de Memoria y Colecciones Dinámicas (ArrayList)',
    '1. ARREGLOS DINÁMICOS EN EL HEAP DE LA JVM' || CHR(10) ||
    'ArrayList<T> mantiene un arreglo contiguo Object[] en el Heap. Cuando se llena, reserva un nuevo bloque ~1.5 veces mayor y copia las referencias con System.arraycopy.' || CHR(10) || CHR(10) ||
    '2. COMPLEJIDAD AMORTIZADA' || CHR(10) ||
    '• Acceso por índice get(i): O(1) directo en caché de CPU.' || CHR(10) ||
    '• Inserción al final add(e): O(1) amortizado.' || CHR(10) ||
    '• Recorrido y filtrado secuencial: O(n).',
    1, 80
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    12, 5,
    'Tablas Hash (HashSet / HashMap) y Detección en O(n)',
    '1. REDUCCIÓN DE O(n²) A O(n) CON HASHING' || CHR(10) ||
    'Detectar elementos repetidos con dos bucles anidados cuesta O(n²). Almacenar los elementos visitados en un HashSet<Integer> permite consultar existencia con contains() en O(1) promedio.' || CHR(10) || CHR(10) ||
    '2. CONTRATO EQUALS() Y HASHCODE()' || CHR(10) ||
    'En Java, si dos objetos son iguales según equals(), obligatoriamente deben producir el mismo valor entero en hashCode(). Desde Java 8, los buckets con muchas colisiones se transforman en árboles rojo-negro O(log n).',
    2, 95
);

-- ------------------------- LECCIONES DE PYTHON -------------------------------
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    4, 2,
    'Protocolos Abstractos (abc.ABC) y Duck Typing',
    '1. CLASES BASE ABSTRACTAS EN PYTHON' || CHR(10) ||
    'En Python, las clases abstractas heredan de abc.ABC y marcan sus operaciones obligatorias con el decorador @abstractmethod. Si una subclase omite la implementación, Python lanza TypeError al intentar instanciarla.' || CHR(10) || CHR(10) ||
    '2. DUCK TYPING VS TIPADO NOMINAL' || CHR(10) ||
    'Python favorece el principio "si camina como pato y grazna como pato, es un pato", combinando flexibilidad dinámica con verificación estructural mediante typing.Protocol.',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    13, 2,
    'Modelo de Datos de Python y Métodos Especiales (Dunder)',
    '1. PROTOCOLO DE OBJETOS CON MÉTODOS __DUNDER__' || CHR(10) ||
    'El comportamiento de los operadores en Python se delega a métodos especiales como __init__, __str__, __repr__, __eq__ y __hash__.' || CHR(10) || CHR(10) ||
    '2. REPRESENTACIÓN Y REGLAS DE DOMINIO' || CHR(10) ||
    '• __str__ define la representación legible para usuarios finales.' || CHR(10) ||
    '• __repr__ define la representación inequívoca para depuración técnica.' || CHR(10) ||
    '• Normalizar cadenas con .strip().upper() permite construir clasificadores limpios y expresivos.',
    2, 70
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    5, 6,
    'Búsqueda Binaria y División Entera (//) en Python',
    '1. PRECISIÓN ARBITRARIA DE ENTEROS EN PYTHON 3' || CHR(10) ||
    'Los enteros int en Python crecen dinámicamente en memoria, por lo que nunca sufren desbordamiento de 32 o 64 bits al sumar (low + high).' || CHR(10) || CHR(10) ||
    '2. DIVISIÓN ENTERA OBLIGATORIA (//)' || CHR(10) ||
    'El operador / siempre retorna un float (ej. 5 / 2 = 2.5), lo cual lanza TypeError al indexar una lista. En búsqueda binaria siempre debe utilizarse //:' || CHR(10) ||
    '    mid = (low + high) // 2',
    1, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    14, 6,
    'Slicing Avanzado e Inversión de Secuencias en Python',
    '1. SINTAXIS DE CORTE [INICIO:FIN:PASO]' || CHR(10) ||
    'Las secuencias inmutables como str y tuple soportan slicing en C a través del intérprete CPython. La expresión texto[::-1] recorre la secuencia con paso -1 generando una copia invertida.' || CHR(10) || CHR(10) ||
    '2. SLICING VS DOS PUNTEROS' || CHR(10) ||
    '• texto == texto[::-1] es conciso y muy rápido en CPython, pero usa O(n) memoria para la copia.' || CHR(10) ||
    '• Dos índices (left, right) mantiene consumo espacial O(1).',
    2, 80
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    6, 7,
    'Listas Dinámicas y Conteo de Referencias en Python',
    '1. ARQUITECTURA INTERNA DE PyListObject' || CHR(10) ||
    'Una lista de Python almacena un vector contiguo de punteros PyObject* que crece con sobre-asignación (~12.5%) para garantizar append() en tiempo O(1) amortizado.' || CHR(10) || CHR(10) ||
    '2. COMPRENSIONES Y EXPRESIONES GENERADORAS' || CHR(10) ||
    'Combinar sum() con filtrado condicional permite procesar colecciones numéricas en una sola línea legible y optimizada por el bytecode de Python.',
    1, 80
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    15, 7,
    'Conjuntos (set) y Diccionarios (dict) en Tiempo O(1)',
    '1. TABLAS HASH ABIERTAS EN CPYTHON' || CHR(10) ||
    'Los tipos set y dict implementan tablas hash con direccionamiento abierto altamente optimizadas. Verificar si un elemento pertenece a un conjunto (x in vistos) toma tiempo O(1) promedio frente a O(n) en una lista.' || CHR(10) || CHR(10) ||
    '2. REQUISITO DE INMUTABILIDAD (HASHABLE)' || CHR(10) ||
    'Solo objetos inmutables que implementen __hash__ (como int, str, tuple) pueden almacenarse en un set o usarse como clave de un dict.',
    2, 95
);

-- ------------------------- LECCIONES DE C++ ----------------------------------
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    7, 3,
    'Funciones Virtuales Puras (= 0) y Tabla Virtual (vtable)',
    '1. FILOSOFÍA DE COSTO CERO EN C++' || CHR(10) ||
    'En C++, las llamadas a métodos se resuelven estáticamente en compilación a menos que se declare la palabra clave virtual. Una función virtual pura (virtual std::string f() const = 0) convierte la clase en abstracta.' || CHR(10) || CHR(10) ||
    '2. DESTRUCTOR VIRTUAL OBLIGATORIO' || CHR(10) ||
    'Toda clase base polimórfica debe declarar virtual ~Base() = default; para garantizar que al liberar un puntero Base* se ejecute también el destructor de la clase derivada.',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    16, 3,
    'Determinismo RAII y Punteros Inteligentes (std::unique_ptr)',
    '1. ADQUISICIÓN DE RECURSOS ES INICIALIZACIÓN (RAII)' || CHR(10) ||
    'C++ no depende de un recolector de basura; los recursos (memoria heap, sockets, archivos) se vinculan al ciclo de vida de un objeto en la pila (stack) cuyo destructor los libera de forma determinista.' || CHR(10) || CHR(10) ||
    '2. PROPIEDAD EXCLUSIVA Y COMPARTIDA' || CHR(10) ||
    '• std::unique_ptr<T>: propiedad exclusiva sin sobrecosto respecto a un puntero crudo.' || CHR(10) ||
    '• std::shared_ptr<T>: conteo atómico de referencias compartidas.',
    2, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    8, 8,
    'Búsqueda Binaria sobre std::vector y Paso por Referencia',
    '1. PASO POR REFERENCIA CONSTANTE (const std::vector<int>&)' || CHR(10) ||
    'En C++, pasar un std::vector por valor copia todos sus elementos en memoria O(n). Recibir const std::vector<int>& evita la copia e impide modificaciones accidentales.' || CHR(10) || CHR(10) ||
    '2. ARITMÉTICA SEGURA DE ÍNDICES' || CHR(10) ||
    'Usando int mid = low + (high - low) / 2 sobre el contenedor contiguo se logra complejidad temporal O(log n) y espacial O(1).',
    1, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    17, 8,
    'Dos Punteros e Iteradores Bidireccionales en std::string',
    '1. ACCESO DIRECTO SOBRE MEMORIA CONTIGUA' || CHR(10) ||
    'std::string almacena sus caracteres de forma contigua (con optimización SSO para cadenas cortas en el stack). Recorrerla con dos índices extremos left y right permite validar simetrías en O(n) sin reservar memoria dinámica.',
    2, 85
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    9, 9,
    'Determinismo RAII, Smart Pointers y std::vector',
    '1. CRECIMIENTO GEOMÉTRICO EN STD::VECTOR' || CHR(10) ||
    'std::vector<int> garantiza localidad espacial en caché L1/L2 del procesador. Distingue entre size() (elementos actuales) y capacity() (espacio reservado antes de realocar).' || CHR(10) || CHR(10) ||
    '2. RECORRIDO BASADO EN RANGO (RANGE-BASED FOR)' || CHR(10) ||
    'La sintaxis for (int v : nums) genera código máquina idéntico al recorrido con punteros sin riesgo de desbordar límites.',
    1, 80
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    18, 9,
    'Tablas Hash en STL (std::unordered_set) en O(1) Promedio',
    '1. CONTENEDORES ASOCIATIVOS ORDENADOS VS HASH EN STL' || CHR(10) ||
    '• std::set y std::map se implementan como árboles binarios balanceados (Rojo-Negro) con búsqueda O(log n).' || CHR(10) ||
    '• std::unordered_set y std::unordered_map usan tablas hash con búsqueda e inserción en O(1) promedio.' || CHR(10) || CHR(10) ||
    '2. DETECCIÓN DE DUPLICADOS EN UNA PASADA' || CHR(10) ||
    'Consultar vistos.count(v) o vistos.find(v) != vistos.end() permite identificar el primer duplicado inmediatamente.',
    2, 100
);

-- =============================================================================
-- 5. RETOS (36 Retos: 1 CODIGO + 1 QUIZ por cada una de las 18 lecciones)
-- =============================================================================

-- --------------------- RETOS DE JAVA (Lecciones 1, 10, 2, 11, 3, 12) ---------
-- Lección 1 (Java POO 1)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    101, 1,
    'Polimorfismo: Despacho Dinámico de Sonidos',
    'Implementa el método emitirSonido(String tipo) para que retorne "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato" (sin importar mayúsculas o minúsculas).',
    'CODIGO', 'FACIL', 60, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    102, 1,
    'Quiz Conceptual: Enlace Dinámico en POO',
    '¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) de la subclase en tiempo de ejecución?',
    'QUIZ', 'FACIL', 40, 2
);

-- Lección 10 (Java POO 2 - Interfaces y SOLID)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    106, 10,
    'Contratos SOLID: Clasificador de Nivel de Acceso',
    'Implementa nivelAcceso(String rol) para que retorne "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE" (ignorando mayúsculas/minúsculas).',
    'CODIGO', 'FACIL', 70, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    107, 10,
    'Quiz Conceptual: Principio de Sustitución de Liskov',
    'Según el Principio de Sustitución de Liskov (LSP) en Java, ¿qué debe cumplirse cuando una clase implementa una interfaz o extiende una clase base?',
    'QUIZ', 'FACIL', 45, 2
);

-- Lección 2 (Java Algoritmos 1 - Búsqueda Binaria)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    103, 2,
    'Búsqueda Binaria Segura O(log n)',
    'Implementa search(int[] a, int target) retornando el índice (0-based) del elemento buscado o -1 si no se encuentra en el arreglo ordenado.',
    'CODIGO', 'MEDIO', 90, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    108, 2,
    'Quiz Conceptual: Prevención de Overflow en Búsqueda Binaria',
    '¿Por qué se prefiere la expresión mid = low + (high - low) / 2 en lugar de (low + high) / 2 en arreglos grandes en Java?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 11 (Java Algoritmos 2 - Dos Punteros)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    109, 11,
    'Dos Punteros: Verificador de Palíndromos',
    'Implementa esPalindromo(String texto) usando dos índices para retornar true si la palabra se lee igual de izquierda a derecha que de derecha a izquierda (ignorando mayúsculas), o false en caso contrario.',
    'CODIGO', 'MEDIO', 85, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    110, 11,
    'Quiz Conceptual: Complejidad de Dos Punteros',
    '¿Cuál es la complejidad temporal y espacial auxiliar de verificar un palíndromo in-place usando dos índices extremos (left y right)?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 3 (Java Colecciones 1 - ArrayList y Filtrado)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    111, 3,
    'Colecciones: Suma de Números Pares',
    'Implementa sumarPares(int[] nums) para recorrer la secuencia y retornar la suma de todos los números pares contenidos en el arreglo.',
    'CODIGO', 'MEDIO', 80, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    112, 3,
    'Quiz Conceptual: Complejidad Amortizada en ArrayList',
    '¿Cuál es el costo temporal de insertar un elemento al final de un ArrayList<T> mediante add(elemento) en análisis amortizado?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 12 (Java Colecciones 2 - HashSet y Duplicados)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    113, 12,
    'Tablas Hash: Primer Elemento Duplicado en O(n)',
    'Implementa primerDuplicado(int[] nums) usando un HashSet para retornar el primer número que aparece repetido al recorrer el arreglo, o -1 si todos son únicos.',
    'CODIGO', 'DIFICIL', 110, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    114, 12,
    'Quiz Conceptual: Contrato equals() y hashCode()',
    '¿Qué ocurre si una clase sobrescribe equals() pero NO sobrescribe hashCode() al almacenarse dentro de un HashSet o HashMap en Java?',
    'QUIZ', 'DIFICIL', 60, 2
);

-- --------------------- RETOS DE PYTHON (Lecciones 4, 13, 5, 14, 6, 15) -------
-- Lección 4 (Python POO 1)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    104, 4,
    'Polimorfismo en Python: Sonidos por Especie',
    'Completa la función emitir_sonido(tipo: str) -> str para retornar "Guau" con "Perro" y "Miau" con "Gato" (ignorando mayúsculas/minúsculas).',
    'CODIGO', 'FACIL', 65, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    202, 4,
    'Quiz Conceptual: Clases Abstractas abc.ABC en Python',
    '¿Qué sucede en Python cuando intentas instanciar una subclase de abc.ABC que no ha implementado todos los métodos decorados con @abstractmethod?',
    'QUIZ', 'FACIL', 40, 2
);

-- Lección 13 (Python POO 2 - Dunder y Clasificación)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    203, 13,
    'Protocolos en Python: Clasificador de Nivel de Acceso',
    'Completa nivel_acceso(rol: str) -> str para retornar "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE".',
    'CODIGO', 'FACIL', 70, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    204, 13,
    'Quiz Conceptual: Métodos Especiales __str__ vs __repr__',
    '¿Cuál es la diferencia fundamental entre los métodos especiales __str__ y __repr__ en el modelo de datos de Python?',
    'QUIZ', 'FACIL', 45, 2
);

-- Lección 5 (Python Algoritmos 1 - Búsqueda Binaria)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    205, 5,
    'Búsqueda Binaria en Python con División Entera (//)',
    'Completa search(a: list, target: int) -> int para retornar el índice del elemento buscado en la lista ordenada o -1 si no existe.',
    'CODIGO', 'MEDIO', 90, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    206, 5,
    'Quiz Conceptual: Operador // e Indexación en Python',
    '¿Por qué es indispensable utilizar el operador // en lugar de / al calcular el índice medio en Python 3?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 14 (Python Algoritmos 2 - Slicing y Palíndromos)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    207, 14,
    'Slicing en Python: Detector de Palíndromos',
    'Completa es_palindromo(texto: str) -> bool invirtiendo o comparando la cadena en minúsculas para evaluar si es un palíndromo.',
    'CODIGO', 'MEDIO', 85, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    208, 14,
    'Quiz Conceptual: Slicing s[::-1] en Memoria',
    '¿Qué hace exactamente la expresión texto[::-1] sobre una cadena str en Python?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 6 (Python Estructuras 1 - Listas y Filtrado)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    209, 6,
    'Listas en Python: Suma de Elementos Pares',
    'Completa sumar_pares(nums: list) -> int para retornar la suma acumulada de los números divisibles por 2.',
    'CODIGO', 'MEDIO', 80, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    210, 6,
    'Quiz Conceptual: Crecimiento de list en CPython',
    '¿Cómo logra la estructura list de CPython que las llamadas sucesivas a append() tengan costo O(1) amortizado?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 15 (Python Estructuras 2 - Sets y Duplicados)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    211, 15,
    'Conjuntos Hash en Python: Primer Duplicado en O(n)',
    'Completa primer_duplicado(nums: list) -> int utilizando un conjunto (set) para retornar el primer valor repetido o -1 si no hay duplicados.',
    'CODIGO', 'DIFICIL', 110, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    212, 15,
    'Quiz Conceptual: Elementos Hashables en set y dict',
    '¿Por qué una tupla (tuple) de enteros puede ser clave en un dict o elemento de un set en Python, pero una lista (list) no?',
    'QUIZ', 'DIFICIL', 60, 2
);

-- --------------------- RETOS DE C++ (Lecciones 7, 16, 8, 17, 9, 18) ----------
-- Lección 7 (C++ POO 1)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    105, 7,
    'Polimorfismo en C++: Clasificación de Sonidos',
    'Completa emitirSonido(std::string tipo) para retornar "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato".',
    'CODIGO', 'MEDIO', 75, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    302, 7,
    'Quiz Conceptual: Destructor Virtual en C++',
    '¿Qué riesgo crítico ocurre en C++ si eliminas con delete un objeto derivado a través de un puntero a su clase base que NO tiene destructor virtual?',
    'QUIZ', 'FACIL', 45, 2
);

-- Lección 16 (C++ POO 2 - RAII y Permisos)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    303, 16,
    'Contratos en C++: Clasificador de Nivel de Acceso',
    'Completa nivelAcceso(std::string rol) para retornar "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE".',
    'CODIGO', 'MEDIO', 75, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    304, 16,
    'Quiz Conceptual: std::unique_ptr y Semántica RAII',
    '¿Cuál es la característica principal de std::unique_ptr<T> introducido en el C++ moderno frente a los punteros crudos con new/delete?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 8 (C++ Algoritmos 1 - Búsqueda Binaria)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    305, 8,
    'Búsqueda Binaria en C++ sobre const std::vector<int>&',
    'Completa search(const std::vector<int>& a, int target) retornando el índice donde se ubica target o -1 si no está presente.',
    'CODIGO', 'MEDIO', 95, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    306, 8,
    'Quiz Conceptual: Paso por Referencia Constante (const T&)',
    '¿Por qué una función de búsqueda en C++ debe recibir const std::vector<int>& en lugar de std::vector<int> por valor?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 17 (C++ Algoritmos 2 - Dos Punteros)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    307, 17,
    'Dos Índices en C++: Verificador de Palíndromos',
    'Completa esPalindromo(std::string texto) recorriendo desde los extremos left y right para evaluar si la cadena es palíndroma.',
    'CODIGO', 'MEDIO', 90, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    308, 17,
    'Quiz Conceptual: Iteradores de Acceso Aleatorio en STL',
    '¿Qué contenedores de la STL de C++ ofrecen iteradores de acceso aleatorio (Random Access Iterators) en tiempo O(1)?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 9 (C++ Contenedores 1 - std::vector)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    309, 9,
    'STL std::vector: Acumulador de Elementos Pares',
    'Completa sumarPares(const std::vector<int>& nums) para acumular y retornar la suma de todos los enteros pares del vector.',
    'CODIGO', 'MEDIO', 85, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    310, 9,
    'Quiz Conceptual: size() vs capacity() en std::vector',
    '¿Qué hace el método reserve(n) en un std::vector<T> de C++ antes de insertar una gran cantidad de elementos?',
    'QUIZ', 'MEDIO', 50, 2
);

-- Lección 18 (C++ Contenedores 2 - std::unordered_set)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    311, 18,
    'STL std::unordered_set: Primer Duplicado en O(n)',
    'Completa primerDuplicado(const std::vector<int>& nums) empleando std::unordered_set<int> para retornar el primer valor repetido o -1.',
    'CODIGO', 'DIFICIL', 115, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    312, 18,
    'Quiz Conceptual: std::unordered_map vs std::map',
    '¿Cuál es la diferencia estructural y de complejidad temporal entre std::unordered_map y std::map en la STL de C++?',
    'QUIZ', 'DIFICIL', 65, 2
);

-- =============================================================================
-- 6. RETO_CODIGO (18 Retos de Código con plantillas por completar)
-- =============================================================================

-- Java 101: Sonidos polimórficos
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    101,
    'public class AnimalSoundSolver {' || CHR(10) ||
    '    public static String emitirSonido(String tipo) {' || CHR(10) ||
    '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '        return "";' || CHR(10) ||
    '    }' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- Java 106: Clasificador de Roles SOLID
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    106,
    'public class AccessPolicySolver {' || CHR(10) ||
    '    public static String nivelAcceso(String rol) {' || CHR(10) ||
    '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '        return "";' || CHR(10) ||
    '    }' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- Java 103: Búsqueda Binaria
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    103,
    'public class BinarySearchSolver {' || CHR(10) ||
    '    public static int search(int[] a, int target) {' || CHR(10) ||
    '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '        return 0;' || CHR(10) ||
    '    }' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- Java 109: Palíndromo con Dos Punteros
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    109,
    'public class PalindromeSolver {' || CHR(10) ||
    '    public static boolean esPalindromo(String texto) {' || CHR(10) ||
    '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '        return false;' || CHR(10) ||
    '    }' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- Java 111: Suma de Pares
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    111,
    'public class EvenSumSolver {' || CHR(10) ||
    '    public static int sumarPares(int[] nums) {' || CHR(10) ||
    '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '        return 0;' || CHR(10) ||
    '    }' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- Java 113: Primer Duplicado con HashSet
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    113,
    'import java.util.HashSet;' || CHR(10) ||
    'import java.util.Set;' || CHR(10) ||
    '' || CHR(10) ||
    'public class DuplicateFinderSolver {' || CHR(10) ||
    '    public static int primerDuplicado(int[] nums) {' || CHR(10) ||
    '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '        return 0;' || CHR(10) ||
    '    }' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- Python 104: Sonidos por Especie
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    104,
    'def emitir_sonido(tipo: str) -> str:' || CHR(10) ||
    '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return ""' || CHR(10) ||
    '',
    2000
);

-- Python 203: Nivel de Acceso
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    203,
    'def nivel_acceso(rol: str) -> str:' || CHR(10) ||
    '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return ""' || CHR(10) ||
    '',
    2000
);

-- Python 205: Búsqueda Binaria
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    205,
    'def search(a: list, target: int) -> int:' || CHR(10) ||
    '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return 0' || CHR(10) ||
    '',
    2000
);

-- Python 207: Palíndromo con Slicing
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    207,
    'def es_palindromo(texto: str) -> bool:' || CHR(10) ||
    '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return False' || CHR(10) ||
    '',
    2000
);

-- Python 209: Suma de Pares
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    209,
    'def sumar_pares(nums: list) -> int:' || CHR(10) ||
    '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return 0' || CHR(10) ||
    '',
    2000
);

-- Python 211: Primer Duplicado con Set
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    211,
    'def primer_duplicado(nums: list) -> int:' || CHR(10) ||
    '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return 0' || CHR(10) ||
    '',
    2000
);

-- C++ 105: Clasificación de Sonidos
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    105,
    '#include <string>' || CHR(10) ||
    '' || CHR(10) ||
    'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
    '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return "";' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- C++ 303: Clasificador de Nivel de Acceso
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    303,
    '#include <string>' || CHR(10) ||
    '' || CHR(10) ||
    'std::string nivelAcceso(std::string rol) {' || CHR(10) ||
    '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return "";' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- C++ 305: Búsqueda Binaria sobre std::vector
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    305,
    '#include <vector>' || CHR(10) ||
    '' || CHR(10) ||
    'int search(const std::vector<int>& a, int target) {' || CHR(10) ||
    '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return 0;' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- C++ 307: Palíndromo con Dos Índices
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    307,
    '#include <string>' || CHR(10) ||
    '#include <cctype>' || CHR(10) ||
    '' || CHR(10) ||
    'bool esPalindromo(std::string texto) {' || CHR(10) ||
    '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return false;' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- C++ 309: Suma de Pares con std::vector
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    309,
    '#include <vector>' || CHR(10) ||
    '' || CHR(10) ||
    'int sumarPares(const std::vector<int>& nums) {' || CHR(10) ||
    '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return 0;' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- C++ 311: Primer Duplicado con std::unordered_set
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    311,
    '#include <vector>' || CHR(10) ||
    '#include <unordered_set>' || CHR(10) ||
    '' || CHR(10) ||
    'int primerDuplicado(const std::vector<int>& nums) {' || CHR(10) ||
    '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
    '    return 0;' || CHR(10) ||
    '}' || CHR(10) ||
    '',
    2000
);

-- =============================================================================
-- 7. CASOS_PRUEBA (54 Casos: 2 visibles + 1 oculto por cada uno de los 18 retos)
-- =============================================================================
-- Java 101 (Sonidos)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (1, 101, 'Perro', 'Guau', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (2, 101, 'Gato', 'Miau', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (3, 101, 'PERRO', 'Guau', 1);

-- Java 106 (Roles)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (4, 106, 'ADMIN', 'TOTAL', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (5, 106, 'ESTUDIANTE', 'LECTURA', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (6, 106, 'admin', 'TOTAL', 1);

-- Java 103 (Búsqueda Binaria)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (7, 103, '[2, 5, 8, 12, 16], target=12', '3', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (8, 103, '[2, 5, 8, 12, 16], target=2', '0', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (9, 103, '[2, 5, 8, 12, 16], target=7', '-1', 1);

-- Java 109 (Palíndromo)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (10, 109, 'reconocer', 'true', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (11, 109, 'levelup', 'false', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (12, 109, 'Radar', 'true', 1);

-- Java 111 (Suma Pares)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (13, 111, '[4, 7, 2, 9, 6]', '12', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (14, 111, '[1, 3, 5]', '0', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (15, 111, '[10, 20, 3, 8]', '38', 1);

-- Java 113 (Primer Duplicado)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (16, 113, '[3, 5, 2, 5, 8, 2]', '5', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (17, 113, '[10, 20, 30, 10]', '10', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (18, 113, '[1, 2, 3, 4]', '-1', 1);

-- Python 104 (Sonidos)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (19, 104, 'Perro', 'Guau', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (20, 104, 'Gato', 'Miau', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (21, 104, 'PERRO', 'Guau', 1);

-- Python 203 (Roles)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (22, 203, 'ADMIN', 'TOTAL', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (23, 203, 'ESTUDIANTE', 'LECTURA', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (24, 203, 'admin', 'TOTAL', 1);

-- Python 205 (Búsqueda Binaria)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (25, 205, '[2, 5, 8, 12, 16], target=12', '3', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (26, 205, '[2, 5, 8, 12, 16], target=2', '0', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (27, 205, '[2, 5, 8, 12, 16], target=7', '-1', 1);

-- Python 207 (Palíndromo)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (28, 207, 'reconocer', 'true', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (29, 207, 'python', 'false', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (30, 207, 'Radar', 'true', 1);

-- Python 209 (Suma Pares)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (31, 209, '[4, 7, 2, 9, 6]', '12', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (32, 209, '[1, 3, 5]', '0', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (33, 209, '[10, 20, 3, 8]', '38', 1);

-- Python 211 (Primer Duplicado)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (34, 211, '[3, 5, 2, 5, 8, 2]', '5', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (35, 211, '[10, 20, 30, 10]', '10', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (36, 211, '[1, 2, 3, 4]', '-1', 1);

-- C++ 105 (Sonidos)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (37, 105, 'Perro', 'Guau', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (38, 105, 'Gato', 'Miau', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (39, 105, 'PERRO', 'Guau', 1);

-- C++ 303 (Roles)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (40, 303, 'ADMIN', 'TOTAL', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (41, 303, 'ESTUDIANTE', 'LECTURA', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (42, 303, 'admin', 'TOTAL', 1);

-- C++ 305 (Búsqueda Binaria)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (43, 305, '[2, 5, 8, 12, 16], target=12', '3', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (44, 305, '[2, 5, 8, 12, 16], target=2', '0', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (45, 305, '[2, 5, 8, 12, 16], target=7', '-1', 1);

-- C++ 307 (Palíndromo)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (46, 307, 'reconocer', 'true', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (47, 307, 'puntero', 'false', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (48, 307, 'Radar', 'true', 1);

-- C++ 309 (Suma Pares)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (49, 309, '[4, 7, 2, 9, 6]', '12', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (50, 309, '[1, 3, 5]', '0', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (51, 309, '[10, 20, 3, 8]', '38', 1);

-- C++ 311 (Primer Duplicado)
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (52, 311, '[3, 5, 2, 5, 8, 2]', '5', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (53, 311, '[10, 20, 30, 10]', '10', 0);
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto) VALUES (54, 311, '[1, 2, 3, 4]', '-1', 1);

-- =============================================================================
-- 8. RETO_QUIZ (18 Quizzes Conceptuales con 4 opciones separadas por '|')
-- =============================================================================
INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    102,
    'Sobrecarga estática en tiempo de compilación (Overloading)|Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)|Conversión implícita de tipos primitivos en la pila|Duplicación de constructores en la clase base',
    'Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    107,
    'La subclase debe poder sustituir a su supertipo manteniendo el contrato sin efectos inesperados|La subclase debe declarar todos sus atributos como públicos|Una interfaz solo puede ser implementada por una única clase en todo el paquete|El compilador prohíbe usar polimorfismo con interfaces funcionales',
    'La subclase debe poder sustituir a su supertipo manteniendo el contrato sin efectos inesperados'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    108,
    'Porque evita que la suma (low + high) supere Integer.MAX_VALUE y desborde a negativo|Porque el operador + está desaconsejado en versiones recientes de Java|Porque convierte automáticamente el arreglo a números de punto flotante|Porque reduce la complejidad de O(log n) a O(1) constante',
    'Porque evita que la suma (low + high) supere Integer.MAX_VALUE y desborde a negativo'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    110,
    'Tiempo O(n) y espacio auxiliar O(1) al comparar caracteres sin copiar la cadena|Tiempo O(n²) y espacio auxiliar O(n) por cada carácter evaluado|Tiempo O(log n) porque descarta la mitad del texto en cada paso|Tiempo O(1) independiente de la longitud del texto',
    'Tiempo O(n) y espacio auxiliar O(1) al comparar caracteres sin copiar la cadena'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    112,
    'O(1) amortizado gracias al redimensionamiento geométrico del arreglo interno|O(n²) porque ordena automáticamente todos los elementos en cada inserción|O(log n) porque internamente es un árbol binario balanceado|O(n) estricto porque copia el arreglo completo en cada llamada a add()',
    'O(1) amortizado gracias al redimensionamiento geométrico del arreglo interno'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    114,
    'Objetos lógicamente iguales pueden caer en buckets distintos y duplicarse dentro del HashSet|El código no compila porque Object no provee una implementación base de hashCode()|El HashSet convierte automáticamente todos los objetos a String antes de guardarlos|La JVM lanza NullPointerException al invocar add()',
    'Objetos lógicamente iguales pueden caer en buckets distintos y duplicarse dentro del HashSet'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    202,
    'Python lanza TypeError en tiempo de ejecución impidiendo instanciar la clase incompleta|Python genera automáticamente métodos vacíos que retornan None|El programa se ejecuta normalmente e ignora el decorador @abstractmethod|Solo muestra una advertencia estética en la consola sin detener el programa',
    'Python lanza TypeError en tiempo de ejecución impidiendo instanciar la clase incompleta'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    204,
    '__str__ produce texto legible para el usuario final y __repr__ una representación técnica inequívoca|__str__ solo funciona con números enteros y __repr__ solo con listas|Ambos métodos son idénticos y no pueden coexistir en la misma clase|__repr__ es privado y no puede ser invocado desde fuera del módulo',
    '__str__ produce texto legible para el usuario final y __repr__ una representación técnica inequívoca'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    206,
    'Porque / retorna un float y las listas en Python exigen índices enteros (int)|Porque // realiza la división en un hilo separado del procesador|Porque el operador / lanza ZeroDivisionError cuando el resultado es decimal|Porque // ordena automáticamente la lista antes de dividir',
    'Porque / retorna un float y las listas en Python exigen índices enteros (int)'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    208,
    'Crea y retorna una nueva cadena con los caracteres en orden inverso usando paso -1|Elimina el último carácter de la cadena modificándola in-place|Convierte la cadena en un diccionario de frecuencias|Lanza IndexError porque los pasos negativos no existen en Python',
    'Crea y retorna una nueva cadena con los caracteres en orden inverso usando paso -1'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    210,
    'Reserva capacidad adicional (sobre-asignación) en el arreglo de punteros PyObject* para evitar realocar en cada append()|Almacena cada elemento en un archivo temporal en disco|Usa una lista doblemente enlazada sin bloques contiguos|Limita el tamaño máximo de las listas a 256 elementos',
    'Reserva capacidad adicional (sobre-asignación) en el arreglo de punteros PyObject* para evitar realocar en cada append()'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    212,
    'Porque tuple es inmutable y su valor hash permanece constante, mientras que list es mutable|Porque list ocupa más de 64 bits en memoria|Porque tuple ordena sus elementos automáticamente al crearse|Porque los conjuntos set solo aceptan cadenas de texto',
    'Porque tuple es inmutable y su valor hash permanece constante, mientras que list es mutable'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    302,
    'Comportamiento indefinido y fuga de recursos porque no se invoca el destructor de la subclase|El compilador de C++ convierte todos los métodos de la clase en privados|El puntero base se multiplica por dos en memoria|El recolector de basura de C++ detiene el programa inmediatamente',
    'Comportamiento indefinido y fuga de recursos porque no se invoca el destructor de la subclase'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    304,
    'Garantiza propiedad exclusiva del recurso y lo libera automáticamente al salir de ámbito sin sobrecosto|Permite que múltiples punteros sean dueños simultáneos mediante conteo atómico|Requiere llamar manualmente a free() antes de que termine la función|Desactiva las comprobaciones de tipos del compilador g++',
    'Garantiza propiedad exclusiva del recurso y lo libera automáticamente al salir de ámbito sin sobrecosto'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    306,
    'Evita copiar los n elementos del vector en memoria (O(1) en paso de parámetro) y protege el contenedor contra cambios|Obliga al compilador a copiar el vector completo en el registro del procesador|Convierte el vector en una lista enlazada dinámicamente|Permite modificar libremente el tamaño del vector dentro de la función',
    'Evita copiar los n elementos del vector en memoria (O(1) en paso de parámetro) y protege el contenedor contra cambios'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    308,
    'std::vector, std::deque y std::string al permitir saltos aritméticos it + n en O(1)|Únicamente std::list y std::forward_list|Solo std::map y std::set|Ningún contenedor de la STL soporta acceso aleatorio',
    'std::vector, std::deque y std::string al permitir saltos aritméticos it + n en O(1)'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    310,
    'Pre-asigna memoria contigua para al menos n elementos evitando realocaciones y copias durante push_back()|Rellena el vector con n ceros modificando su size() inmediatamente|Ordena los elementos de mayor a menor capacidad|Bloquea el vector impidiendo agregar más elementos',
    'Pre-asigna memoria contigua para al menos n elementos evitando realocaciones y copias durante push_back()'
);

INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    312,
    'std::unordered_map usa tabla hash con búsqueda O(1) promedio; std::map usa árbol Rojo-Negro ordenado con O(log n)|Ambos usan tablas hash idénticas pero con distinto nombre de cabecera|std::map no permite almacenar enteros como valor asociado|std::unordered_map mantiene las claves ordenadas alfabéticamente en todo momento',
    'std::unordered_map usa tabla hash con búsqueda O(1) promedio; std::map usa árbol Rojo-Negro ordenado con O(log n)'
);

-- =============================================================================
-- 9. LOGROS (8 Insignias de Gamificación)
-- =============================================================================
INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (1, 'Constancia de Acero', 'Mantén una racha activa de 3 días seguidos practicando.', 'RACHA');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (2, 'Hábito Algorítmico', 'Alcanza una racha continua de 7 días de estudio.', 'RACHA');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (3, 'Explorador Políglota', 'Acumula tus primeros 1,000 puntos de experiencia (XP).', 'EXP');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (4, 'Arquitecto Senior', 'Supera la barrera de los 1,500 XP resolviendo lecciones y retos.', 'EXP');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (5, 'Domador de Casos de Prueba', 'Resuelve exitosamente 5 retos de código o quiz.', 'RETOS');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (6, 'Leyenda del Semestre', 'Alcanza una racha histórica de 14 días consecutivos.', 'RACHA');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (7, 'Maestro de Algoritmos', 'Resuelve exitosamente 12 retos entre Java, Python y C++.', 'RETOS');

INSERT INTO Logros (id_logro, nombre_insignia, descripcion, tipo_condicion)
VALUES (8, 'Sabio Tri-Lenguaje', 'Acumula 2,500 XP dominando los tres lenguajes del currículo.', 'EXP');

-- 10. LOGRO_RACHA
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (1, 3);
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (2, 7);
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (6, 14);

-- 11. LOGRO_EXP
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (3, 1000);
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (4, 1500);
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (8, 2500);

-- 12. LOGRO_RETOS
INSERT INTO Logro_Retos (id_logro, retos_requeridos) VALUES (5, 5);
INSERT INTO Logro_Retos (id_logro, retos_requeridos) VALUES (7, 12);

-- Sincronizar secuencias IDENTITY del catálogo después de inserciones con ID explícito
ALTER TABLE Roles        MODIFY id_rol      GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Lenguajes    MODIFY id_lenguaje GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Modulos      MODIFY id_modulo   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Lecciones    MODIFY id_leccion  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Retos        MODIFY id_reto     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Casos_Prueba MODIFY id_caso     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Logros       MODIFY id_logro    GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);

COMMIT;

-- =============================================================================
-- FASE 4 - USUARIOS E HISTORIAL DE DEMOSTRACIÓN
-- Fuente integrada: database/02b_seed_demo_users.sql
-- =============================================================================
PROMPT FASE 4 - USUARIOS E HISTORIAL DE DEMOSTRACIÓN

-- =============================================================================
-- LEVELUP CODE - CUENTAS Y HISTORIAL DE DEMOSTRACIÓN ACADÉMICA (OPCIONAL)
-- IMPORTANTE: Este script está separado del despliegue normal (02_seed.sql).
-- Contiene cuentas conocidas de prueba para demostración académica local y
-- validación con 04_test_plsql.sql. NO ejecutar en entornos de producción.
-- Ejecutar después de 02_seed.sql en la conexión: LEVELUP_ADMIN
-- =============================================================================

SET DEFINE OFF;

-- 1. USUARIOS DE DEMOSTRACIÓN (Hashes PBKDF2-HMAC-SHA256 verificables por UserService)
-- Contraseña Estudiante demo: estudiante123
INSERT INTO Usuarios (
    id_usuario, id_rol, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
    correo_electronico, password_hash, exp_total, nivel, racha_actual, racha_maxima, vidas,
    fecha_registro, ultima_actividad
) VALUES (
    1, 1, 'Estudiante', NULL, 'Demo', NULL,
    'estudiante@levelup.edu.co',
    'pbkdf2$120000$bGV2ZWx1cF9zYWx0X2VzdA==$TOjMDjzOMgsZXV8tIgJomSIbraYhQBpZLYJkU1BIQBc=',
    1450, 5, 7, 12, 5,
    TRUNC(SYSDATE) - 30, SYSTIMESTAMP
);

-- Contraseña Admin demo: admin12345
INSERT INTO Usuarios (
    id_usuario, id_rol, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
    correo_electronico, password_hash, exp_total, nivel, racha_actual, racha_maxima, vidas,
    fecha_registro, ultima_actividad
) VALUES (
    2, 2, 'Admin', NULL, 'Sistema', NULL,
    'admin@levelup.edu.co',
    'pbkdf2$120000$bGV2ZWx1cF9zYWx0X2FkbQ==$hw4HnfeGWlcKrVXVi9gBxfnc5k8+TsaFjpyqESnXSOY=',
    4200, 10, 21, 30, 5,
    TRUNC(SYSDATE) - 60, SYSTIMESTAMP
);

-- 2. ACTIVIDAD_DIARIA DEMO (Últimos 14 días para pruebas de racha y mapa de calor)
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 13, 60);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 11, 120);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 9, 80);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 6, 120);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 5, 180);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 4, 90);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 3, 210);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 2, 150);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 1, 240);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE), 160);

-- 3. INSCRIPCIONES DEMO
INSERT INTO Inscripciones (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (1, 1, TRUNC(SYSDATE) - 25);
INSERT INTO Inscripciones (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (1, 2, TRUNC(SYSDATE) - 18);
INSERT INTO Inscripciones (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (1, 3, TRUNC(SYSDATE) - 10);

-- 4. PROGRESO_LECCION DEMO
INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 1, 'COMPLETADA', SYSTIMESTAMP - INTERVAL '3' DAY);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 2, 'EN_CURSO', NULL);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 3, 'PENDIENTE', NULL);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 4, 'COMPLETADA', SYSTIMESTAMP - INTERVAL '1' DAY);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 7, 'EN_CURSO', NULL);

-- 5. INTENTOS DEMO
INSERT INTO Intentos (
    id_intento, id_usuario, id_reto, codigo_enviado, salida_obtenida,
    es_correcto, tiempo_ejecucion_ms, exp_obtenida, fecha_intento
) VALUES (
    1, 1, 102,
    'Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)',
    'CORRECT',
    1, 3, 40, SYSTIMESTAMP - INTERVAL '2' DAY
);

-- 6. USUARIO_LOGRO DEMO
INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
VALUES (1, 1, SYSTIMESTAMP - INTERVAL '4' DAY);

INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
VALUES (1, 2, SYSTIMESTAMP - INTERVAL '1' DAY);

INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
VALUES (1, 3, SYSTIMESTAMP - INTERVAL '2' DAY);

-- Sincronizar secuencias IDENTITY de tablas de usuario tras inserciones explícitas
ALTER TABLE Usuarios         MODIFY id_usuario   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Actividad_Diaria MODIFY id_actividad GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Intentos         MODIFY id_intento   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);

COMMIT;

-- =============================================================================
-- FASE 5 - CUADERNO Y RETOS FAVORITOS
-- Fuente integrada: database/05_student_notebook.sql
-- =============================================================================
PROMPT FASE 5 - CUADERNO Y RETOS FAVORITOS

-- Run once as the application schema owner, after 01_schema.sql.
-- Adds personal storage; existing users, progress and attempts are preserved.
CREATE TABLE Reto_Favorito (
    id_usuario NUMBER NOT NULL,
    id_reto NUMBER NOT NULL,
    fecha_guardado TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_reto_favorito PRIMARY KEY (id_usuario, id_reto),
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_reto FOREIGN KEY (id_reto) REFERENCES Retos(id_reto) ON DELETE CASCADE
);
CREATE INDEX idx_favorito_reto ON Reto_Favorito(id_reto);

CREATE TABLE Nota_Estudio (
    id_nota NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_usuario NUMBER NOT NULL,
    id_reto NUMBER,
    titulo VARCHAR2(100) NOT NULL,
    contenido CLOB NOT NULL,
    fecha_edicion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_nota_usuario FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_nota_reto FOREIGN KEY (id_reto) REFERENCES Retos(id_reto) ON DELETE SET NULL
);
CREATE INDEX idx_nota_usuario ON Nota_Estudio(id_usuario, fecha_edicion);
CREATE INDEX idx_nota_reto ON Nota_Estudio(id_reto);

-- =============================================================================
-- FASE 6 - METAS DE APRENDIZAJE
-- Fuente integrada: database/06_learning_goals.sql
-- =============================================================================
PROMPT FASE 6 - METAS DE APRENDIZAJE

-- Run once after 01_schema.sql, as the application schema owner.
CREATE TABLE Meta_Usuario (
    id_meta NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_usuario NUMBER NOT NULL,
    tipo_meta VARCHAR2(15) NOT NULL,
    valor_objetivo NUMBER NOT NULL,
    fecha_inicio DATE DEFAULT TRUNC(SYSDATE) NOT NULL,
    fecha_fin DATE NOT NULL,
    CONSTRAINT fk_meta_usuario FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT ck_meta_tipo CHECK (tipo_meta IN ('RETOS','LECCIONES','XP','DIAS')),
    CONSTRAINT ck_meta_valor CHECK (valor_objetivo BETWEEN 1 AND 10000 AND valor_objetivo=TRUNC(valor_objetivo)),
    CONSTRAINT ck_meta_fechas CHECK (fecha_fin>=fecha_inicio)
);
CREATE INDEX idx_meta_usuario ON Meta_Usuario(id_usuario, fecha_fin);

-- =============================================================================
-- FASE 7 - PREFERENCIAS DE USUARIO
-- Fuente integrada: database/07_user_preferences.sql
-- =============================================================================
PROMPT FASE 7 - PREFERENCIAS DE USUARIO

-- Run once as the application schema owner after 01_schema.sql.
CREATE TABLE Preferencia_Usuario (
    id_usuario NUMBER PRIMARY KEY,
    lenguaje_preferido VARCHAR2(10) DEFAULT 'JAVA' NOT NULL,
    tamano_editor NUMBER DEFAULT 14 NOT NULL,
    espacios_indentacion NUMBER DEFAULT 4 NOT NULL,
    reducir_animaciones NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT fk_preferencia_usuario FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT ck_preferencia_lenguaje CHECK (lenguaje_preferido IN ('JAVA','PYTHON','CPP')),
    CONSTRAINT ck_preferencia_fuente CHECK (tamano_editor BETWEEN 12 AND 24 AND tamano_editor=TRUNC(tamano_editor)),
    CONSTRAINT ck_preferencia_indent CHECK (espacios_indentacion IN (2,4,8)),
    CONSTRAINT ck_preferencia_animacion CHECK (reducir_animaciones IN (0,1))
);

-- =============================================================================
-- FASE 8 - PUBLICACIÓN DE CONTENIDO
-- Fuente integrada: database/08_content_publication.sql
-- =============================================================================
PROMPT FASE 8 - PUBLICACIÓN DE CONTENIDO

-- Run once after the base schema, as its owner.
ALTER TABLE Retos ADD (
    estado VARCHAR2(15) DEFAULT 'PUBLICADO' NOT NULL,
    revision NUMBER DEFAULT 1 NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT ck_retos_estado CHECK (estado IN ('BORRADOR','PUBLICADO','ARCHIVADO'))
);
CREATE TABLE Solucion_Referencia (
    id_reto NUMBER PRIMARY KEY,
    codigo_solucion CLOB NOT NULL,
    CONSTRAINT fk_solucion_reto FOREIGN KEY (id_reto) REFERENCES Reto_Codigo(id_reto) ON DELETE CASCADE
);
CREATE INDEX idx_retos_estado ON Retos(estado, id_leccion);

-- =============================================================================
-- FASE 9 - VERSIONADO DE RETOS
-- Fuente integrada: database/09_content_versions.sql
-- =============================================================================
PROMPT FASE 9 - VERSIONADO DE RETOS

-- Run once after 08_content_publication.sql.
CREATE TABLE Version_Reto (
    id_version NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_reto NUMBER NOT NULL,
    revision NUMBER NOT NULL,
    titulo_reto VARCHAR2(100) NOT NULL,
    enunciado CLOB NOT NULL,
    tipo_reto VARCHAR2(20) NOT NULL,
    dificultad VARCHAR2(15) NOT NULL,
    exp_recompensa NUMBER NOT NULL,
    codigo_inicial CLOB,
    limite_tiempo_ms NUMBER,
    opciones CLOB,
    respuesta_correcta CLOB,
    solucion_referencia CLOB,
    estado VARCHAR2(15) NOT NULL,
    id_administrador NUMBER,
    fecha_guardado TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uq_version_reto UNIQUE (id_reto, revision),
    CONSTRAINT uq_version_identidad UNIQUE (id_version, id_reto),
    CONSTRAINT fk_version_reto FOREIGN KEY (id_reto) REFERENCES Retos(id_reto) ON DELETE CASCADE,
    CONSTRAINT fk_version_admin FOREIGN KEY (id_administrador) REFERENCES Usuarios(id_usuario) ON DELETE SET NULL
);
CREATE TABLE Version_Caso_Prueba (
    id_version NUMBER NOT NULL,
    numero NUMBER NOT NULL,
    entrada CLOB,
    salida_esperada CLOB,
    es_oculto NUMBER(1) NOT NULL,
    CONSTRAINT pk_version_caso PRIMARY KEY (id_version, numero),
    CONSTRAINT fk_version_caso FOREIGN KEY (id_version) REFERENCES Version_Reto(id_version) ON DELETE CASCADE,
    CONSTRAINT ck_version_caso_oculto CHECK (es_oculto IN (0,1))
);

-- Baseline snapshot. Content from edits predating this migration cannot be reconstructed.
INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
    codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado)
SELECT r.id_reto,r.revision,r.titulo_reto,r.enunciado,r.tipo_reto,r.dificultad,r.exp_recompensa,
    c.codigo_inicial,c.limite_tiempo_ms,q.opciones,q.respuesta_correcta,s.codigo_solucion,r.estado
FROM Retos r LEFT JOIN Reto_Codigo c ON c.id_reto=r.id_reto
LEFT JOIN Reto_Quiz q ON q.id_reto=r.id_reto LEFT JOIN Solucion_Referencia s ON s.id_reto=r.id_reto;

INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
SELECT v.id_version,ROW_NUMBER() OVER(PARTITION BY v.id_version ORDER BY c.id_caso),c.entrada,c.salida_esperada,c.es_oculto
FROM Version_Reto v JOIN Casos_Prueba c ON c.id_reto=v.id_reto;

ALTER TABLE Intentos ADD id_version_reto NUMBER;
UPDATE Intentos i SET id_version_reto=(SELECT v.id_version FROM Version_Reto v WHERE v.id_reto=i.id_reto);
ALTER TABLE Intentos MODIFY id_version_reto NOT NULL;
ALTER TABLE Intentos ADD CONSTRAINT fk_intento_version FOREIGN KEY (id_version_reto,id_reto)
    REFERENCES Version_Reto(id_version,id_reto);
CREATE INDEX idx_intento_version ON Intentos(id_version_reto,id_reto);

CREATE OR REPLACE TRIGGER TRG_INTENTO_VERSION
BEFORE INSERT ON Intentos FOR EACH ROW
DECLARE
    v_version NUMBER;
BEGIN
    SELECT v.id_version INTO v_version FROM Version_Reto v JOIN Retos r ON r.id_reto=v.id_reto
    WHERE r.id_reto=:NEW.id_reto AND v.revision=r.revision AND r.estado='PUBLICADO';
    IF :NEW.id_version_reto IS NOT NULL AND :NEW.id_version_reto<>v_version THEN
        RAISE_APPLICATION_ERROR(-20031,'El reto cambió. Abre la versión actual antes de enviar.');
    END IF;
    :NEW.id_version_reto:=v_version;
EXCEPTION WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20032,'El reto no está publicado o no tiene una versión guardada.');
END;
/
COMMIT;

-- =============================================================================
-- FASE 10 - TIPOS AVANZADOS DE RETO
-- Fuente integrada: database/10_challenge_types.sql
-- =============================================================================
PROMPT FASE 10 - TIPOS AVANZADOS DE RETO

-- Run once after 09_content_versions.sql.
ALTER TABLE Retos DROP CONSTRAINT ck_retos_tipo;
ALTER TABLE Retos ADD CONSTRAINT ck_retos_tipo CHECK (tipo_reto IN ('CODIGO','QUIZ','PREDICCION','DEPURACION','ORDENAR'));
CREATE TABLE Reto_Prediccion (
    id_reto NUMBER PRIMARY KEY,
    codigo_ejemplo CLOB NOT NULL,
    CONSTRAINT fk_prediccion_reto FOREIGN KEY (id_reto) REFERENCES Reto_Quiz(id_reto) ON DELETE CASCADE
);
CREATE TABLE Reto_Ordenamiento (
    id_reto NUMBER PRIMARY KEY,
    fragmentos CLOB NOT NULL,
    orden_correcto VARCHAR2(200) NOT NULL,
    CONSTRAINT fk_ordenamiento_reto FOREIGN KEY (id_reto) REFERENCES Retos(id_reto) ON DELETE CASCADE
);
-- Debugging uses the same code/template/time-limit/test-case structure as Reto_Codigo.

-- =============================================================================
-- FASE 11 - PAQUETE PL/SQL DE GAMIFICACIÓN
-- Fuente integrada: database/03_plsql_gamificacion.sql
-- =============================================================================
PROMPT FASE 11 - PAQUETE PL/SQL DE GAMIFICACIÓN

-- =============================================================================
-- LEVELUP CODE — PAQUETE Y COMPONENTES PL/SQL (ORACLE 18c XE / 21c / 23ai)
-- Asignatura: Bases de Datos & Programación III — Universidad Popular del Cesar
-- Autor: Misael Andrés Maestre Acosta (Tech Lead)
--
-- Contenido del archivo:
--   1. TRIGGER: TRG_AUDIT_USUARIOS (Auditoría reactiva de actividad y vidas)
--   2. PACKAGE SPEC: PKG_GAMIFICACION (Definición de funciones y procedimientos)
--   3. PACKAGE BODY: PKG_GAMIFICACION (Implementación transaccional y algoritmos)
--
-- Ejecutar conectado en Oracle SQL Developer con el usuario: LEVELUP_ADMIN
-- =============================================================================

-- Migración compatible para bases existentes: cada envío nuevo recibe una
-- clave estable que evita duplicados cuando el cliente reintenta tras perder
-- la confirmación de red. Los intentos históricos conservan NULL.
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM USER_TAB_COLUMNS
    WHERE TABLE_NAME = 'INTENTOS' AND COLUMN_NAME = 'ID_SOLICITUD';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE Intentos ADD (id_solicitud VARCHAR2(64) DEFAULT RAWTOHEX(SYS_GUID()))';
    ELSE
        EXECUTE IMMEDIATE 'ALTER TABLE Intentos MODIFY (id_solicitud DEFAULT RAWTOHEX(SYS_GUID()))';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM USER_INDEXES
    WHERE INDEX_NAME = 'UQ_INTENTOS_SOLICITUD';
    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'CREATE UNIQUE INDEX UQ_INTENTOS_SOLICITUD ON Intentos (id_solicitud)';
    END IF;
END;
/

-- =============================================================================
-- 1. TRIGGER A NIVEL DE FILA: TRG_AUDIT_USUARIOS
-- Actualiza automáticamente la estampa de última actividad y previene vidas < 0.
-- =============================================================================
CREATE OR REPLACE TRIGGER TRG_AUDIT_USUARIOS
BEFORE UPDATE ON Usuarios
FOR EACH ROW
BEGIN
    -- Actualizar estampa de última actividad automáticamente en cada actualización
    :NEW.ultima_actividad := SYSTIMESTAMP;

    -- Validar regla de integridad de vidas
    IF :NEW.vidas < 0 THEN
        :NEW.vidas := 0;
    END IF;

    -- Prevenir que la experiencia total se reduzca de manera inconsistente
    IF :NEW.exp_total < :OLD.exp_total THEN
        :NEW.exp_total := :OLD.exp_total;
    END IF;
END TRG_AUDIT_USUARIOS;
/

-- =============================================================================
-- 2. ESPECIFICACIÓN DEL PAQUETE: PKG_GAMIFICACION
-- =============================================================================
CREATE OR REPLACE PACKAGE PKG_GAMIFICACION AS

    -- Constantes de negocio
    C_XP_POR_NIVEL     CONSTANT NUMBER := 300;
    C_VIDAS_MAXIMAS    CONSTANT NUMBER := 5;
    C_ESTADO_COMPLETADA CONSTANT VARCHAR2(15) := 'COMPLETADA';
    C_ESTADO_ENCURSO    CONSTANT VARCHAR2(15) := 'EN_CURSO';

    -- -------------------------------------------------------------------------
    -- FUNCIONES
    -- -------------------------------------------------------------------------

    /**
     * Calcula determinísticamente el nivel del estudiante según su experiencia acumulada.
     * Fórmula: Nivel base 1 + (exp_total / 300).
     */
    FUNCTION FN_CALCULAR_NIVEL(
        p_exp_total IN NUMBER
    ) RETURN NUMBER;

    /**
     * Calcula la racha continua de días activos del estudiante analizando la tabla
     * Actividad_Diaria en orden descendente.
     */
    FUNCTION FN_CALCULAR_RACHA_ACTIVA(
        p_id_usuario IN NUMBER
    ) RETURN NUMBER;

    /**
     * Calcula el porcentaje de avance (0 a 100%) en un lenguaje de programación específico,
     * comparando lecciones completadas frente al total de lecciones de sus módulos.
     */
    FUNCTION FN_PORCENTAJE_PROGRESO(
        p_id_usuario  IN NUMBER,
        p_id_lenguaje IN NUMBER
    ) RETURN NUMBER;

    -- -------------------------------------------------------------------------
    -- PROCEDIMIENTOS ALMACENADOS
    -- -------------------------------------------------------------------------

    /**
     * Transacción atómica que procesa el envío de un reto:
     *   1. Inserta en Intentos.
     *   2. Si es correcto y es la primera vez que se supera:
     *      - Otorga XP.
     *      - Actualiza nivel en Usuarios.
     *      - Registra/acumula en Actividad_Diaria (MERGE).
     *      - Evalúa y actualiza la racha actual y máxima.
     *      - Evalúa y desbloquea logros en Usuario_Logro si se cumplen condiciones.
     *   3. Si la respuesta evaluada es incorrecta:
     *      - Resta 1 vida en Usuarios (mínimo 0).
     *      - Los errores internos, de compilación o de infraestructura no consumen vidas.
     *   4. Retorna los estados resultantes en parámetros OUT para el cliente Java.
     */
    PROCEDURE SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         IN  NUMBER,
        p_id_reto            IN  NUMBER,
        p_es_correcto        IN  NUMBER,
        p_codigo_enviado     IN  CLOB,
        p_salida_obtenida    IN  CLOB,
        p_tiempo_ms          IN  NUMBER,
        p_id_solicitud       IN  VARCHAR2,
        p_nuevo_nivel        OUT NUMBER,
        p_nueva_exp          OUT NUMBER,
        p_vidas_restantes    OUT NUMBER,
        p_exp_otorgada       OUT NUMBER,
        p_id_intento_creado  OUT NUMBER
    );

    /**
     * Registra la finalización de una lección en Progreso_Leccion:
     *   - Cambia estado a 'COMPLETADA' con estampa de tiempo.
     *   - Si no estaba completada previamente, otorga la XP de la lección y actualiza nivel.
     */
    PROCEDURE SP_COMPLETAR_LECCION(
        p_id_usuario     IN  NUMBER,
        p_id_leccion     IN  NUMBER,
        p_xp_otorgada    OUT NUMBER,
        p_nuevo_nivel    OUT NUMBER
    );

    /**
     * Restaura las vidas disponibles a 5 al inicio de un nuevo día.
     * Conserva el saldo actual cuando ya hubo intentos durante el día.
     */
    PROCEDURE SP_RECUPERAR_VIDAS(
        p_id_usuario          IN  NUMBER,
        p_vidas_restauradas   OUT NUMBER
    );

    /** Recalcula las rachas actual y máxima usando únicamente días consecutivos reales. */
    PROCEDURE SP_SINCRONIZAR_RACHA(
        p_id_usuario    IN  NUMBER,
        p_racha_actual  OUT NUMBER,
        p_racha_maxima  OUT NUMBER
    );

END PKG_GAMIFICACION;
/

-- =============================================================================
-- 3. CUERPO DEL PAQUETE: PKG_GAMIFICACION (IMPLEMENTACIÓN)
-- =============================================================================
CREATE OR REPLACE PACKAGE BODY PKG_GAMIFICACION AS

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: FN_CALCULAR_NIVEL
    -- -------------------------------------------------------------------------
    FUNCTION FN_CALCULAR_NIVEL(
        p_exp_total IN NUMBER
    ) RETURN NUMBER IS
        v_nivel NUMBER;
    BEGIN
        IF p_exp_total IS NULL OR p_exp_total <= 0 THEN
            RETURN 1;
        END IF;

        v_nivel := GREATEST(1, FLOOR(1 + (p_exp_total / C_XP_POR_NIVEL)));
        RETURN v_nivel;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 1;
    END FN_CALCULAR_NIVEL;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: FN_CALCULAR_RACHA_ACTIVA
    -- -------------------------------------------------------------------------
    FUNCTION FN_CALCULAR_RACHA_ACTIVA(
        p_id_usuario IN NUMBER
    ) RETURN NUMBER IS
        v_racha     NUMBER := 0;
        v_fecha_esp DATE := TRUNC(SYSDATE);
        v_primera   BOOLEAN := TRUE;

        -- Cursor explícito ordenado por fecha descendente
        CURSOR c_actividades IS
            SELECT DISTINCT TRUNC(fecha) AS fecha
            FROM Actividad_Diaria
            WHERE id_usuario = p_id_usuario
            ORDER BY TRUNC(fecha) DESC;
    BEGIN
        FOR r IN c_actividades LOOP
            IF v_primera THEN
                -- Permite que la racha siga viva si la última actividad fue hoy o ayer
                IF r.fecha = v_fecha_esp THEN
                    v_racha := v_racha + 1;
                    v_fecha_esp := v_fecha_esp - 1;
                ELSIF r.fecha = (v_fecha_esp - 1) THEN
                    v_racha := v_racha + 1;
                    v_fecha_esp := r.fecha - 1;
                ELSE
                    EXIT; -- Se rompió la continuidad
                END IF;
                v_primera := FALSE;
            ELSE
                IF r.fecha = v_fecha_esp THEN
                    v_racha := v_racha + 1;
                    v_fecha_esp := v_fecha_esp - 1;
                ELSE
                    EXIT; -- Día faltante en la secuencia consecutiva
                END IF;
            END IF;
        END LOOP;

        RETURN v_racha;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END FN_CALCULAR_RACHA_ACTIVA;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: FN_PORCENTAJE_PROGRESO
    -- -------------------------------------------------------------------------
    FUNCTION FN_PORCENTAJE_PROGRESO(
        p_id_usuario  IN NUMBER,
        p_id_lenguaje IN NUMBER
    ) RETURN NUMBER IS
        v_total_lecciones     NUMBER := 0;
        v_completadas_usuario NUMBER := 0;
    BEGIN
        -- Conteo total de lecciones del lenguaje a través de sus módulos
        SELECT COUNT(l.id_leccion)
        INTO v_total_lecciones
        FROM Lecciones l
        JOIN Modulos m ON m.id_modulo = l.id_modulo
        WHERE m.id_lenguaje = p_id_lenguaje;

        IF v_total_lecciones = 0 THEN
            RETURN 0;
        END IF;

        -- Conteo de lecciones completadas por el usuario en dicho lenguaje
        SELECT COUNT(pl.id_leccion)
        INTO v_completadas_usuario
        FROM Progreso_Leccion pl
        JOIN Lecciones l ON l.id_leccion = pl.id_leccion
        JOIN Modulos m   ON m.id_modulo = l.id_modulo
        WHERE pl.id_usuario = p_id_usuario
          AND m.id_lenguaje = p_id_lenguaje
          AND pl.estado = C_ESTADO_COMPLETADA;

        RETURN ROUND((v_completadas_usuario / v_total_lecciones) * 100, 1);
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END FN_PORCENTAJE_PROGRESO;

    -- -------------------------------------------------------------------------
    -- PROCEDIMIENTO PRIVADO DE EVALUACIÓN DE LOGROS
    -- -------------------------------------------------------------------------
    PROCEDURE EVALUAR_LOGROS_USUARIO(
        p_id_usuario IN NUMBER,
        p_exp_total  IN NUMBER,
        p_racha      IN NUMBER
    ) IS
        v_retos_aprobados NUMBER := 0;

        -- Cursor de logros no desbloqueados aún por el usuario
        CURSOR c_logros_pendientes IS
            SELECT l.id_logro, l.tipo_condicion,
                   lr.dias_requeridos,
                   le.exp_requerida,
                   lc.retos_requeridos
            FROM Logros l
            LEFT JOIN Logro_Racha lr ON lr.id_logro = l.id_logro
            LEFT JOIN Logro_EXP le   ON le.id_logro = l.id_logro
            LEFT JOIN Logro_Retos lc ON lc.id_logro = l.id_logro
            WHERE NOT EXISTS (
                SELECT 1 FROM Usuario_Logro ul
                WHERE ul.id_usuario = p_id_usuario AND ul.id_logro = l.id_logro
            );
    BEGIN
        SELECT COUNT(DISTINCT id_reto)
        INTO v_retos_aprobados
        FROM Intentos
        WHERE id_usuario = p_id_usuario AND es_correcto = 1;

        FOR log IN c_logros_pendientes LOOP
            DECLARE
                v_cumple BOOLEAN := FALSE;
            BEGIN
                IF log.tipo_condicion = 'RACHA' AND p_racha >= NVL(log.dias_requeridos, 999999) THEN
                    v_cumple := TRUE;
                ELSIF log.tipo_condicion = 'EXP' AND p_exp_total >= NVL(log.exp_requerida, 999999) THEN
                    v_cumple := TRUE;
                ELSIF log.tipo_condicion = 'RETOS' AND v_retos_aprobados >= NVL(log.retos_requeridos, 999999) THEN
                    v_cumple := TRUE;
                END IF;

                IF v_cumple THEN
                    INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
                    VALUES (p_id_usuario, log.id_logro, SYSTIMESTAMP);
                END IF;
            EXCEPTION
                WHEN DUP_VAL_ON_INDEX THEN
                    NULL; -- Ya estaba insertado
            END;
        END LOOP;
    END EVALUAR_LOGROS_USUARIO;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_REGISTRAR_INTENTO_RETO
    -- -------------------------------------------------------------------------
    PROCEDURE SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         IN  NUMBER,
        p_id_reto            IN  NUMBER,
        p_es_correcto        IN  NUMBER,
        p_codigo_enviado     IN  CLOB,
        p_salida_obtenida    IN  CLOB,
        p_tiempo_ms          IN  NUMBER,
        p_id_solicitud       IN  VARCHAR2,
        p_nuevo_nivel        OUT NUMBER,
        p_nueva_exp          OUT NUMBER,
        p_vidas_restantes    OUT NUMBER,
        p_exp_otorgada       OUT NUMBER,
        p_id_intento_creado  OUT NUMBER
    ) IS
        v_exp_reto           NUMBER := 0;
        v_exp_actual         NUMBER := 0;
        v_vidas_actuales     NUMBER := C_VIDAS_MAXIMAS;
        v_racha_actual       NUMBER := 0;
        v_racha_max          NUMBER := 0;
        v_ya_aprobado_antes  NUMBER := 0;
        v_id_intento         NUMBER;
        v_intento_existente  NUMBER;
        v_exp_existente      NUMBER;
    BEGIN
        SAVEPOINT sp_proc_intento;

        -- 1. Validar parámetros requeridos
        IF p_id_usuario IS NULL OR p_id_reto IS NULL THEN
            RAISE_APPLICATION_ERROR(-20001, 'Los identificadores de usuario y reto son obligatorios.');
        END IF;
        IF p_id_solicitud IS NULL OR LENGTH(TRIM(p_id_solicitud)) = 0
                OR LENGTH(p_id_solicitud) > 64 THEN
            RAISE_APPLICATION_ERROR(-20005, 'El identificador único del envío es obligatorio.');
        END IF;

        -- 2. Consultar y bloquear fila del usuario para concurrencia segura
        BEGIN
            SELECT exp_total, vidas, racha_actual, racha_maxima
            INTO v_exp_actual, v_vidas_actuales, v_racha_actual, v_racha_max
            FROM Usuarios
            WHERE id_usuario = p_id_usuario
            FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20002, 'El usuario #' || p_id_usuario || ' no existe en la base de datos.');
        END;

        -- Un reintento con la misma clave devuelve el resultado ya confirmado.
        -- Esta comprobación ocurre antes del bloqueo por cero vidas porque el
        -- primer intento pudo haber consumido la última vida.
        BEGIN
            SELECT id_intento, exp_obtenida
            INTO v_intento_existente, v_exp_existente
            FROM Intentos
            WHERE id_solicitud = p_id_solicitud
              AND id_usuario = p_id_usuario
              AND id_reto = p_id_reto;

            p_id_intento_creado := v_intento_existente;
            p_exp_otorgada := v_exp_existente;
            p_nueva_exp := v_exp_actual;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(v_exp_actual);
            p_vidas_restantes := v_vidas_actuales;
            RETURN;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN NULL;
        END;

        IF v_vidas_actuales <= 0 THEN
            RAISE_APPLICATION_ERROR(-20004, 'Sin vidas disponibles. Practica sin guardar y vuelve mañana.');
        END IF;

        -- 3. Consultar datos y recompensa del reto
        BEGIN
            SELECT exp_recompensa
            INTO v_exp_reto
            FROM Retos
            WHERE id_reto = p_id_reto;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20003, 'El reto #' || p_id_reto || ' no existe en el catálogo.');
        END;

        -- 4. Verificar si el usuario ya había aprobado este reto previamente
        SELECT COUNT(*)
        INTO v_ya_aprobado_antes
        FROM Intentos
        WHERE id_usuario = p_id_usuario
          AND id_reto = p_id_reto
          AND es_correcto = 1;

        -- La experiencia solo se concede una única vez por reto
        IF p_es_correcto = 1 AND v_ya_aprobado_antes = 0 THEN
            p_exp_otorgada := v_exp_reto;
        ELSE
            p_exp_otorgada := 0;
        END IF;

        -- 5. Insertar en la tabla Intentos
        INSERT INTO Intentos (
            id_usuario, id_reto, codigo_enviado, salida_obtenida,
            es_correcto, tiempo_ejecucion_ms, exp_obtenida, id_solicitud, fecha_intento
        ) VALUES (
            p_id_usuario, p_id_reto, p_codigo_enviado, p_salida_obtenida,
            p_es_correcto, p_tiempo_ms, p_exp_otorgada, p_id_solicitud, SYSTIMESTAMP
        ) RETURNING id_intento INTO v_id_intento;

        p_id_intento_creado := v_id_intento;

        -- 6. Procesar consecuencias del intento
        IF p_es_correcto = 1 THEN
            p_nueva_exp := v_exp_actual + p_exp_otorgada;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(p_nueva_exp);
            p_vidas_restantes := v_vidas_actuales;

            -- Registrar actividad diaria del día actual con MERGE
            MERGE INTO Actividad_Diaria dst
            USING (SELECT p_id_usuario AS id_usr, TRUNC(SYSDATE) AS fec FROM dual) src
            ON (dst.id_usuario = src.id_usr AND dst.fecha = src.fec)
            WHEN MATCHED THEN
                UPDATE SET dst.exp_ganada = dst.exp_ganada + p_exp_otorgada
            WHEN NOT MATCHED THEN
                INSERT (id_usuario, fecha, exp_ganada)
                VALUES (p_id_usuario, TRUNC(SYSDATE), p_exp_otorgada);

            -- Calcular nueva racha activa
            v_racha_actual := FN_CALCULAR_RACHA_ACTIVA(p_id_usuario);
            v_racha_max := GREATEST(v_racha_max, v_racha_actual);

            -- Actualizar usuario
            UPDATE Usuarios
            SET exp_total = p_nueva_exp,
                nivel = p_nuevo_nivel,
                racha_actual = v_racha_actual,
                racha_maxima = v_racha_max,
                ultima_actividad = SYSTIMESTAMP
            WHERE id_usuario = p_id_usuario;

            -- Evaluar si desbloqueó logros
            EVALUAR_LOGROS_USUARIO(p_id_usuario, p_nueva_exp, v_racha_actual);

        ELSE
            -- Solo una respuesta realmente incorrecta consume una vida. ERROR y
            -- TIMEOUT se registran para diagnóstico sin penalizar al estudiante.
            p_nueva_exp := v_exp_actual;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(p_nueva_exp);
            IF UPPER(TRIM(p_salida_obtenida)) = 'INCORRECT' THEN
                p_vidas_restantes := GREATEST(0, v_vidas_actuales - 1);
            ELSE
                p_vidas_restantes := v_vidas_actuales;
            END IF;

            UPDATE Usuarios
            SET vidas = p_vidas_restantes,
                ultima_actividad = SYSTIMESTAMP
            WHERE id_usuario = p_id_usuario;
        END IF;
        -- El control transaccional (COMMIT / ROLLBACK) pertenece al llamador (JDBC o bloque de prueba).
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK TO sp_proc_intento;
            RAISE;
    END SP_REGISTRAR_INTENTO_RETO;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_COMPLETAR_LECCION
    -- -------------------------------------------------------------------------
    PROCEDURE SP_COMPLETAR_LECCION(
        p_id_usuario     IN  NUMBER,
        p_id_leccion     IN  NUMBER,
        p_xp_otorgada    OUT NUMBER,
        p_nuevo_nivel    OUT NUMBER
    ) IS
        v_estado_actual VARCHAR2(15);
        v_exp_leccion   NUMBER := 0;
        v_exp_usuario   NUMBER := 0;
        v_racha         NUMBER := 0;
        v_racha_max     NUMBER := 0;
    BEGIN
        SAVEPOINT sp_proc_leccion;

        IF p_id_usuario IS NULL OR p_id_leccion IS NULL THEN
            RAISE_APPLICATION_ERROR(-20010, 'Usuario y lección son requeridos.');
        END IF;

        -- Consultar recompensa de la lección
        SELECT exp_recompensa
        INTO v_exp_leccion
        FROM Lecciones
        WHERE id_leccion = p_id_leccion;

        -- Consultar usuario
        SELECT exp_total, racha_actual, racha_maxima
        INTO v_exp_usuario, v_racha, v_racha_max
        FROM Usuarios
        WHERE id_usuario = p_id_usuario
        FOR UPDATE;

        -- Verificar estado previo en Progreso_Leccion
        BEGIN
            SELECT estado
            INTO v_estado_actual
            FROM Progreso_Leccion
            WHERE id_usuario = p_id_usuario AND id_leccion = p_id_leccion;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_estado_actual := 'NO_INICIADA';
        END;

        -- Solo otorgar experiencia si no estaba previamente completada
        IF v_estado_actual = C_ESTADO_COMPLETADA THEN
            p_xp_otorgada := 0;
            p_nuevo_nivel := FN_CALCULAR_NIVEL(v_exp_usuario);
            RETURN;
        END IF;

        p_xp_otorgada := v_exp_leccion;
        v_exp_usuario := v_exp_usuario + p_xp_otorgada;
        p_nuevo_nivel := FN_CALCULAR_NIVEL(v_exp_usuario);

        -- Guardar estado completado con MERGE
        MERGE INTO Progreso_Leccion dst
        USING (SELECT p_id_usuario AS id_usr, p_id_leccion AS id_lec FROM dual) src
        ON (dst.id_usuario = src.id_usr AND dst.id_leccion = src.id_lec)
        WHEN MATCHED THEN
            UPDATE SET dst.estado = C_ESTADO_COMPLETADA, dst.fecha_completado = SYSTIMESTAMP
        WHEN NOT MATCHED THEN
            INSERT (id_usuario, id_leccion, estado, fecha_completado)
            VALUES (p_id_usuario, p_id_leccion, C_ESTADO_COMPLETADA, SYSTIMESTAMP);

        -- Registrar actividad diaria
        MERGE INTO Actividad_Diaria dst
        USING (SELECT p_id_usuario AS id_usr, TRUNC(SYSDATE) AS fec FROM dual) src
        ON (dst.id_usuario = src.id_usr AND dst.fecha = src.fec)
        WHEN MATCHED THEN
            UPDATE SET dst.exp_ganada = dst.exp_ganada + p_xp_otorgada
        WHEN NOT MATCHED THEN
            INSERT (id_usuario, fecha, exp_ganada)
            VALUES (p_id_usuario, TRUNC(SYSDATE), p_xp_otorgada);

        -- Actualizar racha y usuario
        v_racha := FN_CALCULAR_RACHA_ACTIVA(p_id_usuario);
        v_racha_max := GREATEST(v_racha_max, v_racha);

        UPDATE Usuarios
        SET exp_total = v_exp_usuario,
            nivel = p_nuevo_nivel,
            racha_actual = v_racha,
            racha_maxima = v_racha_max,
            ultima_actividad = SYSTIMESTAMP
        WHERE id_usuario = p_id_usuario;

        EVALUAR_LOGROS_USUARIO(p_id_usuario, v_exp_usuario, v_racha);
        -- El control transaccional (COMMIT / ROLLBACK) pertenece al llamador.
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            ROLLBACK TO sp_proc_leccion;
            RAISE_APPLICATION_ERROR(-20011, 'Lección o usuario no encontrado en la base de datos.');
        WHEN OTHERS THEN
            ROLLBACK TO sp_proc_leccion;
            RAISE;
    END SP_COMPLETAR_LECCION;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_RECUPERAR_VIDAS
    -- -------------------------------------------------------------------------
    PROCEDURE SP_RECUPERAR_VIDAS(
        p_id_usuario          IN  NUMBER,
        p_vidas_restauradas   OUT NUMBER
    ) IS
        v_vidas_actuales NUMBER;
        v_intentos_hoy NUMBER;
    BEGIN
        SAVEPOINT sp_proc_vidas;

        IF p_id_usuario IS NULL THEN
            RAISE_APPLICATION_ERROR(-20020, 'El ID de usuario es obligatorio.');
        END IF;

        BEGIN
            SELECT vidas INTO v_vidas_actuales
            FROM Usuarios WHERE id_usuario = p_id_usuario FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20021, 'Usuario #' || p_id_usuario || ' no encontrado.');
        END;

        IF v_vidas_actuales >= C_VIDAS_MAXIMAS THEN
            p_vidas_restauradas := v_vidas_actuales;
            RETURN;
        END IF;

        SELECT COUNT(*) INTO v_intentos_hoy
        FROM Intentos
        WHERE id_usuario = p_id_usuario
          AND fecha_intento >= TRUNC(SYSDATE);

        IF v_intentos_hoy > 0 THEN
            p_vidas_restauradas := v_vidas_actuales;
            RETURN;
        END IF;

        UPDATE Usuarios SET vidas = C_VIDAS_MAXIMAS WHERE id_usuario = p_id_usuario;

        p_vidas_restauradas := C_VIDAS_MAXIMAS;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK TO sp_proc_vidas;
            RAISE;
    END SP_RECUPERAR_VIDAS;

    -- -------------------------------------------------------------------------
    -- IMPLEMENTACIÓN: SP_SINCRONIZAR_RACHA
    -- -------------------------------------------------------------------------
    PROCEDURE SP_SINCRONIZAR_RACHA(
        p_id_usuario    IN  NUMBER,
        p_racha_actual  OUT NUMBER,
        p_racha_maxima  OUT NUMBER
    ) IS
        v_usuario_existe NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_usuario_existe
        FROM Usuarios
        WHERE id_usuario = p_id_usuario;

        IF v_usuario_existe = 0 THEN
            RAISE_APPLICATION_ERROR(-20021, 'Usuario #' || p_id_usuario || ' no encontrado.');
        END IF;

        WITH dias AS (
            SELECT DISTINCT TRUNC(fecha) AS dia
            FROM Actividad_Diaria
            WHERE id_usuario = p_id_usuario
        ),
        numerados AS (
            SELECT dia, dia - ROW_NUMBER() OVER (ORDER BY dia) AS grupo
            FROM dias
        ),
        rachas AS (
            SELECT MAX(dia) AS fin, COUNT(*) AS longitud
            FROM numerados
            GROUP BY grupo
        )
        SELECT NVL(MAX(CASE
                       WHEN fin IN (TRUNC(SYSDATE), TRUNC(SYSDATE) - 1) THEN longitud
                       ELSE 0
                   END), 0),
               NVL(MAX(longitud), 0)
        INTO p_racha_actual, p_racha_maxima
        FROM rachas;

        UPDATE Usuarios
        SET racha_actual = p_racha_actual,
            racha_maxima = p_racha_maxima
        WHERE id_usuario = p_id_usuario
          AND (racha_actual <> p_racha_actual OR racha_maxima <> p_racha_maxima);
    END SP_SINCRONIZAR_RACHA;

END PKG_GAMIFICACION;
/

-- =============================================================================
-- FASE 12 - PLANTILLAS DE ESTUDIANTE Y SOLUCIONES DE REFERENCIA
-- Fuente integrada: database/12_seed_starter_templates.sql
-- =============================================================================
PROMPT FASE 12 - PLANTILLAS DE ESTUDIANTE Y SOLUCIONES DE REFERENCIA

-- Apply after 10_challenge_types.sql as the application schema owner.
-- Re-runnable data migration for the 18 original code exercises.
-- Updates only untouched revision-1 seed templates. Custom content is skipped.
-- Preserves attempts and earlier versions, and creates a new content revision.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_updated NUMBER := 0;

    PROCEDURE separate_template(p_id NUMBER, p_original CLOB, p_starter CLOB) IS
        v_revision NUMBER;
        v_source CLOB;
        v_version NUMBER;
    BEGIN
        BEGIN
            SELECT r.revision,c.codigo_inicial INTO v_revision,v_source
            FROM Retos r JOIN Reto_Codigo c ON c.id_reto=r.id_reto
            WHERE r.id_reto=p_id AND r.tipo_reto='CODIGO' AND r.estado='PUBLICADO'
            FOR UPDATE OF r.revision,c.codigo_inicial;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN RETURN;
        END;

        IF v_revision <> 1 OR v_source IS NULL THEN RETURN; END IF;
        IF DBMS_LOB.COMPARE(v_source,p_original) <> 0
           AND DBMS_LOB.COMPARE(v_source,p_starter) <> 0 THEN RETURN; END IF;

        INSERT INTO Solucion_Referencia(id_reto,codigo_solucion)
        SELECT p_id,p_original FROM dual
        WHERE NOT EXISTS (SELECT 1 FROM Solucion_Referencia WHERE id_reto=p_id);

        UPDATE Reto_Codigo SET codigo_inicial=p_starter WHERE id_reto=p_id;
        UPDATE Retos SET revision=revision+1,fecha_actualizacion=SYSTIMESTAMP WHERE id_reto=p_id;

        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,
            exp_recompensa,codigo_inicial,limite_tiempo_ms,solucion_referencia,estado)
        SELECT r.id_reto,r.revision,r.titulo_reto,r.enunciado,r.tipo_reto,r.dificultad,
            r.exp_recompensa,c.codigo_inicial,c.limite_tiempo_ms,s.codigo_solucion,r.estado
        FROM Retos r JOIN Reto_Codigo c ON c.id_reto=r.id_reto
        JOIN Solucion_Referencia s ON s.id_reto=r.id_reto WHERE r.id_reto=p_id;

        SELECT id_version INTO v_version FROM Version_Reto WHERE id_reto=p_id AND revision=v_revision+1;
        INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
        SELECT v_version,ROW_NUMBER() OVER(ORDER BY id_caso),entrada,salida_esperada,es_oculto
        FROM Casos_Prueba WHERE id_reto=p_id;
        v_updated := v_updated+1;
    END;
BEGIN
    SAVEPOINT starter_migration;
    separate_template(101,
        'public class AnimalSoundSolver {' || CHR(10) ||
        '    public static String emitirSonido(String tipo) {' || CHR(10) ||
        '        if ("Perro".equalsIgnoreCase(tipo)) {' || CHR(10) ||
        '            return "Guau";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        if ("Gato".equalsIgnoreCase(tipo)) {' || CHR(10) ||
        '            return "Miau";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return "Desconocido";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class AnimalSoundSolver {' || CHR(10) ||
        '    public static String emitirSonido(String tipo) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return "";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(106,
        'public class AccessPolicySolver {' || CHR(10) ||
        '    public static String nivelAcceso(String rol) {' || CHR(10) ||
        '        if ("ADMIN".equalsIgnoreCase(rol)) {' || CHR(10) ||
        '            return "TOTAL";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        if ("ESTUDIANTE".equalsIgnoreCase(rol)) {' || CHR(10) ||
        '            return "LECTURA";' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return "DENEGADO";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class AccessPolicySolver {' || CHR(10) ||
        '    public static String nivelAcceso(String rol) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return "";' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(103,
        'public class BinarySearchSolver {' || CHR(10) ||
        '    public static int search(int[] a, int target) {' || CHR(10) ||
        '        int low = 0, high = a.length - 1;' || CHR(10) ||
        '        while (low <= high) {' || CHR(10) ||
        '            int mid = low + (high - low) / 2;' || CHR(10) ||
        '            if (a[mid] == target) return mid;' || CHR(10) ||
        '            if (a[mid] < target) low = mid + 1;' || CHR(10) ||
        '            else high = mid - 1;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return -1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class BinarySearchSolver {' || CHR(10) ||
        '    public static int search(int[] a, int target) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return 0;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(109,
        'public class PalindromeSolver {' || CHR(10) ||
        '    public static boolean esPalindromo(String texto) {' || CHR(10) ||
        '        String s = texto.toLowerCase();' || CHR(10) ||
        '        int left = 0, right = s.length() - 1;' || CHR(10) ||
        '        while (left < right) {' || CHR(10) ||
        '            if (s.charAt(left) != s.charAt(right)) {' || CHR(10) ||
        '                return false;' || CHR(10) ||
        '            }' || CHR(10) ||
        '            left++;' || CHR(10) ||
        '            right--;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return true;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class PalindromeSolver {' || CHR(10) ||
        '    public static boolean esPalindromo(String texto) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return false;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(111,
        'public class EvenSumSolver {' || CHR(10) ||
        '    public static int sumarPares(int[] nums) {' || CHR(10) ||
        '        int total = 0;' || CHR(10) ||
        '        for (int n : nums) {' || CHR(10) ||
        '            if (n % 2 == 0) {' || CHR(10) ||
        '                total += n;' || CHR(10) ||
        '            }' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return total;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'public class EvenSumSolver {' || CHR(10) ||
        '    public static int sumarPares(int[] nums) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return 0;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(113,
        'import java.util.HashSet;' || CHR(10) ||
        'import java.util.Set;' || CHR(10) ||
        '' || CHR(10) ||
        'public class DuplicateFinderSolver {' || CHR(10) ||
        '    public static int primerDuplicado(int[] nums) {' || CHR(10) ||
        '        Set<Integer> vistos = new HashSet<>();' || CHR(10) ||
        '        for (int n : nums) {' || CHR(10) ||
        '            if (!vistos.add(n)) {' || CHR(10) ||
        '                return n;' || CHR(10) ||
        '            }' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return -1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}',
        'import java.util.HashSet;' || CHR(10) ||
        'import java.util.Set;' || CHR(10) ||
        '' || CHR(10) ||
        'public class DuplicateFinderSolver {' || CHR(10) ||
        '    public static int primerDuplicado(int[] nums) {' || CHR(10) ||
        '        // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '        return 0;' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(104,
        'def emitir_sonido(tipo: str) -> str:' || CHR(10) ||
        '    if tipo.lower() == "perro":' || CHR(10) ||
        '        return "Guau"' || CHR(10) ||
        '    elif tipo.lower() == "gato":' || CHR(10) ||
        '        return "Miau"' || CHR(10) ||
        '    return "Desconocido"',
        'def emitir_sonido(tipo: str) -> str:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return ""' || CHR(10) ||
        ''
    );

    separate_template(203,
        'def nivel_acceso(rol: str) -> str:' || CHR(10) ||
        '    if rol.lower() == "admin":' || CHR(10) ||
        '        return "TOTAL"' || CHR(10) ||
        '    elif rol.lower() == "estudiante":' || CHR(10) ||
        '        return "LECTURA"' || CHR(10) ||
        '    return "DENEGADO"',
        'def nivel_acceso(rol: str) -> str:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return ""' || CHR(10) ||
        ''
    );

    separate_template(205,
        'def search(a: list, target: int) -> int:' || CHR(10) ||
        '    low = 0' || CHR(10) ||
        '    high = len(a) - 1' || CHR(10) ||
        '    while low <= high:' || CHR(10) ||
        '        mid = (low + high) // 2' || CHR(10) ||
        '        if a[mid] == target:' || CHR(10) ||
        '            return mid' || CHR(10) ||
        '        elif a[mid] < target:' || CHR(10) ||
        '            low = mid + 1' || CHR(10) ||
        '        else:' || CHR(10) ||
        '            high = mid - 1' || CHR(10) ||
        '    return -1',
        'def search(a: list, target: int) -> int:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0' || CHR(10) ||
        ''
    );

    separate_template(207,
        'def es_palindromo(texto: str) -> bool:' || CHR(10) ||
        '    limpio = texto.lower()' || CHR(10) ||
        '    return limpio == limpio[::-1]',
        'def es_palindromo(texto: str) -> bool:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return False' || CHR(10) ||
        ''
    );

    separate_template(209,
        'def sumar_pares(nums: list) -> int:' || CHR(10) ||
        '    total = 0' || CHR(10) ||
        '    for n in nums:' || CHR(10) ||
        '        if n % 2 == 0:' || CHR(10) ||
        '            total += n' || CHR(10) ||
        '    return total',
        'def sumar_pares(nums: list) -> int:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0' || CHR(10) ||
        ''
    );

    separate_template(211,
        'def primer_duplicado(nums: list) -> int:' || CHR(10) ||
        '    vistos = set()' || CHR(10) ||
        '    for n in nums:' || CHR(10) ||
        '        if n in vistos:' || CHR(10) ||
        '            return n' || CHR(10) ||
        '        vistos.add(n)' || CHR(10) ||
        '    return -1',
        'def primer_duplicado(nums: list) -> int:' || CHR(10) ||
        '    # TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0' || CHR(10) ||
        ''
    );

    separate_template(105,
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
        '    if (tipo == "Perro" || tipo == "PERRO" || tipo == "perro") {' || CHR(10) ||
        '        return "Guau";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    if (tipo == "Gato" || tipo == "GATO" || tipo == "gato") {' || CHR(10) ||
        '        return "Miau";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return "Desconocido";' || CHR(10) ||
        '}',
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return "";' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(303,
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string nivelAcceso(std::string rol) {' || CHR(10) ||
        '    if (rol == "ADMIN" || rol == "admin") {' || CHR(10) ||
        '        return "TOTAL";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    if (rol == "ESTUDIANTE" || rol == "estudiante") {' || CHR(10) ||
        '        return "LECTURA";' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return "DENEGADO";' || CHR(10) ||
        '}',
        '#include <string>' || CHR(10) ||
        '' || CHR(10) ||
        'std::string nivelAcceso(std::string rol) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return "";' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(305,
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int search(const std::vector<int>& a, int target) {' || CHR(10) ||
        '    int low = 0;' || CHR(10) ||
        '    int high = static_cast<int>(a.size()) - 1;' || CHR(10) ||
        '    while (low <= high) {' || CHR(10) ||
        '        int mid = low + (high - low) / 2;' || CHR(10) ||
        '        if (a[mid] == target) return mid;' || CHR(10) ||
        '        if (a[mid] < target) low = mid + 1;' || CHR(10) ||
        '        else high = mid - 1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return -1;' || CHR(10) ||
        '}',
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int search(const std::vector<int>& a, int target) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(307,
        '#include <string>' || CHR(10) ||
        '#include <cctype>' || CHR(10) ||
        '' || CHR(10) ||
        'bool esPalindromo(std::string texto) {' || CHR(10) ||
        '    int left = 0;' || CHR(10) ||
        '    int right = static_cast<int>(texto.size()) - 1;' || CHR(10) ||
        '    while (left < right) {' || CHR(10) ||
        '        if (std::tolower(texto[left]) != std::tolower(texto[right])) {' || CHR(10) ||
        '            return false;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        left++;' || CHR(10) ||
        '        right--;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return true;' || CHR(10) ||
        '}',
        '#include <string>' || CHR(10) ||
        '#include <cctype>' || CHR(10) ||
        '' || CHR(10) ||
        'bool esPalindromo(std::string texto) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return false;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(309,
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int sumarPares(const std::vector<int>& nums) {' || CHR(10) ||
        '    int total = 0;' || CHR(10) ||
        '    for (int n : nums) {' || CHR(10) ||
        '        if (n % 2 == 0) {' || CHR(10) ||
        '            total += n;' || CHR(10) ||
        '        }' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return total;' || CHR(10) ||
        '}',
        '#include <vector>' || CHR(10) ||
        '' || CHR(10) ||
        'int sumarPares(const std::vector<int>& nums) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );

    separate_template(311,
        '#include <vector>' || CHR(10) ||
        '#include <unordered_set>' || CHR(10) ||
        '' || CHR(10) ||
        'int primerDuplicado(const std::vector<int>& nums) {' || CHR(10) ||
        '    std::unordered_set<int> vistos;' || CHR(10) ||
        '    for (int n : nums) {' || CHR(10) ||
        '        if (vistos.count(n) > 0) {' || CHR(10) ||
        '            return n;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        vistos.insert(n);' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return -1;' || CHR(10) ||
        '}',
        '#include <vector>' || CHR(10) ||
        '#include <unordered_set>' || CHR(10) ||
        '' || CHR(10) ||
        'int primerDuplicado(const std::vector<int>& nums) {' || CHR(10) ||
        '    // TODO: implementa la solución siguiendo el enunciado.' || CHR(10) ||
        '    return 0;' || CHR(10) ||
        '}' || CHR(10) ||
        ''
    );
    DBMS_OUTPUT.PUT_LINE('Retos actualizados con plantilla y referencia separadas: ' || v_updated);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO starter_migration;
        RAISE;
END;
/

-- =============================================================================
-- FASE 13 - GUIAS DIDACTICAS
-- Fuente integrada: database/13_challenge_guidance.sql
-- =============================================================================
PROMPT FASE 13 - GUIAS DIDACTICAS

-- Apply after 12_seed_starter_templates.sql as the schema owner.
-- Optional teaching content for older installations, versioned with each challenge.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_tab_columns WHERE table_name='RETOS' AND column_name='GUIA_DIDACTICA';
    IF v_count=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE Retos ADD guia_didactica CLOB'; END IF;
    SELECT COUNT(*) INTO v_count FROM user_tab_columns WHERE table_name='VERSION_RETO' AND column_name='GUIA_DIDACTICA';
    IF v_count=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE Version_Reto ADD guia_didactica CLOB'; END IF;
END;
/

DECLARE
    v_updated NUMBER := 0;
    PROCEDURE add_guidance(p_id NUMBER,p_title VARCHAR2,p_statement CLOB,p_guidance CLOB) IS
        v_revision NUMBER;
        v_version NUMBER;
    BEGIN
        BEGIN
            SELECT revision INTO v_revision FROM Retos
            WHERE id_reto=p_id AND titulo_reto=p_title AND DBMS_LOB.COMPARE(enunciado,p_statement)=0
                AND revision<=2 AND guia_didactica IS NULL
            FOR UPDATE;
        EXCEPTION WHEN NO_DATA_FOUND THEN RETURN;
        END;

        UPDATE Retos SET guia_didactica=p_guidance,revision=revision+1,fecha_actualizacion=SYSTIMESTAMP WHERE id_reto=p_id;
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado,guia_didactica)
        SELECT id_reto,v_revision+1,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado,p_guidance
        FROM Version_Reto WHERE id_reto=p_id AND revision=v_revision;
        IF SQL%ROWCOUNT<>1 THEN RAISE_APPLICATION_ERROR(-20091,'Falta la version actual del reto. Revisa 09_content_versions.sql.'); END IF;
        SELECT id_version INTO v_version FROM Version_Reto WHERE id_reto=p_id AND revision=v_revision+1;
        INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
        SELECT v_version,c.numero,c.entrada,c.salida_esperada,c.es_oculto
        FROM Version_Caso_Prueba c JOIN Version_Reto v ON v.id_version=c.id_version WHERE v.id_reto=p_id AND v.revision=v_revision;
        v_updated := v_updated+1;
    END;
BEGIN
    SAVEPOINT guidance_seed;
    add_guidance(101, 'Polimorfismo: Despacho Dinámico de Sonidos', 'Implementa el método emitirSonido(String tipo) para que retorne "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato" (sin importar mayúsculas o minúsculas).',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHNvbmlkbyBjb3JyZXNwb25kZSBhIGNhZGEgZXNwZWNpZS4=' || CHR(10) ||
        'Q29tcGFyYSBsb3Mgbm9tYnJlcyBpZ25vcmFuZG8gbGFzIGRpZmVyZW5jaWFzIGVudHJlIG1hecO6c2N1bGFzIHkgbWluw7pzY3VsYXMu' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==');
    add_guidance(102, 'Quiz Conceptual: Enlace Dinámico en POO', '¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) de la subclase en tiempo de ejecución?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'RWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtTb2JyZWNhcmdhIGVzdMOhdGljYSBlbiB0aWVtcG8gZGUgY29tcGlsYWNpw7NuIChPdmVybG9hZGluZynCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'RWwgZGVzcGFjaG8gZGluw6FtaWNvIHNlbGVjY2lvbmEgZW4gZWplY3VjacOzbiBsYSBpbXBsZW1lbnRhY2nDs24gZGVsIG9iamV0byByZWFsLiBMYSBzb2JyZWNhcmdhIHNlIHJlc3VlbHZlIHVzYW5kbyBsYXMgZmlybWFzIGRlIGxvcyBtw6l0b2Rvcy4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtDb252ZXJzacOzbiBpbXBsw61jaXRhIGRlIHRpcG9zIHByaW1pdGl2b3MgZW4gbGEgcGlsYcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBkZXNwYWNobyBkaW7DoW1pY28gc2VsZWNjaW9uYSBlbiBlamVjdWNpw7NuIGxhIGltcGxlbWVudGFjacOzbiBkZWwgb2JqZXRvIHJlYWwuIExhIHNvYnJlY2FyZ2Egc2UgcmVzdWVsdmUgdXNhbmRvIGxhcyBmaXJtYXMgZGUgbG9zIG3DqXRvZG9zLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtEdXBsaWNhY2nDs24gZGUgY29uc3RydWN0b3JlcyBlbiBsYSBjbGFzZSBiYXNlwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIGRlc3BhY2hvIGRpbsOhbWljbyBzZWxlY2Npb25hIGVuIGVqZWN1Y2nDs24gbGEgaW1wbGVtZW50YWNpw7NuIGRlbCBvYmpldG8gcmVhbC4gTGEgc29icmVjYXJnYSBzZSByZXN1ZWx2ZSB1c2FuZG8gbGFzIGZpcm1hcyBkZSBsb3MgbcOpdG9kb3Mu');
    add_guidance(106, 'Contratos SOLID: Clasificador de Nivel de Acceso', 'Implementa nivelAcceso(String rol) para que retorne "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE" (ignorando mayúsculas/minúsculas).',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U2VwYXJhIGxvcyByb2xlcyBxdWUgdGllbmVuIGFjY2VzbyBkZSBsb3MgcXVlIGRlYmVuIHNlciByZWNoYXphZG9zLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGVsIHJvbCBhbnRlcyBkZSBjb21wYXJhcmxvIHkgY29uc2VydmEgZWwgdGV4dG8gZGUgc2FsaWRhIGRlbCBlbnVuY2lhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu');
    add_guidance(107, 'Quiz Conceptual: Principio de Sustitución de Liskov', 'Según el Principio de Sustitución de Liskov (LSP) en Java, ¿qué debe cumplirse cuando una clase implementa una interfaz o extiende una clase base?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'RWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'RWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMYSBzdWJjbGFzZSBkZWJlIGRlY2xhcmFyIHRvZG9zIHN1cyBhdHJpYnV0b3MgY29tbyBww7pibGljb3PCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWwgcHJpbmNpcGlvIGRlIHN1c3RpdHVjacOzbiBkZSBMaXNrb3YgZXhpZ2UgcXVlIGVsIHN1YnRpcG8gcHVlZGEgcmVlbXBsYXphciBhbCBzdXBlcnRpcG8gcmVzcGV0YW5kbyBzdSBjb250cmF0byB5IHNpbiBpbnRyb2R1Y2lyIGVmZWN0b3MgaW5lc3BlcmFkb3MgcGFyYSBxdWllbiBsbyB1c2Eu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtVbmEgaW50ZXJmYXogc29sbyBwdWVkZSBzZXIgaW1wbGVtZW50YWRhIHBvciB1bmEgw7puaWNhIGNsYXNlIGVuIHRvZG8gZWwgcGFxdWV0ZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBwcmluY2lwaW8gZGUgc3VzdGl0dWNpw7NuIGRlIExpc2tvdiBleGlnZSBxdWUgZWwgc3VidGlwbyBwdWVkYSByZWVtcGxhemFyIGFsIHN1cGVydGlwbyByZXNwZXRhbmRvIHN1IGNvbnRyYXRvIHkgc2luIGludHJvZHVjaXIgZWZlY3RvcyBpbmVzcGVyYWRvcyBwYXJhIHF1aWVuIGxvIHVzYS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBjb21waWxhZG9yIHByb2jDrWJlIHVzYXIgcG9saW1vcmZpc21vIGNvbiBpbnRlcmZhY2VzIGZ1bmNpb25hbGVzwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIHByaW5jaXBpbyBkZSBzdXN0aXR1Y2nDs24gZGUgTGlza292IGV4aWdlIHF1ZSBlbCBzdWJ0aXBvIHB1ZWRhIHJlZW1wbGF6YXIgYWwgc3VwZXJ0aXBvIHJlc3BldGFuZG8gc3UgY29udHJhdG8geSBzaW4gaW50cm9kdWNpciBlZmVjdG9zIGluZXNwZXJhZG9zIHBhcmEgcXVpZW4gbG8gdXNhLg==');
    add_guidance(103, 'Búsqueda Binaria Segura O(log n)', 'Implementa search(int[] a, int target) retornando el índice (0-based) del elemento buscado o -1 si no se encuentra en el arreglo ordenado.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TGEgc2VjdWVuY2lhIGVzdMOhIG9yZGVuYWRhLiBVc2EgZXNlIG9yZGVuIHBhcmEgZGVzY2FydGFyIHVuYSBwYXJ0ZSBkZSBsYSBiw7pzcXVlZGEu' || CHR(10) ||
        'TWFudMOpbiBsb3MgbMOtbWl0ZXMgZGVsIGludGVydmFsbyB5IGNhbGN1bGEgc3UgcHVudG8gbWVkaW8uIEVuIFB5dGhvbiBuZWNlc2l0YXMgZGl2aXNpw7NuIGVudGVyYS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=');
    add_guidance(108, 'Quiz Conceptual: Prevención de Overflow en Búsqueda Binaria', '¿Por qué se prefiere la expresión mid = low + (high - low) / 2 en lugar de (low + high) / 2 en arreglos grandes en Java?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'bG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu' || CHR(10) ||
        'bG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu' || CHR(10) ||
        'bG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgZWwgb3BlcmFkb3IgKyBlc3TDoSBkZXNhY29uc2VqYWRvIGVuIHZlcnNpb25lcyByZWNpZW50ZXMgZGUgSmF2YcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBsb3cgKyAoaGlnaCAtIGxvdykgLyAyIGV2aXRhIHN1bWFyIGRpcmVjdGFtZW50ZSBkb3Mgw61uZGljZXMgZ3JhbmRlcyBxdWUgcG9kcsOtYW4gZGVzYm9yZGFyIHVuIGludC4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgY29udmllcnRlIGF1dG9tw6F0aWNhbWVudGUgZWwgYXJyZWdsbyBhIG7Dum1lcm9zIGRlIHB1bnRvIGZsb3RhbnRlwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIGxvdyArIChoaWdoIC0gbG93KSAvIDIgZXZpdGEgc3VtYXIgZGlyZWN0YW1lbnRlIGRvcyDDrW5kaWNlcyBncmFuZGVzIHF1ZSBwb2Ryw61hbiBkZXNib3JkYXIgdW4gaW50Lg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgcmVkdWNlIGxhIGNvbXBsZWppZGFkIGRlIE8obG9nIG4pIGEgTygxKSBjb25zdGFudGXCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gbG93ICsgKGhpZ2ggLSBsb3cpIC8gMiBldml0YSBzdW1hciBkaXJlY3RhbWVudGUgZG9zIMOtbmRpY2VzIGdyYW5kZXMgcXVlIHBvZHLDrWFuIGRlc2JvcmRhciB1biBpbnQu');
    add_guidance(109, 'Dos Punteros: Verificador de Palíndromos', 'Implementa esPalindromo(String texto) usando dos índices para retornar true si la palabra se lee igual de izquierda a derecha que de derecha a izquierda (ignorando mayúsculas), o false en caso contrario.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'VW4gcGFsw61uZHJvbW8gY29uc2VydmEgc3VzIGNhcmFjdGVyZXMgYWwgbGVlcmxvIGVuIHNlbnRpZG8gY29udHJhcmlvLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGxhcyBtYXnDunNjdWxhcyB5IGNvbXBhcmEgbG9zIGRvcyBleHRyZW1vcyBkZSBsYSBzZWN1ZW5jaWEu' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u');
    add_guidance(110, 'Quiz Conceptual: Complejidad de Dos Punteros', '¿Cuál es la complejidad temporal y espacial auxiliar de verificar un palíndromo in-place usando dos índices extremos (left y right)?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'TG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtUaWVtcG8gTyhuwrIpIHkgZXNwYWNpbyBhdXhpbGlhciBPKG4pIHBvciBjYWRhIGNhcsOhY3RlciBldmFsdWFkb8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBMb3MgZG9zIHB1bnRlcm9zIHJlY29ycmVuIGNvbW8gbcOheGltbyBsYSBsb25naXR1ZCBkZSBsYSBjYWRlbmEgeSBjb25zZXJ2YW4gc29sbyB1bm9zIHBvY29zIMOtbmRpY2VzLCBwb3IgZXNvIGVsIHRpZW1wbyBlcyBPKG4pIHkgZWwgZXNwYWNpbyBhZGljaW9uYWwgTygxKS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtUaWVtcG8gTyhsb2cgbikgcG9ycXVlIGRlc2NhcnRhIGxhIG1pdGFkIGRlbCB0ZXh0byBlbiBjYWRhIHBhc2/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gTG9zIGRvcyBwdW50ZXJvcyByZWNvcnJlbiBjb21vIG3DoXhpbW8gbGEgbG9uZ2l0dWQgZGUgbGEgY2FkZW5hIHkgY29uc2VydmFuIHNvbG8gdW5vcyBwb2NvcyDDrW5kaWNlcywgcG9yIGVzbyBlbCB0aWVtcG8gZXMgTyhuKSB5IGVsIGVzcGFjaW8gYWRpY2lvbmFsIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtUaWVtcG8gTygxKSBpbmRlcGVuZGllbnRlIGRlIGxhIGxvbmdpdHVkIGRlbCB0ZXh0b8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBMb3MgZG9zIHB1bnRlcm9zIHJlY29ycmVuIGNvbW8gbcOheGltbyBsYSBsb25naXR1ZCBkZSBsYSBjYWRlbmEgeSBjb25zZXJ2YW4gc29sbyB1bm9zIHBvY29zIMOtbmRpY2VzLCBwb3IgZXNvIGVsIHRpZW1wbyBlcyBPKG4pIHkgZWwgZXNwYWNpbyBhZGljaW9uYWwgTygxKS4=');
    add_guidance(111, 'Colecciones: Suma de Números Pares', 'Implementa sumarPares(int[] nums) para recorrer la secuencia y retornar la suma de todos los números pares contenidos en el arreglo.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U29sbyBkZWJlbiBjb250cmlidWlyIGFsIHJlc3VsdGFkbyBsb3MgbsO6bWVyb3MgcXVlIGN1bXBsZW4gbGEgY29uZGljacOzbiBkZSBwYXJpZGFkLg==' || CHR(10) ||
        'UHJlcGFyYSB1biBhY3VtdWxhZG9yIHkgcmVjb3JyZSBjYWRhIGVsZW1lbnRvIGRlIGxhIGNvbGVjY2nDs24u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu');
    add_guidance(112, 'Quiz Conceptual: Complejidad Amortizada en ArrayList', '¿Cuál es el costo temporal de insertar un elemento al final de un ArrayList<T> mediante add(elemento) en análisis amortizado?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'QXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'QXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'QXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPKG7CsikgcG9ycXVlIG9yZGVuYSBhdXRvbcOhdGljYW1lbnRlIHRvZG9zIGxvcyBlbGVtZW50b3MgZW4gY2FkYSBpbnNlcmNpw7Nuwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEFycmF5TGlzdCB1c2EgdW4gYXJyZWdsbyBxdWUgcHVlZGUgY3JlY2VyLiBFbCBhY2Nlc28gcG9yIMOtbmRpY2UgZXMgTygxKSB5IGHDsWFkaXIgYWwgZmluYWwgdGllbmUgY29zdGUgYW1vcnRpemFkbyBPKDEpLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPKGxvZyBuKSBwb3JxdWUgaW50ZXJuYW1lbnRlIGVzIHVuIMOhcmJvbCBiaW5hcmlvIGJhbGFuY2VhZG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gQXJyYXlMaXN0IHVzYSB1biBhcnJlZ2xvIHF1ZSBwdWVkZSBjcmVjZXIuIEVsIGFjY2VzbyBwb3Igw61uZGljZSBlcyBPKDEpIHkgYcOxYWRpciBhbCBmaW5hbCB0aWVuZSBjb3N0ZSBhbW9ydGl6YWRvIE8oMSku' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPKG4pIGVzdHJpY3RvIHBvcnF1ZSBjb3BpYSBlbCBhcnJlZ2xvIGNvbXBsZXRvIGVuIGNhZGEgbGxhbWFkYSBhIGFkZCgpwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEFycmF5TGlzdCB1c2EgdW4gYXJyZWdsbyBxdWUgcHVlZGUgY3JlY2VyLiBFbCBhY2Nlc28gcG9yIMOtbmRpY2UgZXMgTygxKSB5IGHDsWFkaXIgYWwgZmluYWwgdGllbmUgY29zdGUgYW1vcnRpemFkbyBPKDEpLg==');
    add_guidance(113, 'Tablas Hash: Primer Elemento Duplicado en O(n)', 'Implementa primerDuplicado(int[] nums) usando un HashSet para retornar el primer número que aparece repetido al recorrer el arreglo, o -1 si todos son únicos.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TmVjZXNpdGFzIHJlY29yZGFyIGxvcyB2YWxvcmVzIHF1ZSB5YSBhcGFyZWNpZXJvbiBkdXJhbnRlIGVsIHJlY29ycmlkby4=' || CHR(10) ||
        'VW4gY29uanVudG8gcGVybWl0ZSBjb21wcm9iYXIgc2kgdW4gZWxlbWVudG8geWEgc2UgaGEgdmlzaXRhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u');
    add_guidance(114, 'Quiz Conceptual: Contrato equals() y hashCode()', '¿Qué ocurre si una clase sobrescribe equals() pero NO sobrescribe hashCode() al almacenarse dentro de un HashSet o HashMap en Java?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'RG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'RG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBjw7NkaWdvIG5vIGNvbXBpbGEgcG9ycXVlIE9iamVjdCBubyBwcm92ZWUgdW5hIGltcGxlbWVudGFjacOzbiBiYXNlIGRlIGhhc2hDb2RlKCnCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBIYXNoU2V0IGNvbnZpZXJ0ZSBhdXRvbcOhdGljYW1lbnRlIHRvZG9zIGxvcyBvYmpldG9zIGEgU3RyaW5nIGFudGVzIGRlIGd1YXJkYXJsb3PCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRG9zIG9iamV0b3MgaWd1YWxlcyBzZWfDum4gZXF1YWxzIGRlYmVuIHByb2R1Y2lyIGVsIG1pc21vIGhhc2hDb2RlLiBVbiBoYXNoIGNvaW5jaWRlbnRlIHBvciBzw60gc29sbyBubyBnYXJhbnRpemEgaWd1YWxkYWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMYSBKVk0gbGFuemEgTnVsbFBvaW50ZXJFeGNlcHRpb24gYWwgaW52b2NhciBhZGQoKcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBEb3Mgb2JqZXRvcyBpZ3VhbGVzIHNlZ8O6biBlcXVhbHMgZGViZW4gcHJvZHVjaXIgZWwgbWlzbW8gaGFzaENvZGUuIFVuIGhhc2ggY29pbmNpZGVudGUgcG9yIHPDrSBzb2xvIG5vIGdhcmFudGl6YSBpZ3VhbGRhZC4=');
    add_guidance(104, 'Polimorfismo en Python: Sonidos por Especie', 'Completa la función emitir_sonido(tipo: str) -> str para retornar "Guau" con "Perro" y "Miau" con "Gato" (ignorando mayúsculas/minúsculas).',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHNvbmlkbyBjb3JyZXNwb25kZSBhIGNhZGEgZXNwZWNpZS4=' || CHR(10) ||
        'Q29tcGFyYSBsb3Mgbm9tYnJlcyBpZ25vcmFuZG8gbGFzIGRpZmVyZW5jaWFzIGVudHJlIG1hecO6c2N1bGFzIHkgbWluw7pzY3VsYXMu' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==');
    add_guidance(202, 'Quiz Conceptual: Clases Abstractas abc.ABC en Python', '¿Qué sucede en Python cuando intentas instanciar una subclase de abc.ABC que no ha implementado todos los métodos decorados con @abstractmethod?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'YWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'YWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'YWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQeXRob24gZ2VuZXJhIGF1dG9tw6F0aWNhbWVudGUgbcOpdG9kb3MgdmFjw61vcyBxdWUgcmV0b3JuYW4gTm9uZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBhYmMuQUJDIGltcGlkZSBpbnN0YW5jaWFyIHVuYSBzdWJjbGFzZSBxdWUgY29uc2VydmEgbcOpdG9kb3MgYWJzdHJhY3RvcyBzaW4gaW1wbGVtZW50YXIgeSBQeXRob24gbGFuemEgVHlwZUVycm9yLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBwcm9ncmFtYSBzZSBlamVjdXRhIG5vcm1hbG1lbnRlIGUgaWdub3JhIGVsIGRlY29yYWRvciBAYWJzdHJhY3RtZXRob2TCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gYWJjLkFCQyBpbXBpZGUgaW5zdGFuY2lhciB1bmEgc3ViY2xhc2UgcXVlIGNvbnNlcnZhIG3DqXRvZG9zIGFic3RyYWN0b3Mgc2luIGltcGxlbWVudGFyIHkgUHl0aG9uIGxhbnphIFR5cGVFcnJvci4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtTb2xvIG11ZXN0cmEgdW5hIGFkdmVydGVuY2lhIGVzdMOpdGljYSBlbiBsYSBjb25zb2xhIHNpbiBkZXRlbmVyIGVsIHByb2dyYW1hwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIGFiYy5BQkMgaW1waWRlIGluc3RhbmNpYXIgdW5hIHN1YmNsYXNlIHF1ZSBjb25zZXJ2YSBtw6l0b2RvcyBhYnN0cmFjdG9zIHNpbiBpbXBsZW1lbnRhciB5IFB5dGhvbiBsYW56YSBUeXBlRXJyb3Iu');
    add_guidance(203, 'Protocolos en Python: Clasificador de Nivel de Acceso', 'Completa nivel_acceso(rol: str) -> str para retornar "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE".',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U2VwYXJhIGxvcyByb2xlcyBxdWUgdGllbmVuIGFjY2VzbyBkZSBsb3MgcXVlIGRlYmVuIHNlciByZWNoYXphZG9zLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGVsIHJvbCBhbnRlcyBkZSBjb21wYXJhcmxvIHkgY29uc2VydmEgZWwgdGV4dG8gZGUgc2FsaWRhIGRlbCBlbnVuY2lhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu');
    add_guidance(204, 'Quiz Conceptual: Métodos Especiales __str__ vs __repr__', '¿Cuál es la diferencia fundamental entre los métodos especiales __str__ y __repr__ en el modelo de datos de Python?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'X19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'X19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'X19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtfX3N0cl9fIHNvbG8gZnVuY2lvbmEgY29uIG7Dum1lcm9zIGVudGVyb3MgeSBfX3JlcHJfXyBzb2xvIGNvbiBsaXN0YXPCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gX19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtBbWJvcyBtw6l0b2RvcyBzb24gaWTDqW50aWNvcyB5IG5vIHB1ZWRlbiBjb2V4aXN0aXIgZW4gbGEgbWlzbWEgY2xhc2XCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gX19zdHJfXyBidXNjYSB1bmEgcmVwcmVzZW50YWNpw7NuIGxlZ2libGUgcGFyYSBlbCB1c3VhcmlvIHkgX19yZXByX18gdW5hIHJlcHJlc2VudGFjacOzbiB0w6ljbmljYSBkZWwgb2JqZXRvLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtfX3JlcHJfXyBlcyBwcml2YWRvIHkgbm8gcHVlZGUgc2VyIGludm9jYWRvIGRlc2RlIGZ1ZXJhIGRlbCBtw7NkdWxvwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIF9fc3RyX18gYnVzY2EgdW5hIHJlcHJlc2VudGFjacOzbiBsZWdpYmxlIHBhcmEgZWwgdXN1YXJpbyB5IF9fcmVwcl9fIHVuYSByZXByZXNlbnRhY2nDs24gdMOpY25pY2EgZGVsIG9iamV0by4=');
    add_guidance(205, 'Búsqueda Binaria en Python con División Entera (//)', 'Completa search(a: list, target: int) -> int para retornar el índice del elemento buscado en la lista ordenada o -1 si no existe.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TGEgc2VjdWVuY2lhIGVzdMOhIG9yZGVuYWRhLiBVc2EgZXNlIG9yZGVuIHBhcmEgZGVzY2FydGFyIHVuYSBwYXJ0ZSBkZSBsYSBiw7pzcXVlZGEu' || CHR(10) ||
        'TWFudMOpbiBsb3MgbMOtbWl0ZXMgZGVsIGludGVydmFsbyB5IGNhbGN1bGEgc3UgcHVudG8gbWVkaW8uIEVuIFB5dGhvbiBuZWNlc2l0YXMgZGl2aXNpw7NuIGVudGVyYS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=');
    add_guidance(206, 'Quiz Conceptual: Operador // e Indexación en Python', '¿Por qué es indispensable utilizar el operador // en lugar de / al calcular el índice medio en Python 3?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'RWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'RWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgLy8gcmVhbGl6YSBsYSBkaXZpc2nDs24gZW4gdW4gaGlsbyBzZXBhcmFkbyBkZWwgcHJvY2VzYWRvcsK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBvcGVyYWRvciAvIHByb2R1Y2UgdW4gcmVzdWx0YWRvIGRlIHB1bnRvIGZsb3RhbnRlLiBMb3Mgw61uZGljZXMgZGUgdW5hIGxpc3RhIHJlcXVpZXJlbiBlbnRlcm9zLCBwb3IgZXNvIHNlIHV0aWxpemEgLy8u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgZWwgb3BlcmFkb3IgLyBsYW56YSBaZXJvRGl2aXNpb25FcnJvciBjdWFuZG8gZWwgcmVzdWx0YWRvIGVzIGRlY2ltYWzCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWwgb3BlcmFkb3IgLyBwcm9kdWNlIHVuIHJlc3VsdGFkbyBkZSBwdW50byBmbG90YW50ZS4gTG9zIMOtbmRpY2VzIGRlIHVuYSBsaXN0YSByZXF1aWVyZW4gZW50ZXJvcywgcG9yIGVzbyBzZSB1dGlsaXphIC8vLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgLy8gb3JkZW5hIGF1dG9tw6F0aWNhbWVudGUgbGEgbGlzdGEgYW50ZXMgZGUgZGl2aWRpcsK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBvcGVyYWRvciAvIHByb2R1Y2UgdW4gcmVzdWx0YWRvIGRlIHB1bnRvIGZsb3RhbnRlLiBMb3Mgw61uZGljZXMgZGUgdW5hIGxpc3RhIHJlcXVpZXJlbiBlbnRlcm9zLCBwb3IgZXNvIHNlIHV0aWxpemEgLy8u');
    add_guidance(207, 'Slicing en Python: Detector de Palíndromos', 'Completa es_palindromo(texto: str) -> bool invirtiendo o comparando la cadena en minúsculas para evaluar si es un palíndromo.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'VW4gcGFsw61uZHJvbW8gY29uc2VydmEgc3VzIGNhcmFjdGVyZXMgYWwgbGVlcmxvIGVuIHNlbnRpZG8gY29udHJhcmlvLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGxhcyBtYXnDunNjdWxhcyB5IGNvbXBhcmEgbG9zIGRvcyBleHRyZW1vcyBkZSBsYSBzZWN1ZW5jaWEu' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u');
    add_guidance(208, 'Quiz Conceptual: Slicing s[::-1] en Memoria', '¿Qué hace exactamente la expresión texto[::-1] sobre una cadena str en Python?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWwgc2xpY2luZyBjb24gcGFzbyAtMSBjcmVhIHVuYSBzZWN1ZW5jaWEgaW52ZXJ0aWRhLiBFc2EgbnVldmEgc2VjdWVuY2lhIG9jdXBhIGVzcGFjaW8gYWRpY2lvbmFsIHByb3BvcmNpb25hbCBhIHN1IGxvbmdpdHVkLg==' || CHR(10) ||
        'RWwgc2xpY2luZyBjb24gcGFzbyAtMSBjcmVhIHVuYSBzZWN1ZW5jaWEgaW52ZXJ0aWRhLiBFc2EgbnVldmEgc2VjdWVuY2lhIG9jdXBhIGVzcGFjaW8gYWRpY2lvbmFsIHByb3BvcmNpb25hbCBhIHN1IGxvbmdpdHVkLg==' || CHR(10) ||
        'RWwgc2xpY2luZyBjb24gcGFzbyAtMSBjcmVhIHVuYSBzZWN1ZW5jaWEgaW52ZXJ0aWRhLiBFc2EgbnVldmEgc2VjdWVuY2lhIG9jdXBhIGVzcGFjaW8gYWRpY2lvbmFsIHByb3BvcmNpb25hbCBhIHN1IGxvbmdpdHVkLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbGltaW5hIGVsIMO6bHRpbW8gY2Fyw6FjdGVyIGRlIGxhIGNhZGVuYSBtb2RpZmljw6FuZG9sYSBpbi1wbGFjZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbCBzbGljaW5nIGNvbiBwYXNvIC0xIGNyZWEgdW5hIHNlY3VlbmNpYSBpbnZlcnRpZGEuIEVzYSBudWV2YSBzZWN1ZW5jaWEgb2N1cGEgZXNwYWNpbyBhZGljaW9uYWwgcHJvcG9yY2lvbmFsIGEgc3UgbG9uZ2l0dWQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtDb252aWVydGUgbGEgY2FkZW5hIGVuIHVuIGRpY2Npb25hcmlvIGRlIGZyZWN1ZW5jaWFzwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIHNsaWNpbmcgY29uIHBhc28gLTEgY3JlYSB1bmEgc2VjdWVuY2lhIGludmVydGlkYS4gRXNhIG51ZXZhIHNlY3VlbmNpYSBvY3VwYSBlc3BhY2lvIGFkaWNpb25hbCBwcm9wb3JjaW9uYWwgYSBzdSBsb25naXR1ZC4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMYW56YSBJbmRleEVycm9yIHBvcnF1ZSBsb3MgcGFzb3MgbmVnYXRpdm9zIG5vIGV4aXN0ZW4gZW4gUHl0aG9uwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsIHNsaWNpbmcgY29uIHBhc28gLTEgY3JlYSB1bmEgc2VjdWVuY2lhIGludmVydGlkYS4gRXNhIG51ZXZhIHNlY3VlbmNpYSBvY3VwYSBlc3BhY2lvIGFkaWNpb25hbCBwcm9wb3JjaW9uYWwgYSBzdSBsb25naXR1ZC4=');
    add_guidance(209, 'Listas en Python: Suma de Elementos Pares', 'Completa sumar_pares(nums: list) -> int para retornar la suma acumulada de los números divisibles por 2.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U29sbyBkZWJlbiBjb250cmlidWlyIGFsIHJlc3VsdGFkbyBsb3MgbsO6bWVyb3MgcXVlIGN1bXBsZW4gbGEgY29uZGljacOzbiBkZSBwYXJpZGFkLg==' || CHR(10) ||
        'UHJlcGFyYSB1biBhY3VtdWxhZG9yIHkgcmVjb3JyZSBjYWRhIGVsZW1lbnRvIGRlIGxhIGNvbGVjY2nDs24u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu');
    add_guidance(210, 'Quiz Conceptual: Crecimiento de list en CPython', '¿Cómo logra la estructura list de CPython que las llamadas sucesivas a append() tengan costo O(1) amortizado?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'Q1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'Q1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'Q1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtBbG1hY2VuYSBjYWRhIGVsZW1lbnRvIGVuIHVuIGFyY2hpdm8gdGVtcG9yYWwgZW4gZGlzY2/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gQ1B5dGhvbiByZXNlcnZhIGNhcGFjaWRhZCBhZGljaW9uYWwgYWwgaGFjZXIgY3JlY2VyIHVuYSBsaXN0YS4gQXPDrSBkaXN0cmlidXllIGVsIGNvc3RlIGRlIGxhcyByZWFsb2NhY2lvbmVzIGVudHJlIG11Y2hhcyBsbGFtYWRhcyBhIGFwcGVuZCB5IGNvbnNpZ3VlIE8oMSkgYW1vcnRpemFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtVc2EgdW5hIGxpc3RhIGRvYmxlbWVudGUgZW5sYXphZGEgc2luIGJsb3F1ZXMgY29udGlndW9zwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIENQeXRob24gcmVzZXJ2YSBjYXBhY2lkYWQgYWRpY2lvbmFsIGFsIGhhY2VyIGNyZWNlciB1bmEgbGlzdGEuIEFzw60gZGlzdHJpYnV5ZSBlbCBjb3N0ZSBkZSBsYXMgcmVhbG9jYWNpb25lcyBlbnRyZSBtdWNoYXMgbGxhbWFkYXMgYSBhcHBlbmQgeSBjb25zaWd1ZSBPKDEpIGFtb3J0aXphZG8u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtMaW1pdGEgZWwgdGFtYcOxbyBtw6F4aW1vIGRlIGxhcyBsaXN0YXMgYSAyNTYgZWxlbWVudG9zwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIENQeXRob24gcmVzZXJ2YSBjYXBhY2lkYWQgYWRpY2lvbmFsIGFsIGhhY2VyIGNyZWNlciB1bmEgbGlzdGEuIEFzw60gZGlzdHJpYnV5ZSBlbCBjb3N0ZSBkZSBsYXMgcmVhbG9jYWNpb25lcyBlbnRyZSBtdWNoYXMgbGxhbWFkYXMgYSBhcHBlbmQgeSBjb25zaWd1ZSBPKDEpIGFtb3J0aXphZG8u');
    add_guidance(211, 'Conjuntos Hash en Python: Primer Duplicado en O(n)', 'Completa primer_duplicado(nums: list) -> int utilizando un conjunto (set) para retornar el primer valor repetido o -1 si no hay duplicados.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TmVjZXNpdGFzIHJlY29yZGFyIGxvcyB2YWxvcmVzIHF1ZSB5YSBhcGFyZWNpZXJvbiBkdXJhbnRlIGVsIHJlY29ycmlkby4=' || CHR(10) ||
        'VW4gY29uanVudG8gcGVybWl0ZSBjb21wcm9iYXIgc2kgdW4gZWxlbWVudG8geWEgc2UgaGEgdmlzaXRhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u');
    add_guidance(212, 'Quiz Conceptual: Elementos Hashables en set y dict', '¿Por qué una tupla (tuple) de enteros puede ser clave en un dict o elemento de un set en Python, pero una lista (list) no?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'VW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==' || CHR(10) ||
        'VW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==' || CHR(10) ||
        'VW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgbGlzdCBvY3VwYSBtw6FzIGRlIDY0IGJpdHMgZW4gbWVtb3JpYcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBVbmEgdHVwbGEgZGUgZW50ZXJvcyBlcyBpbm11dGFibGUgeSB0aWVuZSB1biBoYXNoIGVzdGFibGUuIExhcyBsaXN0YXMgc29uIG11dGFibGVzIHkgbm8gc29uIGhhc2hhYmxlcywgcG9yIGVzbyBubyBwdWVkZW4gc2VyIGNsYXZlcyBkZSB1biBkaWN0IG5pIGVsZW1lbnRvcyBkZSB1biBzZXQu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgdHVwbGUgb3JkZW5hIHN1cyBlbGVtZW50b3MgYXV0b23DoXRpY2FtZW50ZSBhbCBjcmVhcnNlwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIFVuYSB0dXBsYSBkZSBlbnRlcm9zIGVzIGlubXV0YWJsZSB5IHRpZW5lIHVuIGhhc2ggZXN0YWJsZS4gTGFzIGxpc3RhcyBzb24gbXV0YWJsZXMgeSBubyBzb24gaGFzaGFibGVzLCBwb3IgZXNvIG5vIHB1ZWRlbiBzZXIgY2xhdmVzIGRlIHVuIGRpY3QgbmkgZWxlbWVudG9zIGRlIHVuIHNldC4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQb3JxdWUgbG9zIGNvbmp1bnRvcyBzZXQgc29sbyBhY2VwdGFuIGNhZGVuYXMgZGUgdGV4dG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gVW5hIHR1cGxhIGRlIGVudGVyb3MgZXMgaW5tdXRhYmxlIHkgdGllbmUgdW4gaGFzaCBlc3RhYmxlLiBMYXMgbGlzdGFzIHNvbiBtdXRhYmxlcyB5IG5vIHNvbiBoYXNoYWJsZXMsIHBvciBlc28gbm8gcHVlZGVuIHNlciBjbGF2ZXMgZGUgdW4gZGljdCBuaSBlbGVtZW50b3MgZGUgdW4gc2V0Lg==');
    add_guidance(105, 'Polimorfismo en C++: Clasificación de Sonidos', 'Completa emitirSonido(std::string tipo) para retornar "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato".',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHNvbmlkbyBjb3JyZXNwb25kZSBhIGNhZGEgZXNwZWNpZS4=' || CHR(10) ||
        'Q29tcGFyYSBsb3Mgbm9tYnJlcyBpZ25vcmFuZG8gbGFzIGRpZmVyZW5jaWFzIGVudHJlIG1hecO6c2N1bGFzIHkgbWluw7pzY3VsYXMu' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==' || CHR(10) ||
        'Q29tcHJ1ZWJhIFBlcnJvIHkgR2F0byBhbnRlcyBkZSBkZXZvbHZlciBlbCB2YWxvciBwYXJhIHVuYSBlc3BlY2llIGRlc2Nvbm9jaWRhLg==');
    add_guidance(302, 'Quiz Conceptual: Destructor Virtual en C++', '¿Qué riesgo crítico ocurre en C++ si eliminas con delete un objeto derivado a través de un puntero a su clase base que NO tiene destructor virtual?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'RWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'RWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'RWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBjb21waWxhZG9yIGRlIEMrKyBjb252aWVydGUgdG9kb3MgbG9zIG3DqXRvZG9zIGRlIGxhIGNsYXNlIGVuIHByaXZhZG9zwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIEVsaW1pbmFyIHVuIG9iamV0byBkZXJpdmFkbyBtZWRpYW50ZSB1biBwdW50ZXJvIGJhc2UgcmVxdWllcmUgdW4gZGVzdHJ1Y3RvciB2aXJ0dWFsIHBhcmEgcXVlIHNlIGVqZWN1dGUgY29ycmVjdGFtZW50ZSBsYSBkZXN0cnVjY2nDs24gZGUgbGEgamVyYXJxdcOtYS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCBwdW50ZXJvIGJhc2Ugc2UgbXVsdGlwbGljYSBwb3IgZG9zIGVuIG1lbW9yaWHCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gRWxpbWluYXIgdW4gb2JqZXRvIGRlcml2YWRvIG1lZGlhbnRlIHVuIHB1bnRlcm8gYmFzZSByZXF1aWVyZSB1biBkZXN0cnVjdG9yIHZpcnR1YWwgcGFyYSBxdWUgc2UgZWplY3V0ZSBjb3JyZWN0YW1lbnRlIGxhIGRlc3RydWNjacOzbiBkZSBsYSBqZXJhcnF1w61hLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtFbCByZWNvbGVjdG9yIGRlIGJhc3VyYSBkZSBDKysgZGV0aWVuZSBlbCBwcm9ncmFtYSBpbm1lZGlhdGFtZW50ZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBFbGltaW5hciB1biBvYmpldG8gZGVyaXZhZG8gbWVkaWFudGUgdW4gcHVudGVybyBiYXNlIHJlcXVpZXJlIHVuIGRlc3RydWN0b3IgdmlydHVhbCBwYXJhIHF1ZSBzZSBlamVjdXRlIGNvcnJlY3RhbWVudGUgbGEgZGVzdHJ1Y2Npw7NuIGRlIGxhIGplcmFycXXDrWEu');
    add_guidance(303, 'Contratos en C++: Clasificador de Nivel de Acceso', 'Completa nivelAcceso(std::string rol) para retornar "TOTAL" cuando reciba "ADMIN" y "LECTURA" cuando reciba "ESTUDIANTE".',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U2VwYXJhIGxvcyByb2xlcyBxdWUgdGllbmVuIGFjY2VzbyBkZSBsb3MgcXVlIGRlYmVuIHNlciByZWNoYXphZG9zLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGVsIHJvbCBhbnRlcyBkZSBjb21wYXJhcmxvIHkgY29uc2VydmEgZWwgdGV4dG8gZGUgc2FsaWRhIGRlbCBlbnVuY2lhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu' || CHR(10) ||
        'Q29tcHJ1ZWJhIEFETUlOIHkgRVNUVURJQU5URS4gSW5jbHV5ZSB1biByZXN1bHRhZG8gcGFyYSBjdWFscXVpZXIgb3RybyByb2wu');
    add_guidance(304, 'Quiz Conceptual: std::unique_ptr y Semántica RAII', '¿Cuál es la característica principal de std::unique_ptr<T> introducido en el C++ moderno frente a los punteros crudos con new/delete?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'UkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'UkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'UkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQZXJtaXRlIHF1ZSBtw7psdGlwbGVzIHB1bnRlcm9zIHNlYW4gZHVlw7FvcyBzaW11bHTDoW5lb3MgbWVkaWFudGUgY29udGVvIGF0w7NtaWNvwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIFJBSUkgdmluY3VsYSBsYSB2aWRhIGRlbCByZWN1cnNvIGEgbGEgZGUgc3Ugb2JqZXRvLiB1bmlxdWVfcHRyIGV4cHJlc2EgcHJvcGllZGFkIGV4Y2x1c2l2YSB5IGxpYmVyYSBlbCByZWN1cnNvIGFsIGRlc3RydWlyc2Uu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtSZXF1aWVyZSBsbGFtYXIgbWFudWFsbWVudGUgYSBmcmVlKCkgYW50ZXMgZGUgcXVlIHRlcm1pbmUgbGEgZnVuY2nDs27CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gUkFJSSB2aW5jdWxhIGxhIHZpZGEgZGVsIHJlY3Vyc28gYSBsYSBkZSBzdSBvYmpldG8uIHVuaXF1ZV9wdHIgZXhwcmVzYSBwcm9waWVkYWQgZXhjbHVzaXZhIHkgbGliZXJhIGVsIHJlY3Vyc28gYWwgZGVzdHJ1aXJzZS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtEZXNhY3RpdmEgbGFzIGNvbXByb2JhY2lvbmVzIGRlIHRpcG9zIGRlbCBjb21waWxhZG9yIGcrK8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBSQUlJIHZpbmN1bGEgbGEgdmlkYSBkZWwgcmVjdXJzbyBhIGxhIGRlIHN1IG9iamV0by4gdW5pcXVlX3B0ciBleHByZXNhIHByb3BpZWRhZCBleGNsdXNpdmEgeSBsaWJlcmEgZWwgcmVjdXJzbyBhbCBkZXN0cnVpcnNlLg==');
    add_guidance(305, 'Búsqueda Binaria en C++ sobre const std::vector<int>&', 'Completa search(const std::vector<int>& a, int target) retornando el índice donde se ubica target o -1 si no está presente.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TGEgc2VjdWVuY2lhIGVzdMOhIG9yZGVuYWRhLiBVc2EgZXNlIG9yZGVuIHBhcmEgZGVzY2FydGFyIHVuYSBwYXJ0ZSBkZSBsYSBiw7pzcXVlZGEu' || CHR(10) ||
        'TWFudMOpbiBsb3MgbMOtbWl0ZXMgZGVsIGludGVydmFsbyB5IGNhbGN1bGEgc3UgcHVudG8gbWVkaW8uIEVuIFB5dGhvbiBuZWNlc2l0YXMgZGl2aXNpw7NuIGVudGVyYS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=' || CHR(10) ||
        'U2kgZWwgdmFsb3IgY2VudHJhbCBlcyBtZW5vciBxdWUgZWwgb2JqZXRpdm8sIGF2YW56YSBlbCBsw61taXRlIGl6cXVpZXJkby4gU2kgZXMgbWF5b3IsIHJldHJvY2VkZSBlbCBkZXJlY2hvLiBDb21wcnVlYmEgdGFtYmnDqW4gdW4gdmFsb3IgYXVzZW50ZS4=');
    add_guidance(306, 'Quiz Conceptual: Paso por Referencia Constante (const T&)', '¿Por qué una función de búsqueda en C++ debe recibir const std::vector<int>& en lugar de std::vector<int> por valor?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'VW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'VW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'VW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPYmxpZ2EgYWwgY29tcGlsYWRvciBhIGNvcGlhciBlbCB2ZWN0b3IgY29tcGxldG8gZW4gZWwgcmVnaXN0cm8gZGVsIHByb2Nlc2Fkb3LCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gVW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtDb252aWVydGUgZWwgdmVjdG9yIGVuIHVuYSBsaXN0YSBlbmxhemFkYSBkaW7DoW1pY2FtZW50ZcK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiBVbmEgcmVmZXJlbmNpYSBjb25zdGFudGUgZXZpdGEgY29waWFyIGVsIHZlY3RvciB5IHBlcm1pdGUgbGVlcmxvIHNpbiBtb2RpZmljYXJsbyBhIHRyYXbDqXMgZGUgZXNhIHJlZmVyZW5jaWEu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtQZXJtaXRlIG1vZGlmaWNhciBsaWJyZW1lbnRlIGVsIHRhbWHDsW8gZGVsIHZlY3RvciBkZW50cm8gZGUgbGEgZnVuY2nDs27CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gVW5hIHJlZmVyZW5jaWEgY29uc3RhbnRlIGV2aXRhIGNvcGlhciBlbCB2ZWN0b3IgeSBwZXJtaXRlIGxlZXJsbyBzaW4gbW9kaWZpY2FybG8gYSB0cmF2w6lzIGRlIGVzYSByZWZlcmVuY2lhLg==');
    add_guidance(307, 'Dos Índices en C++: Verificador de Palíndromos', 'Completa esPalindromo(std::string texto) recorriendo desde los extremos left y right para evaluar si la cadena es palíndroma.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'VW4gcGFsw61uZHJvbW8gY29uc2VydmEgc3VzIGNhcmFjdGVyZXMgYWwgbGVlcmxvIGVuIHNlbnRpZG8gY29udHJhcmlvLg==' || CHR(10) ||
        'Tm9ybWFsaXphIGxhcyBtYXnDunNjdWxhcyB5IGNvbXBhcmEgbG9zIGRvcyBleHRyZW1vcyBkZSBsYSBzZWN1ZW5jaWEu' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u' || CHR(10) ||
        'QXZhbnphIGhhY2lhIGVsIGNlbnRybyBtaWVudHJhcyBjb2luY2lkYW4gbG9zIGNhcmFjdGVyZXMuIFVuYSBzb2xhIGRpZmVyZW5jaWEgcGVybWl0ZSBkZXRlbmVyIGxhIGNvbXByb2JhY2nDs24u');
    add_guidance(308, 'Quiz Conceptual: Iteradores de Acceso Aleatorio en STL', '¿Qué contenedores de la STL de C++ ofrecen iteradores de acceso aleatorio (Random Access Iterators) en tiempo O(1)?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'dmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'dmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'dmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqvDmm5pY2FtZW50ZSBzdGQ6Omxpc3QgeSBzdGQ6OmZvcndhcmRfbGlzdMK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiB2ZWN0b3IsIGRlcXVlIHkgc3RyaW5nIG9mcmVjZW4gaXRlcmFkb3JlcyBkZSBhY2Nlc28gYWxlYXRvcmlvIGNvbiBzYWx0b3MgaXQgKyBuIGVuIE8oMSkuIExvcyBpdGVyYWRvcmVzIGRlIGxpc3QgZGViZW4gYXZhbnphciBlbmxhY2UgcG9yIGVubGFjZS4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtTb2xvIHN0ZDo6bWFwIHkgc3RkOjpzZXTCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gdmVjdG9yLCBkZXF1ZSB5IHN0cmluZyBvZnJlY2VuIGl0ZXJhZG9yZXMgZGUgYWNjZXNvIGFsZWF0b3JpbyBjb24gc2FsdG9zIGl0ICsgbiBlbiBPKDEpLiBMb3MgaXRlcmFkb3JlcyBkZSBsaXN0IGRlYmVuIGF2YW56YXIgZW5sYWNlIHBvciBlbmxhY2Uu' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtOaW5nw7puIGNvbnRlbmVkb3IgZGUgbGEgU1RMIHNvcG9ydGEgYWNjZXNvIGFsZWF0b3Jpb8K7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiB2ZWN0b3IsIGRlcXVlIHkgc3RyaW5nIG9mcmVjZW4gaXRlcmFkb3JlcyBkZSBhY2Nlc28gYWxlYXRvcmlvIGNvbiBzYWx0b3MgaXQgKyBuIGVuIE8oMSkuIExvcyBpdGVyYWRvcmVzIGRlIGxpc3QgZGViZW4gYXZhbnphciBlbmxhY2UgcG9yIGVubGFjZS4=');
    add_guidance(309, 'STL std::vector: Acumulador de Elementos Pares', 'Completa sumarPares(const std::vector<int>& nums) para acumular y retornar la suma de todos los enteros pares del vector.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'U29sbyBkZWJlbiBjb250cmlidWlyIGFsIHJlc3VsdGFkbyBsb3MgbsO6bWVyb3MgcXVlIGN1bXBsZW4gbGEgY29uZGljacOzbiBkZSBwYXJpZGFkLg==' || CHR(10) ||
        'UHJlcGFyYSB1biBhY3VtdWxhZG9yIHkgcmVjb3JyZSBjYWRhIGVsZW1lbnRvIGRlIGxhIGNvbGVjY2nDs24u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu' || CHR(10) ||
        'Q29tcHJ1ZWJhIGVsIHJlc3RvIGRlIGxhIGRpdmlzacOzbiBlbnRyZSBkb3MgYW50ZXMgZGUgc3VtYXIuIFVuYSBjb2xlY2Npw7NuIHNpbiBuw7ptZXJvcyBwYXJlcyBkZWJlIGNvbnNlcnZhciBlbCBhY3VtdWxhZG9yIGluaWNpYWwu');
    add_guidance(310, 'Quiz Conceptual: size() vs capacity() en std::vector', '¿Qué hace el método reserve(n) en un std::vector<T> de C++ antes de insertar una gran cantidad de elementos?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'cmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'cmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'cmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtSZWxsZW5hIGVsIHZlY3RvciBjb24gbiBjZXJvcyBtb2RpZmljYW5kbyBzdSBzaXplKCkgaW5tZWRpYXRhbWVudGXCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gcmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtPcmRlbmEgbG9zIGVsZW1lbnRvcyBkZSBtYXlvciBhIG1lbm9yIGNhcGFjaWRhZMK7IG5vIGRlc2NyaWJlIGVsIGNvbmNlcHRvIGV2YWx1YWRvLiByZXNlcnZlKG4pIGdhcmFudGl6YSBjYXBhY2lkYWQgcGFyYSBhbCBtZW5vcyBuIGVsZW1lbnRvcyB5IHJlZHVjZSByZWFsb2NhY2lvbmVzIHBvc3RlcmlvcmVzLiBMYSBjYXBhY2lkYWQgcHVlZGUgYXVtZW50YXIsIHBlcm8gZWwgdGFtYcOxbyB5IGxvcyBlbGVtZW50b3MgZXhpc3RlbnRlcyBzZSBjb25zZXJ2YW4u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtCbG9xdWVhIGVsIHZlY3RvciBpbXBpZGllbmRvIGFncmVnYXIgbcOhcyBlbGVtZW50b3PCuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gcmVzZXJ2ZShuKSBnYXJhbnRpemEgY2FwYWNpZGFkIHBhcmEgYWwgbWVub3MgbiBlbGVtZW50b3MgeSByZWR1Y2UgcmVhbG9jYWNpb25lcyBwb3N0ZXJpb3Jlcy4gTGEgY2FwYWNpZGFkIHB1ZWRlIGF1bWVudGFyLCBwZXJvIGVsIHRhbWHDsW8geSBsb3MgZWxlbWVudG9zIGV4aXN0ZW50ZXMgc2UgY29uc2VydmFuLg==');
    add_guidance(311, 'STL std::unordered_set: Primer Duplicado en O(n)', 'Completa primerDuplicado(const std::vector<int>& nums) empleando std::unordered_set<int> para retornar el primer valor repetido o -1.',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'TmVjZXNpdGFzIHJlY29yZGFyIGxvcyB2YWxvcmVzIHF1ZSB5YSBhcGFyZWNpZXJvbiBkdXJhbnRlIGVsIHJlY29ycmlkby4=' || CHR(10) ||
        'VW4gY29uanVudG8gcGVybWl0ZSBjb21wcm9iYXIgc2kgdW4gZWxlbWVudG8geWEgc2UgaGEgdmlzaXRhZG8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u' || CHR(10) ||
        'Q29tcHJ1ZWJhIGxhIHBlcnRlbmVuY2lhIGFudGVzIGRlIGluc2VydGFyIGNhZGEgdmFsb3IuIERldnVlbHZlIGVsIHByaW1lciByZXBldGlkbyBlbmNvbnRyYWRvIGR1cmFudGUgZWwgcmVjb3JyaWRvIHkgY29udGVtcGxhIHF1ZSBubyBoYXlhIG5pbmd1bm8u');
    add_guidance(312, 'Quiz Conceptual: std::unordered_map vs std::map', '¿Cuál es la diferencia estructural y de complejidad temporal entre std::unordered_map y std::map en la STL de C++?',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBxdcOpIHByb3BpZWRhZCBkZWwgbGVuZ3VhamUgZXN0w6EgZXZhbHVhbmRvIGxhIHByZWd1bnRhLg==' || CHR(10) ||
        'Q29udHJhc3RhIGNhZGEgYWZpcm1hY2nDs24gY29uIGxhIHRlb3LDrWEgZGUgZXN0YSBsZWNjacOzbi4=' || CHR(10) ||
        'dW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'dW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'dW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtBbWJvcyB1c2FuIHRhYmxhcyBoYXNoIGlkw6ludGljYXMgcGVybyBjb24gZGlzdGludG8gbm9tYnJlIGRlIGNhYmVjZXJhwrsgbm8gZGVzY3JpYmUgZWwgY29uY2VwdG8gZXZhbHVhZG8uIHVub3JkZXJlZF9tYXAgdXNhIHVuYSB0YWJsYSBoYXNoIGNvbiBiw7pzcXVlZGEgcHJvbWVkaW8gTygxKS4gbWFwIGNvbnNlcnZhIGxhcyBjbGF2ZXMgb3JkZW5hZGFzIHkgb2ZyZWNlIGLDunNxdWVkYSBPKGxvZyBuKSwgbm9ybWFsbWVudGUgbWVkaWFudGUgdW4gw6FyYm9sIGJhbGFuY2VhZG8u' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtzdGQ6Om1hcCBubyBwZXJtaXRlIGFsbWFjZW5hciBlbnRlcm9zIGNvbW8gdmFsb3IgYXNvY2lhZG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gdW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=' || CHR(10) ||
        'TGEgYWZpcm1hY2nDs24gwqtzdGQ6OnVub3JkZXJlZF9tYXAgbWFudGllbmUgbGFzIGNsYXZlcyBvcmRlbmFkYXMgYWxmYWLDqXRpY2FtZW50ZSBlbiB0b2RvIG1vbWVudG/CuyBubyBkZXNjcmliZSBlbCBjb25jZXB0byBldmFsdWFkby4gdW5vcmRlcmVkX21hcCB1c2EgdW5hIHRhYmxhIGhhc2ggY29uIGLDunNxdWVkYSBwcm9tZWRpbyBPKDEpLiBtYXAgY29uc2VydmEgbGFzIGNsYXZlcyBvcmRlbmFkYXMgeSBvZnJlY2UgYsO6c3F1ZWRhIE8obG9nIG4pLCBub3JtYWxtZW50ZSBtZWRpYW50ZSB1biDDoXJib2wgYmFsYW5jZWFkby4=');
    DBMS_OUTPUT.PUT_LINE('Guias didacticas incorporadas: ' || v_updated);
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK TO guidance_seed;
    RAISE;
END;
/

-- =============================================================================
-- FASE 14 - RETOS FINALES POR MODULO
-- Fuente integrada: database/14_module_finals.sql
-- =============================================================================
PROMPT FASE 14 - RETOS FINALES POR MODULO

-- Apply after 13_challenge_guidance.sql as the schema owner.
-- Adds one final code-reading assessment to each original module.
-- Re-runnable. Existing final assignments and student attempts are preserved.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM user_tables WHERE table_name='FINAL_MODULO';
    IF v_count=0 THEN
        EXECUTE IMMEDIATE 'CREATE TABLE Final_Modulo ('
            || 'id_modulo NUMBER PRIMARY KEY,id_reto NUMBER NOT NULL UNIQUE,'
            || 'CONSTRAINT fk_final_modulo FOREIGN KEY(id_modulo) REFERENCES Modulos(id_modulo) ON DELETE CASCADE,'
            || 'CONSTRAINT fk_final_reto FOREIGN KEY(id_reto) REFERENCES Retos(id_reto) ON DELETE CASCADE)';
    END IF;
END;
/

DECLARE
    v_created NUMBER := 0;
    PROCEDURE create_final(p_module NUMBER,p_lesson NUMBER,p_lesson_title VARCHAR2,p_title VARCHAR2,
            p_statement CLOB,p_code CLOB,p_options CLOB,p_answer VARCHAR2,p_guidance CLOB) IS
        v_id NUMBER;
        v_module NUMBER;
        v_lesson NUMBER;
        v_count NUMBER;
        v_order NUMBER;
    BEGIN
        BEGIN
            SELECT id_modulo INTO v_module FROM Modulos WHERE id_modulo=p_module FOR UPDATE;
            SELECT id_leccion INTO v_lesson FROM Lecciones
            WHERE id_leccion=p_lesson AND id_modulo=p_module AND titulo=p_lesson_title FOR UPDATE;
        EXCEPTION WHEN NO_DATA_FOUND THEN RETURN;
        END;
        SELECT COUNT(*) INTO v_count FROM Final_Modulo WHERE id_modulo=p_module;
        IF v_count>0 THEN RETURN; END IF;
        SELECT NVL(MAX(orden_secuencia),0)+1 INTO v_order FROM Retos WHERE id_leccion=p_lesson;
        INSERT INTO Retos(id_leccion,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,orden_secuencia,estado,revision,guia_didactica)
        VALUES(p_lesson,p_title,p_statement,'PREDICCION','MEDIO',100,v_order,'PUBLICADO',1,p_guidance)
        RETURNING id_reto INTO v_id;
        INSERT INTO Reto_Quiz(id_reto,opciones,respuesta_correcta) VALUES(v_id,p_options,p_answer);
        INSERT INTO Reto_Prediccion(id_reto,codigo_ejemplo) VALUES(v_id,p_code);
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,opciones,respuesta_correcta,estado,guia_didactica)
        VALUES(v_id,1,p_title,p_statement,'PREDICCION','MEDIO',100,p_code,p_options,p_answer,'PUBLICADO',p_guidance);
        INSERT INTO Final_Modulo(id_modulo,id_reto) VALUES(p_module,v_id);
        v_created := v_created+1;
    END;
BEGIN
    SAVEPOINT module_finals;
    create_final(1, 10, 'Interfaces, Contratos Múltiples y Sustitución de Liskov (SOLID)',
        'Reto final · Servicio de mensajes', TO_CLOB('Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado. Evalúa solve("5") en la clase mostrada.'),
        TO_CLOB('public class FinalModuleSolver {') || CHR(10) ||
        '    public static String solve(String input) {' || CHR(10) ||
        '        Message message = new Greeting();' || CHR(10) ||
        '        return message.text().toUpperCase();' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        'interface Message { String text(); }' || CHR(10) ||
        'class Greeting implements Message {' || CHR(10) ||
        '    public String text() { return "Hola"; }' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SG9sYQ==' || CHR(10) ||
        'SE9MQQ==' || CHR(10) ||
        'RXJyb3IgZGUgdGlwbw==' || CHR(10) ||
        'TWVuc2FqZQ==', 'HOLA',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBlbCB0aXBvIGNvbmNyZXRvIGRlbCBvYmpldG8gcXVlIHJlY2liZSBsYSBsbGFtYWRhLg==' || CHR(10) ||
        'U2lndWUgZWwgbcOpdG9kbyBxdWUgZ2VuZXJhIGVsIG1lbnNhamUgYW50ZXMgZGUgYXBsaWNhciBsYSB0cmFuc2Zvcm1hY2nDs24u' || CHR(10) ||
        'RGlzdGluZ3VlIGVsIG1lbnNhamUgb3JpZ2luYWwgZGVsIHJlc3VsdGFkbyBkZSBjb252ZXJ0aXJsbyBhIG1hecO6c2N1bGFzLg==' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=');

    create_final(4, 11, 'Técnica de Dos Punteros (Two Pointers) sobre Secuencias',
        'Reto final · Buscador de coincidencias', TO_CLOB('Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto. Evalúa solve("5") en la clase mostrada.'),
        TO_CLOB('public class FinalModuleSolver {') || CHR(10) ||
        '    public static String solve(String input) {' || CHR(10) ||
        '        int[] values = {2, 5, 5, 9, 12};' || CHR(10) ||
        '        int target = Integer.parseInt(input);' || CHR(10) ||
        '        int low = 0, high = values.length - 1, found = -1;' || CHR(10) ||
        '        while (low <= high) {' || CHR(10) ||
        '            int mid = low + (high - low) / 2;' || CHR(10) ||
        '            if (values[mid] >= target) {' || CHR(10) ||
        '                if (values[mid] == target) found = mid;' || CHR(10) ||
        '                high = mid - 1;' || CHR(10) ||
        '            } else {' || CHR(10) ||
        '                low = mid + 1;' || CHR(10) ||
        '            }' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return Integer.toString(found);' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'Mg==' || CHR(10) ||
        'NQ==' || CHR(10) ||
        'MQ==' || CHR(10) ||
        'LTE=', '1',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgYXJyZWdsbyBjb250aWVuZSBkb3MgYXBhcmljaW9uZXMgZGVsIG9iamV0aXZvLg==' || CHR(10) ||
        'QW5vdGEgbG93LCBoaWdoLCBtaWQgeSBlbCBjYW5kaWRhdG8gZW5jb250cmFkbyBkZXNwdcOpcyBkZSBjYWRhIGl0ZXJhY2nDs24u' || CHR(10) ||
        'T2JzZXJ2YSBxdWUgZW5jb250cmFyIGVsIG9iamV0aXZvIG11ZXZlIGhpZ2ggaGFjaWEgbGEgaXpxdWllcmRhIHkgY29uc2VydmEgZWwgY2FuZGlkYXRvLg==' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu');

    create_final(5, 12, 'Tablas Hash (HashSet / HashMap) y Detección en O(n)',
        'Reto final · Informe de valores únicos', TO_CLOB('Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total. Evalúa solve("5") en la clase mostrada.'),
        TO_CLOB('import java.util.HashSet;') || CHR(10) ||
        'public class FinalModuleSolver {' || CHR(10) ||
        '    public static String solve(String input) {' || CHR(10) ||
        '        int[] values = {2, 2, 3, 4, 4, 6};' || CHR(10) ||
        '        var seen = new HashSet<Integer>();' || CHR(10) ||
        '        int total = 0;' || CHR(10) ||
        '        for (int value : values) {' || CHR(10) ||
        '            if (seen.add(value) && value % 2 == 0) total += value;' || CHR(10) ||
        '        }' || CHR(10) ||
        '        return Integer.toString(total);' || CHR(10) ||
        '    }' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'MTg=' || CHR(10) ||
        'Mw==' || CHR(10) ||
        'MjE=' || CHR(10) ||
        'MTI=', '12',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgY29uanVudG8gcmVjdWVyZGEgbG9zIHZhbG9yZXMgeWEgcHJvY2VzYWRvcy4=' || CHR(10) ||
        'VW4gdmFsb3IgcmVwZXRpZG8gbm8gZGViZSB2b2x2ZXIgYSBlbnRyYXIgZW4gZWwgYWN1bXVsYWRvci4=' || CHR(10) ||
        'U2VwYXJhIGxvcyBwYXJlcyBkaXN0aW50b3MgeSBjYWxjdWxhIHN1IHN1bWEu' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=');

    create_final(2, 13, 'Modelo de Datos de Python y Métodos Especiales (Dunder)',
        'Reto final · Servicio de mensajes', TO_CLOB('Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado. Selecciona lo que imprime el programa.'),
        TO_CLOB('from abc import ABC, abstractmethod') || CHR(10) ||
        'class Message(ABC):' || CHR(10) ||
        '    @abstractmethod' || CHR(10) ||
        '    def text(self): pass' || CHR(10) ||
        '    def __str__(self): return self.text()' || CHR(10) ||
        'class Greeting(Message):' || CHR(10) ||
        '    def text(self): return "Hola"' || CHR(10) ||
        'message = Greeting()' || CHR(10) ||
        'print(str(message).upper())' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SG9sYQ==' || CHR(10) ||
        'SE9MQQ==' || CHR(10) ||
        'RXJyb3IgZGUgdGlwbw==' || CHR(10) ||
        'TWVuc2FqZQ==', 'HOLA',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBlbCB0aXBvIGNvbmNyZXRvIGRlbCBvYmpldG8gcXVlIHJlY2liZSBsYSBsbGFtYWRhLg==' || CHR(10) ||
        'U2lndWUgZWwgbcOpdG9kbyBxdWUgZ2VuZXJhIGVsIG1lbnNhamUgYW50ZXMgZGUgYXBsaWNhciBsYSB0cmFuc2Zvcm1hY2nDs24u' || CHR(10) ||
        'RGlzdGluZ3VlIGVsIG1lbnNhamUgb3JpZ2luYWwgZGVsIHJlc3VsdGFkbyBkZSBjb252ZXJ0aXJsbyBhIG1hecO6c2N1bGFzLg==' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=');

    create_final(6, 14, 'Slicing Avanzado e Inversión de Secuencias en Python',
        'Reto final · Buscador de coincidencias', TO_CLOB('Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto. Selecciona lo que imprime el programa.'),
        TO_CLOB('values = [2, 5, 5, 9, 12]') || CHR(10) ||
        'target = 5' || CHR(10) ||
        'low, high, found = 0, len(values) - 1, -1' || CHR(10) ||
        'while low <= high:' || CHR(10) ||
        '    mid = (low + high) // 2' || CHR(10) ||
        '    if values[mid] >= target:' || CHR(10) ||
        '        if values[mid] == target: found = mid' || CHR(10) ||
        '        high = mid - 1' || CHR(10) ||
        '    else:' || CHR(10) ||
        '        low = mid + 1' || CHR(10) ||
        'print(found)' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'Mg==' || CHR(10) ||
        'NQ==' || CHR(10) ||
        'MQ==' || CHR(10) ||
        'LTE=', '1',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgYXJyZWdsbyBjb250aWVuZSBkb3MgYXBhcmljaW9uZXMgZGVsIG9iamV0aXZvLg==' || CHR(10) ||
        'QW5vdGEgbG93LCBoaWdoLCBtaWQgeSBlbCBjYW5kaWRhdG8gZW5jb250cmFkbyBkZXNwdcOpcyBkZSBjYWRhIGl0ZXJhY2nDs24u' || CHR(10) ||
        'T2JzZXJ2YSBxdWUgZW5jb250cmFyIGVsIG9iamV0aXZvIG11ZXZlIGhpZ2ggaGFjaWEgbGEgaXpxdWllcmRhIHkgY29uc2VydmEgZWwgY2FuZGlkYXRvLg==' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu');

    create_final(7, 15, 'Conjuntos (set) y Diccionarios (dict) en Tiempo O(1)',
        'Reto final · Informe de valores únicos', TO_CLOB('Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total. Selecciona lo que imprime el programa.'),
        TO_CLOB('values = [2, 2, 3, 4, 4, 6]') || CHR(10) ||
        'seen = set()' || CHR(10) ||
        'total = 0' || CHR(10) ||
        'for value in values:' || CHR(10) ||
        '    if value not in seen:' || CHR(10) ||
        '        seen.add(value)' || CHR(10) ||
        '        if value % 2 == 0: total += value' || CHR(10) ||
        'print(total)' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'MTg=' || CHR(10) ||
        'Mw==' || CHR(10) ||
        'MjE=' || CHR(10) ||
        'MTI=', '12',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgY29uanVudG8gcmVjdWVyZGEgbG9zIHZhbG9yZXMgeWEgcHJvY2VzYWRvcy4=' || CHR(10) ||
        'VW4gdmFsb3IgcmVwZXRpZG8gbm8gZGViZSB2b2x2ZXIgYSBlbnRyYXIgZW4gZWwgYWN1bXVsYWRvci4=' || CHR(10) ||
        'U2VwYXJhIGxvcyBwYXJlcyBkaXN0aW50b3MgeSBjYWxjdWxhIHN1IHN1bWEu' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=');

    create_final(3, 16, 'Determinismo RAII y Punteros Inteligentes (std::unique_ptr)',
        'Reto final · Servicio de mensajes', TO_CLOB('Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado. Selecciona lo que imprime el programa.'),
        TO_CLOB('#include <iostream>') || CHR(10) ||
        '#include <memory>' || CHR(10) ||
        '#include <string>' || CHR(10) ||
        '#include <cctype>' || CHR(10) ||
        'struct Message {' || CHR(10) ||
        '    virtual std::string text() const = 0;' || CHR(10) ||
        '    virtual ~Message() = default;' || CHR(10) ||
        '};' || CHR(10) ||
        'struct Greeting : Message {' || CHR(10) ||
        '    std::string text() const override { return "Hola"; }' || CHR(10) ||
        '};' || CHR(10) ||
        'int main() {' || CHR(10) ||
        '    std::unique_ptr<Message> message = std::make_unique<Greeting>();' || CHR(10) ||
        '    std::string text = message->text();' || CHR(10) ||
        '    for (char& value : text) value = std::toupper(static_cast<unsigned char>(value));' || CHR(10) ||
        '    std::cout << text;' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SG9sYQ==' || CHR(10) ||
        'SE9MQQ==' || CHR(10) ||
        'RXJyb3IgZGUgdGlwbw==' || CHR(10) ||
        'TWVuc2FqZQ==', 'HOLA',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'SWRlbnRpZmljYSBlbCB0aXBvIGNvbmNyZXRvIGRlbCBvYmpldG8gcXVlIHJlY2liZSBsYSBsbGFtYWRhLg==' || CHR(10) ||
        'U2lndWUgZWwgbcOpdG9kbyBxdWUgZ2VuZXJhIGVsIG1lbnNhamUgYW50ZXMgZGUgYXBsaWNhciBsYSB0cmFuc2Zvcm1hY2nDs24u' || CHR(10) ||
        'RGlzdGluZ3VlIGVsIG1lbnNhamUgb3JpZ2luYWwgZGVsIHJlc3VsdGFkbyBkZSBjb252ZXJ0aXJsbyBhIG1hecO6c2N1bGFzLg==' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'RWwgb2JqZXRvIGNvbmNyZXRvIHByb2R1Y2UgSG9sYSBhIHRyYXbDqXMgZGVsIGNvbnRyYXRvLiBMYSB0cmFuc2Zvcm1hY2nDs24gcG9zdGVyaW9yIGRldnVlbHZlIEhPTEEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbCBvYmpldG8gY29uY3JldG8gcHJvZHVjZSBIb2xhIGEgdHJhdsOpcyBkZWwgY29udHJhdG8uIExhIHRyYW5zZm9ybWFjacOzbiBwb3N0ZXJpb3IgZGV2dWVsdmUgSE9MQS4=');

    create_final(8, 17, 'Dos Punteros e Iteradores Bidireccionales en std::string',
        'Reto final · Buscador de coincidencias', TO_CLOB('Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto. Selecciona lo que imprime el programa.'),
        TO_CLOB('#include <iostream>') || CHR(10) ||
        '#include <vector>' || CHR(10) ||
        'int firstMatch(const std::vector<int>& values, int target) {' || CHR(10) ||
        '    int low = 0, high = static_cast<int>(values.size()) - 1, found = -1;' || CHR(10) ||
        '    while (low <= high) {' || CHR(10) ||
        '        int mid = low + (high - low) / 2;' || CHR(10) ||
        '        if (values[mid] >= target) {' || CHR(10) ||
        '            if (values[mid] == target) found = mid;' || CHR(10) ||
        '            high = mid - 1;' || CHR(10) ||
        '        } else low = mid + 1;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    return found;' || CHR(10) ||
        '}' || CHR(10) ||
        'int main() { std::cout << firstMatch({2, 5, 5, 9, 12}, 5); }' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'Mg==' || CHR(10) ||
        'NQ==' || CHR(10) ||
        'MQ==' || CHR(10) ||
        'LTE=', '1',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgYXJyZWdsbyBjb250aWVuZSBkb3MgYXBhcmljaW9uZXMgZGVsIG9iamV0aXZvLg==' || CHR(10) ||
        'QW5vdGEgbG93LCBoaWdoLCBtaWQgeSBlbCBjYW5kaWRhdG8gZW5jb250cmFkbyBkZXNwdcOpcyBkZSBjYWRhIGl0ZXJhY2nDs24u' || CHR(10) ||
        'T2JzZXJ2YSBxdWUgZW5jb250cmFyIGVsIG9iamV0aXZvIG11ZXZlIGhpZ2ggaGFjaWEgbGEgaXpxdWllcmRhIHkgY29uc2VydmEgZWwgY2FuZGlkYXRvLg==' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu' || CHR(10) ||
        'RW5jb250cmFyIHVuIDUgY29uc2VydmEgdW4gY2FuZGlkYXRvIHkgY29udGluw7phIGhhY2lhIGxhIGl6cXVpZXJkYS4gRWwgcHJpbWVyIDUgZXN0w6EgZW4gZWwgw61uZGljZSAxLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBFbmNvbnRyYXIgdW4gNSBjb25zZXJ2YSB1biBjYW5kaWRhdG8geSBjb250aW7DumEgaGFjaWEgbGEgaXpxdWllcmRhLiBFbCBwcmltZXIgNSBlc3TDoSBlbiBlbCDDrW5kaWNlIDEu');

    create_final(9, 18, 'Tablas Hash en STL (std::unordered_set) en O(1) Promedio',
        'Reto final · Informe de valores únicos', TO_CLOB('Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total. Selecciona lo que imprime el programa.'),
        TO_CLOB('#include <iostream>') || CHR(10) ||
        '#include <vector>' || CHR(10) ||
        '#include <unordered_set>' || CHR(10) ||
        'int main() {' || CHR(10) ||
        '    std::vector<int> values = {2, 2, 3, 4, 4, 6};' || CHR(10) ||
        '    std::unordered_set<int> seen;' || CHR(10) ||
        '    int total = 0;' || CHR(10) ||
        '    for (int value : values) {' || CHR(10) ||
        '        if (seen.insert(value).second && value % 2 == 0) total += value;' || CHR(10) ||
        '    }' || CHR(10) ||
        '    std::cout << total;' || CHR(10) ||
        '}' || CHR(10) ||
        '',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'MTg=' || CHR(10) ||
        'Mw==' || CHR(10) ||
        'MjE=' || CHR(10) ||
        'MTI=', '12',
        TO_CLOB('LEVELUP_OPTIONS_V2') || CHR(10) ||
        'RWwgY29uanVudG8gcmVjdWVyZGEgbG9zIHZhbG9yZXMgeWEgcHJvY2VzYWRvcy4=' || CHR(10) ||
        'VW4gdmFsb3IgcmVwZXRpZG8gbm8gZGViZSB2b2x2ZXIgYSBlbnRyYXIgZW4gZWwgYWN1bXVsYWRvci4=' || CHR(10) ||
        'U2VwYXJhIGxvcyBwYXJlcyBkaXN0aW50b3MgeSBjYWxjdWxhIHN1IHN1bWEu' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'UmV2aXNhIGPDs21vIHNlIG9idGllbmUgZWwgcmVzdWx0YWRvLiBMb3MgdmFsb3JlcyBwYXJlcyBkaXN0aW50b3Mgc29uIDIsIDQgeSA2LiBDYWRhIHVubyBjb250cmlidXllIHVuYSBzb2xhIHZleiB5IGVsIHRvdGFsIGVzIDEyLg==' || CHR(10) ||
        'TG9zIHZhbG9yZXMgcGFyZXMgZGlzdGludG9zIHNvbiAyLCA0IHkgNi4gQ2FkYSB1bm8gY29udHJpYnV5ZSB1bmEgc29sYSB2ZXogeSBlbCB0b3RhbCBlcyAxMi4=');
    DBMS_OUTPUT.PUT_LINE('Retos finales creados: ' || v_created);
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK TO module_finals;
    RAISE;
END;
/

-- =============================================================================
-- FASE 15 - PRUEBAS PL/SQL
-- Fuente enlazada: database/15_expanded_curriculum.sql
-- @@ resuelve la ruta con respecto a este script; conserva 15 como migración idempotente reutilizable.
@@15_expanded_curriculum.sql

-- Fuente integrada: database/04_test_plsql.sql
-- =============================================================================
PROMPT FASE 15 - PRUEBAS PL/SQL

-- =============================================================================
-- LEVELUP CODE — BATERÍA DE PRUEBAS PL/SQL EN BLOQUE ANÓNIMO
-- Asignatura: Bases de Datos & Programación III — Universidad Popular del Cesar
-- Autor: Misael Andrés Maestre Acosta (Tech Lead)
--
-- Propósito:
--   Ejecutar una suite completa de pruebas unitarias sobre el paquete
--   PKG_GAMIFICACION y el disparador TRG_AUDIT_USUARIOS, demostrando el uso de:
--     - Bloques anónimos (DECLARE ... BEGIN ... EXCEPTION ... END;)
--     - Cursores explícitos y estructuras de control (IF, CASE, FOR LOOP)
--     - Invocación de funciones y procedimientos con parámetros IN y OUT
--     - Control transaccional (SAVEPOINT / ROLLBACK)
--     - Captura y validación de excepciones (RAISE_APPLICATION_ERROR)
--     - Salida formateada por consola con DBMS_OUTPUT
--
-- Ejecución:
--   En Oracle SQL Developer o DBeaver, asegúrate de activar la consola:
--     SET SERVEROUTPUT ON SIZE UNLIMITED;
--   Y ejecutar este script con la conexión: LEVELUP_ADMIN
-- =============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;

DECLARE
    -- Variables para almacenar resultados de funciones y parámetros OUT
    v_nivel_calc       NUMBER;
    v_racha_calc       NUMBER;
    v_porcentaje_calc  NUMBER;

    v_nuevo_nivel      NUMBER;
    v_nueva_exp        NUMBER;
    v_vidas_restantes  NUMBER;
    v_exp_otorgada     NUMBER;
    v_id_intento       NUMBER;
    v_id_original      NUMBER;
    v_request_id       VARCHAR2(64);
    v_vidas_rest       NUMBER;

    -- Estado inicial y posterior al ROLLBACK para verificar aislamiento transaccional
    v_exp_inicial      NUMBER := 0;
    v_vidas_iniciales  NUMBER := 0;
    v_exp_post         NUMBER := 0;
    v_vidas_post       NUMBER := 0;

    -- Contadores de pruebas
    v_total_pruebas    NUMBER := 0;
    v_pruebas_exito    NUMBER := 0;

    -- Cursor explícito para auditar estudiantes de prueba
    CURSOR c_estudiantes IS
        SELECT u.id_usuario, u.primer_nombre, u.correo_electronico, u.exp_total, u.nivel, u.vidas
        FROM Usuarios u
        WHERE u.id_rol = 1
        FETCH FIRST 3 ROWS ONLY;

    -- Procedimiento local auxiliar dentro del bloque anónimo para registrar resultados
    PROCEDURE REGISTRAR_TEST(
        p_nombre_test IN VARCHAR2,
        p_condicion   IN BOOLEAN,
        p_detalle     IN VARCHAR2
    ) IS
    BEGIN
        v_total_pruebas := v_total_pruebas + 1;
        IF p_condicion THEN
            v_pruebas_exito := v_pruebas_exito + 1;
            DBMS_OUTPUT.PUT_LINE('  [PASS] ' || RPAD(p_nombre_test, 38) || ' | ' || p_detalle);
        ELSE
            DBMS_OUTPUT.PUT_LINE('  [FAIL] ' || RPAD(p_nombre_test, 38) || ' | ' || p_detalle);
        END IF;
    END REGISTRAR_TEST;

BEGIN
    DBMS_OUTPUT.PUT_LINE('================================================================================');
    DBMS_OUTPUT.PUT_LINE('     LEVELUP CODE — SUITE DE PRUEBAS UNITARIAS PL/SQL (ORACLE 18c XE)          ');
    DBMS_OUTPUT.PUT_LINE('================================================================================');
    DBMS_OUTPUT.PUT_LINE('Fecha de ejecución: ' || TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    -- Capturar estado previo del estudiante #1 antes de iniciar las pruebas
    SELECT exp_total, vidas
    INTO v_exp_inicial, v_vidas_iniciales
    FROM Usuarios
    WHERE id_usuario = 1;

    -- Punto de restauración para dejar la base de datos intacta tras las pruebas
    SAVEPOINT sp_inicio_pruebas;

    -- =========================================================================
    -- CASO 1: PRUEBAS UNITARIAS DE LA FUNCIÓN FN_CALCULAR_NIVEL
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('1. PRUEBAS DE LA FUNCIÓN: PKG_GAMIFICACION.FN_CALCULAR_NIVEL');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(0);
    REGISTRAR_TEST('Nivel con 0 XP', v_nivel_calc = 1, 'Esperado: 1, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(299);
    REGISTRAR_TEST('Nivel con 299 XP (umbral)', v_nivel_calc = 1, 'Esperado: 1, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(300);
    REGISTRAR_TEST('Nivel con 300 XP (salto)', v_nivel_calc = 2, 'Esperado: 2, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(1450);
    REGISTRAR_TEST('Nivel con 1450 XP', v_nivel_calc = 5, 'Esperado: 5, Obtenido: ' || v_nivel_calc);

    v_nivel_calc := PKG_GAMIFICACION.FN_CALCULAR_NIVEL(NULL);
    REGISTRAR_TEST('Manejo de valor NULL', v_nivel_calc = 1, 'Esperado: 1, Obtenido: ' || v_nivel_calc);

    -- =========================================================================
    -- CASO 2: PRUEBAS DE LA FUNCIÓN FN_PORCENTAJE_PROGRESO
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('2. PRUEBAS DE LA FUNCIÓN: PKG_GAMIFICACION.FN_PORCENTAJE_PROGRESO');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    -- Lenguaje 1 = Java
    v_porcentaje_calc := PKG_GAMIFICACION.FN_PORCENTAJE_PROGRESO(1, 1);
    REGISTRAR_TEST('Porcentaje Java Estudiante #1', v_porcentaje_calc >= 0 AND v_porcentaje_calc <= 100,
                   'Porcentaje obtenido: ' || v_porcentaje_calc || '%');

    -- Lenguaje inexistente
    v_porcentaje_calc := PKG_GAMIFICACION.FN_PORCENTAJE_PROGRESO(1, 999);
    REGISTRAR_TEST('Lenguaje inexistente (999)', v_porcentaje_calc = 0,
                   'Esperado: 0%, Obtenido: ' || v_porcentaje_calc || '%');

    -- =========================================================================
    -- CASO 3: PROCEDIMIENTO SP_REGISTRAR_INTENTO_RETO (INTENTO EXITOSO)
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('3. PROCEDIMIENTO: PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO (EXITOSO)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    v_request_id := RAWTOHEX(SYS_GUID());
    PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         => 1,
        p_id_reto            => 101,
        p_es_correcto        => 1,
        p_codigo_enviado     => 'public class SolucionPrueba { /* código correcto */ }',
        p_salida_obtenida    => 'Aprobado: 3/3 casos',
        p_tiempo_ms          => 32,
        p_id_solicitud       => v_request_id,
        p_nuevo_nivel        => v_nuevo_nivel,
        p_nueva_exp          => v_nueva_exp,
        p_vidas_restantes    => v_vidas_restantes,
        p_exp_otorgada       => v_exp_otorgada,
        p_id_intento_creado  => v_id_intento
    );

    REGISTRAR_TEST('Generación de ID Intento', v_id_intento > 0, 'ID Intento generado: #' || v_id_intento);
    REGISTRAR_TEST('Integridad de Vidas', v_vidas_restantes = 5, 'Vidas tras acierto: ' || v_vidas_restantes);
    REGISTRAR_TEST('Cálculo reactivo de nivel', v_nuevo_nivel >= 5, 'Nivel tras envío: ' || v_nuevo_nivel);
    v_id_original := v_id_intento;

    PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario => 1, p_id_reto => 101, p_es_correcto => 1,
        p_codigo_enviado => 'reintento seguro', p_salida_obtenida => 'CORRECT', p_tiempo_ms => 32,
        p_id_solicitud => v_request_id,
        p_nuevo_nivel => v_nuevo_nivel, p_nueva_exp => v_nueva_exp,
        p_vidas_restantes => v_vidas_restantes, p_exp_otorgada => v_exp_otorgada,
        p_id_intento_creado => v_id_intento
    );
    REGISTRAR_TEST('Reintento idempotente', v_id_intento = v_id_original,
                   'ID original/reintento: ' || v_id_original || '/' || v_id_intento);

    -- =========================================================================
    -- CASO 4: PROCEDIMIENTO SP_REGISTRAR_INTENTO_RETO (INTENTO FALLIDO)
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('4. PROCEDIMIENTO: PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO (FALLIDO)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
        p_id_usuario         => 1,
        p_id_reto            => 101,
        p_es_correcto        => 0,
        p_codigo_enviado     => 'public class SolucionErronea { /* error */ }',
        p_salida_obtenida    => 'INCORRECT',
        p_tiempo_ms          => 18,
        p_id_solicitud       => RAWTOHEX(SYS_GUID()),
        p_nuevo_nivel        => v_nuevo_nivel,
        p_nueva_exp          => v_nueva_exp,
        p_vidas_restantes    => v_vidas_restantes,
        p_exp_otorgada       => v_exp_otorgada,
        p_id_intento_creado  => v_id_intento
    );

    REGISTRAR_TEST('Descuento de 1 vida por fallo', v_vidas_restantes = 4, 'Vidas tras fallo: ' || v_vidas_restantes);
    REGISTRAR_TEST('Cero XP otorgada en fallo', v_exp_otorgada = 0, 'XP otorgada: ' || v_exp_otorgada);

    -- =========================================================================
    -- CASO 5: PROCEDIMIENTO SP_RECUPERAR_VIDAS
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('5. PROCEDIMIENTO: PKG_GAMIFICACION.SP_RECUPERAR_VIDAS');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_RECUPERAR_VIDAS(
        p_id_usuario        => 1,
        p_vidas_restauradas => v_vidas_rest
    );

    REGISTRAR_TEST('Sin restauración mientras aún quedan vidas', v_vidas_rest = 4, 'Vidas restantes: ' || v_vidas_rest);

    UPDATE Usuarios SET vidas = 0 WHERE id_usuario = 1;
    PKG_GAMIFICACION.SP_RECUPERAR_VIDAS(
        p_id_usuario => 1,
        p_vidas_restauradas => v_vidas_rest
    );
    REGISTRAR_TEST('Sin recuperación el mismo día', v_vidas_rest = 0,
                   'Vidas tras intentar recuperar: ' || v_vidas_rest);

    BEGIN
        PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
            p_id_usuario => 1, p_id_reto => 101, p_es_correcto => 1,
            p_codigo_enviado => 'test', p_salida_obtenida => 'test', p_tiempo_ms => 10,
            p_id_solicitud => RAWTOHEX(SYS_GUID()),
            p_nuevo_nivel => v_nuevo_nivel, p_nueva_exp => v_nueva_exp,
            p_vidas_restantes => v_vidas_restantes, p_exp_otorgada => v_exp_otorgada,
            p_id_intento_creado => v_id_intento
        );
        REGISTRAR_TEST('Bloqueo de intento sin vidas', FALSE, 'No debió guardar el intento');
    EXCEPTION
        WHEN OTHERS THEN
            REGISTRAR_TEST('Bloqueo de intento sin vidas', SQLCODE = -20004,
                           'Código esperado: -20004 | Error: ' || SQLERRM);
    END;
    UPDATE Usuarios SET vidas = 4 WHERE id_usuario = 1;

    -- =========================================================================
    -- CASO 6: PROCEDIMIENTO SP_COMPLETAR_LECCION
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('6. PROCEDIMIENTO: PKG_GAMIFICACION.SP_COMPLETAR_LECCION');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    PKG_GAMIFICACION.SP_COMPLETAR_LECCION(
        p_id_usuario  => 1,
        p_id_leccion  => 2,
        p_xp_otorgada => v_exp_otorgada,
        p_nuevo_nivel => v_nuevo_nivel
    );

    REGISTRAR_TEST('Lección completada con éxito', v_nuevo_nivel >= 5,
                   'XP otorgada: +' || v_exp_otorgada || ' | Nivel actual: ' || v_nuevo_nivel);

    -- =========================================================================
    -- CASO 7: MANEJO ROBUSTO DE EXCEPCIONES CONTROLADAS
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('7. PRUEBA DE DISPARO Y CAPTURA DE EXCEPCIONES PERSONALIZADAS');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    BEGIN
        -- Intentar registrar intento con un usuario inexistente (-999)
        PKG_GAMIFICACION.SP_REGISTRAR_INTENTO_RETO(
            p_id_usuario         => -999,
            p_id_reto            => 101,
            p_es_correcto        => 1,
            p_codigo_enviado     => 'test',
            p_salida_obtenida    => 'test',
            p_tiempo_ms          => 10,
            p_id_solicitud       => RAWTOHEX(SYS_GUID()),
            p_nuevo_nivel        => v_nuevo_nivel,
            p_nueva_exp          => v_nueva_exp,
            p_vidas_restantes    => v_vidas_restantes,
            p_exp_otorgada       => v_exp_otorgada,
            p_id_intento_creado  => v_id_intento
        );
        REGISTRAR_TEST('Excepción Usuario Inexistente', FALSE, 'No debió permitir la operación');
    EXCEPTION
        WHEN OTHERS THEN
            REGISTRAR_TEST('Excepción Usuario Inexistente', SQLCODE = -20002,
                           'Código esperado: -20002 | Error: ' || SQLERRM);
    END;

    -- =========================================================================
    -- CASO 8: RECORRIDO DE CURSOR EXPLÍCITO Y AUDITORÍA
    -- =========================================================================
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('8. AUDITORÍA MEDIANTE CURSOR EXPLÍCITO (ESTUDIANTES REGISTRADOS)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');

    FOR est IN c_estudiantes LOOP
        DBMS_OUTPUT.PUT_LINE('  ID: #' || LPAD(est.id_usuario, 2, '0')
                             || ' | Nombre: ' || RPAD(est.primer_nombre, 14)
                             || ' | Correo: ' || RPAD(est.correo_electronico, 26)
                             || ' | Nivel: ' || est.nivel
                             || ' | XP: ' || est.exp_total
                             || ' | Vidas: ' || est.vidas);
    END LOOP;

    -- Deshacer cambios de la prueba para mantener la base de datos en su estado inicial
    ROLLBACK TO sp_inicio_pruebas;

    -- Verificar que el ROLLBACK restauró exactamente los datos previos del estudiante #1
    SELECT exp_total, vidas
    INTO v_exp_post, v_vidas_post
    FROM Usuarios
    WHERE id_usuario = 1;

    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('9. VERIFICACIÓN DE AISLAMIENTO TRANSACCIONAL (SAVEPOINT / ROLLBACK)');
    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');
    REGISTRAR_TEST('Restauración íntegra tras ROLLBACK',
                   v_exp_post = v_exp_inicial AND v_vidas_post = v_vidas_iniciales,
                   'XP inicial/final: ' || v_exp_inicial || '/' || v_exp_post
                   || ' | Vidas: ' || v_vidas_iniciales || '/' || v_vidas_post);

    DBMS_OUTPUT.PUT_LINE('--------------------------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('RESUMEN DE PRUEBAS PL/SQL: ' || v_pruebas_exito || ' de ' || v_total_pruebas || ' pruebas superadas.');
    IF v_pruebas_exito = v_total_pruebas THEN
        DBMS_OUTPUT.PUT_LINE('ESTADO FINAL: [APROBADO] — Todos los objetos operan al 100% sin alterar datos.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('ESTADO FINAL: [REVISAR] — Algunas pruebas fallaron.');
    END IF;
    DBMS_OUTPUT.PUT_LINE('================================================================================');

EXCEPTION
    WHEN OTHERS THEN
        BEGIN
            ROLLBACK TO sp_inicio_pruebas;
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
        END;
        DBMS_OUTPUT.PUT_LINE('ERROR CRÍTICO NO CONTROLADO EN EL BLOQUE ANÓNIMO: ' || SQLERRM);
END;
/

COMMIT;

PROMPT ============================================================
PROMPT LEVELUP CODE - RECONSTRUCCION Y VALIDACION FINALIZADAS
PROMPT ============================================================
