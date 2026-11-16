package com.mycompany.levelup_code.domain.model;

/**
 * Especialización de Logro desbloqueable por alcanzar una racha de días consecutivos.
 * Corresponde a LogroRacha según RF-11.
 */
public class StreakAchievement extends Achievement {

    // Número de días consecutivos requeridos para desbloquear el logro (requiredDays)
    private int requiredStreakDays;

    /**
     * Constructor por defecto
     */
    public StreakAchievement() {
        super();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del logro
     * @param description Descripción
     * @param icon Icono
     * @param requiredStreakDays Días consecutivos requeridos
     */
    public StreakAchievement(int id, String name, String description, String icon, int requiredStreakDays) {
        super(id, name, description, icon);
        this.requiredStreakDays = requiredStreakDays;
    }

    @Override
    public boolean isUnlockedBy(UserStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getCurrentStreak() >= requiredStreakDays;
    }

    @Override
    public String getAchievementType() {
        return "STREAK";
    }

    @Override
    public int getTargetValue() {
        return requiredStreakDays;
    }

    // --- Getters y Setters ---

    public int getRequiredStreakDays() {
        return requiredStreakDays;
    }

    public void setRequiredStreakDays(int requiredStreakDays) {
        this.requiredStreakDays = requiredStreakDays;
    }

    public int getRequiredDays() {
        return requiredStreakDays;
    }

    public void setRequiredDays(int requiredDays) {
        this.requiredStreakDays = requiredDays;
    }
}
