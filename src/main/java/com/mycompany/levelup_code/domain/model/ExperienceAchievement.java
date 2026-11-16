package com.mycompany.levelup_code.domain.model;

/**
 * Especialización de Logro desbloqueable por acumulación de puntos de experiencia (XP).
 * Mantiene compatibilidad con la denominación ExpAchievement definida en el documento de arquitectura.
 */
public class ExperienceAchievement extends ExpAchievement {

    public ExperienceAchievement() {
        super();
    }

    public ExperienceAchievement(int id, String name, String description, String icon, int requiredExperience) {
        super(id, name, description, icon, requiredExperience);
    }
}
