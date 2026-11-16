package com.mycompany.levelup_code.domain.model;

/**
 * Objeto de valor (Value Object) que concentra las estadísticas acumuladas de un usuario.
 * Se utiliza como parámetro para evaluar el cumplimiento y desbloqueo de logros (RF-11).
 */
public class UserStats {

    // Racha actual de días consecutivos de práctica
    private int currentStreak;

    // Mayor racha histórica alcanzada por el usuario
    private int maxStreak;

    // Experiencia total acumulada
    private int totalExp;

    // Cantidad total de retos resueltos exitosamente
    private int solvedChallenges;

    /**
     * Constructor por defecto
     */
    public UserStats() {
    }

    /**
     * Constructor con parámetros
     *
     * @param currentStreak Racha actual en días
     * @param maxStreak Racha máxima histórica
     * @param totalExp Experiencia acumulada
     * @param solvedChallenges Cantidad de retos aprobados
     */
    public UserStats(int currentStreak, int maxStreak, int totalExp, int solvedChallenges) {
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
        this.totalExp = totalExp;
        this.solvedChallenges = solvedChallenges;
    }

    // --- Getters y Setters ---

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setMaxStreak(int maxStreak) {
        this.maxStreak = maxStreak;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(int totalExp) {
        this.totalExp = totalExp;
    }

    public int getSolvedChallenges() {
        return solvedChallenges;
    }

    public void setSolvedChallenges(int solvedChallenges) {
        this.solvedChallenges = solvedChallenges;
    }
}
