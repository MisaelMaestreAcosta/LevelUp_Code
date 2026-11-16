package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.domain.model.*;
import java.util.Comparator;
import java.util.List;

/** A practical code-reading assessment at the end of each original module. */
public final class SeedModuleFinals {
    private SeedModuleFinals() { }

    public static void addTo(List<Language> languages) {
        for (Language language : languages) for (CourseModule module : language.getModules()) {
            Lesson lesson = module.getLessons().stream().max(Comparator.comparingInt(Lesson::getOrderIndex)).orElseThrow();
            int topic = module.getOrderIndex() - 1;
            List<String> options = switch (topic) {
                case 0 -> List.of("Hola", "HOLA", "Error de tipo", "Mensaje");
                case 1 -> List.of("2", "5", "1", "-1");
                default -> List.of("18", "3", "21", "12");
            };
            String answer = List.of("HOLA", "1", "12").get(topic);
            String scenario = switch (topic) {
                case 0 -> "Un servicio recibe un objeto a través de su contrato y transforma su mensaje. Identifica el resultado.";
                case 1 -> "Un buscador debe encontrar la primera aparición de 5 en [2, 5, 5, 9, 12]. Sigue los límites del algoritmo e identifica el índice devuelto.";
                default -> "Un informe suma los números pares sin contar valores repetidos. Sigue el conjunto y el acumulador para identificar el total.";
            };
            String explanation = switch (topic) {
                case 0 -> "El objeto concreto produce Hola a través del contrato. La transformación posterior devuelve HOLA.";
                case 1 -> "Encontrar un 5 conserva un candidato y continúa hacia la izquierda. El primer 5 está en el índice 1.";
                default -> "Los valores pares distintos son 2, 4 y 6. Cada uno contribuye una sola vez y el total es 12.";
            };
            List<String> hints = switch (topic) {
                case 0 -> List.of("Identifica el tipo concreto del objeto que recibe la llamada.", "Sigue el método que genera el mensaje antes de aplicar la transformación.", "Distingue el mensaje original del resultado de convertirlo a mayúsculas.");
                case 1 -> List.of("El arreglo contiene dos apariciones del objetivo.", "Anota low, high, mid y el candidato encontrado después de cada iteración.", "Observa que encontrar el objetivo mueve high hacia la izquierda y conserva el candidato.");
                default -> List.of("El conjunto recuerda los valores ya procesados.", "Un valor repetido no debe volver a entrar en el acumulador.", "Separa los pares distintos y calcula su suma.");
            };
            String invocation = language.getId() == 1 ? " Evalúa solve(\"5\") en la clase mostrada." : " Selecciona lo que imprime el programa.";
            var challenge = new OutputPredictionChallenge(95000 + module.getId(),
                    "Reto final · " + List.of("Servicio de mensajes", "Buscador de coincidencias", "Informe de valores únicos").get(topic),
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
            default -> """
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
            default -> """
                    values = [2, 2, 3, 4, 4, 6]
                    seen = set()
                    total = 0
                    for value in values:
                        if value not in seen:
                            seen.add(value)
                            if value % 2 == 0: total += value
                    print(total)
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
            default -> """
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
        };
    }
}
