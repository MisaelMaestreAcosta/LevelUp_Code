package com.mycompany.levelup_code.domain.model;

/**
 * Entidad que representa un caso de prueba para la validación automática de código.
 * Según RF-07, contiene datos de entrada y salida esperada, y puede ser visible
 * u oculto para el estudiante.
 */
public class TestCase {

    // Identificador único del caso de prueba
    private int id;

    // Reto de código al que pertenece el caso de prueba
    private CodeChallenge challenge;

    // Entrada estándar o argumentos de prueba
    private String input;

    // Salida esperada que debe generar la solución del estudiante
    private String expectedOutput;

    // Indica si el caso de prueba es oculto (para evaluación estricta) o visible (ejemplo)
    private boolean hidden;

    /**
     * Constructor por defecto
     */
    public TestCase() {
        this.hidden = false;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param challenge Reto de código asociado
     * @param input Entrada de prueba
     * @param expectedOutput Salida esperada
     * @param hidden Visibilidad del caso de prueba
     */
    public TestCase(int id, CodeChallenge challenge, String input, String expectedOutput, boolean hidden) {
        this.id = id;
        this.challenge = challenge;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.hidden = hidden;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CodeChallenge getChallenge() {
        return challenge;
    }

    public void setChallenge(CodeChallenge challenge) {
        this.challenge = challenge;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Valida si una salida generada por la ejecución coincide con la salida esperada del caso de prueba.
     *
     * @param actualOutput Salida obtenida
     * @return true si coincide ignorando saltos y espacios residuales, false en caso contrario
     */
    public boolean matches(String actualOutput) {
        if (expectedOutput == null && actualOutput == null) {
            return true;
        }
        if (expectedOutput == null || actualOutput == null) {
            return false;
        }
        return expectedOutput.trim().equals(actualOutput.trim());
    }
}
