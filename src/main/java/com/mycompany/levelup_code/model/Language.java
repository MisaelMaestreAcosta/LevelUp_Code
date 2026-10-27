package com.mycompany.levelup_code.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Lenguaje de programación disponible en la plataforma.
 * Incluye información como nombre, descripción, si está activo, su icono y sus módulos asociados.
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
    private boolean active;

    // Lista de módulos temáticos contenidos en este lenguaje
    private List<CourseModule> modules;

    /**
     * Constructor por defecto
     */
    public Language() {
        this.active = true;
        this.modules = new ArrayList<>();
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param name Nombre del lenguaje
     * @param description Descripción del lenguaje
     * @param icon Ruta o nombre del icono
     * @param active Estado de actividad
     */
    public Language(int id, String name, String description, String icon, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.active = active;
        this.modules = new ArrayList<>();
    }

    /**
     * Agrega un módulo a la colección de módulos del lenguaje.
     *
     * @param module Módulo a agregar
     */
    public void addModule(CourseModule module) {
        if (module != null) {
            module.setLanguage(this);
            this.modules.add(module);
        }
    }

    /**
     * Desactiva la disponibilidad del lenguaje sin eliminar su contenido (RNF-04).
     */
    public void deactivate() {
        this.active = false;
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

    public String getIconPath() {
        return icon;
    }

    public void setIconPath(String iconPath) {
        this.icon = iconPath;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<CourseModule> getModules() {
        return modules;
    }

    public void setModules(List<CourseModule> modules) {
        this.modules = modules != null ? modules : new ArrayList<>();
    }
}
