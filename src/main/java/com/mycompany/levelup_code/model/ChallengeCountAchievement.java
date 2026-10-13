package com.mycompany.levelup_code.model;

/**
 * Especialización de Logro desbloqueable por cantidad de retos resueltos.
 * Corresponde a LogroRetos según RF-11.
 */
public class ChallengeCountAchievement extends Achievement {

    // Número de retos resueltos con éxito necesarios para desbloquear el logro
    private int requiredChallengesCount;

    /**
     * Constructor por defecto
     */
    public ChallengeCountAchievement() {
        super();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del logro
     * @param description Descripción
     * @param icon Icono
     * @param requiredChallengesCount Cantidad de retos requeridos
     */
    public ChallengeCountAchievement(int id, String name, String description, String icon, int requiredChallengesCount) {
        super(id, name, description, icon);
        this.requiredChallengesCount = requiredChallengesCount;
    }

    @Override
    public String getAchievementType() {
        return "CHALLENGES";
    }

    @Override
    public int getTargetValue() {
        return requiredChallengesCount;
    }

    // --- Getters y Setters ---

    public int getRequiredChallengesCount() {
        return requiredChallengesCount;
    }

    public void setRequiredChallengesCount(int requiredChallengesCount) {
        this.requiredChallengesCount = requiredChallengesCount;
    }
}
