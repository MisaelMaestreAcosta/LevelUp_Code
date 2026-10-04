package com.mycompany.levelup_code.model;

/**
 * Entidad que representa un Lenguaje de programación disponible en la plataforma.
 * Incluye información como nombre, descripción, si está activo y su icono.
 */
public class Language {

    // Identificador único del lenguaje
    private int id;

    // Nombre del lenguaje (ej. Java, Python, C++)
    private String name;

    // Descripción general del lenguaje
    private String description;

    // Icono o ruta de la imagen representativa del lenguaje
    private String icon;

    // Indica si el lenguaje está activo y disponible para los estudiantes
    private boolean isActive;

    /**
     * Constructor por defecto
     */
    public Language() {
        this.isActive = true;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del lenguaje
     * @param description Descripción del lenguaje
     * @param icon Ruta o nombre del icono
     * @param isActive Estado de actividad
     */
    public Language(int id, String name, String description, String icon, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isActive = isActive;
    }

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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
