package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Evaluador híbrido de retos de código:
 * <ul>
 *   <li>Para <b>Java</b>: compila realmente el código con {@link JavaCompiler} (JDK 21) y ejecuta
 *       cada caso de prueba en un subproceso JVM aislado con límite de memoria ({@code -Xmx64m})
 *       y tiempo de reloj estricto.</li>
 *   <li>Para lenguajes sin compilador/intérprete nativo disponible en el entorno local (ej. Python o C++
 *       en equipos de demostración): utiliza análisis heurístico de demostración e informa
 *       explícitamente en el resumen que se trata de una validación simulada.</li>
 * </ul>
 */
public class SimulatedCodeRunner implements CodeRunner {

    private static final Pattern PUBLIC_CLASS_DECL = Pattern.compile("(?:public\\s+)?class\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern STATIC_METHOD_DECL = Pattern.compile(
            "public\\s+static\\s+([A-Za-z0-9_<>\\[\\]]+)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^\\)]*)\\)"
    );

    private static final Pattern BLOCK_COMMENTS = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern PY_DOCSTRINGS = Pattern.compile("(\"\"\"|''').*?\\1", Pattern.DOTALL);
    private static final Pattern LINE_COMMENTS = Pattern.compile("(//|#).*$", Pattern.MULTILINE);

    private static final Pattern CLINE_IF_BRANCH = Pattern.compile(
            "if\\s*\\(([^\\{]+)\\)\\s*\\{?\\s*(?:return|System\\.out\\.print(?:ln)?\\s*\\(|std::cout\\s*<<)\\s*\"([^\"]*)\"",
            Pattern.DOTALL
    );
    private static final Pattern PY_IF_BRANCH = Pattern.compile(
            "(?:if|elif)\\s+([^:\\n]+):\\s*(?:return|print\\s*\\()\\s*[\"']([^\"']*)[\"']",
            Pattern.DOTALL
    );
    private static final Pattern STRING_LITERAL = Pattern.compile("[\"']([^\"']+)[\"']");
    private static final Pattern RETURN_STATEMENT = Pattern.compile(
            "(?:return|print\\s*\\(|System\\.out\\.print(?:ln)?\\s*\\()\\s*\"?([^\"\\n;\\)]+)\"?"
    );
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[([^\\]]*)\\]");
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

        int effectiveTimeout = Math.max(1, timeoutSeconds);

        if (canExecuteRealJava(languageName)) {
            return runIsolatedJava(sourceCode, cleanCode, input, effectiveTimeout);
        }

        return runSimulatedHeuristic(cleanCode, input, effectiveTimeout);
    }

    /**
     * Indica si el lenguaje dado cuenta con ejecución real aislada en este entorno.
     */
    public boolean supportsRealIsolatedExecution(String languageName) {
        return canExecuteRealJava(languageName);
    }

    private boolean canExecuteRealJava(String languageName) {
        String lang = languageName != null ? languageName.trim().toLowerCase(Locale.ROOT) : "java";
        return lang.contains("java") && ToolProvider.getSystemJavaCompiler() != null;
    }

    private String runIsolatedJava(
            String rawSourceCode,
            String cleanCode,
            String input,
            int timeoutSeconds) throws IOException, InterruptedException, TimeoutException {

        Matcher classMatcher = PUBLIC_CLASS_DECL.matcher(cleanCode);
        if (!classMatcher.find()) {
            throw new IOException("Error de compilación Java: no se encontró una declaración 'class NombreClase'.");
        }
        String userClassName = classMatcher.group(1);

        Matcher methodMatcher = STATIC_METHOD_DECL.matcher(cleanCode);
        String targetMethod = null;
        String returnType = "void";
        String paramsSpec = "";
        while (methodMatcher.find()) {
            String candidateName = methodMatcher.group(2);
            if (!"main".equals(candidateName)) {
                returnType = methodMatcher.group(1).trim();
                targetMethod = candidateName;
                paramsSpec = methodMatcher.group(3).trim();
                break;
            }
        }

        Path sandboxDir = Files.createTempDirectory("levelup_jvm_sandbox_");
        try {
            Path userFile = sandboxDir.resolve(userClassName + ".java");
            String sourceToWrite = rawSourceCode;
            if (!rawSourceCode.contains("import java.util.") && !rawSourceCode.trim().startsWith("package ")) {
                sourceToWrite = "import java.util.*;\n" + rawSourceCode;
            }
            Files.writeString(userFile, sourceToWrite, StandardCharsets.UTF_8);

            String runnerClassName = "__LevelUpRunnerMain";
            List<File> filesToCompile = new ArrayList<>();
            filesToCompile.add(userFile.toFile());

            String normalizedInput = input != null ? input.trim() : "";
            if (targetMethod != null) {
                String harnessSource = buildJavaHarnessSource(
                        runnerClassName, userClassName, targetMethod, returnType, paramsSpec, normalizedInput);
                Path harnessFile = sandboxDir.resolve(runnerClassName + ".java");
                Files.writeString(harnessFile, harnessSource, StandardCharsets.UTF_8);
                filesToCompile.add(harnessFile.toFile());
            } else {
                runnerClassName = userClassName;
            }

            compileJavaFiles(filesToCompile, sandboxDir);
            return executeJavaSubprocess(sandboxDir, runnerClassName, normalizedInput, timeoutSeconds);
        } finally {
            deleteDirectoryQuietly(sandboxDir);
        }
    }

    private String buildJavaHarnessSource(
            String runnerClassName,
            String userClassName,
            String methodName,
            String returnType,
            String paramsSpec,
            String normalizedInput) throws IOException {

        String callExpression;
        String compactParams = paramsSpec.replaceAll("\\s+", " ");
        if (compactParams.isEmpty()) {
            callExpression = userClassName + "." + methodName + "()";
        } else if (compactParams.contains("int[]") && compactParams.contains(",")) {
            Matcher arrMatcher = ARRAY_PATTERN.matcher(normalizedInput);
            Matcher targetMatcher = TARGET_PATTERN.matcher(normalizedInput);
            if (!arrMatcher.find() || !targetMatcher.find()) {
                throw new IOException("Formato de entrada inválido para (int[], int): " + normalizedInput);
            }
            String csv = arrMatcher.group(1).trim();
            int target = Integer.parseInt(targetMatcher.group(1));
            callExpression = userClassName + "." + methodName + "(new int[]{" + csv + "}, " + target + ")";
        } else if (compactParams.contains("int[]")) {
            Matcher arrMatcher = ARRAY_PATTERN.matcher(normalizedInput);
            String csv = arrMatcher.find() ? arrMatcher.group(1).trim() : normalizedInput;
            callExpression = userClassName + "." + methodName + "(new int[]{" + csv + "})";
        } else if (compactParams.startsWith("int ") || "int".equals(compactParams)
                || compactParams.startsWith("long ") || "long".equals(compactParams)) {
            String[] parts = normalizedInput.split(",");
            StringBuilder args = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    args.append(", ");
                }
                args.append(Long.parseLong(parts[i].trim()));
            }
            callExpression = userClassName + "." + methodName + "(" + args + ")";
        } else {
            callExpression = userClassName + "." + methodName + "(\"" + escapeJavaString(normalizedInput) + "\")";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("public final class ").append(runnerClassName).append(" {\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        if ("void".equals(returnType)) {
            sb.append("        ").append(callExpression).append(";\n");
        } else {
            sb.append("        Object __res = ").append(callExpression).append(";\n");
            sb.append("        System.out.print(__res == null ? \"\" : String.valueOf(__res));\n");
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private void compileJavaFiles(List<File> files, Path outputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(files);
            List<String> options = Arrays.asList("-d", outputDir.toString(), "-encoding", "UTF-8");
            Boolean ok = compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
            if (ok == null || !ok) {
                StringBuilder err = new StringBuilder("Error de compilación Java: ");
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.ERROR) {
                        err.append("Línea ")
                           .append(d.getLineNumber())
                           .append(": ")
                           .append(d.getMessage(Locale.getDefault()))
                           .append(" ");
                        break;
                    }
                }
                throw new IOException(err.toString().trim());
            }
        }
    }

    private String executeJavaSubprocess(
            Path sandboxDir,
            String mainClass,
            String stdinPayload,
            int timeoutSeconds) throws IOException, InterruptedException, TimeoutException {

        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-Xmx64m",
                "-Xss512k",
                "-XX:+DisableAttachMechanism",
                "-cp",
                sandboxDir.toString(),
                mainClass
        );
        pb.directory(sandboxDir.toFile());
        Process process = pb.start();

        if (stdinPayload != null && !stdinPayload.isEmpty()) {
            try {
                process.getOutputStream().write(stdinPayload.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
        }
        try {
            process.getOutputStream().close();
        } catch (IOException ignored) {
        }

        ByteArrayOutputStream stdoutBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuf = new ByteArrayOutputStream();
        Thread outReader = startStreamCollector(process.getInputStream(), stdoutBuf, 16_384);
        Thread errReader = startStreamCollector(process.getErrorStream(), stderrBuf, 16_384);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            outReader.join(200);
            errReader.join(200);
            throw new TimeoutException("Tiempo límite excedido (" + timeoutSeconds + "s) en ejecución aislada JVM.");
        }

        outReader.join(500);
        errReader.join(500);

        if (process.exitValue() != 0) {
            String errMsg = stderrBuf.toString(StandardCharsets.UTF_8).trim();
            throw new IOException("Excepción en tiempo de ejecución: " + (errMsg.isEmpty() ? "código " + process.exitValue() : errMsg));
        }

        return stdoutBuf.toString(StandardCharsets.UTF_8).trim();
    }

    private Thread startStreamCollector(InputStream stream, ByteArrayOutputStream target, int maxBytes) {
        Thread t = new Thread(() -> {
            byte[] buf = new byte[1024];
            int total = 0;
            int read;
            try {
                while ((read = stream.read(buf)) != -1) {
                    int allowed = Math.min(read, maxBytes - total);
                    if (allowed > 0) {
                        target.write(buf, 0, allowed);
                        total += allowed;
                    }
                }
            } catch (IOException ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static String escapeJavaString(String raw) {
        return raw.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }

    private static void deleteDirectoryQuietly(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private String runSimulatedHeuristic(
            String cleanCode,
            String input,
            int timeoutSeconds) throws IOException, TimeoutException {

        if (hasInfiniteLoop(cleanCode)) {
            throw new TimeoutException("Tiempo límite excedido (" + timeoutSeconds + "s): bucle infinito detectado.");
        }

        if (!hasBalancedDelimiters(cleanCode)) {
            throw new IOException("Error de sintaxis: llaves {}, paréntesis () o corchetes [] desbalanceados.");
        }

        String normalizedInput = input != null ? input.trim() : "";

        Matcher arrMatcher = ARRAY_PATTERN.matcher(normalizedInput);
        Matcher targetMatcher = TARGET_PATTERN.matcher(normalizedInput);
        if (arrMatcher.find()) {
            if (targetMatcher.find()) {
                return evaluateArraySearch(cleanCode, arrMatcher.group(1), Integer.parseInt(targetMatcher.group(1)));
            }
            String singleArrayResult = evaluateSingleArrayAlgorithm(cleanCode, arrMatcher.group(1));
            if (singleArrayResult != null) {
                return singleArrayResult;
            }
        }

        String branchOutput = evaluateConditionalBranches(cleanCode, normalizedInput);
        if (branchOutput != null) {
            return branchOutput;
        }

        String palindromeOutput = evaluatePalindromeLogic(cleanCode, normalizedInput);
        if (palindromeOutput != null) {
            return palindromeOutput;
        }

        if (normalizedInput.matches("-?\\d+(?:\\s*,\\s*-?\\d+)*")) {
            String numericResult = evaluateNumericLogic(cleanCode, normalizedInput);
            if (numericResult != null) {
                return numericResult;
            }
        }

        return extractDefaultReturn(cleanCode);
    }

    /**
     * Ejecuta todos los casos de prueba de un {@link CodeChallenge} y devuelve el detalle
     * individual junto con el modo real o simulado empleado.
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

        boolean sandboxed = supportsRealIsolatedExecution(langName);
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
        return new ExecutionSummary(passed, cases.size(), seconds, outcomes, sandboxed);
    }

    private String evaluateConditionalBranches(String cleanCode, String inputValue) {
        boolean codeNormalizesCase = cleanCode.contains("toLowerCase")
                || cleanCode.contains("toUpperCase")
                || cleanCode.contains(".lower()")
                || cleanCode.contains(".upper()");

        Matcher javaMatcher = CLINE_IF_BRANCH.matcher(cleanCode);
        while (javaMatcher.find()) {
            String condition = javaMatcher.group(1);
            String returnVal = javaMatcher.group(2);
            if (conditionMatchesInput(condition, inputValue, codeNormalizesCase)) {
                return returnVal;
            }
        }

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

    private String evaluateSingleArrayAlgorithm(String cleanCode, String csvNumbers) {
        String[] parts = csvNumbers.split(",");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            if (!p.trim().isEmpty()) {
                list.add(Integer.parseInt(p.trim()));
            }
        }

        boolean looksLikeDuplicateSearch = cleanCode.contains("set(")
                || cleanCode.contains("unordered_set")
                || cleanCode.contains("HashSet")
                || cleanCode.contains("vistos")
                || cleanCode.contains("seen")
                || cleanCode.contains(".count(")
                || cleanCode.contains(".insert(");
        if (looksLikeDuplicateSearch && (cleanCode.contains("for") || cleanCode.contains("while"))) {
            List<Integer> seen = new ArrayList<>();
            for (int v : list) {
                if (seen.contains(v)) {
                    return String.valueOf(v);
                }
                seen.add(v);
            }
            return cleanCode.contains("-1") ? "-1" : "0";
        }

        boolean looksLikeEvenSum = (cleanCode.contains("% 2") || cleanCode.contains("%2"))
                && (cleanCode.contains("sum") || cleanCode.contains("total") || cleanCode.contains("+=") || cleanCode.contains("+"));
        if (looksLikeEvenSum) {
            int sum = 0;
            for (int v : list) {
                if (v % 2 == 0) {
                    sum += v;
                }
            }
            return String.valueOf(sum);
        }

        return null;
    }

    private String evaluatePalindromeLogic(String cleanCode, String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        boolean looksLikePalindrome = cleanCode.contains("[::-1]")
                || cleanCode.contains("reverse")
                || (cleanCode.contains("left") && cleanCode.contains("right"))
                || (cleanCode.contains("izq") && cleanCode.contains("der"))
                || cleanCode.contains("palindrom")
                || cleanCode.contains("Palindrom");
        if (!looksLikePalindrome) {
            return null;
        }
        boolean ignoreCase = cleanCode.contains("lower")
                || cleanCode.contains("Lower")
                || cleanCode.contains("tolower");
        String candidate = ignoreCase ? input.toLowerCase(Locale.ROOT) : input;
        String reversed = new StringBuilder(candidate).reverse().toString();
        return candidate.equals(reversed) ? "true" : "false";
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

        if ((cleanCode.contains("*=") || (cleanCode.contains("*") && cleanCode.contains("- 1"))) && n >= 0 && n <= 20) {
            long fact = 1;
            for (long i = 2; i <= n; i++) {
                fact *= i;
            }
            return String.valueOf(fact);
        }

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
        private final boolean sandboxedExecution;

        public ExecutionSummary(
                int passedCount,
                int totalCount,
                double executionTimeSeconds,
                List<CaseOutcome> caseOutcomes,
                boolean sandboxedExecution) {
            this.passedCount = passedCount;
            this.totalCount = totalCount;
            this.executionTimeSeconds = executionTimeSeconds;
            this.caseOutcomes = Collections.unmodifiableList(new ArrayList<>(caseOutcomes));
            this.sandboxedExecution = sandboxedExecution;
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

        public boolean isSandboxedExecution() {
            return sandboxedExecution;
        }

        public String getExecutionModeDescription() {
            return sandboxedExecution
                    ? "Ejecución real aislada en JVM (-Xmx64m)"
                    : "Validación simulada de demostración (sin compilador nativo externo)";
        }

        public boolean isAllPassed() {
            return totalCount > 0 && passedCount == totalCount;
        }
    }
}
