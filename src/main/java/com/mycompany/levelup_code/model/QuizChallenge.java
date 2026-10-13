package com.mycompany.levelup_code.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Especialización de reto para preguntas de tipo quiz / selección múltiple.
 * Según RF-06, contiene un conjunto de opciones de respuesta y la respuesta correcta.
 */
public class QuizChallenge extends Challenge {

    // Lista de opciones textuales disponibles para la pregunta
    private List<String> options;

    // Texto de la opción correcta; debe coincidir con una de las opciones de la lista
    private String correctAnswer;

    /**
     * Constructor por defecto
     */
    public QuizChallenge() {
        super();
        this.options = new ArrayList<>();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param title Título de la pregunta
     * @param description Enunciado del quiz
     * @param lesson Lección asociada
     * @param expReward Puntos de experiencia
     * @param options Lista de opciones
     * @param correctAnswer Respuesta correcta
     */
    public QuizChallenge(int id, String title, String description, Lesson lesson, int expReward,
                         List<String> options, String correctAnswer) {
        super(id, title, description, lesson, expReward);
        this.options = (options != null) ? options : new ArrayList<>();
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String getChallengeType() {
        return "QUIZ";
    }

    /**
     * Agrega una opción de respuesta al quiz
     *
     * @param option Texto de la opción
     */
    public void addOption(String option) {
        this.options.add(option);
    }

    // --- Getters y Setters ---

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
