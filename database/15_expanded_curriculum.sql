-- LEVELUP CODE - EXPANSIÓN CURRICULAR
-- Ejecutar después de 14_module_finals.sql y antes de recompilar 03_plsql_gamificacion.sql.
-- Añade 3 módulos y 6 lecciones por lenguaje, 36 retos base, 9 finales y 9 logros.
-- Es idempotente y no elimina progreso ni sobrescribe retos que ya hayan sido creados.
SET DEFINE OFF;
SET SERVEROUTPUT ON;

DECLARE
    v_created_modules NUMBER := 0;
    v_created_lessons NUMBER := 0;
    v_created_challenges NUMBER := 0;
    v_created_achievements NUMBER := 0;
    v_module NUMBER;
    v_lesson NUMBER;
    v_language_id NUMBER;
    v_topic NUMBER;

    FUNCTION language_name(p_language NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_language WHEN 1 THEN 'Java' WHEN 2 THEN 'Python' ELSE 'C++' END;
    END;

    FUNCTION module_title(p_module_order NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_module_order
            WHEN 4 THEN 'Recursividad y Ordenamiento'
            WHEN 5 THEN 'Pilas, Colas y Procesamiento'
            ELSE 'Árboles y Grafos'
        END;
    END;

    FUNCTION module_description(p_module_order NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_module_order
            WHEN 4 THEN 'Casos base, pila de llamadas, división y conquista y ordenamiento O(n log n).'
            WHEN 5 THEN 'Estructuras LIFO/FIFO, validación de expresiones y simulación de turnos.'
            ELSE 'Árboles binarios, recorridos recursivos y exploración BFS/DFS de grafos.'
        END;
    END;

    FUNCTION lesson_title(p_topic NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_topic
            WHEN 1 THEN 'Recursividad, Casos Base y Pila de Llamadas'
            WHEN 2 THEN 'Merge Sort, División y Conquista y Complejidad'
            WHEN 3 THEN 'Pilas (LIFO) y Validación de Expresiones'
            WHEN 4 THEN 'Colas (FIFO), Prioridades y Simulación de Turnos'
            WHEN 5 THEN 'Árboles Binarios y Recorridos Recursivos'
            ELSE 'Grafos y Recorridos BFS y DFS'
        END;
    END;

    FUNCTION lesson_theory(p_topic NUMBER, p_language NUMBER) RETURN CLOB IS
        v_language VARCHAR2(20) := language_name(p_language);
    BEGIN
        RETURN TO_CLOB(CASE p_topic
            WHEN 1 THEN '1. CASO BASE Y PASO RECURSIVO' || CHR(10) ||
                'Toda función recursiva debe acercarse a un caso base que detenga las llamadas.' || CHR(10) || CHR(10) ||
                '2. PILA DE LLAMADAS' || CHR(10) ||
                'Cada invocación conserva parámetros y variables locales. En ' || v_language || ' debe controlarse la profundidad para evitar desbordar la pila.'
            WHEN 2 THEN '1. DIVISIÓN Y CONQUISTA' || CHR(10) ||
                'Merge Sort divide la colección, ordena cada mitad y combina los resultados.' || CHR(10) || CHR(10) ||
                '2. COMPLEJIDAD' || CHR(10) ||
                'Su tiempo es O(n log n) y utiliza O(n) memoria auxiliar. La implementación se adapta a las colecciones de ' || v_language || '.'
            WHEN 3 THEN '1. PRINCIPIO LIFO' || CHR(10) ||
                'Una pila retira primero el último elemento insertado.' || CHR(10) || CHR(10) ||
                '2. APLICACIÓN' || CHR(10) ||
                'Los delimitadores se validan apilando aperturas y comprobando cada cierre mediante las estructuras de ' || v_language || '.'
            WHEN 4 THEN '1. PRINCIPIO FIFO' || CHR(10) ||
                'Una cola atiende primero al elemento que llegó primero.' || CHR(10) || CHR(10) ||
                '2. SIMULACIÓN' || CHR(10) ||
                'Los tiempos de espera se obtienen acumulando las duraciones anteriores en el orden de llegada.'
            WHEN 5 THEN '1. ESTRUCTURA JERÁRQUICA' || CHR(10) ||
                'Un árbol binario conecta cada nodo con hasta dos hijos.' || CHR(10) || CHR(10) ||
                '2. RECORRIDOS' || CHR(10) ||
                'Preorden, inorden y postorden se expresan naturalmente con recursividad. La altura sigue el camino más largo hasta una hoja.'
            ELSE '1. REPRESENTACIÓN' || CHR(10) ||
                'Un grafo relaciona vértices mediante aristas y puede almacenarse con listas de adyacencia.' || CHR(10) || CHR(10) ||
                '2. EXPLORACIÓN' || CHR(10) ||
                'BFS usa una cola y DFS usa recursividad o una pila. Un conjunto de visitados evita ciclos.'
        END);
    END;

    FUNCTION challenge_title(p_topic NUMBER, p_language NUMBER) RETURN VARCHAR2 IS
        v_language VARCHAR2(20) := language_name(p_language);
    BEGIN
        RETURN CASE p_topic
            WHEN 1 THEN 'Recursividad: Factorial Seguro en ' || v_language
            WHEN 2 THEN 'Ordenamiento: Merge Sort en ' || v_language
            WHEN 3 THEN 'Pilas: Delimitadores Balanceados en ' || v_language
            WHEN 4 THEN 'Colas: Tiempo Total de Espera en ' || v_language
            WHEN 5 THEN 'Árboles: Altura de un BST en ' || v_language
            ELSE 'Grafos: Vértices Alcanzables con BFS en ' || v_language
        END;
    END;

    FUNCTION challenge_statement(p_topic NUMBER) RETURN CLOB IS
    BEGIN
        RETURN TO_CLOB(CASE p_topic
            WHEN 1 THEN 'Implementa factorial(n) usando un caso base y una llamada recursiva. Retorna 1 para n igual a 0.'
            WHEN 2 THEN 'Ordena los enteros de menor a mayor aplicando la estrategia de Merge Sort.'
            WHEN 3 THEN 'Retorna true si los delimitadores (), [] y {} están correctamente balanceados y false en caso contrario.'
            WHEN 4 THEN 'Calcula la suma de los tiempos de espera de todos los turnos atendidos en orden FIFO.'
            WHEN 5 THEN 'Inserta los valores en un árbol binario de búsqueda y retorna su altura; un árbol vacío tiene altura 0.'
            ELSE 'Interpreta aristas no dirigidas con formato u-v y retorna cuántos vértices son alcanzables desde el origen usando BFS.'
        END);
    END;

    FUNCTION starter_code(p_language NUMBER, p_topic NUMBER) RETURN CLOB IS
    BEGIN
        IF p_language = 1 THEN
            RETURN CASE p_topic
                WHEN 1 THEN TO_CLOB(q'~public class RecursionSolver {
    public static long factorial(int n) {
        // TODO: define el caso base y el paso recursivo.
        return 0;
    }
}
~')
                WHEN 2 THEN TO_CLOB(q'~public class MergeSortSolver {
    public static String ordenar(int[] nums) {
        // TODO: implementa Merge Sort y devuelve Arrays.toString(nums).
        return "";
    }
}
~')
                WHEN 3 THEN TO_CLOB(q'~public class StackSolver {
    public static boolean balanceados(String texto) {
        // TODO: usa una pila para validar (), [] y {}.
        return false;
    }
}
~')
                WHEN 4 THEN TO_CLOB(q'~public class QueueSolver {
    public static int esperaTotal(int[] duraciones) {
        // TODO: acumula el tiempo esperado por cada turno.
        return 0;
    }
}
~')
                WHEN 5 THEN TO_CLOB(q'~public class TreeSolver {
    public static int altura(int[] valores) {
        // TODO: inserta los valores en un BST y calcula su altura.
        return 0;
    }
}
~')
                ELSE TO_CLOB(q'~public class GraphSolver {
    public static int alcanzables(String entrada) {
        // TODO: construye la lista de adyacencia y recorre con BFS.
        return 0;
    }
}
~')
            END;
        ELSIF p_language = 2 THEN
            RETURN CASE p_topic
                WHEN 1 THEN TO_CLOB(q'~def factorial(n):
    # TODO: define el caso base y el paso recursivo.
    return 0
~')
                WHEN 2 THEN TO_CLOB(q'~def ordenar(nums):
    # TODO: implementa Merge Sort.
    return []
~')
                WHEN 3 THEN TO_CLOB(q'~def balanceados(texto):
    # TODO: usa una lista como pila.
    return False
~')
                WHEN 4 THEN TO_CLOB(q'~def espera_total(duraciones):
    # TODO: acumula el tiempo esperado por cada turno.
    return 0
~')
                WHEN 5 THEN TO_CLOB(q'~def altura(valores):
    # TODO: construye un BST y calcula su altura.
    return 0
~')
                ELSE TO_CLOB(q'~def alcanzables(entrada):
    # TODO: construye la lista de adyacencia y recorre con BFS.
    return 0
~')
            END;
        ELSE
            RETURN CASE p_topic
                WHEN 1 THEN TO_CLOB(q'~long long factorial(int n) {
    // TODO: define el caso base y el paso recursivo.
    return 0;
}
~')
                WHEN 2 THEN TO_CLOB(q'~std::vector<int> ordenar(std::vector<int> nums) {
    // TODO: implementa Merge Sort.
    return {};
}
~')
                WHEN 3 THEN TO_CLOB(q'~bool balanceados(const std::string& texto) {
    // TODO: usa std::stack para validar delimitadores.
    return false;
}
~')
                WHEN 4 THEN TO_CLOB(q'~int esperaTotal(const std::vector<int>& duraciones) {
    // TODO: acumula el tiempo esperado por cada turno.
    return 0;
}
~')
                WHEN 5 THEN TO_CLOB(q'~int altura(const std::vector<int>& valores) {
    // TODO: construye un BST y calcula su altura.
    return 0;
}
~')
                ELSE TO_CLOB(q'~int alcanzables(const std::string& entrada) {
    // TODO: construye la lista de adyacencia y recorre con BFS.
    return 0;
}
~')
            END;
        END IF;
    END;

    FUNCTION solution_code(p_language NUMBER, p_topic NUMBER) RETURN CLOB IS
    BEGIN
        IF p_language = 1 THEN
            RETURN CASE p_topic
                WHEN 1 THEN TO_CLOB(q'~public class RecursionSolver {
    public static long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }
}
~')
                WHEN 2 THEN TO_CLOB(q'~public class MergeSortSolver {
    public static String ordenar(int[] nums) { java.util.Arrays.sort(nums); return java.util.Arrays.toString(nums); }
}
~')
                WHEN 3 THEN TO_CLOB(q'~public class StackSolver {
    public static boolean balanceados(String texto) {
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();
        for (char c : texto.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') stack.push(c);
            else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) return false; char open = stack.pop();
                if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) return false;
            }
        }
        return stack.isEmpty();
    }
}
~')
                WHEN 4 THEN TO_CLOB(q'~public class QueueSolver {
    public static int esperaTotal(int[] duraciones) {
        int elapsed = 0, total = 0;
        for (int duration : duraciones) { total += elapsed; elapsed += duration; }
        return total;
    }
}
~')
                WHEN 5 THEN TO_CLOB(q'~public class TreeSolver {
    private static class Node { int value; Node left, right; Node(int value) { this.value = value; } }
    public static int altura(int[] values) { Node root = null; for (int value : values) root = insert(root, value); return height(root); }
    private static Node insert(Node node, int value) { if (node == null) return new Node(value); if (value < node.value) node.left = insert(node.left, value); else if (value > node.value) node.right = insert(node.right, value); return node; }
    private static int height(Node node) { return node == null ? 0 : 1 + Math.max(height(node.left), height(node.right)); }
}
~')
                ELSE TO_CLOB(q'~public class GraphSolver {
    public static int alcanzables(String input) {
        String[] parts = input.split(";"); int start = Integer.parseInt(parts[1]);
        java.util.Map<Integer, java.util.List<Integer>> graph = new java.util.HashMap<>();
        if (!parts[0].isBlank()) for (String edge : parts[0].split(",")) { String[] uv = edge.split("-"); int u = Integer.parseInt(uv[0]), v = Integer.parseInt(uv[1]); graph.computeIfAbsent(u, k -> new java.util.ArrayList<>()).add(v); graph.computeIfAbsent(v, k -> new java.util.ArrayList<>()).add(u); }
        java.util.Set<Integer> visited = new java.util.HashSet<>(); java.util.Queue<Integer> queue = new java.util.ArrayDeque<>(); visited.add(start); queue.add(start);
        while (!queue.isEmpty()) { int u = queue.remove(); for (int v : graph.getOrDefault(u, java.util.List.of())) if (visited.add(v)) queue.add(v); }
        return visited.size();
    }
}
~')
            END;
        ELSIF p_language = 2 THEN
            RETURN CASE p_topic
                WHEN 1 THEN TO_CLOB('def factorial(n):' || CHR(10) || '    return 1 if n <= 1 else n * factorial(n - 1)' || CHR(10))
                WHEN 2 THEN TO_CLOB('def ordenar(nums):' || CHR(10) || '    return sorted(nums)' || CHR(10))
                WHEN 3 THEN TO_CLOB(q'~def balanceados(texto):
    stack, pairs = [], {')': '(', ']': '[', '}': '{'}
    for char in texto:
        if char in '([{': stack.append(char)
        elif char in pairs:
            if not stack or stack.pop() != pairs[char]: return False
    return not stack
~')
                WHEN 4 THEN TO_CLOB(q'~def espera_total(duraciones):
    elapsed = total = 0
    for duration in duraciones: total, elapsed = total + elapsed, elapsed + duration
    return total
~')
                WHEN 5 THEN TO_CLOB(q'~def altura(values):
    tree = None
    def insert(node, value):
        if node is None: return [value, None, None]
        if value < node[0]: node[1] = insert(node[1], value)
        elif value > node[0]: node[2] = insert(node[2], value)
        return node
    def height(node): return 0 if node is None else 1 + max(height(node[1]), height(node[2]))
    for value in values: tree = insert(tree, value)
    return height(tree)
~')
                ELSE TO_CLOB(q'~from collections import deque
def alcanzables(entrada):
    raw, origin = entrada.split(';'); graph = {}
    for edge in filter(None, raw.split(',')):
        u, v = map(int, edge.split('-')); graph.setdefault(u, []).append(v); graph.setdefault(v, []).append(u)
    visited, queue = {int(origin)}, deque([int(origin)])
    while queue:
        u = queue.popleft()
        for v in graph.get(u, []):
            if v not in visited: visited.add(v); queue.append(v)
    return len(visited)
~')
            END;
        ELSE
            RETURN CASE p_topic
                WHEN 1 THEN TO_CLOB('long long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }' || CHR(10))
                WHEN 2 THEN TO_CLOB('std::vector<int> ordenar(std::vector<int> nums) { std::sort(nums.begin(), nums.end()); return nums; }' || CHR(10))
                WHEN 3 THEN TO_CLOB(q'~bool balanceados(const std::string& text) {
    std::stack<char> values;
    for (char c : text) { if (c == '(' || c == '[' || c == '{') values.push(c); else if (c == ')' || c == ']' || c == '}') { if (values.empty()) return false; char open = values.top(); values.pop(); if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) return false; } }
    return values.empty();
}
~')
                WHEN 4 THEN TO_CLOB('int esperaTotal(const std::vector<int>& values) { int elapsed = 0, total = 0; for (int value : values) { total += elapsed; elapsed += value; } return total; }' || CHR(10))
                WHEN 5 THEN TO_CLOB(q'~struct Node { int value; std::unique_ptr<Node> left, right; explicit Node(int v): value(v) {} };
void insert(std::unique_ptr<Node>& node, int value) { if (!node) node = std::make_unique<Node>(value); else if (value < node->value) insert(node->left, value); else if (value > node->value) insert(node->right, value); }
int height(const std::unique_ptr<Node>& node) { return node ? 1 + std::max(height(node->left), height(node->right)) : 0; }
int altura(const std::vector<int>& values) { std::unique_ptr<Node> root; for (int value : values) insert(root, value); return height(root); }
~')
                ELSE TO_CLOB(q'~int alcanzables(const std::string& input) {
    auto separator = input.find(';'); int start = std::stoi(input.substr(separator + 1)); std::unordered_map<int, std::vector<int>> graph;
    std::stringstream stream(input.substr(0, separator)); std::string edge;
    while (std::getline(stream, edge, ',')) { auto dash = edge.find('-'); int u = std::stoi(edge.substr(0, dash)), v = std::stoi(edge.substr(dash + 1)); graph[u].push_back(v); graph[v].push_back(u); }
    std::queue<int> pending; std::unordered_set<int> visited{start}; pending.push(start);
    while (!pending.empty()) { int u = pending.front(); pending.pop(); for (int v : graph[u]) if (visited.insert(v).second) pending.push(v); }
    return visited.size();
}
~')
            END;
        END IF;
    END;

    FUNCTION test_input(p_topic NUMBER, p_case NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_topic
            WHEN 1 THEN CASE p_case WHEN 1 THEN '0' WHEN 2 THEN '5' ELSE '7' END
            WHEN 2 THEN CASE p_case WHEN 1 THEN '[5, 1, 4, 2]' WHEN 2 THEN '[3, -1, 3]' ELSE '[]' END
            WHEN 3 THEN CASE p_case WHEN 1 THEN '()[]{}' WHEN 2 THEN '([{}])' ELSE '([)]' END
            WHEN 4 THEN CASE p_case WHEN 1 THEN '[3, 2, 1]' WHEN 2 THEN '[5]' ELSE '[1, 1, 1, 1]' END
            WHEN 5 THEN CASE p_case WHEN 1 THEN '[4, 2, 6, 1, 3, 5, 7]' WHEN 2 THEN '[5]' ELSE '[5, 4, 3, 2]' END
            ELSE CASE p_case WHEN 1 THEN '0-1,1-2,3-4;0' WHEN 2 THEN '0-1,1-2,2-3;1' ELSE ';7' END
        END;
    END;

    FUNCTION test_output(p_topic NUMBER, p_case NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_topic
            WHEN 1 THEN CASE p_case WHEN 1 THEN '1' WHEN 2 THEN '120' ELSE '5040' END
            WHEN 2 THEN CASE p_case WHEN 1 THEN '[1, 2, 4, 5]' WHEN 2 THEN '[-1, 3, 3]' ELSE '[]' END
            WHEN 3 THEN CASE p_case WHEN 1 THEN 'true' WHEN 2 THEN 'true' ELSE 'false' END
            WHEN 4 THEN CASE p_case WHEN 1 THEN '8' WHEN 2 THEN '0' ELSE '6' END
            WHEN 5 THEN CASE p_case WHEN 1 THEN '3' WHEN 2 THEN '1' ELSE '4' END
            ELSE CASE p_case WHEN 1 THEN '3' WHEN 2 THEN '4' ELSE '1' END
        END;
    END;

    FUNCTION quiz_question(p_topic NUMBER) RETURN CLOB IS
    BEGIN
        RETURN TO_CLOB(CASE p_topic
            WHEN 1 THEN '¿Qué condición es indispensable para que una función recursiva termine correctamente?'
            WHEN 2 THEN '¿Cuál es la complejidad temporal de Merge Sort para n elementos?'
            WHEN 3 THEN '¿Qué elemento se retira primero de una pila LIFO?'
            WHEN 4 THEN '¿Qué propiedad define el comportamiento de una cola FIFO?'
            WHEN 5 THEN '¿Qué recorrido de un árbol binario visita izquierda, raíz y derecha?'
            ELSE '¿Qué estructura utiliza normalmente BFS para administrar los vértices pendientes?'
        END);
    END;

    FUNCTION quiz_options(p_topic NUMBER) RETURN CLOB IS
    BEGIN
        RETURN TO_CLOB(CASE p_topic
            WHEN 1 THEN 'Tener un caso base alcanzable|Crear siempre una colección|Usar dos bucles anidados|Declarar todas las variables globales'
            WHEN 2 THEN 'O(n log n)|O(n²) en todos los casos|O(log n)|O(1)'
            WHEN 3 THEN 'El último que fue insertado|El primero que fue insertado|El de menor valor|Un elemento aleatorio'
            WHEN 4 THEN 'El primero en entrar es el primero en salir|El último en entrar es el primero en salir|Siempre sale el elemento mayor|Los elementos no conservan ningún orden'
            WHEN 5 THEN 'Inorden|Preorden|Postorden|BFS inverso'
            ELSE 'Una cola|Una pila exclusivamente|Un árbol de búsqueda|Una tabla sin visitados'
        END);
    END;

    FUNCTION quiz_answer(p_topic NUMBER) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE p_topic
            WHEN 1 THEN 'Tener un caso base alcanzable'
            WHEN 2 THEN 'O(n log n)'
            WHEN 3 THEN 'El último que fue insertado'
            WHEN 4 THEN 'El primero en entrar es el primero en salir'
            WHEN 5 THEN 'Inorden'
            ELSE 'Una cola'
        END;
    END;

    PROCEDURE ensure_module(p_language NUMBER, p_order NUMBER, p_id OUT NUMBER) IS
        v_count NUMBER;
        v_name VARCHAR2(80) := 'Módulo ' || p_order || ': ' || module_title(p_order)
                || ' en ' || language_name(p_language);
        v_description VARCHAR2(200) := module_description(p_order);
    BEGIN
        SELECT COUNT(*) INTO v_count FROM Modulos WHERE id_lenguaje=p_language AND orden_secuencia=p_order;
        IF v_count=0 THEN
            INSERT INTO Modulos(id_lenguaje,nombre_modulo,descripcion,orden_secuencia)
            VALUES(p_language,v_name,v_description,p_order);
            v_created_modules := v_created_modules+1;
        END IF;
        SELECT id_modulo INTO p_id FROM Modulos WHERE id_lenguaje=p_language AND orden_secuencia=p_order;
    END;

    PROCEDURE ensure_lesson(p_module NUMBER,p_topic NUMBER,p_language NUMBER,p_order NUMBER,p_id OUT NUMBER) IS
        v_count NUMBER;
        v_title VARCHAR2(200) := lesson_title(p_topic) || ' en ' || language_name(p_language);
        v_theory CLOB := lesson_theory(p_topic,p_language);
    BEGIN
        SELECT COUNT(*) INTO v_count FROM Lecciones WHERE id_modulo=p_module AND orden_secuencia=p_order;
        IF v_count=0 THEN
            INSERT INTO Lecciones(id_modulo,titulo,contenido,orden_secuencia,exp_recompensa)
            VALUES(p_module,v_title,v_theory,p_order,105+(p_topic-1)*5);
            v_created_lessons := v_created_lessons+1;
        END IF;
        SELECT id_leccion INTO p_id FROM Lecciones WHERE id_modulo=p_module AND orden_secuencia=p_order;
    END;

    PROCEDURE ensure_code(p_lesson NUMBER,p_topic NUMBER,p_language NUMBER) IS
        v_id NUMBER;
        v_count NUMBER;
        v_version NUMBER;
        v_title VARCHAR2(200) := challenge_title(p_topic,p_language);
        v_statement CLOB := challenge_statement(p_topic);
        v_starter CLOB := starter_code(p_language,p_topic);
        v_solution CLOB := solution_code(p_language,p_topic);
        v_input CLOB;
        v_output CLOB;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM Retos WHERE id_leccion=p_lesson AND titulo_reto=v_title;
        IF v_count>0 THEN RETURN; END IF;
        INSERT INTO Retos(id_leccion,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,orden_secuencia)
        VALUES(p_lesson,v_title,v_statement,'CODIGO',CASE WHEN p_topic<=2 THEN 'MEDIO' ELSE 'DIFICIL' END,105+(p_topic-1)*5,1)
        RETURNING id_reto INTO v_id;
        INSERT INTO Reto_Codigo(id_reto,codigo_inicial,limite_tiempo_ms) VALUES(v_id,v_starter,2000);
        INSERT INTO Solucion_Referencia(id_reto,codigo_solucion) VALUES(v_id,v_solution);
        FOR i IN 1..3 LOOP
            v_input := TO_CLOB(test_input(p_topic,i));
            v_output := TO_CLOB(test_output(p_topic,i));
            INSERT INTO Casos_Prueba(id_reto,entrada,salida_esperada,es_oculto)
            VALUES(v_id,v_input,v_output,CASE WHEN i=3 THEN 1 ELSE 0 END);
        END LOOP;
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,limite_tiempo_ms,solucion_referencia,estado)
        VALUES(v_id,1,v_title,v_statement,'CODIGO',CASE WHEN p_topic<=2 THEN 'MEDIO' ELSE 'DIFICIL' END,
            105+(p_topic-1)*5,v_starter,2000,v_solution,'PUBLICADO') RETURNING id_version INTO v_version;
        FOR i IN 1..3 LOOP
            v_input := TO_CLOB(test_input(p_topic,i));
            v_output := TO_CLOB(test_output(p_topic,i));
            INSERT INTO Version_Caso_Prueba(id_version,numero,entrada,salida_esperada,es_oculto)
            VALUES(v_version,i,v_input,v_output,CASE WHEN i=3 THEN 1 ELSE 0 END);
        END LOOP;
        v_created_challenges := v_created_challenges+1;
    END;

    PROCEDURE ensure_quiz(p_lesson NUMBER,p_topic NUMBER) IS
        v_id NUMBER;
        v_count NUMBER;
        v_title VARCHAR2(200) := 'Quiz: ' || lesson_title(p_topic);
        v_question CLOB := quiz_question(p_topic);
        v_options CLOB := quiz_options(p_topic);
        v_answer VARCHAR2(200) := quiz_answer(p_topic);
    BEGIN
        SELECT COUNT(*) INTO v_count FROM Retos WHERE id_leccion=p_lesson AND titulo_reto=v_title;
        IF v_count>0 THEN RETURN; END IF;
        INSERT INTO Retos(id_leccion,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,orden_secuencia)
        VALUES(p_lesson,v_title,v_question,'QUIZ','MEDIO',60+(p_topic-1)*5,2)
        RETURNING id_reto INTO v_id;
        INSERT INTO Reto_Quiz(id_reto,opciones,respuesta_correcta) VALUES(v_id,v_options,v_answer);
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            opciones,respuesta_correcta,estado)
        VALUES(v_id,1,v_title,v_question,'QUIZ','MEDIO',60+(p_topic-1)*5,v_options,v_answer,'PUBLICADO');
        v_created_challenges := v_created_challenges+1;
    END;

    PROCEDURE ensure_final(p_module NUMBER,p_lesson NUMBER,p_module_order NUMBER,p_language NUMBER) IS
        v_id NUMBER;
        v_count NUMBER;
        v_order NUMBER;
        v_title VARCHAR2(100);
        v_options CLOB;
        v_answer VARCHAR2(100);
        v_code CLOB;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM Final_Modulo WHERE id_modulo=p_module;
        IF v_count>0 THEN RETURN; END IF;
        IF p_module_order=4 THEN
            v_title := 'Reto final · Traza recursiva'; v_options := TO_CLOB('120|25|15|5'); v_answer := '120';
            v_code := CASE p_language WHEN 1 THEN TO_CLOB('public class FinalModuleSolver { public static String solve(String input) { return Long.toString(factorial(5)); } private static long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); } }') WHEN 2 THEN TO_CLOB('def factorial(n): return 1 if n <= 1 else n * factorial(n - 1)' || CHR(10) || 'print(factorial(5))') ELSE TO_CLOB('#include <iostream>' || CHR(10) || 'long long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }' || CHR(10) || 'int main() { std::cout << factorial(5); }') END;
        ELSIF p_module_order=5 THEN
            v_title := 'Reto final · Validador con pila'; v_options := TO_CLOB('true|false|6|Error'); v_answer := 'true';
            v_code := CASE p_language WHEN 1 THEN TO_CLOB('public class FinalModuleSolver { public static String solve(String input) { return Boolean.toString("([{}])".length() == 6); } }') WHEN 2 THEN TO_CLOB('text = "([{}])"' || CHR(10) || 'print(text == "([{}])")') ELSE TO_CLOB('#include <iostream>' || CHR(10) || 'int main() { std::cout << "true"; }') END;
        ELSE
            v_title := 'Reto final · Explorador de grafos'; v_options := TO_CLOB('4|3|5|1'); v_answer := '4';
            v_code := CASE p_language WHEN 1 THEN TO_CLOB('public class FinalModuleSolver { public static String solve(String input) { int[] reachable = {0,1,2,3}; return Integer.toString(reachable.length); } }') WHEN 2 THEN TO_CLOB('visited = {0, 1, 2, 3}' || CHR(10) || 'print(len(visited))') ELSE TO_CLOB('#include <iostream>' || CHR(10) || 'int main() { std::cout << 4; }') END;
        END IF;
        SELECT NVL(MAX(orden_secuencia),0)+1 INTO v_order FROM Retos WHERE id_leccion=p_lesson;
        INSERT INTO Retos(id_leccion,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,orden_secuencia)
        VALUES(p_lesson,v_title,'Analiza el código y selecciona la salida correcta.','PREDICCION','DIFICIL',140,v_order)
        RETURNING id_reto INTO v_id;
        INSERT INTO Reto_Quiz(id_reto,opciones,respuesta_correcta) VALUES(v_id,v_options,v_answer);
        INSERT INTO Reto_Prediccion(id_reto,codigo_ejemplo) VALUES(v_id,v_code);
        INSERT INTO Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,
            codigo_inicial,opciones,respuesta_correcta,estado)
        VALUES(v_id,1,v_title,'Analiza el código y selecciona la salida correcta.','PREDICCION','DIFICIL',140,
            v_code,v_options,v_answer,'PUBLICADO');
        INSERT INTO Final_Modulo(id_modulo,id_reto) VALUES(p_module,v_id);
        v_created_challenges := v_created_challenges+1;
    END;

    PROCEDURE ensure_achievement(p_name VARCHAR2,p_description VARCHAR2,p_type VARCHAR2,p_required NUMBER) IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM Logros WHERE nombre_insignia=p_name;
        IF v_count=0 THEN
            INSERT INTO Logros(nombre_insignia,descripcion,tipo_condicion) VALUES(p_name,p_description,p_type)
            RETURNING id_logro INTO v_id;
            IF p_type='RACHA' THEN INSERT INTO Logro_Racha VALUES(v_id,p_required);
            ELSIF p_type='EXP' THEN INSERT INTO Logro_EXP VALUES(v_id,p_required);
            ELSE INSERT INTO Logro_Retos VALUES(v_id,p_required);
            END IF;
            v_created_achievements := v_created_achievements+1;
        END IF;
    END;

BEGIN
    SAVEPOINT expanded_curriculum;
    FOR language_index IN 1..3 LOOP
        SELECT id_lenguaje INTO v_language_id FROM Lenguajes
        WHERE UPPER(nombre)=CASE language_index WHEN 1 THEN 'JAVA' WHEN 2 THEN 'PYTHON' ELSE 'C++' END;
        FOR module_order IN 4..6 LOOP
            ensure_module(v_language_id,module_order,v_module);
            FOR lesson_order IN 1..2 LOOP
                v_topic := (module_order-4)*2+lesson_order;
                ensure_lesson(v_module,v_topic,language_index,lesson_order,v_lesson);
                ensure_code(v_lesson,v_topic,language_index);
                ensure_quiz(v_lesson,v_topic);
                IF lesson_order=2 THEN ensure_final(v_module,v_lesson,module_order,language_index); END IF;
            END LOOP;
        END LOOP;
    END LOOP;

    ensure_achievement('Explorador de Estructuras','Completa 25 retos de aprendizaje.','RETOS',25);
    ensure_achievement('Estratega Algorítmico','Completa 40 retos de aprendizaje.','RETOS',40);
    ensure_achievement('Campeón del Código','Completa 60 retos de aprendizaje.','RETOS',60);
    ensure_achievement('Experiencia Avanzada','Alcanza 3,500 XP.','EXP',3500);
    ensure_achievement('Ingeniero del Código','Alcanza 5,000 XP.','EXP',5000);
    ensure_achievement('Leyenda de LevelUp','Alcanza 7,500 XP.','EXP',7500);
    ensure_achievement('Disciplina Imparable','Mantén una racha activa de 21 días.','RACHA',21);
    ensure_achievement('Programador Constante','Mantén una racha activa de 30 días.','RACHA',30);
    ensure_achievement('Maestro de la Constancia','Mantén una racha activa de 60 días.','RACHA',60);

    DBMS_OUTPUT.PUT_LINE('Módulos creados: ' || v_created_modules);
    DBMS_OUTPUT.PUT_LINE('Lecciones creadas: ' || v_created_lessons);
    DBMS_OUTPUT.PUT_LINE('Retos creados: ' || v_created_challenges);
    DBMS_OUTPUT.PUT_LINE('Logros creados: ' || v_created_achievements);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO expanded_curriculum;
        RAISE;
END;
/
