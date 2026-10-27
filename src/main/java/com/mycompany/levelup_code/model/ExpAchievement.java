package com.mycompany.levelup_code.model;

/**
 * Especialización de Logro desbloqueable por acumulación de puntos de experiencia (XP).
 * Corresponde a LogroExp según RF-11.
 */
public class ExpAchievement extends Achievement {

    // Cantidad total de experiencia (XP) requerida para desbloquear el logro (requiredExp)
    private int requiredExperience;

    /**
     * Constructor por defecto
     */
    public ExpAchievement() {
        super();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del logro
     * @param description Descripción
     * @param icon Icono
     * @param requiredExperience Experiencia requerida
     */
    public ExpAchievement(int id, String name, String description, String icon, int requiredExperience) {
        super(id, name, description, icon);
        this.requiredExperience = requiredExperience;
    }

    @Override
    public boolean isUnlockedBy(UserStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getTotalExp() >= requiredExperience;
    }

    @Override
    public String getAchievementType() {
        return "EXPERIENCE";
    }

    @Override
    public int getTargetValue() {
        return requiredExperience;
    }

    // --- Getters y Setters ---

    public int getRequiredExperience() {
        return requiredExperience;
    }

    public void setRequiredExperience(int requiredExperience) {
        this.requiredExperience = requiredExperience;
    }

    public int getRequiredExp() {
        return requiredExperience;
    }

    public void setRequiredExp(int requiredExp) {
        this.requiredExperience = requiredExp;
    }
}
