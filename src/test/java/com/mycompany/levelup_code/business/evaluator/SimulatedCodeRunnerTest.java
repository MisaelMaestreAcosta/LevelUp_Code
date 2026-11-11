package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.presentation.controller.DemoSeedCatalog;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatedCodeRunnerTest {

    private final SimulatedCodeRunner runner = new SimulatedCodeRunner();

    @Test
    void compilesAndExecutesRealJavaCodeInIsolatedSubprocess() {
        Language javaLang = new Language(1, "Java", "Java 21", true);
        CourseModule mod = new CourseModule(1, "Algoritmos", "Búsqueda", 1, javaLang);
        Lesson lesson = new Lesson(1, "Búsqueda Binaria", "Teoría", 1, 75, mod);

        CodeChallenge challenge = new CodeChallenge(
                103, "Binary Search", "Busca target", lesson, 90,
                "public class BinarySearchSolver { public static int search(int[] a, int target) { return -1; } }",
                3
        );
        challenge.addTestCase(new TestCase(1, challenge, "[2, 5, 8, 12, 16], target=12", "3", false));
        challenge.addTestCase(new TestCase(2, challenge, "[2, 5, 8, 12, 16], target=7", "-1", true));

        // Un código engañoso con palabras clave pero retorno constante falso DEBE fallar en compilación/ejecución real
        String fakeAlgorithm =
                "public class BinarySearchSolver {\n"
                + "    public static int search(int[] a, int target) {\n"
                + "        int mid = 0; // palabra clave engañosa\n"
                + "        return 99;\n"
                + "    }\n"
                + "}";
        SimulatedCodeRunner.ExecutionSummary fakeSummary = runner.runAndValidate(challenge, fakeAlgorithm);
        assertTrue(fakeSummary.isSandboxedExecution());
        assertFalse(fakeSummary.isAllPassed(), "La ejecución real debe rechazar código que no implementa el algoritmo");

        // Una implementación real de búsqueda binaria en Java DEBE compilar y aprobar todos los casos
        String realAlgorithm =
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
                + "}";
        SimulatedCodeRunner.ExecutionSummary okSummary = runner.runAndValidate(challenge, realAlgorithm);
        assertTrue(okSummary.isSandboxedExecution());
        assertTrue(okSummary.isAllPassed());
        assertEquals(2, okSummary.getPassedCount());
        assertFalse(okSummary.getCaseOutcomes().get(1).getMessage().contains("-1"),
                "La salida de un caso oculto no debe mostrarse al estudiante");
    }

    @Test
    void enforcesTimeoutWhenJavaSubprocessHangs() {
        String hangingJava =
                "public class InfiniteSolver {\n"
                + "    public static String solve(String s) {\n"
                + "        long i = 0;\n"
                + "        while (i >= 0) { i++; }\n"
                + "        return s;\n"
                + "    }\n"
                + "}";

        assertThrows(TimeoutException.class, () -> runner.run("Java", hangingJava, "Perro", 1));
    }

    @Test
    void blocksStudentCodeFromReadingFilesOutsideTheSandbox() {
        String fileReader =
                "import java.nio.file.*;\n"
                + "public class FileReaderSolver {\n"
                + "    public static String solve(String input) throws Exception {\n"
                + "        return Files.readString(Path.of(System.getProperty(\"user.home\"), \"secret.txt\"));\n"
                + "    }\n"
                + "}";

        IOException denied = assertThrows(IOException.class,
                () -> runner.run("Java", fileReader, "x", 2));
        assertTrue(denied.getMessage().contains("API no permitida"),
                "El proceso debe negar el acceso a archivos externos");
    }

    @Test
    void marksPythonEvaluationExplicitlyAsSimulatedDemoMode() {
        Language pyLang = new Language(2, "Python", "Python 3.12", true);
        CourseModule mod = new CourseModule(2, "POO", "ABC", 1, pyLang);
        Lesson lesson = new Lesson(4, "Protocolos", "Teoría", 1, 60, mod);

        CodeChallenge pyChallenge = new CodeChallenge(
                104, "Sonidos Python", "Emite sonido", lesson, 65,
                "def emitir_sonido(tipo: str) -> str:\n    return ''\n", 2
        );
        pyChallenge.addTestCase(new TestCase(1, pyChallenge, "Perro", "Guau", false));

        String pyCode = "def emitir_sonido(tipo: str) -> str:\n    if tipo.lower() == 'perro': return 'Guau'\n    return ''\n";
        SimulatedCodeRunner.ExecutionSummary summary = runner.runAndValidate(pyChallenge, pyCode);

        assertFalse(summary.isSandboxedExecution(), "Python sin sandbox externo debe declararse explícitamente como simulado");
        assertTrue(summary.getExecutionModeDescription().toLowerCase().contains("simulada"));
    }

    @Test
    void validatesEntireExpandedCurriculumAcrossAllThreeLanguages() {
        List<Language> languages = DemoSeedCatalog.createSeedLanguages();
        assertEquals(3, languages.size());

        int totalModules = 0;
        int totalLessons = 0;
        int totalChallenges = 0;
        int totalTestCases = 0;

        for (Language lang : languages) {
            assertEquals(3, lang.getModules().size(), "Cada lenguaje debe tener 3 módulos");
            for (CourseModule mod : lang.getModules()) {
                totalModules++;
                assertEquals(2, mod.getLessons().size(), "Cada módulo debe tener 2 lecciones");
                for (Lesson lesson : mod.getLessons()) {
                    totalLessons++;
                    assertEquals(2, lesson.getChallenges().size(),
                            "Cada lección debe tener 2 retos (Código y Quiz): " + lesson.getTitle());
                    for (Challenge ch : lesson.getChallenges()) {
                        totalChallenges++;
                        if (ch instanceof CodeChallenge) {
                            CodeChallenge cc = (CodeChallenge) ch;
                            assertEquals(3, cc.getTestCases().size(),
                                    "Cada reto de código debe tener 3 casos de prueba: " + cc.getTitle());
                            totalTestCases += cc.getTestCases().size();
                            SimulatedCodeRunner.ExecutionSummary summary =
                                    runner.runAndValidate(cc, cc.getInitialTemplate());
                            assertTrue(summary.isAllPassed(),
                                    "El reto #" + cc.getId() + " (" + cc.getTitle() + ") falló validación: "
                                    + summary.getCaseOutcomes().stream()
                                            .map(SimulatedCodeRunner.CaseOutcome::getMessage)
                                            .reduce("", (a, b) -> a + " | " + b));
                        } else if (ch instanceof QuizChallenge) {
                            QuizChallenge qc = (QuizChallenge) ch;
                            assertEquals(4, qc.getOptions().size());
                            assertTrue(qc.getOptions().contains(qc.getCorrectAnswer()));
                        }
                    }
                }
            }
        }

        assertEquals(9, totalModules);
        assertEquals(18, totalLessons);
        assertEquals(36, totalChallenges);
        assertEquals(54, totalTestCases);
    }
}

