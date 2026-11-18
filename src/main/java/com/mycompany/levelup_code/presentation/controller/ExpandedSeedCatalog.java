package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import java.util.List;

/** Additional modules shared by the Oracle catalog and the offline demo catalog. */
final class ExpandedSeedCatalog {

    private static final String[] MODULE_TITLES = {
        "Módulo 4: Recursividad y Ordenamiento",
        "Módulo 5: Pilas, Colas y Procesamiento",
        "Módulo 6: Árboles y Grafos"
    };

    private static final String[] MODULE_DESCRIPTIONS = {
        "Casos base, pila de llamadas, división y conquista y ordenamiento O(n log n).",
        "Estructuras LIFO/FIFO, validación de expresiones y simulación de turnos.",
        "Árboles binarios, recorridos recursivos y exploración BFS/DFS de grafos."
    };

    private static final String[] LESSON_TITLES = {
        "Recursividad, Casos Base y Pila de Llamadas",
        "Merge Sort, División y Conquista y Complejidad",
        "Pilas (LIFO) y Validación de Expresiones",
        "Colas (FIFO), Prioridades y Simulación de Turnos",
        "Árboles Binarios y Recorridos Recursivos",
        "Grafos y Recorridos BFS y DFS"
    };

    private static final String[] THEORIES = {
        "1. CASO BASE Y PASO RECURSIVO\nToda función recursiva debe acercarse a un caso base que detenga las llamadas.\n\n2. PILA DE LLAMADAS\nCada invocación conserva sus parámetros y variables locales. La profundidad debe controlarse para evitar desbordar la pila.",
        "1. DIVISIÓN Y CONQUISTA\nMerge Sort divide la colección, ordena cada mitad y combina los resultados.\n\n2. COMPLEJIDAD\nSu tiempo es O(n log n) y utiliza O(n) memoria auxiliar, ofreciendo un rendimiento predecible.",
        "1. PRINCIPIO LIFO\nUna pila retira primero el último elemento insertado.\n\n2. APLICACIÓN\nLos delimitadores se validan apilando aperturas y comprobando que cada cierre corresponda al elemento superior.",
        "1. PRINCIPIO FIFO\nUna cola atiende primero al elemento que llegó primero.\n\n2. SIMULACIÓN\nLos tiempos de espera se obtienen acumulando la duración de las tareas anteriores sin modificar su orden.",
        "1. ESTRUCTURA JERÁRQUICA\nUn árbol binario conecta cada nodo con hasta dos hijos.\n\n2. RECORRIDOS\nPreorden, inorden y postorden se expresan naturalmente con recursividad. La altura depende del camino más largo hasta una hoja.",
        "1. REPRESENTACIÓN\nUn grafo relaciona vértices mediante aristas y puede almacenarse con listas de adyacencia.\n\n2. EXPLORACIÓN\nBFS usa una cola y DFS usa recursividad o una pila. Un conjunto de visitados evita ciclos."
    };

    private ExpandedSeedCatalog() { }

    static void addTo(List<Language> languages) {
        for (int languageIndex = 0; languageIndex < languages.size(); languageIndex++) {
            Language language = languages.get(languageIndex);
            for (int moduleOffset = 0; moduleOffset < 3; moduleOffset++) {
                int moduleId = 10 + languageIndex * 3 + moduleOffset;
                CourseModule module = new CourseModule(moduleId,
                        MODULE_TITLES[moduleOffset] + " en " + language.getName(),
                        MODULE_DESCRIPTIONS[moduleOffset], 4 + moduleOffset, language);

                for (int lessonOffset = 0; lessonOffset < 2; lessonOffset++) {
                    int topic = moduleOffset * 2 + lessonOffset;
                    int lessonId = 19 + languageIndex * 6 + topic;
                    Lesson lesson = new Lesson(lessonId,
                            LESSON_TITLES[topic] + " en " + language.getName(),
                            THEORIES[topic] + languageNote(languageIndex, topic),
                            lessonOffset + 1, 105 + topic * 5, module);
                    addActivities(lesson, languageIndex, topic);
                    module.addLesson(lesson);
                }
                language.addModule(module);
            }
        }
    }

    private static String languageNote(int languageIndex, int topic) {
        String detail = switch (languageIndex) {
            case 0 -> switch (topic) {
                case 0 -> "La JVM limita la profundidad mediante el tamaño del stack de cada hilo.";
                case 1 -> "Java permite comparar soluciones manuales con Arrays.sort y Comparator.";
                case 2 -> "Deque<Character> evita las limitaciones históricas de Stack.";
                case 3 -> "ArrayDeque es una cola eficiente para operaciones en ambos extremos.";
                case 4 -> "Los nodos pueden modelarse con clases genéricas y referencias tipadas.";
                default -> "ArrayDeque implementa la frontera de BFS y HashSet registra los visitados.";
            };
            case 1 -> switch (topic) {
                case 0 -> "Python aplica un límite de recursión y favorece alternativas iterativas para profundidades grandes.";
                case 1 -> "sorted() usa Timsort; implementar Merge Sort permite estudiar la estrategia de división.";
                case 2 -> "Una list funciona como pila con append() y pop().";
                case 3 -> "collections.deque ofrece popleft() en tiempo O(1).";
                case 4 -> "Los nodos se representan de forma clara con clases o dataclasses.";
                default -> "collections.deque y set permiten implementar BFS de forma directa.";
            };
            default -> switch (topic) {
                case 0 -> "La recursividad utiliza la pila nativa y debe respetar la vida de los objetos automáticos.";
                case 1 -> "std::sort es introspectivo; Merge Sort ayuda a estudiar estabilidad y memoria auxiliar.";
                case 2 -> "std::stack adapta un contenedor para exponer push, top y pop.";
                case 3 -> "std::queue implementa FIFO y std::priority_queue atiende por prioridad.";
                case 4 -> "RAII permite que cada nodo administre su propiedad con punteros inteligentes.";
                default -> "std::queue y std::unordered_set forman una implementación eficiente de BFS.";
            };
        };
        return "\n\n3. APLICACIÓN EN EL LENGUAJE\n" + detail;
    }

    private static void addActivities(Lesson lesson, int languageIndex, int topic) {
        int codeId = (languageIndex + 1) * 1000 + topic * 2 + 1;
        int quizId = codeId + 1;
        SeedChallengeContent.Exercise exercise = exercise(languageIndex, topic);
        CodeChallenge code = new CodeChallenge(codeId, codeTitle(topic, lesson.getModule().getLanguage().getName()),
                codeStatement(topic), lesson, 105 + topic * 5, exercise.starter(), 2);
        List<TestSpec> tests = tests(topic);
        for (int i = 0; i < tests.size(); i++) {
            TestSpec test = tests.get(i);
            code.addTestCase(new TestCase(1000 + languageIndex * 100 + topic * 3 + i,
                    code, test.input(), test.output(), i == tests.size() - 1));
        }
        lesson.addChallenge(code);

        QuizSpec quiz = quiz(topic);
        lesson.addChallenge(new QuizChallenge(quizId, "Quiz: " + LESSON_TITLES[topic],
                quiz.question(), lesson, 60 + topic * 5, quiz.options(), quiz.answer()));
    }

    static SeedChallengeContent.Exercise exerciseForId(int challengeId) {
        int language = challengeId / 1000 - 1;
        int local = challengeId % 1000;
        if (language < 0 || language > 2 || local < 1 || local > 11 || local % 2 == 0) return null;
        return exercise(language, (local - 1) / 2);
    }

    private static SeedChallengeContent.Exercise exercise(int language, int topic) {
        return switch (language) {
            case 0 -> javaExercise(topic);
            case 1 -> pythonExercise(topic);
            default -> cppExercise(topic);
        };
    }

    private static SeedChallengeContent.Exercise javaExercise(int topic) {
        return switch (topic) {
            case 0 -> new SeedChallengeContent.Exercise(
                    "public class RecursionSolver {\n    public static long factorial(int n) {\n        // TODO: define el caso base y el paso recursivo.\n        return 0;\n    }\n}\n",
                    "public class RecursionSolver {\n    public static long factorial(int n) {\n        if (n <= 1) return 1;\n        return n * factorial(n - 1);\n    }\n}\n");
            case 1 -> new SeedChallengeContent.Exercise(
                    "public class MergeSortSolver {\n    public static String ordenar(int[] nums) {\n        // TODO: ordena la colección y devuelve Arrays.toString(nums).\n        return \"\";\n    }\n}\n",
                    "public class MergeSortSolver {\n    public static String ordenar(int[] nums) {\n        mergeSort(nums, 0, nums.length - 1);\n        return java.util.Arrays.toString(nums);\n    }\n    private static void mergeSort(int[] a, int left, int right) {\n        if (left >= right) return;\n        int mid = left + (right - left) / 2;\n        mergeSort(a, left, mid); mergeSort(a, mid + 1, right);\n        int[] tmp = new int[right - left + 1]; int i = left, j = mid + 1, k = 0;\n        while (i <= mid && j <= right) tmp[k++] = a[i] <= a[j] ? a[i++] : a[j++];\n        while (i <= mid) tmp[k++] = a[i++]; while (j <= right) tmp[k++] = a[j++];\n        System.arraycopy(tmp, 0, a, left, tmp.length);\n    }\n}\n");
            case 2 -> new SeedChallengeContent.Exercise(
                    "public class StackSolver {\n    public static boolean balanceados(String texto) {\n        // TODO: usa una pila para validar (), [] y {}.\n        return false;\n    }\n}\n",
                    "public class StackSolver {\n    public static boolean balanceados(String texto) {\n        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();\n        for (char c : texto.toCharArray()) {\n            if (c == '(' || c == '[' || c == '{') stack.push(c);\n            else if (c == ')' || c == ']' || c == '}') {\n                if (stack.isEmpty()) return false; char open = stack.pop();\n                if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) return false;\n            }\n        }\n        return stack.isEmpty();\n    }\n}\n");
            case 3 -> new SeedChallengeContent.Exercise(
                    "public class QueueSolver {\n    public static int esperaTotal(int[] duraciones) {\n        // TODO: acumula el tiempo esperado por cada turno.\n        return 0;\n    }\n}\n",
                    "public class QueueSolver {\n    public static int esperaTotal(int[] duraciones) {\n        int transcurrido = 0, total = 0;\n        for (int duracion : duraciones) { total += transcurrido; transcurrido += duracion; }\n        return total;\n    }\n}\n");
            case 4 -> new SeedChallengeContent.Exercise(
                    "public class TreeSolver {\n    public static int altura(int[] valores) {\n        // TODO: inserta los valores en un BST y calcula su altura.\n        return 0;\n    }\n}\n",
                    "public class TreeSolver {\n    private static class Node { int value; Node left, right; Node(int value) { this.value = value; } }\n    public static int altura(int[] valores) { Node root = null; for (int v : valores) root = insert(root, v); return height(root); }\n    private static Node insert(Node n, int v) { if (n == null) return new Node(v); if (v < n.value) n.left = insert(n.left, v); else if (v > n.value) n.right = insert(n.right, v); return n; }\n    private static int height(Node n) { return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right)); }\n}\n");
            default -> new SeedChallengeContent.Exercise(
                    "public class GraphSolver {\n    public static int alcanzables(String entrada) {\n        // Formato: aristas separadas por coma, punto y coma, origen. Ejemplo 0-1,1-2;0\n        return 0;\n    }\n}\n",
                    "public class GraphSolver {\n    public static int alcanzables(String entrada) {\n        String[] parts = entrada.split(\";\"); int start = Integer.parseInt(parts[1]);\n        java.util.Map<Integer, java.util.List<Integer>> graph = new java.util.HashMap<>();\n        if (!parts[0].isBlank()) for (String edge : parts[0].split(\",\")) { String[] uv = edge.split(\"-\"); int u = Integer.parseInt(uv[0]), v = Integer.parseInt(uv[1]); graph.computeIfAbsent(u, k -> new java.util.ArrayList<>()).add(v); graph.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(u); }\n        java.util.Set<Integer> visited = new java.util.HashSet<>(); java.util.Queue<Integer> queue = new java.util.ArrayDeque<>(); visited.add(start); queue.add(start);\n        while (!queue.isEmpty()) { int u = queue.remove(); for (int v : graph.getOrDefault(u, java.util.List.of())) if (visited.add(v)) queue.add(v); }\n        return visited.size();\n    }\n}\n");
        };
    }

    private static SeedChallengeContent.Exercise pythonExercise(int topic) {
        return switch (topic) {
            case 0 -> new SeedChallengeContent.Exercise("def factorial(n):\n    # TODO: define el caso base y el paso recursivo.\n    return 0\n",
                    "def factorial(n):\n    if n <= 1:\n        return 1\n    return n * factorial(n - 1)\n");
            case 1 -> new SeedChallengeContent.Exercise("def ordenar(nums):\n    # TODO: implementa Merge Sort y retorna la lista ordenada.\n    return []\n",
                    "def ordenar(nums):\n    if len(nums) <= 1:\n        return nums\n    mid = len(nums) // 2\n    left, right = ordenar(nums[:mid]), ordenar(nums[mid:])\n    result = []\n    while left and right:\n        result.append(left.pop(0) if left[0] <= right[0] else right.pop(0))\n    return result + left + right\n");
            case 2 -> new SeedChallengeContent.Exercise("def balanceados(texto):\n    # TODO: usa una lista como pila.\n    return False\n",
                    "def balanceados(texto):\n    stack = []\n    pairs = {')': '(', ']': '[', '}': '{'}\n    for char in texto:\n        if char in '([{': stack.append(char)\n        elif char in pairs:\n            if not stack or stack.pop() != pairs[char]: return False\n    return not stack\n");
            case 3 -> new SeedChallengeContent.Exercise("def espera_total(duraciones):\n    # TODO: acumula el tiempo esperado por cada turno.\n    return 0\n",
                    "def espera_total(duraciones):\n    transcurrido = total = 0\n    for duracion in duraciones:\n        total += transcurrido\n        transcurrido += duracion\n    return total\n");
            case 4 -> new SeedChallengeContent.Exercise("def altura(valores):\n    # TODO: construye un BST y calcula su altura.\n    return 0\n",
                    "def altura(valores):\n    tree = None\n    def insert(node, value):\n        if node is None: return [value, None, None]\n        if value < node[0]: node[1] = insert(node[1], value)\n        elif value > node[0]: node[2] = insert(node[2], value)\n        return node\n    def height(node): return 0 if node is None else 1 + max(height(node[1]), height(node[2]))\n    for value in valores: tree = insert(tree, value)\n    return height(tree)\n");
            default -> new SeedChallengeContent.Exercise("def alcanzables(entrada):\n    # TODO: analiza las aristas y recorre el grafo con BFS.\n    return 0\n",
                    "from collections import deque\ndef alcanzables(entrada):\n    raw, origin = entrada.split(';')\n    graph = {}\n    for edge in filter(None, raw.split(',')):\n        u, v = map(int, edge.split('-'))\n        graph.setdefault(u, []).append(v); graph.setdefault(v, []).append(u)\n    visited, queue = {int(origin)}, deque([int(origin)])\n    while queue:\n        u = queue.popleft()\n        for v in graph.get(u, []):\n            if v not in visited: visited.add(v); queue.append(v)\n    return len(visited)\n");
        };
    }

    private static SeedChallengeContent.Exercise cppExercise(int topic) {
        return switch (topic) {
            case 0 -> new SeedChallengeContent.Exercise("long long factorial(int n) {\n    // TODO: define el caso base y el paso recursivo.\n    return 0;\n}\n",
                    "long long factorial(int n) {\n    if (n <= 1) return 1;\n    return n * factorial(n - 1);\n}\n");
            case 1 -> new SeedChallengeContent.Exercise("std::vector<int> ordenar(std::vector<int> nums) {\n    // TODO: implementa Merge Sort.\n    return {};\n}\n",
                    "std::vector<int> ordenar(std::vector<int> nums) {\n    if (nums.size() <= 1) return nums;\n    auto mid = nums.begin() + nums.size() / 2;\n    auto left = ordenar({nums.begin(), mid}); auto right = ordenar({mid, nums.end()});\n    std::vector<int> result; std::merge(left.begin(), left.end(), right.begin(), right.end(), std::back_inserter(result));\n    return result;\n}\n");
            case 2 -> new SeedChallengeContent.Exercise("bool balanceados(const std::string& texto) {\n    // TODO: usa std::stack para validar delimitadores.\n    return false;\n}\n",
                    "bool balanceados(const std::string& texto) {\n    std::stack<char> stack;\n    for (char c : texto) {\n        if (c == '(' || c == '[' || c == '{') stack.push(c);\n        else if (c == ')' || c == ']' || c == '}') { if (stack.empty()) return false; char open = stack.top(); stack.pop(); if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) return false; }\n    }\n    return stack.empty();\n}\n");
            case 3 -> new SeedChallengeContent.Exercise("int esperaTotal(const std::vector<int>& duraciones) {\n    // TODO: acumula el tiempo esperado por cada turno.\n    return 0;\n}\n",
                    "int esperaTotal(const std::vector<int>& duraciones) {\n    int transcurrido = 0, total = 0;\n    for (int duracion : duraciones) { total += transcurrido; transcurrido += duracion; }\n    return total;\n}\n");
            case 4 -> new SeedChallengeContent.Exercise("int altura(const std::vector<int>& valores) {\n    // TODO: construye un BST y calcula su altura.\n    return 0;\n}\n",
                    "struct Node { int value; std::unique_ptr<Node> left, right; explicit Node(int v): value(v) {} };\n+void insert(std::unique_ptr<Node>& n, int v) { if (!n) n = std::make_unique<Node>(v); else if (v < n->value) insert(n->left, v); else if (v > n->value) insert(n->right, v); }\n+int height(const std::unique_ptr<Node>& n) { return n ? 1 + std::max(height(n->left), height(n->right)) : 0; }\n+int altura(const std::vector<int>& valores) { std::unique_ptr<Node> root; for (int v : valores) insert(root, v); return height(root); }\n");
            default -> new SeedChallengeContent.Exercise("int alcanzables(const std::string& entrada) {\n    // TODO: construye la lista de adyacencia y recorre con BFS.\n    return 0;\n}\n",
                    "int alcanzables(const std::string& entrada) {\n    auto sep = entrada.find(';'); int start = std::stoi(entrada.substr(sep + 1)); std::unordered_map<int, std::vector<int>> graph;\n    std::stringstream ss(entrada.substr(0, sep)); std::string edge;\n    while (std::getline(ss, edge, ',')) { auto dash = edge.find('-'); int u = std::stoi(edge.substr(0, dash)), v = std::stoi(edge.substr(dash + 1)); graph[u].push_back(v); graph[v].push_back(u); }\n    std::queue<int> queue; std::unordered_set<int> visited{start}; queue.push(start);\n    while (!queue.empty()) { int u = queue.front(); queue.pop(); for (int v : graph[u]) if (visited.insert(v).second) queue.push(v); }\n    return static_cast<int>(visited.size());\n}\n");
        };
    }

    private static List<TestSpec> tests(int topic) {
        return switch (topic) {
            case 0 -> List.of(new TestSpec("0", "1"), new TestSpec("5", "120"), new TestSpec("7", "5040"));
            case 1 -> List.of(new TestSpec("[5, 1, 4, 2]", "[1, 2, 4, 5]"), new TestSpec("[3, -1, 3]", "[-1, 3, 3]"), new TestSpec("[]", "[]"));
            case 2 -> List.of(new TestSpec("()[]{}", "true"), new TestSpec("([{}])", "true"), new TestSpec("([)]", "false"));
            case 3 -> List.of(new TestSpec("[3, 2, 1]", "8"), new TestSpec("[5]", "0"), new TestSpec("[1, 1, 1, 1]", "6"));
            case 4 -> List.of(new TestSpec("[4, 2, 6, 1, 3, 5, 7]", "3"), new TestSpec("[5]", "1"), new TestSpec("[5, 4, 3, 2]", "4"));
            default -> List.of(new TestSpec("0-1,1-2,3-4;0", "3"), new TestSpec("0-1,1-2,2-3;1", "4"), new TestSpec(";7", "1"));
        };
    }

    private static QuizSpec quiz(int topic) {
        return switch (topic) {
            case 0 -> new QuizSpec("¿Qué condición es indispensable para que una función recursiva termine correctamente?",
                    List.of("Tener un caso base alcanzable", "Crear siempre una colección", "Usar dos bucles anidados", "Declarar todas las variables globales"), "Tener un caso base alcanzable");
            case 1 -> new QuizSpec("¿Cuál es la complejidad temporal de Merge Sort para n elementos?",
                    List.of("O(n log n)", "O(n²) en todos los casos", "O(log n)", "O(1)"), "O(n log n)");
            case 2 -> new QuizSpec("¿Qué elemento se retira primero de una pila LIFO?",
                    List.of("El último que fue insertado", "El primero que fue insertado", "El de menor valor", "Un elemento aleatorio"), "El último que fue insertado");
            case 3 -> new QuizSpec("¿Qué propiedad define el comportamiento de una cola FIFO?",
                    List.of("El primero en entrar es el primero en salir", "El último en entrar es el primero en salir", "Siempre sale el elemento mayor", "Los elementos no conservan ningún orden"), "El primero en entrar es el primero en salir");
            case 4 -> new QuizSpec("¿Qué recorrido de un árbol binario visita izquierda, raíz y derecha?",
                    List.of("Inorden", "Preorden", "Postorden", "BFS inverso"), "Inorden");
            default -> new QuizSpec("¿Qué estructura utiliza normalmente BFS para administrar los vértices pendientes?",
                    List.of("Una cola", "Una pila exclusivamente", "Un árbol de búsqueda", "Una tabla sin visitados"), "Una cola");
        };
    }

    private static String codeTitle(int topic, String language) {
        return switch (topic) {
            case 0 -> "Recursividad: Factorial Seguro en " + language;
            case 1 -> "Ordenamiento: Merge Sort en " + language;
            case 2 -> "Pilas: Delimitadores Balanceados en " + language;
            case 3 -> "Colas: Tiempo Total de Espera en " + language;
            case 4 -> "Árboles: Altura de un BST en " + language;
            default -> "Grafos: Vértices Alcanzables con BFS en " + language;
        };
    }

    private static String codeStatement(int topic) {
        return switch (topic) {
            case 0 -> "Implementa factorial(n) usando un caso base y una llamada recursiva. Retorna 1 para n igual a 0.";
            case 1 -> "Ordena los enteros de menor a mayor aplicando la estrategia de Merge Sort.";
            case 2 -> "Retorna true si los delimitadores (), [] y {} están correctamente balanceados y false en caso contrario.";
            case 3 -> "Calcula la suma de los tiempos de espera de todos los turnos atendidos en orden FIFO.";
            case 4 -> "Inserta los valores en un árbol binario de búsqueda y retorna su altura; un árbol vacío tiene altura 0.";
            default -> "Interpreta las aristas no dirigidas con formato u-v y retorna cuántos vértices son alcanzables desde el origen usando BFS.";
        };
    }

    private record TestSpec(String input, String output) { }
    private record QuizSpec(String question, List<String> options, String answer) { }
}
