package com.mycompany.levelup_code.domain.model;

import com.mycompany.levelup_code.domain.enums.ChallengeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Especialización de reto para ejercicios de programación en código fuente.
 * Según RF-06, incluye una plantilla de código inicial y un límite de tiempo de ejecución.
 * Se evalúa mediante casos de prueba (RF-07).
 */
public class CodeChallenge extends Challenge {

    // Plantilla o código base inicial proporcionado al estudiante (RF-06)
    private String initialTemplate;

    // Límite de tiempo de ejecución en segundos (RF-06)
    private int timeLimitSeconds;

    // Lista de casos de prueba asociados al reto (visibles y ocultos, RF-07)
    private List<TestCase> testCases;

    /**
     * Constructor por defecto
     */
    public CodeChallenge() {
        super();
        this.testCases = new ArrayList<>();
        this.timeLimitSeconds = 2; // Valor por defecto estándar
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param title Título del reto
     * @param description Enunciado del problema
     * @param lesson Lección asociada
     * @param expReward Puntos de experiencia
     * @param initialTemplate Plantilla de código inicial
     * @param timeLimitSeconds Límite de tiempo de ejecución en segundos
     */
    public CodeChallenge(int id, String title, String description, Lesson lesson, int expReward,
                         String initialTemplate, int timeLimitSeconds) {
        super(id, title, description, lesson, expReward);
        this.initialTemplate = initialTemplate;
        this.timeLimitSeconds = timeLimitSeconds;
        this.testCases = new ArrayList<>();
    }

    @Override
    public ChallengeType getType() {
        return ChallengeType.CODE;
    }

    @Override
    public String getSummary() {
        int casesCount = testCases != null ? testCases.size() : 0;
        return "Reto de código (Límite: " + timeLimitSeconds + "s, Casos de prueba: " + casesCount + ")";
    }

    /**
     * Agrega un caso de prueba al reto
     *
     * @param testCase Caso de prueba a vincular
     */
    public void addTestCase(TestCase testCase) {
        if (testCase != null) {
            testCase.setChallenge(this);
            this.testCases.add(testCase);
        }
    }

    /**
     * Retorna únicamente los casos de prueba visibles para el estudiante (RF-07).
     *
     * @return Lista filtrada de casos de prueba visibles
     */
    public List<TestCase> getVisibleTestCases() {
        List<TestCase> visible = new ArrayList<>();
        if (testCases != null) {
            for (TestCase tc : testCases) {
                if (!tc.isHidden()) {
                    visible.add(tc);
                }
            }
        }
        return visible;
    }

    // --- Getters y Setters ---

    public String getInitialTemplate() {
        return initialTemplate;
    }

    public void setInitialTemplate(String initialTemplate) {
        this.initialTemplate = initialTemplate;
    }

    public String getStarterTemplate() {
        return initialTemplate;
    }

    public void setStarterTemplate(String starterTemplate) {
        this.initialTemplate = starterTemplate;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }
}
