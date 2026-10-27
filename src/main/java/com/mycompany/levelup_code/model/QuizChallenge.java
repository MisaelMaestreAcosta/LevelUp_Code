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

    // Índice de la opción correcta dentro de la lista de opciones (0-indexed)
    private int correctOptionIndex;

    /**
     * Constructor por defecto
     */
    public QuizChallenge() {
        super();
        this.options = new ArrayList<>();
        this.correctOptionIndex = -1;
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
        this.correctOptionIndex = this.options.indexOf(correctAnswer);
    }

    @Override
    public ChallengeType getType() {
        return ChallengeType.QUIZ;
    }

    @Override
    public String getSummary() {
        int optionsCount = options != null ? options.size() : 0;
        return "Reto de quiz (" + optionsCount + " opciones de respuesta)";
    }

    /**
     * Verifica si el índice seleccionado corresponde a la opción correcta.
     *
     * @param index Posición de la opción elegida por el estudiante
     * @return true si es la opción correcta, false en caso contrario
     */
    public boolean isCorrectOption(int index) {
        if (options == null || index < 0 || index >= options.size()) {
            return false;
        }
        if (correctOptionIndex >= 0 && index == correctOptionIndex) {
            return true;
        }
        String chosen = options.get(index);
        return correctAnswer != null && correctAnswer.trim().equalsIgnoreCase(chosen.trim());
    }

    /**
     * Verifica si el texto de la opción seleccionada corresponde a la respuesta correcta.
     *
     * @param optionText Texto de la opción enviada
     * @return true si coincide con la respuesta correcta, false en caso contrario
     */
    public boolean isCorrectOption(String optionText) {
        if (correctAnswer == null || optionText == null) {
            return false;
        }
        return correctAnswer.trim().equalsIgnoreCase(optionText.trim());
    }

    /**
     * Agrega una opción de respuesta al quiz.
     *
     * @param option Texto de la opción
     */
    public void addOption(String option) {
        if (option != null) {
            this.options.add(option);
            if (correctAnswer != null && correctAnswer.trim().equalsIgnoreCase(option.trim())) {
                this.correctOptionIndex = this.options.size() - 1;
            }
        }
    }

    // --- Getters y Setters ---

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
        if (correctAnswer != null) {
            this.correctOptionIndex = this.options.indexOf(correctAnswer);
        }
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
        if (options != null && correctAnswer != null) {
            this.correctOptionIndex = options.indexOf(correctAnswer);
        }
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }
}
