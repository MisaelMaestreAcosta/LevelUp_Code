package com.mycompany.levelup_code.model;

/**
 * Entidad que representa un módulo de aprendizaje dentro de un lenguaje.
 * Según RF-04, organiza el contenido pedagógico en unidades temáticas ordenadas.
 * Se nombra CourseModule para no confundirse con java.lang.Module.
 */
public class CourseModule {

    // Identificador único del módulo
    private int id;

    // Título o nombre temático del módulo
    private String title;

    // Descripción del contenido y objetivos del módulo
    private String description;

    // Orden secuencial del módulo dentro del curso del lenguaje
    private int orderIndex;

    // Lenguaje de programación al que pertenece el módulo
    private Language language;

    /**
     * Constructor por defecto
     */
    public CourseModule() {
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param title Título del módulo
     * @param description Descripción del módulo
     * @param orderIndex Posición o número de orden secuencial
     * @param language Lenguaje al que pertenece
     */
    public CourseModule(int id, String title, String description, int orderIndex, Language language) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.language = language;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
