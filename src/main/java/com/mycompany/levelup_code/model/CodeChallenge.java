package com.mycompany.levelup_code.model;

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
    public String getChallengeType() {
        return "CODE";
    }

    /**
     * Agrega un caso de prueba al reto
     *
     * @param testCase Caso de prueba a vincular
     */
    public void addTestCase(TestCase testCase) {
        testCase.setChallenge(this);
        this.testCases.add(testCase);
    }

    // --- Getters y Setters ---

    public String getInitialTemplate() {
        return initialTemplate;
    }

    public void setInitialTemplate(String initialTemplate) {
        this.initialTemplate = initialTemplate;
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
        this.testCases = testCases;
    }
}
