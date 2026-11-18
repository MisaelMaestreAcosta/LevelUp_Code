package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.domain.model.*;
import java.util.Comparator;
import java.util.List;

/** A practical code-reading assessment at the end of every seed module. */
public final class SeedModuleFinals {
    private SeedModuleFinals() { }

    public static void addTo(List<Language> languages) {
        for (Language language : languages) for (CourseModule module : language.getModules()) {
            Lesson lesson = module.getLessons().stream().max(Comparator.comparingInt(Lesson::getOrderIndex)).orElseThrow();
            int topic = module.getOrderIndex() - 1;
            List<String> options = switch (topic) {
                case 0 -> List.of("Hola", "HOLA", "Error de tipo", "Mensaje");
                case 1 -> List.of("2", "5", "1", "-1");
                case 2 -> List.of("18", "3", "21", "12");
                case 3 -> List.of("120", "25", "15", "5");
                case 4 -> List.of("true", "false", "6", "Error");
                default -> List.of("4", "3", "5", "1");
            };
            String answer = List.of("HOLA", "1", "12", "120", "true", "4").get(topic);
            String scenario = switch (topic) {
                case 0 -> "Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado.";
                case 1 -> "Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto.";
                case 2 -> "Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total.";
                case 3 -> "Una función recursiva calcula el factorial de 5. Sigue el caso base y el retorno de cada llamada.";
                case 4 -> "Una pila procesa una expresión con delimitadores anidados. Determina si todos los cierres corresponden.";
                default -> "Un recorrido BFS parte del vértice 0. Sigue la cola y el conjunto de visitados para contar los vértices alcanzables.";
            };
            String explanation = switch (topic) {
                case 0 -> "El objeto concreto produce Hola a través del contrato. La transformación posterior devuelve HOLA.";
                case 1 -> "Encontrar un 5 conserva un candidato y continúa hacia la izquierda. El primer 5 está en el índice 1.";
                case 2 -> "Los valores pares distintos son 2, 4 y 6. Cada uno contribuye una sola vez y el total es 12.";
                case 3 -> "El caso base retorna 1 y los retornos pendientes calculan 5 × 4 × 3 × 2 × 1 = 120.";
                case 4 -> "Cada cierre coincide con la apertura ubicada en la parte superior y la pila termina vacía.";
                default -> "Desde 0 se alcanzan 0, 1, 2 y 3. El vértice 4 pertenece a otro componente, por lo que el total es 4.";
            };
            List<String> hints = switch (topic) {
                case 0 -> List.of("Identifica el tipo concreto del objeto que recibe la llamada.", "Sigue el método que genera el mensaje antes de aplicar la transformación.", "Distingue el mensaje original del resultado de convertirlo a mayúsculas.");
                case 1 -> List.of("El arreglo contiene dos apariciones del objetivo.", "Anota low, high, mid y el candidato encontrado después de cada iteración.", "Observa que encontrar el objetivo mueve high hacia la izquierda y conserva el candidato.");
                case 2 -> List.of("El conjunto recuerda los valores ya procesados.", "Un valor repetido no debe volver a entrar en el acumulador.", "Separa los pares distintos y calcula su suma.");
                case 3 -> List.of("Empieza por la llamada factorial(5).", "Cada llamada multiplica por el resultado del número anterior.", "La cadena se detiene cuando n es menor o igual a 1.");
                case 4 -> List.of("Apila cada delimitador de apertura.", "Cada cierre debe coincidir con el elemento superior.", "Comprueba también que la pila quede vacía al terminar.");
                default -> List.of("Empieza con 0 en la cola y en visitados.", "Agrega únicamente vecinos todavía no visitados.", "El conjunto final contiene cuatro vértices.");
            };
            String invocation = language.getId() == 1 ? " Evalúa solve(\"5\") en la clase mostrada." : " Selecciona lo que imprime el programa.";
            var challenge = new OutputPredictionChallenge(95000 + module.getId(),
                    "Reto final · " + List.of("Servicio de mensajes", "Buscador de coincidencias", "Informe de valores únicos",
                            "Traza recursiva", "Validador con pila", "Explorador de grafos").get(topic),
                    scenario + invocation, lesson, 100, options, answer, code(language.getId(), topic));
            challenge.setModuleFinal(true);
            challenge.setGuidance(new ChallengeGuidance(hints, explanation, options.stream()
                    .map(option -> option.equals(answer) ? explanation : "Revisa cómo se obtiene el resultado. " + explanation).toList()));
            lesson.addChallenge(challenge);
        }
    }

    private static String code(int language, int topic) {
        if (language == 1) return javaCode(topic);
        if (language == 2) return pythonCode(topic);
        return cppCode(topic);
    }

    private static String javaCode(int topic) {
        return switch (topic) {
            case 0 -> """
                    public class FinalModuleSolver {
                        public static String solve(String input) {
                            Message message = new Greeting();
                            return message.text().toUpperCase();
                        }
                    }
                    interface Message { String text(); }
                    class Greeting implements Message {
                        public String text() { return "Hola"; }
                    }
                    """;
            case 1 -> """
                    public class FinalModuleSolver {
                        public static String solve(String input) {
                            int[] values = {2, 5, 5, 9, 12};
                            int target = Integer.parseInt(input);
                            int low = 0, high = values.length - 1, found = -1;
                            while (low <= high) {
                                int mid = low + (high - low) / 2;
                                if (values[mid] >= target) {
                                    if (values[mid] == target) found = mid;
                                    high = mid - 1;
                                } else {
                                    low = mid + 1;
                                }
                            }
                            return Integer.toString(found);
                        }
                    }
                    """;
            case 2 -> """
                    import java.util.HashSet;
                    public class FinalModuleSolver {
                        public static String solve(String input) {
                            int[] values = {2, 2, 3, 4, 4, 6};
                            var seen = new HashSet<Integer>();
                            int total = 0;
                            for (int value : values) {
                                if (seen.add(value) && value % 2 == 0) total += value;
                            }
                            return Integer.toString(total);
                        }
                    }
                    """;
            case 3 -> """
                    public class FinalModuleSolver {
                        public static String solve(String input) {
                            return Long.toString(factorial(Integer.parseInt(input)));
                        }
                        private static long factorial(int n) {
                            return n <= 1 ? 1 : n * factorial(n - 1);
                        }
                    }
                    """;
            case 4 -> """
                    import java.util.ArrayDeque;
                    public class FinalModuleSolver {
                        public static String solve(String input) {
                            String text = "([{}])";
                            var stack = new ArrayDeque<Character>();
                            for (char c : text.toCharArray()) {
                                if (c == '(' || c == '[' || c == '{') stack.push(c);
                                else {
                                    if (stack.isEmpty()) return "false";
                                    char open = stack.pop();
                                    if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) return "false";
                                }
                            }
                            return Boolean.toString(stack.isEmpty());
                        }
                    }
                    """;
            default -> """
                    import java.util.*;
                    public class FinalModuleSolver {
                        public static String solve(String input) {
                            int[][] graph = {{1}, {0, 2}, {1, 3}, {2}, {}};
                            var queue = new ArrayDeque<Integer>();
                            var visited = new HashSet<Integer>();
                            queue.add(0); visited.add(0);
                            while (!queue.isEmpty()) {
                                int current = queue.remove();
                                for (int next : graph[current]) if (visited.add(next)) queue.add(next);
                            }
                            return Integer.toString(visited.size());
                        }
                    }
                    """;
        };
    }

    private static String pythonCode(int topic) {
        return switch (topic) {
            case 0 -> """
                    from abc import ABC, abstractmethod
                    class Message(ABC):
                        @abstractmethod
                        def text(self): pass
                        def __str__(self): return self.text()
                    class Greeting(Message):
                        def text(self): return "Hola"
                    message = Greeting()
                    print(str(message).upper())
                    """;
            case 1 -> """
                    values = [2, 5, 5, 9, 12]
                    target = 5
                    low, high, found = 0, len(values) - 1, -1
                    while low <= high:
                        mid = (low + high) // 2
                        if values[mid] >= target:
                            if values[mid] == target: found = mid
                            high = mid - 1
                        else:
                            low = mid + 1
                    print(found)
                    """;
            case 2 -> """
                    values = [2, 2, 3, 4, 4, 6]
                    seen = set()
                    total = 0
                    for value in values:
                        if value not in seen:
                            seen.add(value)
                            if value % 2 == 0: total += value
                    print(total)
                    """;
            case 3 -> """
                    def factorial(n): return 1 if n <= 1 else n * factorial(n - 1)
                    print(factorial(5))
                    """;
            case 4 -> """
                    text, stack, pairs = "([{}])", [], {')': '(', ']': '[', '}': '{'}
                    valid = True
                    for char in text:
                        if char in "([{" : stack.append(char)
                        elif not stack or stack.pop() != pairs[char]: valid = False; break
                    print(valid and not stack)
                    """;
            default -> """
                    from collections import deque
                    graph = {0: [1], 1: [0, 2], 2: [1, 3], 3: [2], 4: []}
                    queue, visited = deque([0]), {0}
                    while queue:
                        current = queue.popleft()
                        for neighbor in graph[current]:
                            if neighbor not in visited: visited.add(neighbor); queue.append(neighbor)
                    print(len(visited))
                    """;
        };
    }

    private static String cppCode(int topic) {
        return switch (topic) {
            case 0 -> """
                    #include <iostream>
                    #include <memory>
                    #include <string>
                    #include <cctype>
                    struct Message {
                        virtual std::string text() const = 0;
                        virtual ~Message() = default;
                    };
                    struct Greeting : Message {
                        std::string text() const override { return "Hola"; }
                    };
                    int main() {
                        std::unique_ptr<Message> message = std::make_unique<Greeting>();
                        std::string text = message->text();
                        for (char& value : text) value = std::toupper(static_cast<unsigned char>(value));
                        std::cout << text;
                    }
                    """;
            case 1 -> """
                    #include <iostream>
                    #include <vector>
                    int firstMatch(const std::vector<int>& values, int target) {
                        int low = 0, high = static_cast<int>(values.size()) - 1, found = -1;
                        while (low <= high) {
                            int mid = low + (high - low) / 2;
                            if (values[mid] >= target) {
                                if (values[mid] == target) found = mid;
                                high = mid - 1;
                            } else low = mid + 1;
                        }
                        return found;
                    }
                    int main() { std::cout << firstMatch({2, 5, 5, 9, 12}, 5); }
                    """;
            case 2 -> """
                    #include <iostream>
                    #include <vector>
                    #include <unordered_set>
                    int main() {
                        std::vector<int> values = {2, 2, 3, 4, 4, 6};
                        std::unordered_set<int> seen;
                        int total = 0;
                        for (int value : values) {
                            if (seen.insert(value).second && value % 2 == 0) total += value;
                        }
                        std::cout << total;
                    }
                    """;
            case 3 -> """
                    #include <iostream>
                    long long factorial(int n) { return n <= 1 ? 1 : n * factorial(n - 1); }
                    int main() { std::cout << factorial(5); }
                    """;
            case 4 -> """
                    #include <iostream>
                    #include <stack>
                    #include <string>
                    int main() {
                        std::string text = "([{}])"; std::stack<char> values; bool valid = true;
                        for (char c : text) {
                            if (c == '(' || c == '[' || c == '{') values.push(c);
                            else { if (values.empty()) { valid = false; break; } char open = values.top(); values.pop(); if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) valid = false; }
                        }
                        std::cout << (valid && values.empty() ? "true" : "false");
                    }
                    """;
            default -> """
                    #include <iostream>
                    #include <queue>
                    #include <unordered_set>
                    #include <vector>
                    int main() {
                        std::vector<std::vector<int>> graph = {{1}, {0, 2}, {1, 3}, {2}, {}};
                        std::queue<int> pending; std::unordered_set<int> visited{0}; pending.push(0);
                        while (!pending.empty()) { int current = pending.front(); pending.pop(); for (int next : graph[current]) if (visited.insert(next).second) pending.push(next); }
                        std::cout << visited.size();
                    }
                    """;
        };
    }
}
