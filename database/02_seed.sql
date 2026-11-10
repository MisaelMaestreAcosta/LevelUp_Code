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
-- 6. RETO_CODIGO (18 Retos de Código con plantillas listas para evaluar)
-- =============================================================================

-- Java 101: Sonidos polimórficos
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    101,
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
    2000
);

-- Java 106: Clasificador de Roles SOLID
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    106,
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
    2000
);

-- Java 103: Búsqueda Binaria
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

-- Java 109: Palíndromo con Dos Punteros
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    109,
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
    2000
);

-- Java 111: Suma de Pares
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    111,
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
    2000
);

-- Java 113: Primer Duplicado con HashSet
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    113,
    'import java.util.HashSet;' || CHR(10) ||
    'import java.util.Set;' || CHR(10) || CHR(10) ||
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
    2000
);

-- Python 104: Sonidos por Especie
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

-- Python 203: Nivel de Acceso
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    203,
    'def nivel_acceso(rol: str) -> str:' || CHR(10) ||
    '    if rol.lower() == "admin":' || CHR(10) ||
    '        return "TOTAL"' || CHR(10) ||
    '    elif rol.lower() == "estudiante":' || CHR(10) ||
    '        return "LECTURA"' || CHR(10) ||
    '    return "DENEGADO"',
    2000
);

-- Python 205: Búsqueda Binaria
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    205,
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
    2000
);

-- Python 207: Palíndromo con Slicing
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    207,
    'def es_palindromo(texto: str) -> bool:' || CHR(10) ||
    '    limpio = texto.lower()' || CHR(10) ||
    '    return limpio == limpio[::-1]',
    2000
);

-- Python 209: Suma de Pares
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    209,
    'def sumar_pares(nums: list) -> int:' || CHR(10) ||
    '    total = 0' || CHR(10) ||
    '    for n in nums:' || CHR(10) ||
    '        if n % 2 == 0:' || CHR(10) ||
    '            total += n' || CHR(10) ||
    '    return total',
    2000
);

-- Python 211: Primer Duplicado con Set
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    211,
    'def primer_duplicado(nums: list) -> int:' || CHR(10) ||
    '    vistos = set()' || CHR(10) ||
    '    for n in nums:' || CHR(10) ||
    '        if n in vistos:' || CHR(10) ||
    '            return n' || CHR(10) ||
    '        vistos.add(n)' || CHR(10) ||
    '    return -1',
    2000
);

-- C++ 105: Clasificación de Sonidos
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    105,
    '#include <string>' || CHR(10) || CHR(10) ||
    'std::string emitirSonido(std::string tipo) {' || CHR(10) ||
    '    if (tipo == "Perro" || tipo == "PERRO" || tipo == "perro") {' || CHR(10) ||
    '        return "Guau";' || CHR(10) ||
    '    }' || CHR(10) ||
    '    if (tipo == "Gato" || tipo == "GATO" || tipo == "gato") {' || CHR(10) ||
    '        return "Miau";' || CHR(10) ||
    '    }' || CHR(10) ||
    '    return "Desconocido";' || CHR(10) ||
    '}',
    2000
);

-- C++ 303: Clasificador de Nivel de Acceso
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    303,
    '#include <string>' || CHR(10) || CHR(10) ||
    'std::string nivelAcceso(std::string rol) {' || CHR(10) ||
    '    if (rol == "ADMIN" || rol == "admin") {' || CHR(10) ||
    '        return "TOTAL";' || CHR(10) ||
    '    }' || CHR(10) ||
    '    if (rol == "ESTUDIANTE" || rol == "estudiante") {' || CHR(10) ||
    '        return "LECTURA";' || CHR(10) ||
    '    }' || CHR(10) ||
    '    return "DENEGADO";' || CHR(10) ||
    '}',
    2000
);

-- C++ 305: Búsqueda Binaria sobre std::vector
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    305,
    '#include <vector>' || CHR(10) || CHR(10) ||
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
    2000
);

-- C++ 307: Palíndromo con Dos Índices
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    307,
    '#include <string>' || CHR(10) ||
    '#include <cctype>' || CHR(10) || CHR(10) ||
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
    2000
);

-- C++ 309: Suma de Pares con std::vector
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    309,
    '#include <vector>' || CHR(10) || CHR(10) ||
    'int sumarPares(const std::vector<int>& nums) {' || CHR(10) ||
    '    int total = 0;' || CHR(10) ||
    '    for (int n : nums) {' || CHR(10) ||
    '        if (n % 2 == 0) {' || CHR(10) ||
    '            total += n;' || CHR(10) ||
    '        }' || CHR(10) ||
    '    }' || CHR(10) ||
    '    return total;' || CHR(10) ||
    '}',
    2000
);

-- C++ 311: Primer Duplicado con std::unordered_set
INSERT INTO Reto_Codigo (id_reto, codigo_inicial, limite_tiempo_ms)
VALUES (
    311,
    '#include <vector>' || CHR(10) ||
    '#include <unordered_set>' || CHR(10) || CHR(10) ||
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
