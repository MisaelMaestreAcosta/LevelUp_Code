package com.mycompany.levelup_code.model;

import java.time.LocalDateTime;

/**
 * Entidad que representa el seguimiento de progreso de un usuario en una lección específica.
 * Según RF-05, registra el estado de avance (pendiente, en curso, completada)
 * y la fecha en la que fue aprobada.
 */
public class LessonProgress {

    // Identificador único del registro de progreso
    private int id;

    // Usuario asociado al progreso
    private User user;

    // Lección correspondiente
    private Lesson lesson;

    // Estado actual del progreso en la lección
    private ProgressStatus status;

    // Fecha y hora en la que la lección fue completada y aprobada
    private LocalDateTime approvedAt;

    /**
     * Constructor por defecto
     */
    public LessonProgress() {
        this.status = ProgressStatus.PENDING;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param user Usuario
     * @param lesson Lección
     * @param status Estado de avance
     * @param approvedAt Fecha de aprobación
     */
    public LessonProgress(int id, User user, Lesson lesson, ProgressStatus status, LocalDateTime approvedAt) {
        this.id = id;
        this.user = user;
        this.lesson = lesson;
        this.status = status;
        this.approvedAt = approvedAt;
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

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
