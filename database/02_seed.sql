-- =============================================================================
-- LEVELUP CODE - CATÁLOGO BASE DE DESPLIEGUE (ROLES, CURSOS, RETOS Y LOGROS)
-- Incluye roles, lenguajes (Java, Python, C++), módulos y lecciones espejo por
-- orden_secuencia, retos de código y quiz, casos de prueba y catálogo de logros.
-- NOTA: Las cuentas e historiales de demostración están aislados en
--       02b_seed_demo_users.sql para mantener limpio un despliegue normal.
-- Ejecutar después de 01_schema.sql en la conexión: LEVELUP_ADMIN
-- =============================================================================

SET DEFINE OFF;

-- 1. ROLES
INSERT INTO Roles (id_rol, nombre_rol, descripcion)
VALUES (1, 'Estudiante', 'Acceso al catálogo de lenguajes, lecciones comparativas, laboratorio de retos y logros.');

INSERT INTO Roles (id_rol, nombre_rol, descripcion)
VALUES (2, 'Administrador', 'Gestión integral de roles, lenguajes, módulos, lecciones, retos y logros.');

-- 2. LENGUAJES
INSERT INTO Lenguajes (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (1, 'Java', 'POO, Jerarquías ES-UN, Principios SOLID y Colecciones Tipadas en JVM (Java 21 LTS).', 'java-icon', 1);

INSERT INTO Lenguajes (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (2, 'Python', 'Estructuras Dinámicas, Comprensiones, Tipado Gradual y Algoritmos (Python 3.12).', 'python-icon', 1);

INSERT INTO Lenguajes (id_lenguaje, nombre, descripcion, icono_url, activo)
VALUES (3, 'C++', 'Gestión Determinista de Memoria (RAII), Referencias Constantes y STL (ISO C++ 20).', 'cpp-icon', 1);

-- 3. MODULOS (Alineados por orden_secuencia entre los 3 lenguajes)
INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (1, 1, 'Fundamentos Comparativos y POO en Java', 'Herencia, polimorfismo, colecciones y búsqueda algorítmica en JVM.', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (2, 2, 'Fundamentos Comparativos y POO en Python', 'Clases abstractas ABC, listas dinámicas y búsqueda binaria en Python.', 1);

INSERT INTO Modulos (id_modulo, id_lenguaje, nombre_modulo, descripcion, orden_secuencia)
VALUES (3, 3, 'Fundamentos Comparativos y POO en C++', 'Funciones virtuales puras, std::vector, referencias constantes y RAII.', 1);

-- 7. LECCIONES (Espejo por orden_secuencia 1, 2 y 3 en cada lenguaje)
-- Lecciones de JAVA (id_modulo = 1)
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    1, 1,
    'Herencia, Clases Abstractas y Despacho Dinámico',
    '1. CONTRATOS ABSTRACTOS Y DESPACHO DINÁMICO EN JAVA' || CHR(10) ||
    'En Java, todos los métodos de instancia no marcados como final ni private se enlazan dinámicamente en tiempo de ejecución mediante la tabla virtual de la JVM.' || CHR(10) || CHR(10) ||
    '2. COMPARACIÓN CON PYTHON Y C++' || CHR(10) ||
    '• Java exige @Override y abstract explícito.' || CHR(10) ||
    '• Python usa el módulo abc y @abstractmethod.' || CHR(10) ||
    '• C++ requiere virtual explícito y destructor virtual (= default).',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    2, 1,
    'Búsqueda Binaria O(log n) y Aritmética Segura',
    '1. DIVISIÓN DEL ESPACIO DE BÚSQUEDA' || CHR(10) ||
    'La búsqueda binaria reduce a la mitad el intervalo [low, high] en cada paso sobre un arreglo ordenado.' || CHR(10) || CHR(10) ||
    '2. PREVENCIÓN DE OVERFLOW EN ENTEROS DE 32 BITS' || CHR(10) ||
    'En Java y C++, int tiene 32 bits con signo. Calcular mid = low + (high - low) / 2 evita que low + high supere Integer.MAX_VALUE.',
    2, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    3, 1,
    'Gestión de Memoria y Colecciones Dinámicas (ArrayList)',
    'En Java, ArrayList<T> redimensiona un arreglo interno en el Heap en tiempo O(1) amortizado, mientras el recolector G1GC libera objetos sin referencias activas.',
    3, 80
);

-- Lecciones de PYTHON (id_modulo = 2)
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    4, 2,
    'Protocolos Abstractos (abc.ABC) y Duck Typing',
    'En Python, las clases base abstractas se definen heredando de abc.ABC y decorando métodos con @abstractmethod. Si una subclase no implementa el método, Python impide instanciarla en tiempo de ejecución.',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    5, 2,
    'Búsqueda Binaria y División Entera (//) en Python',
    'En Python, los enteros (int) tienen precisión arbitraria por lo que nunca sufren overflow de 32 bits. Sin embargo, es indispensable usar el operador // para obtener un índice entero.',
    2, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    6, 2,
    'Listas Dinámicas y Conteo de Referencias en Python',
    'Las listas de Python (list) almacenan punteros a PyObject en un bloque contiguo que crece con sobre-asignación para garantizar append() en O(1) amortizado.',
    3, 80
);

-- Lecciones de C++ (id_modulo = 3)
INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    7, 3,
    'Funciones Virtuales Puras (= 0) y Tabla Virtual (vtable)',
    'En C++, el polimorfismo dinámico solo se activa cuando el método base se declara virtual. Una clase con al menos una función virtual pura (virtual T f() = 0) es abstracta y exige destructor virtual.',
    1, 60
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    8, 3,
    'Búsqueda Binaria sobre std::vector y Paso por Referencia',
    'Al recibir const std::vector<int>& a, C++ accede en O(1) sin copiar el contenedor en memoria, manteniendo complejidad temporal O(log n) y espacial O(1).',
    2, 75
);

INSERT INTO Lecciones (id_leccion, id_modulo, titulo, contenido, orden_secuencia, exp_recompensa)
VALUES (
    9, 3,
    'Determinismo RAII, Smart Pointers y std::vector',
    'A diferencia del Garbage Collector de Java y Python, C++ destruye los objetos de forma determinista al cerrarse su ámbito léxico (RAII) mediante destructores y std::unique_ptr.',
    3, 80
);

-- 5. RETOS (Especializados en Reto_Codigo y Reto_Quiz)
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

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    103, 2,
    'Búsqueda Binaria Segura O(log n)',
    'Implementa search(int[] a, int target) retornando el índice del elemento buscado o -1 si no se encuentra en el arreglo ordenado.',
    'CODIGO', 'MEDIO', 90, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    104, 4,
    'Polimorfismo en Python: Sonidos por Especie',
    'Completa la función emitir_sonido(tipo: str) -> str para retornar "Guau" con "Perro" y "Miau" con "Gato".',
    'CODIGO', 'FACIL', 65, 1
);

INSERT INTO Retos (id_reto, id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa, orden_secuencia)
VALUES (
    105, 7,
    'Polimorfismo en C++: Clasificación de Sonidos',
    'Completa emitirSonido(std::string tipo) para retornar "Guau" cuando reciba "Perro" y "Miau" cuando reciba "Gato".',
    'CODIGO', 'MEDIO', 75, 1
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

INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    104,
    'def emitir_sonido(tipo: str) -> str:' || CHR(10) ||
    '    if tipo.lower() == "perro":' || CHR(10) ||
    '        return "Guau"' || CHR(10) ||
    '    elif tipo.lower() == "gato":' || CHR(10) ||
    '        return "Miau"' || CHR(10) ||
    '    return "Desconocido"',
    2000
);

INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    105,
    '#include <string>' || CHR(10) || CHR(10) ||
    'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
    '    if (tipo == "Perro" || tipo == "PERRO") {' || CHR(10) ||
    '        return "Guau";' || CHR(10) ||
    '    }' || CHR(10) ||
    '    if (tipo == "Gato") {' || CHR(10) ||
    '        return "Miau";' || CHR(10) ||
    '    }' || CHR(10) ||
    '    return "Desconocido";' || CHR(10) ||
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

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (6, 104, 'Perro', 'Guau', 0);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (7, 104, 'Gato', 'Miau', 0);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (8, 105, 'Perro', 'Guau', 0);

INSERT INTO Casos_Prueba (id_caso, id_reto, entrada, salida_esperada, es_oculto)
VALUES (9, 105, 'Gato', 'Miau', 0);

-- 8. RETO_QUIZ
INSERT INTO Reto_Quiz (id_reto, opciones, respuesta_correcta)
VALUES (
    102,
    'Sobrecarga estática en tiempo de compilación (Overloading)|Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)|Conversión implícita de tipos primitivos en la pila|Duplicación de constructores en la clase base',
    'Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)'
);

-- 9. LOGROS
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

-- 10. LOGRO_RACHA
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (1, 3);
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (2, 7);
INSERT INTO Logro_Racha (id_logro, dias_requeridos) VALUES (6, 14);

-- 11. LOGRO_EXP
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (3, 1000);
INSERT INTO Logro_EXP (id_logro, exp_requerida) VALUES (4, 1500);

-- 12. LOGRO_RETOS
INSERT INTO Logro_Retos (id_logro, retos_requeridos) VALUES (5, 5);

-- Sincronizar secuencias IDENTITY del catálogo después de inserciones con ID explícito
ALTER TABLE Roles        MODIFY id_rol      GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Lenguajes    MODIFY id_lenguaje GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Modulos      MODIFY id_modulo   GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Lecciones    MODIFY id_leccion  GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Retos        MODIFY id_reto     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Casos_Prueba MODIFY id_caso     GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);
ALTER TABLE Logros       MODIFY id_logro    GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);

COMMIT;
