package com.mycompany.levelup_code.model;

import java.time.LocalDateTime;

/**
 * Entidad que asocia un logro desbloqueado con un usuario específico.
 * Según RF-11 y RNF-06, almacena la fecha y hora exacta en que el logro fue otorgado,
 * garantizando la trazabilidad histórica de los logros del estudiante.
 */
public class UserAchievement {

    // Identificador único del registro
    private int id;

    // Usuario que obtuvo el logro
    private User user;

    // Logro específico otorgado (StreakAchievement, ExperienceAchievement o ChallengeCountAchievement)
    private Achievement achievement;

    // Fecha y hora en la que fue desbloqueado el logro (RNF-06)
    private LocalDateTime unlockedAt;

    /**
     * Constructor por defecto
     */
    public UserAchievement() {
        this.unlockedAt = LocalDateTime.now();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param user Usuario
     * @param achievement Logro obtenido
     * @param unlockedAt Fecha de desbloqueo
     */
    public UserAchievement(int id, User user, Achievement achievement, LocalDateTime unlockedAt) {
        this.id = id;
        this.user = user;
        this.achievement = achievement;
        this.unlockedAt = unlockedAt;
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

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}
