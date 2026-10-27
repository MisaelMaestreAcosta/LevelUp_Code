package com.mycompany.levelup_code.model;

import java.time.LocalDateTime;

/**
 * Entidad que representa la inscripción de un usuario a un curso de lenguaje de programación.
 * Según RF-03, permite al usuario inscribirse a uno o varios lenguajes y registrar la fecha.
 * Según RNF-06, asegura la trazabilidad temporal del registro.
 */
public class Enrollment {

    // Identificador único de la inscripción
    private int id;

    // Usuario que se inscribe en el curso
    private User user;

    // Lenguaje al cual se inscribe el usuario
    private Language language;

    // Fecha y hora en la que se realizó la inscripción
    private LocalDateTime enrollmentDate;

    // Indica si la inscripción sigue activa o fue archivada
    private boolean active;

    /**
     * Constructor por defecto
     */
    public Enrollment() {
        this.enrollmentDate = LocalDateTime.now();
        this.active = true;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param user Usuario inscrito
     * @param language Lenguaje asociado
     * @param enrollmentDate Fecha de inscripción
     * @param active Estado de la inscripción
     */
    public Enrollment(int id, User user, Language language, LocalDateTime enrollmentDate, boolean active) {
        this.id = id;
        this.user = user;
        this.language = language;
        this.enrollmentDate = enrollmentDate;
        this.active = active;
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Consulta si la inscripción corresponde a un lenguaje dado por su identificador.
     *
     * @param languageId Identificador del lenguaje
     * @return true si coincide el lenguaje, false en caso contrario
     */
    public boolean isForLanguage(int languageId) {
        return language != null && language.getId() == languageId;
    }

    public LocalDateTime getEnrolledAt() {
        return enrollmentDate;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrollmentDate = enrolledAt;
    }
}
