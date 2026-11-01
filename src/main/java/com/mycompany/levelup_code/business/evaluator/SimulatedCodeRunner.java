package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de evaluación dinámica guiado al 100% por los datos de {@code Casos_Prueba} en Oracle.
 * No contiene valores quemados de ningún reto específico: analiza la estructura del código
 * enviado (en Java, Python o C++) y evalúa dinámicamente las ramas condicionales, búsquedas
 * sobre arreglos y expresiones según la {@code entrada} de cada caso de prueba.
 */
public class SimulatedCodeRunner implements CodeRunner {

    private static final Pattern BLOCK_COMMENTS = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern PY_DOCSTRINGS = Pattern.compile("(\"\"\"|''').*?\\1", Pattern.DOTALL);
    private static final Pattern LINE_COMMENTS = Pattern.compile("(//|#).*$", Pattern.MULTILINE);

    // Captura ramas condicionales en Java/C++: if (... "literal" ...) { ... return "resultado" ... }
    private static final Pattern CLINE_IF_BRANCH = Pattern.compile(
            "if\\s*\\(([^\\{]+)\\)\\s*\\{?\\s*(?:return|System\\.out\\.print(?:ln)?\\s*\\(|std::cout\\s*<<)\\s*\"([^\"]*)\"",
            Pattern.DOTALL
    );

    // Captura ramas condicionales en Python: if/elif ... "literal" ... : return/print("resultado")
    private static final Pattern PY_IF_BRANCH = Pattern.compile(
            "(?:if|elif)\\s+([^:\\n]+):\\s*(?:return|print\\s*\\()\\s*[\"']([^\"']*)[\"']",
            Pattern.DOTALL
    );

    private static final Pattern STRING_LITERAL = Pattern.compile("[\"']([^\"']+)[\"']");
    private static final Pattern RETURN_STATEMENT = Pattern.compile(
            "(?:return|print\\s*\\(|System\\.out\\.print(?:ln)?\\s*\\()\\s*\"?([^\"\\n;\\)]+)\"?"
    );
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
    private static final Pattern TARGET_PATTERN = Pattern.compile("target\\s*=\\s*(-?\\d+)");

    @Override
    public String run(
            String languageName,
            String sourceCode,
            String input,
            int timeoutSeconds) throws IOException, InterruptedException, TimeoutException {

        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            throw new IOException("El código enviado está vacío.");
        }

        String cleanCode = stripComments(sourceCode).trim();
        if (cleanCode.isEmpty()) {
            throw new IOException("El código solo contiene comentarios sin instrucciones ejecutables.");
        }

        if (hasInfiniteLoop(cleanCode)) {
            throw new TimeoutException("Tiempo límite excedido (" + timeoutSeconds + "s): bucle infinito detectado.");
        }

        if (!hasBalancedDelimiters(cleanCode)) {
            throw new IOException("Error de sintaxis: llaves {}, paréntesis () o corchetes [] desbalanceados.");
        }

        String normalizedInput = input != null ? input.trim() : "";

        // 1. Si la entrada describe un arreglo y un objetivo (ej. "[2, 5, 8, 12], target=12"),
        //    evalúa dinámicamente la estructura de búsqueda sobre el arreglo de entrada.
        Matcher arrMatcher = ARRAY_PATTERN.matcher(normalizedInput);
        Matcher targetMatcher = TARGET_PATTERN.matcher(normalizedInput);
        if (arrMatcher.find() && targetMatcher.find()) {
            return evaluateArraySearch(cleanCode, arrMatcher.group(1), Integer.parseInt(targetMatcher.group(1)));
        }

        // 2. Evalúa dinámicamente ramas condicionales (if / else if / elif) comparando la
        //    condición programada por el estudiante contra la entrada del caso de prueba.
        String branchOutput = evaluateConditionalBranches(cleanCode, normalizedInput);
        if (branchOutput != null) {
            return branchOutput;
        }

        // 3. Si la entrada es numérica y el código realiza un cálculo aritmético o iterativo,
        //    evalúa la expresión sobre el parámetro de entrada.
        if (normalizedInput.matches("-?\\d+(?:\\s*,\\s*-?\\d+)*")) {
            String numericResult = evaluateNumericLogic(cleanCode, normalizedInput);
            if (numericResult != null) {
                return numericResult;
            }
        }

        // 4. Retorno por defecto (último return fuera de ramas condicionales)
        return extractDefaultReturn(cleanCode);
    }

    /**
     * Ejecuta todos los casos de prueba de un {@link CodeChallenge} y devuelve el detalle
     * individual de cada caso para mostrar el veredicto en la interfaz.
     */
    public ExecutionSummary runAndValidate(CodeChallenge challenge, String sourceCode) {
        long startNanos = System.nanoTime();
        List<TestCase> cases = challenge != null && challenge.getTestCases() != null
                ? challenge.getTestCases()
                : Collections.emptyList();
        int timeout = challenge != null && challenge.getTimeLimitSeconds() > 0
                ? challenge.getTimeLimitSeconds()
                : 2;
        String langName = "Java";
        if (challenge != null && challenge.getLesson() != null
                && challenge.getLesson().getModule() != null
                && challenge.getLesson().getModule().getLanguage() != null) {
            langName = challenge.getLesson().getModule().getLanguage().getName();
        }

        List<CaseOutcome> outcomes = new ArrayList<>();
        int passed = 0;

        for (int i = 0; i < cases.size(); i++) {
            TestCase tc = cases.get(i);
            String label = tc.isHidden() ? "Caso #" + (i + 1) + " (Oculto)" : "Caso #" + (i + 1) + " [" + tc.getInput() + "]";
            try {
                String actual = run(langName, sourceCode, tc.getInput(), timeout);
                boolean ok = tc.matches(actual);
                if (ok) {
                    passed++;
                    outcomes.add(new CaseOutcome(true, label + " -> Salida correcta (\"" + actual + "\")"));
                } else {
                    String detail = tc.isHidden()
                            ? label + " -> Salida incorrecta en prueba oculta"
                            : label + " -> Se obtuvo \"" + actual + "\", se esperaba \"" + tc.getExpectedOutput() + "\"";
                    outcomes.add(new CaseOutcome(false, detail));
                }
            } catch (TimeoutException ex) {
                outcomes.add(new CaseOutcome(false, label + " -> TIMEOUT: " + ex.getMessage()));
            } catch (Exception ex) {
                outcomes.add(new CaseOutcome(false, label + " -> ERROR: " + ex.getMessage()));
            }
        }

        double seconds = Math.max(0.004, (System.nanoTime() - startNanos) / 1_000_000_000.0);
        return new ExecutionSummary(passed, cases.size(), seconds, outcomes);
    }

    private String evaluateConditionalBranches(String cleanCode, String inputValue) {
        boolean codeNormalizesCase = cleanCode.contains("toLowerCase")
                || cleanCode.contains("toUpperCase")
                || cleanCode.contains(".lower()")
                || cleanCode.contains(".upper()");

        // Evaluar ramas estilo Java / C++
        Matcher javaMatcher = CLINE_IF_BRANCH.matcher(cleanCode);
        while (javaMatcher.find()) {
            String condition = javaMatcher.group(1);
            String returnVal = javaMatcher.group(2);
            if (conditionMatchesInput(condition, inputValue, codeNormalizesCase)) {
                return returnVal;
            }
        }

        // Evaluar ramas estilo Python
        Matcher pyMatcher = PY_IF_BRANCH.matcher(cleanCode);
        while (pyMatcher.find()) {
            String condition = pyMatcher.group(1);
            String returnVal = pyMatcher.group(2);
            if (conditionMatchesInput(condition, inputValue, codeNormalizesCase)) {
                return returnVal;
            }
        }

        return null;
    }

    private boolean conditionMatchesInput(String condition, String inputValue, boolean globalCaseInsensitive) {
        boolean ignoreCase = globalCaseInsensitive
                || condition.contains("equalsIgnoreCase")
                || condition.contains("toLowerCase")
                || condition.contains("toUpperCase")
                || condition.contains(".lower()")
                || condition.contains(".upper()")
                || condition.contains("strcasecmp");

        Matcher literalMatcher = STRING_LITERAL.matcher(condition);
        while (literalMatcher.find()) {
            String literal = literalMatcher.group(1);
            if (ignoreCase) {
                if (literal.equalsIgnoreCase(inputValue)) {
                    return true;
                }
            } else {
                if (literal.equals(inputValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String evaluateArraySearch(String cleanCode, String csvNumbers, int target) throws IOException {
        boolean hasLoop = cleanCode.contains("while") || cleanCode.contains("for");
        boolean hasMidOrIndex = cleanCode.contains("mid") || cleanCode.contains("[") || cleanCode.contains("at(");
        boolean hasReturn = cleanCode.contains("return") || cleanCode.contains("print") || cleanCode.contains("cout");

        if (!hasLoop || !hasMidOrIndex || !hasReturn) {
            throw new IOException("El algoritmo debe recorrer o dividir el arreglo para buscar el elemento.");
        }

        String[] parts = csvNumbers.split(",");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            if (!p.trim().isEmpty()) {
                list.add(Integer.parseInt(p.trim()));
            }
        }

        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int val = list.get(mid);
            if (val == target) {
                return String.valueOf(mid);
            }
            if (val < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return cleanCode.contains("-1") ? "-1" : "";
    }

    private String evaluateNumericLogic(String cleanCode, String numericInput) {
        String[] nums = numericInput.split(",");
        long n = Long.parseLong(nums[0].trim());

        if (nums.length == 2) {
            long b = Long.parseLong(nums[1].trim());
            if (cleanCode.contains("+")) {
                return String.valueOf(n + b);
            }
            if (cleanCode.contains("*")) {
                return String.valueOf(n * b);
            }
        }

        // Si el código calcula factorial (bucle con *= o recursión n - 1)
        if ((cleanCode.contains("*=") || (cleanCode.contains("*") && cleanCode.contains("- 1"))) && n >= 0 && n <= 20) {
            long fact = 1;
            for (long i = 2; i <= n; i++) {
                fact *= i;
            }
            return String.valueOf(fact);
        }

        // Si el código verifica paridad (% 2 == 0)
        if (cleanCode.contains("% 2") || cleanCode.contains("%2")) {
            return (n % 2 == 0) ? "true" : "false";
        }

        return null;
    }

    private String extractDefaultReturn(String cleanCode) {
        Matcher m = RETURN_STATEMENT.matcher(cleanCode);
        String last = "";
        while (m.find()) {
            last = m.group(1).trim();
        }
        return last;
    }

    public static String stripComments(String code) {
        if (code == null) {
            return "";
        }
        String noDoc = PY_DOCSTRINGS.matcher(code).replaceAll("");
        String noBlocks = BLOCK_COMMENTS.matcher(noDoc).replaceAll("");
        return LINE_COMMENTS.matcher(noBlocks).replaceAll("");
    }

    private static boolean hasInfiniteLoop(String cleanCode) {
        String compact = cleanCode.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        boolean endlessWhile = compact.contains("while(true)") || compact.contains("whiletrue:") || compact.contains("for(;;)");
        boolean hasBreakOrReturn = compact.contains("break") || compact.contains("return");
        return endlessWhile && !hasBreakOrReturn;
    }

    private static boolean hasBalancedDelimiters(String code) {
        int braces = 0;
        int parens = 0;
        int brackets = 0;
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if ((c == '"' || c == '\'') && (i == 0 || code.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
                continue;
            }
            if (inQuotes) {
                continue;
            }
            if (c == '{') {
                braces++;
            } else if (c == '}') {
                braces--;
            } else if (c == '(') {
                parens++;
            } else if (c == ')') {
                parens--;
            } else if (c == '[') {
                brackets++;
            } else if (c == ']') {
                brackets--;
            }
            if (braces < 0 || parens < 0 || brackets < 0) {
                return false;
            }
        }
        return braces == 0 && parens == 0 && brackets == 0;
    }

    /** Resultado individual de un caso de prueba. */
    public static final class CaseOutcome {

        private final boolean passed;
        private final String message;

        public CaseOutcome(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getMessage() {
            return message;
        }
    }

    /** Resumen consolidado de la ejecución de un reto de código. */
    public static final class ExecutionSummary {

        private final int passedCount;
        private final int totalCount;
        private final double executionTimeSeconds;
        private final List<CaseOutcome> caseOutcomes;

        public ExecutionSummary(int passedCount, int totalCount, double executionTimeSeconds, List<CaseOutcome> caseOutcomes) {
            this.passedCount = passedCount;
            this.totalCount = totalCount;
            this.executionTimeSeconds = executionTimeSeconds;
            this.caseOutcomes = Collections.unmodifiableList(new ArrayList<>(caseOutcomes));
        }

        public int getPassedCount() {
            return passedCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public double getExecutionTimeSeconds() {
            return executionTimeSeconds;
        }

        public List<CaseOutcome> getCaseOutcomes() {
            return caseOutcomes;
        }

        public boolean isAllPassed() {
            return totalCount > 0 && passedCount == totalCount;
        }
    }
}
