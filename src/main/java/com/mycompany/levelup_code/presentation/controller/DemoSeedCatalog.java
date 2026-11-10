package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.StreakAchievement;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catálogo completo de datos semilla y fragmentos comparativos tri-lenguaje.
 * Espeja los 3 lenguajes, 9 módulos, 18 lecciones, 36 retos, 54 casos de prueba
 * y 8 logros de {@code database/02_seed.sql} para garantizar paridad total en modo local.
 */
public final class DemoSeedCatalog {

    private DemoSeedCatalog() {
    }

    public static User createDefaultStudent() {
        User u = new User();
        u.setId(1);
        u.setUsername("Estudiante");
        u.setEmail("estudiante@levelup.edu.co");
        u.setExperience(1450);
        u.setLevel(5);
        u.setCurrentStreak(7);
        u.setMaxStreak(12);
        return u;
    }

    public static List<Language> createSeedLanguages() {
        Language javaLang = new Language(1, "Java",
                "POO, Jerarquías ES-UN, Principios SOLID, Algoritmos y Colecciones Tipadas en JVM (Java 21 LTS).", true);
        Language pythonLang = new Language(2, "Python",
                "Protocolos ABC, Métodos Dunder, Slicing, Comprensiones y Estructuras Hash (Python 3.12).", true);
        Language cppLang = new Language(3, "C++",
                "Polimorfismo Virtual, Gestión Determinista RAII, Smart Pointers y Contenedores STL (ISO C++ 20).", true);

        buildJavaCurriculum(javaLang);
        buildPythonCurriculum(pythonLang);
        buildCppCurriculum(cppLang);

        return Arrays.asList(javaLang, pythonLang, cppLang);
    }

    private static void buildJavaCurriculum(Language javaLang) {
        CourseModule mod1 = new CourseModule(1, "Módulo 1: POO, Polimorfismo y SOLID en Java",
                "Clases abstractas, despacho dinámico en JVM, interfaces y sustitución de Liskov.", 1, javaLang);
        CourseModule mod2 = new CourseModule(4, "Módulo 2: Algoritmos de Búsqueda y Dos Punteros",
                "Búsqueda binaria O(log n) libre de overflow y técnica de dos punteros sobre cadenas.", 2, javaLang);
        CourseModule mod3 = new CourseModule(5, "Módulo 3: Colecciones Dinámicas y Tablas Hash",
                "Complejidad amortizada en ArrayList y detección de duplicados en O(n) con HashSet.", 3, javaLang);

        // Lección 1 (Java POO 1)
        Lesson l1 = new Lesson(1, "Herencia, Clases Abstractas y Despacho Dinámico",
                "1. CONTRATOS ABSTRACTOS Y DESPACHO DINÁMICO EN JAVA\n"
                + "En Java, todos los métodos de instancia no marcados como final, static ni private se enlazan dinámicamente en tiempo de ejecución mediante la vtable de la JVM.\n\n"
                + "2. REGLAS CLAVE DE DISEÑO POO\n"
                + "• Una clase abstracta define estado común y métodos abstractos.\n"
                + "• @Override verifica en compilación que la firma coincida con la superclase.",
                1, 60, mod1);
        CodeChallenge c101 = new CodeChallenge(101, "Polimorfismo: Despacho Dinámico de Sonidos",
                "Implementa el método emitirSonido(String tipo) para que retorne \"Guau\" cuando reciba \"Perro\" y \"Miau\" cuando reciba \"Gato\" (sin importar mayúsculas o minúsculas).",
                l1, 60,
                "public class AnimalSoundSolver {\n"
                + "    public static String emitirSonido(String tipo) {\n"
                + "        if (\"Perro\".equalsIgnoreCase(tipo)) {\n"
                + "            return \"Guau\";\n"
                + "        }\n"
                + "        if (\"Gato\".equalsIgnoreCase(tipo)) {\n"
                + "            return \"Miau\";\n"
                + "        }\n"
                + "        return \"Desconocido\";\n"
                + "    }\n"
                + "}", 2);
        c101.addTestCase(new TestCase(1, c101, "Perro", "Guau", false));
        c101.addTestCase(new TestCase(2, c101, "Gato", "Miau", false));
        c101.addTestCase(new TestCase(3, c101, "PERRO", "Guau", true));
        l1.addChallenge(c101);
        l1.addChallenge(new QuizChallenge(102, "Quiz Conceptual: Enlace Dinámico en POO",
                "¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) de la subclase en tiempo de ejecución?",
                l1, 40,
                Arrays.asList(
                        "Sobrecarga estática en tiempo de compilación (Overloading)",
                        "Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)",
                        "Conversión implícita de tipos primitivos en la pila",
                        "Duplicación de constructores en la clase base"),
                "Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)"));

        // Lección 10 (Java POO 2)
        Lesson l10 = new Lesson(10, "Interfaces, Contratos Múltiples y Sustitución de Liskov (SOLID)",
                "1. DESACOPLAMIENTO MEDIANTE INTERFACES\n"
                + "Una clase en Java puede implementar múltiples interfaces (implements), separando el QUÉ hace un componente de CÓMO lo ejecuta.\n\n"
                + "2. PRINCIPIO DE SUSTITUCIÓN DE LISKOV (LSP)\n"
                + "Cualquier implementación debe poder sustituir a su interfaz sin alterar la correctitud del sistema.",
                2, 70, mod1);
        CodeChallenge c106 = new CodeChallenge(106, "Contratos SOLID: Clasificador de Nivel de Acceso",
                "Implementa nivelAcceso(String rol) para que retorne \"TOTAL\" cuando reciba \"ADMIN\" y \"LECTURA\" cuando reciba \"ESTUDIANTE\" (ignorando mayúsculas/minúsculas).",
                l10, 70,
                "public class AccessPolicySolver {\n"
                + "    public static String nivelAcceso(String rol) {\n"
                + "        if (\"ADMIN\".equalsIgnoreCase(rol)) {\n"
                + "            return \"TOTAL\";\n"
                + "        }\n"
                + "        if (\"ESTUDIANTE\".equalsIgnoreCase(rol)) {\n"
                + "            return \"LECTURA\";\n"
                + "        }\n"
                + "        return \"DENEGADO\";\n"
                + "    }\n"
                + "}", 2);
        c106.addTestCase(new TestCase(4, c106, "ADMIN", "TOTAL", false));
        c106.addTestCase(new TestCase(5, c106, "ESTUDIANTE", "LECTURA", false));
        c106.addTestCase(new TestCase(6, c106, "admin", "TOTAL", true));
        l10.addChallenge(c106);
        l10.addChallenge(new QuizChallenge(107, "Quiz Conceptual: Principio de Sustitución de Liskov",
                "Según el Principio de Sustitución de Liskov (LSP) en Java, ¿qué debe cumplirse cuando una clase implementa una interfaz o extiende una clase base?",
                l10, 45,
                Arrays.asList(
                        "La subclase debe poder sustituir a su supertipo manteniendo el contrato sin efectos inesperados",
                        "La subclase debe declarar todos sus atributos como públicos",
                        "Una interfaz solo puede ser implementada por una única clase en todo el paquete",
                        "El compilador prohíbe usar polimorfismo con interfaces funcionales"),
                "La subclase debe poder sustituir a su supertipo manteniendo el contrato sin efectos inesperados"));

        // Lección 2 (Java Algoritmos 1)
        Lesson l2 = new Lesson(2, "Búsqueda Binaria O(log n) y Aritmética Segura",
                "1. DIVISIÓN DEL ESPACIO DE BÚSQUEDA\n"
                + "La búsqueda binaria reduce a la mitad el intervalo [low, high] en cada iteración sobre un arreglo ordenado.\n\n"
                + "2. PREVENCIÓN DE OVERFLOW EN ENTEROS DE 32 BITS\n"
                + "Calcular mid = low + (high - low) / 2 evita que low + high supere Integer.MAX_VALUE.",
                1, 75, mod2);
        CodeChallenge c103 = new CodeChallenge(103, "Búsqueda Binaria Segura O(log n)",
                "Implementa search(int[] a, int target) retornando el índice (0-based) del elemento buscado o -1 si no se encuentra en el arreglo ordenado.",
                l2, 90,
                "public class BinarySearchSolver {\n"
                + "    public static int search(int[] a, int target) {\n"
                + "        int low = 0, high = a.length - 1;\n"
                + "        while (low <= high) {\n"
                + "            int mid = low + (high - low) / 2;\n"
                + "            if (a[mid] == target) return mid;\n"
                + "            if (a[mid] < target) low = mid + 1;\n"
                + "            else high = mid - 1;\n"
                + "        }\n"
                + "        return -1;\n"
                + "    }\n"
                + "}", 2);
        c103.addTestCase(new TestCase(7, c103, "[2, 5, 8, 12, 16], target=12", "3", false));
        c103.addTestCase(new TestCase(8, c103, "[2, 5, 8, 12, 16], target=2", "0", false));
        c103.addTestCase(new TestCase(9, c103, "[2, 5, 8, 12, 16], target=7", "-1", true));
        l2.addChallenge(c103);
        l2.addChallenge(new QuizChallenge(108, "Quiz Conceptual: Prevención de Overflow en Búsqueda Binaria",
                "¿Por qué se prefiere la expresión mid = low + (high - low) / 2 en lugar de (low + high) / 2 en arreglos grandes en Java?",
                l2, 50,
                Arrays.asList(
                        "Porque evita que la suma (low + high) supere Integer.MAX_VALUE y desborde a negativo",
                        "Porque el operador + está desaconsejado en versiones recientes de Java",
                        "Porque convierte automáticamente el arreglo a números de punto flotante",
                        "Porque reduce la complejidad de O(log n) a O(1) constante"),
                "Porque evita que la suma (low + high) supere Integer.MAX_VALUE y desborde a negativo"));

        // Lección 11 (Java Algoritmos 2)
        Lesson l11 = new Lesson(11, "Técnica de Dos Punteros (Two Pointers) sobre Secuencias",
                "1. OPTIMIZACIÓN DE TIEMPO Y MEMORIA\n"
                + "La técnica de dos punteros coloca un índice al inicio (left = 0) y otro al final (right = n - 1), avanzando hacia el centro en tiempo O(n) y espacio auxiliar O(1).",
                2, 80, mod2);
        CodeChallenge c109 = new CodeChallenge(109, "Dos Punteros: Verificador de Palíndromos",
                "Implementa esPalindromo(String texto) usando dos índices para retornar true si la palabra se lee igual en ambos sentidos (ignorando mayúsculas), o false en caso contrario.",
                l11, 85,
                "public class PalindromeSolver {\n"
                + "    public static boolean esPalindromo(String texto) {\n"
                + "        String s = texto.toLowerCase();\n"
                + "        int left = 0, right = s.length() - 1;\n"
                + "        while (left < right) {\n"
                + "            if (s.charAt(left) != s.charAt(right)) {\n"
                + "                return false;\n"
                + "            }\n"
                + "            left++;\n"
                + "            right--;\n"
                + "        }\n"
                + "        return true;\n"
                + "    }\n"
                + "}", 2);
        c109.addTestCase(new TestCase(10, c109, "reconocer", "true", false));
        c109.addTestCase(new TestCase(11, c109, "levelup", "false", false));
        c109.addTestCase(new TestCase(12, c109, "Radar", "true", true));
        l11.addChallenge(c109);
        l11.addChallenge(new QuizChallenge(110, "Quiz Conceptual: Complejidad de Dos Punteros",
                "¿Cuál es la complejidad temporal y espacial auxiliar de verificar un palíndromo in-place usando dos índices extremos (left y right)?",
                l11, 50,
                Arrays.asList(
                        "Tiempo O(n) y espacio auxiliar O(1) al comparar caracteres sin copiar la cadena",
                        "Tiempo O(n²) y espacio auxiliar O(n) por cada carácter evaluado",
                        "Tiempo O(log n) porque descarta la mitad del texto en cada paso",
                        "Tiempo O(1) independiente de la longitud del texto"),
                "Tiempo O(n) y espacio auxiliar O(1) al comparar caracteres sin copiar la cadena"));

        // Lección 3 (Java Colecciones 1)
        Lesson l3 = new Lesson(3, "Gestión de Memoria y Colecciones Dinámicas (ArrayList)",
                "1. ARREGLOS DINÁMICOS EN EL HEAP DE LA JVM\n"
                + "ArrayList<T> redimensiona un arreglo contiguo Object[] en tiempo O(1) amortizado al insertar al final, manteniendo localidad espacial en caché.",
                1, 80, mod3);
        CodeChallenge c111 = new CodeChallenge(111, "Colecciones: Suma de Números Pares",
                "Implementa sumarPares(int[] nums) para recorrer la secuencia y retornar la suma de todos los números pares contenidos en el arreglo.",
                l3, 80,
                "public class EvenSumSolver {\n"
                + "    public static int sumarPares(int[] nums) {\n"
                + "        int total = 0;\n"
                + "        for (int n : nums) {\n"
                + "            if (n % 2 == 0) {\n"
                + "                total += n;\n"
                + "            }\n"
                + "        }\n"
                + "        return total;\n"
                + "    }\n"
                + "}", 2);
        c111.addTestCase(new TestCase(13, c111, "[4, 7, 2, 9, 6]", "12", false));
        c111.addTestCase(new TestCase(14, c111, "[1, 3, 5]", "0", false));
        c111.addTestCase(new TestCase(15, c111, "[10, 20, 3, 8]", "38", true));
        l3.addChallenge(c111);
        l3.addChallenge(new QuizChallenge(112, "Quiz Conceptual: Complejidad Amortizada en ArrayList",
                "¿Cuál es el costo temporal de insertar un elemento al final de un ArrayList<T> mediante add(elemento) en análisis amortizado?",
                l3, 50,
                Arrays.asList(
                        "O(1) amortizado gracias al redimensionamiento geométrico del arreglo interno",
                        "O(n²) porque ordena automáticamente todos los elementos en cada inserción",
                        "O(log n) porque internamente es un árbol binario balanceado",
                        "O(n) estricto porque copia el arreglo completo en cada llamada a add()"),
                "O(1) amortizado gracias al redimensionamiento geométrico del arreglo interno"));

        // Lección 12 (Java Colecciones 2)
        Lesson l12 = new Lesson(12, "Tablas Hash (HashSet / HashMap) y Detección en O(n)",
                "1. REDUCCIÓN DE O(n²) A O(n) CON HASHING\n"
                + "Almacenar los elementos visitados en un HashSet<Integer> permite consultar existencia en O(1) promedio respetando el contrato equals() y hashCode().",
                2, 95, mod3);
        CodeChallenge c113 = new CodeChallenge(113, "Tablas Hash: Primer Elemento Duplicado en O(n)",
                "Implementa primerDuplicado(int[] nums) usando un HashSet para retornar el primer número que aparece repetido al recorrer el arreglo, o -1 si todos son únicos.",
                l12, 110,
                "import java.util.HashSet;\nimport java.util.Set;\n\n"
                + "public class DuplicateFinderSolver {\n"
                + "    public static int primerDuplicado(int[] nums) {\n"
                + "        Set<Integer> vistos = new HashSet<>();\n"
                + "        for (int n : nums) {\n"
                + "            if (!vistos.add(n)) {\n"
                + "                return n;\n"
                + "            }\n"
                + "        }\n"
                + "        return -1;\n"
                + "    }\n"
                + "}", 2);
        c113.addTestCase(new TestCase(16, c113, "[3, 5, 2, 5, 8, 2]", "5", false));
        c113.addTestCase(new TestCase(17, c113, "[10, 20, 30, 10]", "10", false));
        c113.addTestCase(new TestCase(18, c113, "[1, 2, 3, 4]", "-1", true));
        l12.addChallenge(c113);
        l12.addChallenge(new QuizChallenge(114, "Quiz Conceptual: Contrato equals() y hashCode()",
                "¿Qué ocurre si una clase sobrescribe equals() pero NO sobrescribe hashCode() al almacenarse dentro de un HashSet o HashMap en Java?",
                l12, 60,
                Arrays.asList(
                        "Objetos lógicamente iguales pueden caer en buckets distintos y duplicarse dentro del HashSet",
                        "El código no compila porque Object no provee una implementación base de hashCode()",
                        "El HashSet convierte automáticamente todos los objetos a String antes de guardarlos",
                        "La JVM lanza NullPointerException al invocar add()"),
                "Objetos lógicamente iguales pueden caer en buckets distintos y duplicarse dentro del HashSet"));

        mod1.addLesson(l1);
        mod1.addLesson(l10);
        mod2.addLesson(l2);
        mod2.addLesson(l11);
        mod3.addLesson(l3);
        mod3.addLesson(l12);
        javaLang.addModule(mod1);
        javaLang.addModule(mod2);
        javaLang.addModule(mod3);
    }

    private static void buildPythonCurriculum(Language pythonLang) {
        CourseModule mod1 = new CourseModule(2, "Módulo 1: Protocolos Abstractos y Modelo de Datos",
                "Clases abstractas abc.ABC, Duck Typing y métodos especiales (__str__, __eq__).", 1, pythonLang);
        CourseModule mod2 = new CourseModule(6, "Módulo 2: Algoritmos Eficientes y Secuencias",
                "Búsqueda binaria con división entera (//), slicing e inversión de secuencias.", 2, pythonLang);
        CourseModule mod3 = new CourseModule(7, "Módulo 3: Listas Dinámicas, Comprensiones y Sets",
                "Sobre-asignación en list, filtrado expresivo y búsqueda O(1) con set y dict.", 3, pythonLang);

        // Lección 4 (Python POO 1)
        Lesson l4 = new Lesson(4, "Protocolos Abstractos (abc.ABC) y Duck Typing",
                "En Python, las clases base abstractas se definen heredando de abc.ABC y decorando métodos con @abstractmethod. Si una subclase no los implementa, Python lanza TypeError al instanciarla.",
                1, 60, mod1);
        CodeChallenge c104 = new CodeChallenge(104, "Polimorfismo en Python: Sonidos por Especie",
                "Completa la función emitir_sonido(tipo: str) -> str para retornar \"Guau\" con \"Perro\" y \"Miau\" con \"Gato\".",
                l4, 65,
                "def emitir_sonido(tipo: str) -> str:\n"
                + "    if tipo.lower() == \"perro\":\n"
                + "        return \"Guau\"\n"
                + "    elif tipo.lower() == \"gato\":\n"
                + "        return \"Miau\"\n"
                + "    return \"Desconocido\"\n", 2);
        c104.addTestCase(new TestCase(19, c104, "Perro", "Guau", false));
        c104.addTestCase(new TestCase(20, c104, "Gato", "Miau", false));
        c104.addTestCase(new TestCase(21, c104, "PERRO", "Guau", true));
        l4.addChallenge(c104);
        l4.addChallenge(new QuizChallenge(202, "Quiz Conceptual: Clases Abstractas abc.ABC en Python",
                "¿Qué sucede en Python cuando intentas instanciar una subclase de abc.ABC que no ha implementado todos los métodos decorados con @abstractmethod?",
                l4, 40,
                Arrays.asList(
                        "Python lanza TypeError en tiempo de ejecución impidiendo instanciar la clase incompleta",
                        "Python genera automáticamente métodos vacíos que retornan None",
                        "El programa se ejecuta normalmente e ignora el decorador @abstractmethod",
                        "Solo muestra una advertencia estética en la consola sin detener el programa"),
                "Python lanza TypeError en tiempo de ejecución impidiendo instanciar la clase incompleta"));

        // Lección 13 (Python POO 2)
        Lesson l13 = new Lesson(13, "Modelo de Datos de Python y Métodos Especiales (Dunder)",
                "El comportamiento de los objetos en Python se personaliza mediante métodos especiales como __init__, __str__, __repr__ y __eq__.",
                2, 70, mod1);
        CodeChallenge c203 = new CodeChallenge(203, "Protocolos en Python: Clasificador de Nivel de Acceso",
                "Completa nivel_acceso(rol: str) -> str para retornar \"TOTAL\" cuando reciba \"ADMIN\" y \"LECTURA\" cuando reciba \"ESTUDIANTE\".",
                l13, 70,
                "def nivel_acceso(rol: str) -> str:\n"
                + "    if rol.lower() == \"admin\":\n"
                + "        return \"TOTAL\"\n"
                + "    elif rol.lower() == \"estudiante\":\n"
                + "        return \"LECTURA\"\n"
                + "    return \"DENEGADO\"\n", 2);
        c203.addTestCase(new TestCase(22, c203, "ADMIN", "TOTAL", false));
        c203.addTestCase(new TestCase(23, c203, "ESTUDIANTE", "LECTURA", false));
        c203.addTestCase(new TestCase(24, c203, "admin", "TOTAL", true));
        l13.addChallenge(c203);
        l13.addChallenge(new QuizChallenge(204, "Quiz Conceptual: Métodos Especiales __str__ vs __repr__",
                "¿Cuál es la diferencia fundamental entre los métodos especiales __str__ y __repr__ en el modelo de datos de Python?",
                l13, 45,
                Arrays.asList(
                        "__str__ produce texto legible para el usuario final y __repr__ una representación técnica inequívoca",
                        "__str__ solo funciona con números enteros y __repr__ solo con listas",
                        "Ambos métodos son idénticos y no pueden coexistir en la misma clase",
                        "__repr__ es privado y no puede ser invocado desde fuera del módulo"),
                "__str__ produce texto legible para el usuario final y __repr__ una representación técnica inequívoca"));

        // Lección 5 (Python Algoritmos 1)
        Lesson l5 = new Lesson(5, "Búsqueda Binaria y División Entera (//) en Python",
                "En Python, los enteros tienen precisión arbitraria, pero es obligatorio usar el operador // para obtener un índice entero al dividir.",
                1, 75, mod2);
        CodeChallenge c205 = new CodeChallenge(205, "Búsqueda Binaria en Python con División Entera (//)",
                "Completa search(a: list, target: int) -> int para retornar el índice del elemento buscado en la lista ordenada o -1 si no existe.",
                l5, 90,
                "def search(a: list, target: int) -> int:\n"
                + "    low = 0\n"
                + "    high = len(a) - 1\n"
                + "    while low <= high:\n"
                + "        mid = (low + high) // 2\n"
                + "        if a[mid] == target:\n"
                + "            return mid\n"
                + "        elif a[mid] < target:\n"
                + "            low = mid + 1\n"
                + "        else:\n"
                + "            high = mid - 1\n"
                + "    return -1\n", 2);
        c205.addTestCase(new TestCase(25, c205, "[2, 5, 8, 12, 16], target=12", "3", false));
        c205.addTestCase(new TestCase(26, c205, "[2, 5, 8, 12, 16], target=2", "0", false));
        c205.addTestCase(new TestCase(27, c205, "[2, 5, 8, 12, 16], target=7", "-1", true));
        l5.addChallenge(c205);
        l5.addChallenge(new QuizChallenge(206, "Quiz Conceptual: Operador // e Indexación en Python",
                "¿Por qué es indispensable utilizar el operador // en lugar de / al calcular el índice medio en Python 3?",
                l5, 50,
                Arrays.asList(
                        "Porque / retorna un float y las listas en Python exigen índices enteros (int)",
                        "Porque // realiza la división en un hilo separado del procesador",
                        "Porque el operador / lanza ZeroDivisionError cuando el resultado es decimal",
                        "Porque // ordena automáticamente la lista antes de dividir"),
                "Porque / retorna un float y las listas en Python exigen índices enteros (int)"));

        // Lección 14 (Python Algoritmos 2)
        Lesson l14 = new Lesson(14, "Slicing Avanzado e Inversión de Secuencias en Python",
                "La expresión texto[::-1] recorre la secuencia con paso -1 generando una copia invertida de forma altamente optimizada en CPython.",
                2, 80, mod2);
        CodeChallenge c207 = new CodeChallenge(207, "Slicing en Python: Detector de Palíndromos",
                "Completa es_palindromo(texto: str) -> bool invirtiendo o comparando la cadena en minúsculas para evaluar si es un palíndromo.",
                l14, 85,
                "def es_palindromo(texto: str) -> bool:\n"
                + "    limpio = texto.lower()\n"
                + "    return limpio == limpio[::-1]\n", 2);
        c207.addTestCase(new TestCase(28, c207, "reconocer", "true", false));
        c207.addTestCase(new TestCase(29, c207, "python", "false", false));
        c207.addTestCase(new TestCase(30, c207, "Radar", "true", true));
        l14.addChallenge(c207);
        l14.addChallenge(new QuizChallenge(208, "Quiz Conceptual: Slicing s[::-1] en Memoria",
                "¿Qué hace exactamente la expresión texto[::-1] sobre una cadena str en Python?",
                l14, 50,
                Arrays.asList(
                        "Crea y retorna una nueva cadena con los caracteres en orden inverso usando paso -1",
                        "Elimina el último carácter de la cadena modificándola in-place",
                        "Convierte la cadena en un diccionario de frecuencias",
                        "Lanza IndexError porque los pasos negativos no existen en Python"),
                "Crea y retorna una nueva cadena con los caracteres en orden inverso usando paso -1"));

        // Lección 6 (Python Estructuras 1)
        Lesson l6 = new Lesson(6, "Listas Dinámicas y Conteo de Referencias en Python",
                "Las listas de Python almacenan punteros a PyObject en un bloque contiguo con sobre-asignación para garantizar append() en O(1) amortizado.",
                1, 80, mod3);
        CodeChallenge c209 = new CodeChallenge(209, "Listas en Python: Suma de Elementos Pares",
                "Completa sumar_pares(nums: list) -> int para retornar la suma acumulada de los números divisibles por 2.",
                l6, 80,
                "def sumar_pares(nums: list) -> int:\n"
                + "    total = 0\n"
                + "    for n in nums:\n"
                + "        if n % 2 == 0:\n"
                + "            total += n\n"
                + "    return total\n", 2);
        c209.addTestCase(new TestCase(31, c209, "[4, 7, 2, 9, 6]", "12", false));
        c209.addTestCase(new TestCase(32, c209, "[1, 3, 5]", "0", false));
        c209.addTestCase(new TestCase(33, c209, "[10, 20, 3, 8]", "38", true));
        l6.addChallenge(c209);
        l6.addChallenge(new QuizChallenge(210, "Quiz Conceptual: Crecimiento de list en CPython",
                "¿Cómo logra la estructura list de CPython que las llamadas sucesivas a append() tengan costo O(1) amortizado?",
                l6, 50,
                Arrays.asList(
                        "Reserva capacidad adicional (sobre-asignación) en el arreglo de punteros PyObject* para evitar realocar en cada append()",
                        "Almacena cada elemento en un archivo temporal en disco",
                        "Usa una lista doblemente enlazada sin bloques contiguos",
                        "Limita el tamaño máximo de las listas a 256 elementos"),
                "Reserva capacidad adicional (sobre-asignación) en el arreglo de punteros PyObject* para evitar realocar en cada append()"));

        // Lección 15 (Python Estructuras 2)
        Lesson l15 = new Lesson(15, "Conjuntos (set) y Diccionarios (dict) en Tiempo O(1)",
                "Los tipos set y dict implementan tablas hash con búsqueda en O(1) promedio para elementos inmutables (hashables).",
                2, 95, mod3);
        CodeChallenge c211 = new CodeChallenge(211, "Conjuntos Hash en Python: Primer Duplicado en O(n)",
                "Completa primer_duplicado(nums: list) -> int utilizando un conjunto (set) para retornar el primer valor repetido o -1 si no hay duplicados.",
                l15, 110,
                "def primer_duplicado(nums: list) -> int:\n"
                + "    vistos = set()\n"
                + "    for n in nums:\n"
                + "        if n in vistos:\n"
                + "            return n\n"
                + "        vistos.add(n)\n"
                + "    return -1\n", 2);
        c211.addTestCase(new TestCase(34, c211, "[3, 5, 2, 5, 8, 2]", "5", false));
        c211.addTestCase(new TestCase(35, c211, "[10, 20, 30, 10]", "10", false));
        c211.addTestCase(new TestCase(36, c211, "[1, 2, 3, 4]", "-1", true));
        l15.addChallenge(c211);
        l15.addChallenge(new QuizChallenge(212, "Quiz Conceptual: Elementos Hashables en set y dict",
                "¿Por qué una tupla (tuple) de enteros puede ser clave en un dict o elemento de un set en Python, pero una lista (list) no?",
                l15, 60,
                Arrays.asList(
                        "Porque tuple es inmutable y su valor hash permanece constante, mientras que list es mutable",
                        "Porque list ocupa más de 64 bits en memoria",
                        "Porque tuple ordena sus elementos automáticamente al crearse",
                        "Porque los conjuntos set solo aceptan cadenas de texto"),
                "Porque tuple es inmutable y su valor hash permanece constante, mientras que list es mutable"));

        mod1.addLesson(l4);
        mod1.addLesson(l13);
        mod2.addLesson(l5);
        mod2.addLesson(l14);
        mod3.addLesson(l6);
        mod3.addLesson(l15);
        pythonLang.addModule(mod1);
        pythonLang.addModule(mod2);
        pythonLang.addModule(mod3);
    }

    private static void buildCppCurriculum(Language cppLang) {
        CourseModule mod1 = new CourseModule(3, "Módulo 1: Polimorfismo Virtual y Memoria RAII",
                "Funciones virtuales puras (= 0), vtable, destructores virtuales y std::unique_ptr.", 1, cppLang);
        CourseModule mod2 = new CourseModule(8, "Módulo 2: Algoritmos STL y Paso por Referencia",
                "Búsqueda binaria sobre const std::vector<int>& e iteradores bidireccionales.", 2, cppLang);
        CourseModule mod3 = new CourseModule(9, "Módulo 3: Contenedores Secuenciales y Asociativos STL",
                "Gestión de capacidad en std::vector y tablas hash con std::unordered_set.", 3, cppLang);

        // Lección 7 (C++ POO 1)
        Lesson l7 = new Lesson(7, "Funciones Virtuales Puras (= 0) y Tabla Virtual (vtable)",
                "En C++, el polimorfismo dinámico solo se activa declarando virtual. Toda clase base polimórfica exige un destructor virtual.",
                1, 60, mod1);
        CodeChallenge c105 = new CodeChallenge(105, "Polimorfismo en C++: Clasificación de Sonidos",
                "Completa emitirSonido(std::string tipo) para retornar \"Guau\" cuando reciba \"Perro\" y \"Miau\" cuando reciba \"Gato\".",
                l7, 75,
                "#include <string>\n\n"
                + "std::string emitirSonido(std::string tipo) {\n"
                + "    if (tipo == \"Perro\" || tipo == \"PERRO\" || tipo == \"perro\") {\n"
                + "        return \"Guau\";\n"
                + "    }\n"
                + "    if (tipo == \"Gato\" || tipo == \"GATO\" || tipo == \"gato\") {\n"
                + "        return \"Miau\";\n"
                + "    }\n"
                + "    return \"Desconocido\";\n"
                + "}\n", 2);
        c105.addTestCase(new TestCase(37, c105, "Perro", "Guau", false));
        c105.addTestCase(new TestCase(38, c105, "Gato", "Miau", false));
        c105.addTestCase(new TestCase(39, c105, "PERRO", "Guau", true));
        l7.addChallenge(c105);
        l7.addChallenge(new QuizChallenge(302, "Quiz Conceptual: Destructor Virtual en C++",
                "¿Qué riesgo crítico ocurre en C++ si eliminas con delete un objeto derivado a través de un puntero a su clase base que NO tiene destructor virtual?",
                l7, 45,
                Arrays.asList(
                        "Comportamiento indefinido y fuga de recursos porque no se invoca el destructor de la subclase",
                        "El compilador de C++ convierte todos los métodos de la clase en privados",
                        "El puntero base se multiplica por dos en memoria",
                        "El recolector de basura de C++ detiene el programa inmediatamente"),
                "Comportamiento indefinido y fuga de recursos porque no se invoca el destructor de la subclase"));

        // Lección 16 (C++ POO 2)
        Lesson l16 = new Lesson(16, "Determinismo RAII y Punteros Inteligentes (std::unique_ptr)",
                "C++ destruye los recursos de forma determinista al salir de ámbito (RAII) mediante destructores y punteros inteligentes como std::unique_ptr.",
                2, 75, mod1);
        CodeChallenge c303 = new CodeChallenge(303, "Contratos en C++: Clasificador de Nivel de Acceso",
                "Completa nivelAcceso(std::string rol) para retornar \"TOTAL\" cuando reciba \"ADMIN\" y \"LECTURA\" cuando reciba \"ESTUDIANTE\".",
                l16, 75,
                "#include <string>\n\n"
                + "std::string nivelAcceso(std::string rol) {\n"
                + "    if (rol == \"ADMIN\" || rol == \"admin\") {\n"
                + "        return \"TOTAL\";\n"
                + "    }\n"
                + "    if (rol == \"ESTUDIANTE\" || rol == \"estudiante\") {\n"
                + "        return \"LECTURA\";\n"
                + "    }\n"
                + "    return \"DENEGADO\";\n"
                + "}\n", 2);
        c303.addTestCase(new TestCase(40, c303, "ADMIN", "TOTAL", false));
        c303.addTestCase(new TestCase(41, c303, "ESTUDIANTE", "LECTURA", false));
        c303.addTestCase(new TestCase(42, c303, "admin", "TOTAL", true));
        l16.addChallenge(c303);
        l16.addChallenge(new QuizChallenge(304, "Quiz Conceptual: std::unique_ptr y Semántica RAII",
                "¿Cuál es la característica principal de std::unique_ptr<T> introducido en el C++ moderno frente a los punteros crudos con new/delete?",
                l16, 50,
                Arrays.asList(
                        "Garantiza propiedad exclusiva del recurso y lo libera automáticamente al salir de ámbito sin sobrecosto",
                        "Permite que múltiples punteros sean dueños simultáneos mediante conteo atómico",
                        "Requiere llamar manualmente a free() antes de que termine la función",
                        "Desactiva las comprobaciones de tipos del compilador g++"),
                "Garantiza propiedad exclusiva del recurso y lo libera automáticamente al salir de ámbito sin sobrecosto"));

        // Lección 8 (C++ Algoritmos 1)
        Lesson l8 = new Lesson(8, "Búsqueda Binaria sobre std::vector y Paso por Referencia",
                "Al recibir const std::vector<int>& a, C++ accede en O(1) sin copiar el contenedor en memoria, manteniendo complejidad O(log n).",
                1, 75, mod2);
        CodeChallenge c305 = new CodeChallenge(305, "Búsqueda Binaria en C++ sobre const std::vector<int>&",
                "Completa search(const std::vector<int>& a, int target) retornando el índice donde se ubica target o -1 si no está presente.",
                l8, 95,
                "#include <vector>\n\n"
                + "int search(const std::vector<int>& a, int target) {\n"
                + "    int low = 0;\n"
                + "    int high = static_cast<int>(a.size()) - 1;\n"
                + "    while (low <= high) {\n"
                + "        int mid = low + (high - low) / 2;\n"
                + "        if (a[mid] == target) return mid;\n"
                + "        if (a[mid] < target) low = mid + 1;\n"
                + "        else high = mid - 1;\n"
                + "    }\n"
                + "    return -1;\n"
                + "}\n", 2);
        c305.addTestCase(new TestCase(43, c305, "[2, 5, 8, 12, 16], target=12", "3", false));
        c305.addTestCase(new TestCase(44, c305, "[2, 5, 8, 12, 16], target=2", "0", false));
        c305.addTestCase(new TestCase(45, c305, "[2, 5, 8, 12, 16], target=7", "-1", true));
        l8.addChallenge(c305);
        l8.addChallenge(new QuizChallenge(306, "Quiz Conceptual: Paso por Referencia Constante (const T&)",
                "¿Por qué una función de búsqueda en C++ debe recibir const std::vector<int>& en lugar de std::vector<int> por valor?",
                l8, 50,
                Arrays.asList(
                        "Evita copiar los n elementos del vector en memoria (O(1) en paso de parámetro) y protege el contenedor contra cambios",
                        "Obliga al compilador a copiar el vector completo en el registro del procesador",
                        "Convierte el vector en una lista enlazada dinámicamente",
                        "Permite modificar libremente el tamaño del vector dentro de la función"),
                "Evita copiar los n elementos del vector en memoria (O(1) en paso de parámetro) y protege el contenedor contra cambios"));

        // Lección 17 (C++ Algoritmos 2)
        Lesson l17 = new Lesson(17, "Dos Punteros e Iteradores Bidireccionales en std::string",
                "Recorrer std::string con dos índices extremos left y right permite validar simetrías en O(n) sin reservar memoria adicional.",
                2, 85, mod2);
        CodeChallenge c307 = new CodeChallenge(307, "Dos Índices en C++: Verificador de Palíndromos",
                "Completa esPalindromo(std::string texto) recorriendo desde los extremos left y right para evaluar si la cadena es palíndroma.",
                l17, 90,
                "#include <string>\n#include <cctype>\n\n"
                + "bool esPalindromo(std::string texto) {\n"
                + "    int left = 0;\n"
                + "    int right = static_cast<int>(texto.size()) - 1;\n"
                + "    while (left < right) {\n"
                + "        if (std::tolower(texto[left]) != std::tolower(texto[right])) {\n"
                + "            return false;\n"
                + "        }\n"
                + "        left++;\n"
                + "        right--;\n"
                + "    }\n"
                + "    return true;\n"
                + "}\n", 2);
        c307.addTestCase(new TestCase(46, c307, "reconocer", "true", false));
        c307.addTestCase(new TestCase(47, c307, "puntero", "false", false));
        c307.addTestCase(new TestCase(48, c307, "Radar", "true", true));
        l17.addChallenge(c307);
        l17.addChallenge(new QuizChallenge(308, "Quiz Conceptual: Iteradores de Acceso Aleatorio en STL",
                "¿Qué contenedores de la STL de C++ ofrecen iteradores de acceso aleatorio (Random Access Iterators) en tiempo O(1)?",
                l17, 50,
                Arrays.asList(
                        "std::vector, std::deque y std::string al permitir saltos aritméticos it + n en O(1)",
                        "Únicamente std::list y std::forward_list",
                        "Solo std::map y std::set",
                        "Ningún contenedor de la STL soporta acceso aleatorio"),
                "std::vector, std::deque y std::string al permitir saltos aritméticos it + n en O(1)"));

        // Lección 9 (C++ Contenedores 1)
        Lesson l9 = new Lesson(9, "Determinismo RAII, Smart Pointers y std::vector",
                "std::vector<int> garantiza localidad espacial en caché de CPU y distingue entre size() y capacity().",
                1, 80, mod3);
        CodeChallenge c309 = new CodeChallenge(309, "STL std::vector: Acumulador de Elementos Pares",
                "Completa sumarPares(const std::vector<int>& nums) para acumular y retornar la suma de todos los enteros pares del vector.",
                l9, 85,
                "#include <vector>\n\n"
                + "int sumarPares(const std::vector<int>& nums) {\n"
                + "    int total = 0;\n"
                + "    for (int n : nums) {\n"
                + "        if (n % 2 == 0) {\n"
                + "            total += n;\n"
                + "        }\n"
                + "    }\n"
                + "    return total;\n"
                + "}\n", 2);
        c309.addTestCase(new TestCase(49, c309, "[4, 7, 2, 9, 6]", "12", false));
        c309.addTestCase(new TestCase(50, c309, "[1, 3, 5]", "0", false));
        c309.addTestCase(new TestCase(51, c309, "[10, 20, 3, 8]", "38", true));
        l9.addChallenge(c309);
        l9.addChallenge(new QuizChallenge(310, "Quiz Conceptual: size() vs capacity() en std::vector",
                "¿Qué hace el método reserve(n) en un std::vector<T> de C++ antes de insertar una gran cantidad de elementos?",
                l9, 50,
                Arrays.asList(
                        "Pre-asigna memoria contigua para al menos n elementos evitando realocaciones y copias durante push_back()",
                        "Rellena el vector con n ceros modificando su size() inmediatamente",
                        "Ordena los elementos de mayor a menor capacidad",
                        "Bloquea el vector impidiendo agregar más elementos"),
                "Pre-asigna memoria contigua para al menos n elementos evitando realocaciones y copias durante push_back()"));

        // Lección 18 (C++ Contenedores 2)
        Lesson l18 = new Lesson(18, "Tablas Hash en STL (std::unordered_set) en O(1) Promedio",
                "std::unordered_set utiliza una tabla hash para consultar e insertar elementos en tiempo O(1) promedio.",
                2, 100, mod3);
        CodeChallenge c311 = new CodeChallenge(311, "STL std::unordered_set: Primer Duplicado en O(n)",
                "Completa primerDuplicado(const std::vector<int>& nums) empleando std::unordered_set<int> para retornar el primer valor repetido o -1.",
                l18, 115,
                "#include <vector>\n#include <unordered_set>\n\n"
                + "int primerDuplicado(const std::vector<int>& nums) {\n"
                + "    std::unordered_set<int> vistos;\n"
                + "    for (int n : nums) {\n"
                + "        if (vistos.count(n) > 0) {\n"
                + "            return n;\n"
                + "        }\n"
                + "        vistos.insert(n);\n"
                + "    }\n"
                + "    return -1;\n"
                + "}\n", 2);
        c311.addTestCase(new TestCase(52, c311, "[3, 5, 2, 5, 8, 2]", "5", false));
        c311.addTestCase(new TestCase(53, c311, "[10, 20, 30, 10]", "10", false));
        c311.addTestCase(new TestCase(54, c311, "[1, 2, 3, 4]", "-1", true));
        l18.addChallenge(c311);
        l18.addChallenge(new QuizChallenge(312, "Quiz Conceptual: std::unordered_map vs std::map",
                "¿Cuál es la diferencia estructural y de complejidad temporal entre std::unordered_map y std::map en la STL de C++?",
                l18, 65,
                Arrays.asList(
                        "std::unordered_map usa tabla hash con búsqueda O(1) promedio; std::map usa árbol Rojo-Negro ordenado con O(log n)",
                        "Ambos usan tablas hash idénticas pero con distinto nombre de cabecera",
                        "std::map no permite almacenar enteros como valor asociado",
                        "std::unordered_map mantiene las claves ordenadas alfabéticamente en todo momento"),
                "std::unordered_map usa tabla hash con búsqueda O(1) promedio; std::map usa árbol Rojo-Negro ordenado con O(log n)"));

        mod1.addLesson(l7);
        mod1.addLesson(l16);
        mod2.addLesson(l8);
        mod2.addLesson(l17);
        mod3.addLesson(l9);
        mod3.addLesson(l18);
        cppLang.addModule(mod1);
        cppLang.addModule(mod2);
        cppLang.addModule(mod3);
    }

    public static List<Achievement> createSeedAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new StreakAchievement(1, "Constancia de Acero", "Mantén una racha activa de 3 días seguidos practicando.", "FLAME", 3));
        list.add(new StreakAchievement(2, "Hábito Algorítmico", "Alcanza una racha continua de 7 días de estudio.", "FLAME", 7));
        list.add(new ExperienceAchievement(3, "Explorador Políglota", "Acumula tus primeros 1,000 puntos de experiencia (XP).", "BOLT", 1000));
        list.add(new ExperienceAchievement(4, "Arquitecto Senior", "Supera la barrera de los 1,500 XP resolviendo lecciones y retos.", "TROPHY", 1500));
        list.add(new ChallengeCountAchievement(5, "Domador de Casos de Prueba", "Resuelve exitosamente 5 retos de código o quiz.", "CODE", 5));
        list.add(new StreakAchievement(6, "Leyenda del Semestre", "Alcanza una racha histórica de 14 días consecutivos.", "SHIELD", 14));
        list.add(new ChallengeCountAchievement(7, "Maestro de Algoritmos", "Resuelve exitosamente 12 retos entre Java, Python y C++.", "STAR", 12));
        list.add(new ExperienceAchievement(8, "Sabio Tri-Lenguaje", "Acumula 2,500 XP dominando los tres lenguajes del currículo.", "SPARK", 2500));
        return list;
    }

    public static Map<LocalDate, Integer> createSampleActivityHistory() {
        Map<LocalDate, Integer> map = new HashMap<>();
        LocalDate today = LocalDate.now();
        int[] sampleXp = {60, 0, 120, 0, 80, 0, 0, 120, 180, 90, 210, 150, 240, 160};
        for (int i = 0; i < sampleXp.length; i++) {
            LocalDate d = today.minusDays(sampleXp.length - 1 - i);
            if (sampleXp[i] > 0) {
                map.put(d, sampleXp[i]);
            }
        }
        return map;
    }

    /**
     * Devuelve el fragmento comparativo y la nota técnica para una lección y lenguaje dados.
     */
    public static String[] resolveComparativeSnippet(Lesson lesson, String langKey) {
        String title = lesson != null && lesson.getTitle() != null
                ? lesson.getTitle().toLowerCase(Locale.ROOT)
                : "";
        String lang = langKey != null ? langKey.toUpperCase(Locale.ROOT) : "JAVA";

        // Tema 1: Búsqueda Binaria
        if (title.contains("binaria")) {
            if ("JAVA".equals(lang)) {
                return new String[]{
                    "// Java 21: Búsqueda binaria O(log n) libre de overflow\n"
                    + "public static int search(int[] a, int target) {\n"
                    + "    int low = 0, high = a.length - 1;\n"
                    + "    while (low <= high) {\n"
                    + "        int mid = low + (high - low) / 2;\n"
                    + "        if (a[mid] == target) return mid;\n"
                    + "        if (a[mid] < target) low = mid + 1;\n"
                    + "        else high = mid - 1;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}",
                    "Java: 'low + (high - low) / 2' previene desbordamiento aritmético en enteros de 32 bits."
                };
            } else if ("PYTHON".equals(lang)) {
                return new String[]{
                    "# Python 3.12: División entera obligatoria con //\n"
                    + "def search(a: list[int], target: int) -> int:\n"
                    + "    low, high = 0, len(a) - 1\n"
                    + "    while low <= high:\n"
                    + "        mid = (low + high) // 2\n"
                    + "        if a[mid] == target:\n"
                    + "            return mid\n"
                    + "        elif a[mid] < target:\n"
                    + "            low = mid + 1\n"
                    + "        else:\n"
                    + "            high = mid - 1\n"
                    + "    return -1",
                    "Python: int tiene precisión arbitraria, pero '//' es indispensable para obtener un índice entero."
                };
            } else {
                return new String[]{
                    "// C++ 20: Paso por referencia constante (const std::vector<int>&)\n"
                    + "#include <vector>\n\n"
                    + "int search(const std::vector<int>& a, int target) {\n"
                    + "    int low = 0, high = static_cast<int>(a.size()) - 1;\n"
                    + "    while (low <= high) {\n"
                    + "        int mid = low + (high - low) / 2;\n"
                    + "        if (a[mid] == target) return mid;\n"
                    + "        if (a[mid] < target) low = mid + 1;\n"
                    + "        else high = mid - 1;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}",
                    "C++: 'const std::vector<int>&' evita copiar el contenedor en memoria manteniendo O(1) espacial."
                };
            }
        }

        // Tema 2: Dos Punteros / Slicing / Palíndromo
        if (title.contains("punteros") || title.contains("slicing") || title.contains("secuencias")) {
            if ("JAVA".equals(lang)) {
                return new String[]{
                    "// Java 21: Dos punteros sobre String en O(n) tiempo y O(1) espacio\n"
                    + "public static boolean esPalindromo(String texto) {\n"
                    + "    String s = texto.toLowerCase();\n"
                    + "    int left = 0, right = s.length() - 1;\n"
                    + "    while (left < right) {\n"
                    + "        if (s.charAt(left++) != s.charAt(right--)) return false;\n"
                    + "    }\n"
                    + "    return true;\n"
                    + "}",
                    "Java: charAt(i) accede en O(1) sobre el arreglo interno de bytes compactos del String."
                };
            } else if ("PYTHON".equals(lang)) {
                return new String[]{
                    "# Python 3.12: Slicing expresivo con paso negativo [::-1]\n"
                    + "def es_palindromo(texto: str) -> bool:\n"
                    + "    limpio = texto.lower()\n"
                    + "    return limpio == limpio[::-1]",
                    "Python: 'limpio[::-1]' invierte la secuencia en C dentro de CPython de forma declarativa."
                };
            } else {
                return new String[]{
                    "// C++ 20: Dos índices extremos sobre std::string\n"
                    + "#include <string>\n#include <cctype>\n\n"
                    + "bool esPalindromo(const std::string& texto) {\n"
                    + "    int l = 0, r = static_cast<int>(texto.size()) - 1;\n"
                    + "    while (l < r) {\n"
                    + "        if (std::tolower(texto[l++]) != std::tolower(texto[r--])) return false;\n"
                    + "    }\n"
                    + "    return true;\n"
                    + "}",
                    "C++: Recorre el buffer contiguo de std::string sin asignaciones adicionales en el Heap."
                };
            }
        }

        // Tema 3: Tablas Hash (HashSet / set / std::unordered_set)
        if (title.contains("hash") || title.contains("conjuntos") || title.contains("unordered")) {
            if ("JAVA".equals(lang)) {
                return new String[]{
                    "// Java 21: Detección de duplicados en O(n) con HashSet<Integer>\n"
                    + "public static int primerDuplicado(int[] nums) {\n"
                    + "    Set<Integer> vistos = new HashSet<>();\n"
                    + "    for (int n : nums) {\n"
                    + "        if (!vistos.add(n)) return n;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}",
                    "Java: vistos.add(n) retorna false en O(1) promedio si el elemento ya existe en el conjunto."
                };
            } else if ("PYTHON".equals(lang)) {
                return new String[]{
                    "# Python 3.12: Conjunto hash nativo (set) con búsqueda O(1)\n"
                    + "def primer_duplicado(nums: list[int]) -> int:\n"
                    + "    vistos = set()\n"
                    + "    for n in nums:\n"
                    + "        if n in vistos:\n"
                    + "            return n\n"
                    + "        vistos.add(n)\n"
                    + "    return -1",
                    "Python: 'n in vistos' consulta la tabla hash abierta en O(1) promedio."
                };
            } else {
                return new String[]{
                    "// C++ 20: Contenedor asociativo hash std::unordered_set<int>\n"
                    + "#include <vector>\n#include <unordered_set>\n\n"
                    + "int primerDuplicado(const std::vector<int>& nums) {\n"
                    + "    std::unordered_set<int> vistos;\n"
                    + "    for (int n : nums) {\n"
                    + "        if (vistos.count(n) > 0) return n;\n"
                    + "        vistos.insert(n);\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}",
                    "C++: std::unordered_set opera en O(1) promedio frente a O(log n) de std::set."
                };
            }
        }

        // Tema 4: Colecciones dinámicas (ArrayList / list / std::vector)
        if (title.contains("arraylist") || title.contains("listas") || title.contains("vector")) {
            if ("JAVA".equals(lang)) {
                return new String[]{
                    "// Java 21: Recorrido y acumulación de pares en O(n)\n"
                    + "public static int sumarPares(int[] nums) {\n"
                    + "    int total = 0;\n"
                    + "    for (int n : nums) {\n"
                    + "        if (n % 2 == 0) total += n;\n"
                    + "    }\n"
                    + "    return total;\n"
                    + "}",
                    "Java: El bucle for-each recorre el bloque contiguo aprovechando la línea de caché de CPU."
                };
            } else if ("PYTHON".equals(lang)) {
                return new String[]{
                    "# Python 3.12: Filtrado expresivo y acumulación sobre list\n"
                    + "def sumar_pares(nums: list[int]) -> int:\n"
                    + "    return sum(n for n in nums if n % 2 == 0)",
                    "Python: Las expresiones generadoras evalúan perezosamente cada elemento sin duplicar la lista."
                };
            } else {
                return new String[]{
                    "// C++ 20: Range-based for sobre const std::vector<int>&\n"
                    + "#include <vector>\n\n"
                    + "int sumarPares(const std::vector<int>& nums) {\n"
                    + "    int total = 0;\n"
                    + "    for (int n : nums) {\n"
                    + "        if (n % 2 == 0) total += n;\n"
                    + "    }\n"
                    + "    return total;\n"
                    + "}",
                    "C++: std::vector almacena los int sin indirección (unboxed) en un único bloque contiguo."
                };
            }
        }

        // Tema por defecto: POO, Polimorfismo y Contratos
        if ("JAVA".equals(lang)) {
            return new String[]{
                "// Java 21: Clase abstracta y sobrescritura verificada\n"
                + "public abstract class Animal {\n"
                + "    public abstract String emitirSonido();\n"
                + "}\n\n"
                + "public class Perro extends Animal {\n"
                + "    @Override\n"
                + "    public String emitirSonido() {\n"
                + "        return \"Guau\";\n"
                + "    }\n"
                + "}",
                "Java: @Override valida en tiempo de compilación que la firma coincida con la superclase."
            };
        } else if ("PYTHON".equals(lang)) {
            return new String[]{
                "# Python 3.12: Clases Base Abstractas con el módulo abc\n"
                + "from abc import ABC, abstractmethod\n\n"
                + "class Animal(ABC):\n"
                + "    @abstractmethod\n"
                + "    def emitir_sonido(self) -> str:\n"
                + "        pass\n\n"
                + "class Perro(Animal):\n"
                + "    def emitir_sonido(self) -> str:\n"
                + "        return \"Guau\"",
                "Python: @abstractmethod impide instanciar Animal si la subclase no implementa emitir_sonido()."
            };
        } else {
            return new String[]{
                "// C++ 20: Función virtual pura (= 0) y destructor virtual\n"
                + "#include <string>\n\n"
                + "class Animal {\n"
                + "public:\n"
                + "    virtual ~Animal() = default;\n"
                + "    virtual std::string emitirSonido() const = 0;\n"
                + "};\n\n"
                + "class Perro : public Animal {\n"
                + "public:\n"
                + "    std::string emitirSonido() const override {\n"
                + "        return \"Guau\";\n"
                + "    }\n"
                + "};",
                "C++: '= 0' define una clase abstracta pura y 'override' garantiza despacho vía vtable."
            };
        }
    }
}
