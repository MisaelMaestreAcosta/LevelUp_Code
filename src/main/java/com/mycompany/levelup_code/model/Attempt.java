package com.mycompany.levelup_code.model;

import java.time.LocalDateTime;

/**
 * Entidad que registra el intento de resolución de un reto por parte de un usuario.
 * Según RF-08, almacena el código o respuesta enviada, el resultado obtenido,
 * el tiempo de ejecución y la experiencia otorgada.
 * Según RNF-06, asegura la trazabilidad histórica de los intentos.
 */
public class Attempt {

    // Identificador único del intento
    private int id;

    // Usuario que realizó el intento
    private User user;

    // Reto al que corresponde el intento (CodeChallenge o QuizChallenge)
    private Challenge challenge;

    // Código fuente o respuesta textual enviada por el estudiante
    private String submittedContent;

    // Indica si el intento fue resuelto con éxito (aprobado)
    private boolean successful;

    // Tiempo de ejecución registrado en milisegundos
    private long executionTimeMs;

    // Experiencia otorgada por este intento
    private int earnedExperience;

    // Fecha y hora exacta en la que se registró el intento
    private LocalDateTime attemptDate;

    /**
     * Constructor por defecto
     */
    public Attempt() {
        this.attemptDate = LocalDateTime.now();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param user Usuario
     * @param challenge Reto evaluado
     * @param submittedContent Código o respuesta enviada
     * @param successful Si fue exitoso
     * @param executionTimeMs Tiempo de ejecución en milisegundos
     * @param earnedExperience Experiencia otorgada
     * @param attemptDate Fecha del intento
     */
    public Attempt(int id, User user, Challenge challenge, String submittedContent,
                   boolean successful, long executionTimeMs, int earnedExperience, LocalDateTime attemptDate) {
        this.id = id;
        this.user = user;
        this.challenge = challenge;
        this.submittedContent = submittedContent;
        this.successful = successful;
        this.executionTimeMs = executionTimeMs;
        this.earnedExperience = earnedExperience;
        this.attemptDate = attemptDate;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public String getSubmittedContent() {
        return submittedContent;
    }

    public void setSubmittedContent(String submittedContent) {
        this.submittedContent = submittedContent;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getEarnedExperience() {
        return earnedExperience;
    }

    public void setEarnedExperience(int earnedExperience) {
        this.earnedExperience = earnedExperience;
    }

    public LocalDateTime getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(LocalDateTime attemptDate) {
        this.attemptDate = attemptDate;
    }
}
