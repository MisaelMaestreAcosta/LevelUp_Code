package com.mycompany.levelup_code.business.evaluator;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de evaluación simulada que analiza el código fuente sin comentarios,
 * verifica estructura sintáctica básica y simula la salida para cada entrada de prueba.
 */
public class SimulatedCodeRunner implements CodeRunner {

    private static final Pattern BLOCK_COMMENTS = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern LINE_COMMENTS = Pattern.compile("(//|#).*$", Pattern.MULTILINE);
    private static final Pattern RETURN_LITERAL = Pattern.compile("return\\s+\"([^\"]*)\"");

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
            throw new IOException("El código no contiene instrucciones ejecutables.");
        }

        if (cleanCode.contains("while(true)") || cleanCode.contains("while (true)")) {
            throw new TimeoutException("Bucle infinito detectado durante la evaluación.");
        }

        if (!hasBalancedBraces(cleanCode)) {
            throw new IOException("Error de sintaxis: llaves o paréntesis desbalanceados.");
        }

        String normalizedInput = input != null ? input.trim() : "";

        // Simulación para retos de polimorfismo / clasificación de cadenas (Perro / Gato)
        if ("Perro".equalsIgnoreCase(normalizedInput)) {
            boolean handlesPerro = containsIgnoreCase(cleanCode, "\"Perro\"")
                    && containsReturnLiteral(cleanCode, "Guau");
            if (handlesPerro && (!"PERRO".equals(normalizedInput)
                    || cleanCode.contains("equalsIgnoreCase")
                    || cleanCode.contains("toLowerCase")
                    || cleanCode.contains("lower()"))) {
                return "Guau";
            }
        }

        if ("Gato".equalsIgnoreCase(normalizedInput)) {
            boolean handlesGato = containsIgnoreCase(cleanCode, "\"Gato\"")
                    && containsReturnLiteral(cleanCode, "Miau");
            if (handlesGato) {
                return "Miau";
            }
        }

        // Simulación para retos de búsqueda binaria O(log n)
        if (normalizedInput.contains("target=12")) {
            if (cleanCode.contains("while") && cleanCode.contains("mid") && cleanCode.contains("return mid")) {
                return "3";
            }
        }
        if (normalizedInput.contains("target=7")) {
            if (cleanCode.contains("while") && cleanCode.contains("return -1")) {
                return "-1";
            }
        }

        // Evaluación general de retorno literal en código ejecutable (nunca en comentarios)
        Matcher matcher = RETURN_LITERAL.matcher(cleanCode);
        String lastLiteral = "";
        while (matcher.find()) {
            lastLiteral = matcher.group(1);
        }
        return lastLiteral;
    }

    public static String stripComments(String code) {
        if (code == null) {
            return "";
        }
        String withoutBlocks = BLOCK_COMMENTS.matcher(code).replaceAll("");
        return LINE_COMMENTS.matcher(withoutBlocks).replaceAll("");
    }

    private static boolean containsIgnoreCase(String text, String needle) {
        return text.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static boolean containsReturnLiteral(String cleanCode, String literal) {
        Matcher m = RETURN_LITERAL.matcher(cleanCode);
        while (m.find()) {
            if (literal.equals(m.group(1))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBalancedBraces(String code) {
        int braces = 0;
        int parens = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '{') {
                braces++;
            } else if (c == '}') {
                braces--;
            } else if (c == '(') {
                parens++;
            } else if (c == ')') {
                parens--;
            }
            if (braces < 0 || parens < 0) {
                return false;
            }
        }
        return braces == 0 && parens == 0;
    }
}
