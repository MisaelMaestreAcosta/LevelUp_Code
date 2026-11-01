-- =============================================================================
-- LEVELUP CODE - DATOS SEMILLA INICIALES (18 TABLAS - ORACLE SQL)
-- Incluye roles, cuentas con hash PBKDF2-HMAC-SHA256 (120k iteraciones),
-- lenguajes (Java, Python, C++), módulos, lecciones, retos de código y quiz,
-- casos de prueba visibles/ocultos, logros polimórficos e historial inicial.
-- Ejecutar después de 01_schema.sql en la conexión: level_up_admin
-- =============================================================================

-- 1. ROLES
INSERT INTO Roles (id_rol, nombre_rol, descripcion)
VALUES (1, 'Estudiante', 'Acceso al catálogo de lenguajes, lecciones comparativas, laboratorio de retos y logros.');

INSERT INTO Roles (id_rol, nombre_rol, descripcion)
VALUES (2, 'Administrador', 'Gestión integral de roles, lenguajes, módulos, lecciones, retos y logros.');

-- 2. USUARIOS (Hashes PBKDF2-HMAC-SHA256 verificables por UserService)
-- Contraseña Estudiante: estudiante123
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

-- Contraseña Admin: admin12345
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

-- 3. ACTIVIDAD_DIARIA (Últimos 7 días de racha para el Estudiante)
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 6, 120);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 5, 180);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 4, 90);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 3, 210);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 2, 150);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE) - 1, 240);
INSERT INTO Actividad_Diaria (id_usuario, fecha, exp_ganada) VALUES (1, TRUNC(SYSDATE), 160);

-- 4. LENGUAJES
INSERT INTO Lenguajes (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (1, 'Java', 'POO, Jerarquías ES-UN, Principios SOLID y Colecciones Tipadas en JVM (Java 21 LTS).', 'java-icon', 1);

INSERT INTO Lenguajes (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (2, 'Python', 'Estructuras Dinámicas, Comprensiones, Tipado Gradual y Algoritmos (Python 3.12).', 'python-icon', 1);

INSERT INTO Lenguajes (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (3, 'C++', 'Gestión Determinista de Memoria (RAII), Referencias Constantes y STL (ISO C++ 20).', 'cpp-icon', 1);

-- 5. INSCRIPCIONES
INSERT INTO Inscripciones (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (1, 1, TRUNC(SYSDATE) - 25);
INSERT INTO Inscripciones (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (1, 2, TRUNC(SYSDATE) - 18);
INSERT INTO Inscripciones (id_usuario, id_lenguaje, fecha_inscripcion) VALUES (1, 3, TRUNC(SYSDATE) - 10);

-- 6. MODULOS
INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (1, 1, 'Arquitectura Orientada a Objetos', 'Abstracción, herencia, polimorfismo y despacho dinámico en Java.', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (2, 1, 'Estructuras Lineales y Búsqueda', 'Arreglos, listas dinámicas y búsqueda binaria O(log n).', 2);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (3, 2, 'Algoritmia y Secuencias en Python', 'Listas dinámicas, slicing, búsqueda binaria y protocolos ABC.', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (4, 3, 'Memoria, Referencias y Contenedores STL', 'Paso por referencia constante, constructores y std::vector.', 1);

-- 7. LECCIONES
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    1, 1,
    'Herencia, Clases Abstractas y Despacho Dinámico',
    '1. JERARQUÍAS ES-UN Y CONTRATOS DE COMPORTAMIENTO' || CHR(10) ||
    'Las clases abstractas permiten factorizar estado y comportamiento común mientras delegan operaciones específicas a las subclases mediante métodos abstractos.' || CHR(10) || CHR(10) ||
    '2. POLIMORFISMO EN JAVA VS PYTHON VS C++' || CHR(10) ||
    '• En Java, todo método no final ni static se enlaza dinámicamente en tiempo de ejecución.' || CHR(10) ||
    '• En C++, el despacho dinámico exige declarar el método como virtual en la clase base.' || CHR(10) ||
    '• En Python, el protocolo se resuelve en tiempo de ejecución mediante Duck Typing o abc.ABC.',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    2, 1,
    'Principios SOLID y Desacoplamiento por Capas',
    '1. INVERSIÓN DE DEPENDENCIAS (DIP)' || CHR(10) ||
    'Los módulos de alto nivel (Servicios de Negocio) no deben depender de implementaciones concretas de datos (Oracle JDBC), sino de abstracciones (Interfaces Repository).' || CHR(10) || CHR(10) ||
    '2. ABIERTO / CERRADO (OCP)' || CHR(10) ||
    'Las entidades de software deben estar abiertas para su extensión mediante polimorfismo pero cerradas para su modificación directa.',
    2, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    3, 2,
    'Búsqueda Binaria y Prevención de Overflow',
    'La búsqueda binaria divide el espacio de búsqueda a la mitad en cada iteración logrando complejidad O(log n).' || CHR(10) ||
    'Calcular el punto medio como mid = low + (high - low) / 2 evita el desbordamiento aritmético que ocurre con (low + high) / 2 en arreglos masivos.',
    1, 80
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    4, 3,
    'Complejidad Temporal y Búsqueda Binaria en Python',
    'En Python, las listas son arreglos dinámicos de referencias en memoria contigua. El operador // realiza división entera truncada hacia abajo sin riesgo de desbordamiento porque int tiene precisión arbitraria.',
    1, 65
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    5, 4,
    'Paso por Valor vs Referencia Constante (const T&)',
    'En C++, pasar un std::vector por valor copia todos sus elementos en memoria O(n). Usar const std::vector<int>& evita la copia en memoria y garantiza inmutabilidad durante la función.',
    1, 80
);

-- 8. PROGRESO_LECCION
INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 1, 'COMPLETADA', SYSTIMESTAMP - INTERVAL '3' DAY);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 2, 'EN_CURSO', NULL);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 3, 'PENDIENTE', NULL);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 4, 'COMPLETADA', SYSTIMESTAMP - INTERVAL '1' DAY);

INSERT INTO Progreso_Leccion (id_usuario, id_leccion, estado, fecha_completado)
VALUES (1, 5, 'EN_CURSO', NULL);

-- 9. RETOS (Especializados en Reto_Codigo y Reto_Quiz)
INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    101, 1,
    'Polimorfismo: Despacho Dinámico de Sonidos',
    'Implementa el método emitirSonido(String tipo) para que retorne "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato".',
    'CODIGO', 'FACIL', 60, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    102, 1,
    'Quiz Conceptual: Enlace Dinámico en POO',
    '¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) de la subclase en tiempo de ejecución?',
    'QUIZ', 'FACIL', 40, 2
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    103, 3,
    'Búsqueda Binaria Segura O(log n)',
    'Implementa search(int[] a, int target) retornando el índice del elemento o -1 si no se encuentra en el arreglo ordenado.',
    'CODIGO', 'MEDIO', 90, 1
);

-- 10. RETO_CODIGO
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    101,
    'public class AnimalSoundSolver {' || CHR(10) ||
    '    public static String emitirSonido(String tipo) {' || CHR(10) ||
    '        if ("Perro".equalsIgnoreCase(tipo)) {' || CHR(10) ||
    '            return "Guau";' || CHR(10) ||
    '        }' || CHR(10) ||
    '        // Completa el caso para Gato aquí' || CHR(10) ||
    '        return "";' || CHR(10) ||
    '    }' || CHR(10) ||
    '}',
    2000
);

INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    103,
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
    2000
);

-- 11. CASOS_PRUEBA
INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (1, 101, 'Perro', 'Guau', 0);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (2, 101, 'Gato', 'Miau', 0);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (3, 101, 'PERRO', 'Guau', 1);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (4, 103, '[2, 5, 8, 12, 16], target=12', '3', 0);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (5, 103, '[2, 5, 8, 12, 16], target=7', '-1', 1);

-- 12. RETO_QUIZ
INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    102,
    'Sobrecarga estática en tiempo de compilación (Overloading)|Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)|Conversión implícita de tipos primitivos en la pila|Duplicación de constructores en la clase base',
    'Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)'
);

-- 13. INTENTOS
INSERT INTO Intentos (
    id_intento, id_usuario, id_reto, codigo_enviado, salida_obtenida,
    es_correcto, tiempo_ejecucion_ms, exp_obtenida, fecha_intento
) VALUES (
    1, 1, 102,
    'Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)',
    'Respuesta correcta',
    1, 3, 40, SYSTIMESTAMP - INTERVAL '2' DAY
);

-- 14. LOGROS
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

-- 15. LOGRO_RACHA
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (1, 3);
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (2, 7);
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (6, 14);

-- 16. LOGRO_EXP
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (3, 1000);
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (4, 1500);

-- 17. LOGRO_RETOS
INSERT INTO Logro_Retos (id_logro, retos_requeridos) VALUES (5, 5);

-- 18. USUARIO_LOGRO
INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
VALUES (1, 1, SYSTIMESTAMP - INTERVAL '4' DAY);

INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
VALUES (1, 2, SYSTIMESTAMP - INTERVAL '1' DAY);

INSERT INTO Usuario_Logro (id_usuario, id_logro, fecha_desbloqueado)
VALUES (1, 3, SYSTIMESTAMP - INTERVAL '2' DAY);

-- Sincronizar secuencias IDENTITY después de inserciones con ID explícito
ALTER TABLE Roles            MODIFY id_rol      GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Usuarios         MODIFY id_usuario  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Actividad_Diaria MODIFY id_actividad GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Lenguajes        MODIFY id_lenguaje GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Modulos          MODIFY id_modulo   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Lecciones        MODIFY id_leccion  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Retos            MODIFY id_reto     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Casos_Prueba     MODIFY id_caso     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Intentos         MODIFY id_intento  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Logros           MODIFY id_logro    GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);

COMMIT;
