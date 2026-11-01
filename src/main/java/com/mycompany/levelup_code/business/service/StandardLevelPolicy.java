package com.mycompany.levelup_code.business.service;

/**
 * Política estándar de progresión de nivel: cada 300 puntos de experiencia (XP)
 * acumulados incrementan un nivel a partir del nivel 1.
 */
public class StandardLevelPolicy implements LevelPolicy {

    public static final int XP_PER_LEVEL = 300;

    @Override
    public int levelForExperience(int totalExperience) {
        return 1 + Math.max(0, totalExperience) / XP_PER_LEVEL;
    }
}

