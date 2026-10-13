package com.mycompany.levelup_code.model;

/**
 * Clase abstracta que representa un logro dentro del sistema de gamificación.
 * Modela la especialización ES-UN entre logros genéricos y sus subtipos
 * (StreakAchievement, ExperienceAchievement y ChallengeCountAchievement),
 * según lo especificado en RF-11 y la arquitectura del proyecto.
 */
public abstract class Achievement {

    // Identificador único del logro
    private int id;

    // Nombre distintivo del logro o insignia
    private String name;

    // Descripción del logro y de su criterio de obtención
    private String description;

    // Icono o insignia gráfica que representa el logro
    private String icon;

    /**
     * Constructor por defecto
     */
    public Achievement() {
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del logro
     * @param description Descripción
     * @param icon Ruta o nombre del icono
     */
    public Achievement(int id, String name, String description, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    /**
     * Retorna el tipo de criterio del logro (ej. "STREAK", "EXPERIENCE", "CHALLENGES").
     *
     * @return Tipo de logro
     */
    public abstract String getAchievementType();

    /**
     * Retorna el valor objetivo numérico requerido para desbloquear el logro.
     *
     * @return Meta numérica a alcanzar
     */
    public abstract int getTargetValue();

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
