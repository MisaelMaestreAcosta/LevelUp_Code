package com.mycompany.levelup_code.model;

import java.time.LocalDate;

/**
 * Entidad que registra la actividad diaria de práctica de un usuario.
 * Según RF-10, almacena la experiencia ganada por día, sirviendo como base
 * para el cálculo de la racha actual y de la racha máxima histórica.
 */
public class DailyActivity {

    // Identificador único del registro de actividad
    private int id;

    // Usuario asociado a la actividad
    private User user;

    // Fecha del día en que se realizó la práctica
    private LocalDate activityDate;

    // Experiencia total ganada durante este día
    private int experienceGained;

    /**
     * Constructor por defecto
     */
    public DailyActivity() {
        this.activityDate = LocalDate.now();
        this.experienceGained = 0;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param user Usuario
     * @param activityDate Fecha de la actividad
     * @param experienceGained Experiencia acumulada en el día
     */
    public DailyActivity(int id, User user, LocalDate activityDate, int experienceGained) {
        this.id = id;
        this.user = user;
        this.activityDate = activityDate;
        this.experienceGained = experienceGained;
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

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public int getExperienceGained() {
        return experienceGained;
    }

    public void setExperienceGained(int experienceGained) {
        this.experienceGained = experienceGained;
    }

    /**
     * Suma experiencia al registro del día actual.
     *
     * @param amount Puntos de experiencia a agregar
     */
    public void addExperience(int amount) {
        if (amount > 0) {
            this.experienceGained += amount;
        }
    }

    public int getExpEarned() {
        return experienceGained;
    }

    public void setExpEarned(int expEarned) {
        this.experienceGained = expEarned;
    }
}
