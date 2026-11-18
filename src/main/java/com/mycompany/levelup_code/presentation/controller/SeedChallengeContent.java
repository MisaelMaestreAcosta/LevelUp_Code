package com.mycompany.levelup_code.presentation.controller;

import java.util.Map;

/** Seed exercise starters and reference solutions have separate roles. */
public final class SeedChallengeContent {
    public record Exercise(String starter, String reference) { }

    private static final Map<Integer, Exercise> EXERCISES = Map.ofEntries(
            Map.entry(101, new Exercise(
                    """
                    public class AnimalSoundSolver {
                        public static String emitirSonido(String tipo) {
                            // TODO: implementa la solución siguiendo el enunciado.
                            return "";
                        }
                    }
                    """,
                    """
                    public class AnimalSoundSolver {
                        public static String emitirSonido(String tipo) {
                            if ("Perro".equalsIgnoreCase(tipo)) {
                                return "Guau";
                            }
                            if ("Gato".equalsIgnoreCase(tipo)) {
                                return "Miau";
                            }
                            return "Desconocido";
                        }
                    }
                    """)),
            Map.entry(106, new Exercise(
                    """
                    public class AccessPolicySolver {
                        public static String nivelAcceso(String rol) {
                            // TODO: implementa la solución siguiendo el enunciado.
                            return "";
                        }
                    }
                    """,
                    """
                    public class AccessPolicySolver {
                        public static String nivelAcceso(String rol) {
                            if ("ADMIN".equalsIgnoreCase(rol)) {
                                return "TOTAL";
                            }
                            if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
                                return "LECTURA";
                            }
                            return "DENEGADO";
                        }
                    }
                    """)),
            Map.entry(103, new Exercise(
                    """
                    public class BinarySearchSolver {
                        public static int search(int[] a, int target) {
                            // TODO: implementa la solución siguiendo el enunciado.
                            return 0;
                        }
                    }
                    """,
                    """
                    public class BinarySearchSolver {
                        public static int search(int[] a, int target) {
                            int low = 0, high = a.length - 1;
                            while (low <= high) {
                                int mid = low + (high - low) / 2;
                                if (a[mid] == target) return mid;
                                if (a[mid] < target) low = mid + 1;
                                else high = mid - 1;
                            }
                            return -1;
                        }
                    }
                    """)),
            Map.entry(109, new Exercise(
                    """
                    public class PalindromeSolver {
                        public static boolean esPalindromo(String texto) {
                            // TODO: implementa la solución siguiendo el enunciado.
                            return false;
                        }
                    }
                    """,
                    """
                    public class PalindromeSolver {
                        public static boolean esPalindromo(String texto) {
                            String s = texto.toLowerCase();
                            int left = 0, right = s.length() - 1;
                            while (left < right) {
                                if (s.charAt(left) != s.charAt(right)) {
                                    return false;
                                }
                                left++;
                                right--;
                            }
                            return true;
                        }
                    }
                    """)),
            Map.entry(111, new Exercise(
                    """
                    public class EvenSumSolver {
                        public static int sumarPares(int[] nums) {
                            // TODO: implementa la solución siguiendo el enunciado.
                            return 0;
                        }
                    }
                    """,
                    """
                    public class EvenSumSolver {
                        public static int sumarPares(int[] nums) {
                            int total = 0;
                            for (int n : nums) {
                                if (n % 2 == 0) {
                                    total += n;
                                }
                            }
                            return total;
                        }
                    }
                    """)),
            Map.entry(113, new Exercise(
                    """
                    import java.util.HashSet;
                    import java.util.Set;

                    public class DuplicateFinderSolver {
                        public static int primerDuplicado(int[] nums) {
                            // TODO: implementa la solución siguiendo el enunciado.
                            return 0;
                        }
                    }
                    """,
                    """
                    import java.util.HashSet;
                    import java.util.Set;

                    public class DuplicateFinderSolver {
                        public static int primerDuplicado(int[] nums) {
                            Set<Integer> vistos = new HashSet<>();
                            for (int n : nums) {
                                if (!vistos.add(n)) {
                                    return n;
                                }
                            }
                            return -1;
                        }
                    }
                    """)),
            Map.entry(104, new Exercise(
                    """
                    def emitir_sonido(tipo: str) -> str:
                        # TODO: implementa la solución siguiendo el enunciado.
                        return ""
                    """,
                    """
                    def emitir_sonido(tipo: str) -> str:
                        if tipo.lower() == "perro":
                            return "Guau"
                        elif tipo.lower() == "gato":
                            return "Miau"
                        return "Desconocido"
                    """)),
            Map.entry(203, new Exercise(
                    """
                    def nivel_acceso(rol: str) -> str:
                        # TODO: implementa la solución siguiendo el enunciado.
                        return ""
                    """,
                    """
                    def nivel_acceso(rol: str) -> str:
                        if rol.lower() == "admin":
                            return "TOTAL"
                        elif rol.lower() == "estudiante":
                            return "LECTURA"
                        return "DENEGADO"
                    """)),
            Map.entry(205, new Exercise(
                    """
                    def search(a: list, target: int) -> int:
                        # TODO: implementa la solución siguiendo el enunciado.
                        return 0
                    """,
                    """
                    def search(a: list, target: int) -> int:
                        low = 0
                        high = len(a) - 1
                        while low <= high:
                            mid = (low + high) // 2
                            if a[mid] == target:
                                return mid
                            elif a[mid] < target:
                                low = mid + 1
                            else:
                                high = mid - 1
                        return -1
                    """)),
            Map.entry(207, new Exercise(
                    """
                    def es_palindromo(texto: str) -> bool:
                        # TODO: implementa la solución siguiendo el enunciado.
                        return False
                    """,
                    """
                    def es_palindromo(texto: str) -> bool:
                        limpio = texto.lower()
                        return limpio == limpio[::-1]
                    """)),
            Map.entry(209, new Exercise(
                    """
                    def sumar_pares(nums: list) -> int:
                        # TODO: implementa la solución siguiendo el enunciado.
                        return 0
                    """,
                    """
                    def sumar_pares(nums: list) -> int:
                        total = 0
                        for n in nums:
                            if n % 2 == 0:
                                total += n
                        return total
                    """)),
            Map.entry(211, new Exercise(
                    """
                    def primer_duplicado(nums: list) -> int:
                        # TODO: implementa la solución siguiendo el enunciado.
                        return 0
                    """,
                    """
                    def primer_duplicado(nums: list) -> int:
                        vistos = set()
                        for n in nums:
                            if n in vistos:
                                return n
                            vistos.add(n)
                        return -1
                    """)),
            Map.entry(105, new Exercise(
                    """
                    #include <string>

                    std::string emitirSonido(std::string tipo) {
                        // TODO: implementa la solución siguiendo el enunciado.
                        return "";
                    }
                    """,
                    """
                    #include <string>

                    std::string emitirSonido(std::string tipo) {
                        if (tipo == "Perro" || tipo == "PERRO" || tipo == "perro") {
                            return "Guau";
                        }
                        if (tipo == "Gato" || tipo == "GATO" || tipo == "gato") {
                            return "Miau";
                        }
                        return "Desconocido";
                    }
                    """)),
            Map.entry(303, new Exercise(
                    """
                    #include <string>

                    std::string nivelAcceso(std::string rol) {
                        // TODO: implementa la solución siguiendo el enunciado.
                        return "";
                    }
                    """,
                    """
                    #include <string>

                    std::string nivelAcceso(std::string rol) {
                        if (rol == "ADMIN" || rol == "admin") {
                            return "TOTAL";
                        }
                        if (rol == "ESTUDIANTE" || rol == "estudiante") {
                            return "LECTURA";
                        }
                        return "DENEGADO";
                    }
                    """)),
            Map.entry(305, new Exercise(
                    """
                    #include <vector>

                    int search(const std::vector<int>& a, int target) {
                        // TODO: implementa la solución siguiendo el enunciado.
                        return 0;
                    }
                    """,
                    """
                    #include <vector>

                    int search(const std::vector<int>& a, int target) {
                        int low = 0;
                        int high = static_cast<int>(a.size()) - 1;
                        while (low <= high) {
                            int mid = low + (high - low) / 2;
                            if (a[mid] == target) return mid;
                            if (a[mid] < target) low = mid + 1;
                            else high = mid - 1;
                        }
                        return -1;
                    }
                    """)),
            Map.entry(307, new Exercise(
                    """
                    #include <string>
                    #include <cctype>

                    bool esPalindromo(std::string texto) {
                        // TODO: implementa la solución siguiendo el enunciado.
                        return false;
                    }
                    """,
                    """
                    #include <string>
                    #include <cctype>

                    bool esPalindromo(std::string texto) {
                        int left = 0;
                        int right = static_cast<int>(texto.size()) - 1;
                        while (left < right) {
                            if (std::tolower(texto[left]) != std::tolower(texto[right])) {
                                return false;
                            }
                            left++;
                            right--;
                        }
                        return true;
                    }
                    """)),
            Map.entry(309, new Exercise(
                    """
                    #include <vector>

                    int sumarPares(const std::vector<int>& nums) {
                        // TODO: implementa la solución siguiendo el enunciado.
                        return 0;
                    }
                    """,
                    """
                    #include <vector>

                    int sumarPares(const std::vector<int>& nums) {
                        int total = 0;
                        for (int n : nums) {
                            if (n % 2 == 0) {
                                total += n;
                            }
                        }
                        return total;
                    }
                    """)),
            Map.entry(311, new Exercise(
                    """
                    #include <vector>
                    #include <unordered_set>

                    int primerDuplicado(const std::vector<int>& nums) {
                        // TODO: implementa la solución siguiendo el enunciado.
                        return 0;
                    }
                    """,
                    """
                    #include <vector>
                    #include <unordered_set>

                    int primerDuplicado(const std::vector<int>& nums) {
                        std::unordered_set<int> vistos;
                        for (int n : nums) {
                            if (vistos.count(n) > 0) {
                                return n;
                            }
                            vistos.insert(n);
                        }
                        return -1;
                    }
                    """))
    );

    private SeedChallengeContent() { }

    public static Exercise forId(int challengeId) {
        Exercise exercise = EXERCISES.get(challengeId);
        if (exercise == null) exercise = ExpandedSeedCatalog.exerciseForId(challengeId);
        if (exercise == null) throw new IllegalArgumentException("El reto no pertenece al catálogo inicial.");
        return exercise;
    }
}
