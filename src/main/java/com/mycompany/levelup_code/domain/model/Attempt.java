package com.mycompany.levelup_code.domain.model;

import com.mycompany.levelup_code.domain.enums.AttemptResult;

import java.time.LocalDateTime;

/**
 * Entidad que registra el intento de resolución de un reto por parte de un usuario.
 * Según RF-08, almacena el código o respuesta enviada, el resultado obtenido (AttemptResult),
 * el tiempo de ejecución y la experiencia otorgada.
 * Según RNF-06, asegura la trazabilidad histórica de los envíos.
 */
public class Attempt {

    // Identificador único del intento
    private int id;

    // Usuario que realizó el intento
    private User user;

    // Reto al que corresponde el intento (CodeChallenge o QuizChallenge)
    private Challenge challenge;

    // Código fuente o respuesta textual enviada por el estudiante (submission)
    private String submittedContent;

    // Resultado formal de la evaluación del intento (CORRECT, INCORRECT, ERROR, TIMEOUT)
    private AttemptResult result;

    // Tiempo de ejecución registrado en milisegundos
    private long executionTimeMs;

    // Experiencia otorgada por este intento
    private int earnedExperience;

    // Fecha y hora exacta en la que se registró el intento (submittedAt)
    private LocalDateTime attemptDate;

    /**
     * Constructor por defecto
     */
    public Attempt() {
        this.attemptDate = LocalDateTime.now();
        this.result = AttemptResult.INCORRECT;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param user Usuario
     * @param challenge Reto evaluado
     * @param submittedContent Código o respuesta enviada
     * @param result Resultado de la evaluación
     * @param executionTimeMs Tiempo de ejecución en milisegundos
     * @param earnedExperience Experiencia otorgada
     * @param attemptDate Fecha del intento
     */
    public Attempt(int id, User user, Challenge challenge, String submittedContent,
                   AttemptResult result, long executionTimeMs, int earnedExperience, LocalDateTime attemptDate) {
        this.id = id;
        this.user = user;
        this.challenge = challenge;
        this.submittedContent = submittedContent;
        this.result = result;
        this.executionTimeMs = executionTimeMs;
        this.earnedExperience = earnedExperience;
        this.attemptDate = attemptDate;
    }

    /**
     * Consulta si el intento fue resuelto de forma correcta.
     *
     * @return true si el resultado es CORRECT, false en caso contrario
     */
    public boolean isCorrect() {
        return this.result == AttemptResult.CORRECT;
    }

    /**
     * Alias de compatibilidad para consultar si fue exitoso.
     *
     * @return true si fue correcto
     */
    public boolean isSuccessful() {
        return isCorrect();
    }

    public void setSuccessful(boolean successful) {
        this.result = successful ? AttemptResult.CORRECT : AttemptResult.INCORRECT;
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

    public int getUserId() {
        return user != null ? user.getId() : 0;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public int getChallengeId() {
        return challenge != null ? challenge.getId() : 0;
    }

    public String getSubmittedContent() {
        return submittedContent;
    }

    public void setSubmittedContent(String submittedContent) {
        this.submittedContent = submittedContent;
    }

    public String getSubmission() {
        return submittedContent;
    }

    public void setSubmission(String submission) {
        this.submittedContent = submission;
    }

    public AttemptResult getResult() {
        return result;
    }

    public void setResult(AttemptResult result) {
        this.result = result;
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

    public int getExpEarned() {
        return earnedExperience;
    }

    public void setExpEarned(int expEarned) {
        this.earnedExperience = expEarned;
    }

    public LocalDateTime getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(LocalDateTime attemptDate) {
        this.attemptDate = attemptDate;
    }

    public LocalDateTime getSubmittedAt() {
        return attemptDate;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.attemptDate = submittedAt;
    }
}
