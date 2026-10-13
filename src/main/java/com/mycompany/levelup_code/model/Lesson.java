package com.mycompany.levelup_code.model;

/**
 * Entidad que representa una lección dentro de un módulo de aprendizaje.
 * Según RF-04, contiene teoría explicativa, un orden secuencial dentro del módulo
 * y una recompensa de puntos de experiencia al ser completada.
 */
public class Lesson {

    // Identificador único de la lección
    private int id;

    // Título de la lección
    private String title;

    // Contenido teórico o explicación conceptual de la lección
    private String theory;

    // Orden secuencial de la lección dentro del módulo
    private int orderIndex;

    // Recompensa de puntos de experiencia (XP) otorgada al completar la lección
    private int expReward;

    // Módulo al que pertenece la lección
    private CourseModule module;

    /**
     * Constructor por defecto
     */
    public Lesson() {
        this.expReward = 0;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param title Título de la lección
     * @param theory Contenido teórico
     * @param orderIndex Posición u orden dentro del módulo
     * @param expReward Puntos de experiencia otorgados
     * @param module Módulo contenedor
     */
    public Lesson(int id, String title, String theory, int orderIndex, int expReward, CourseModule module) {
        this.id = id;
        this.title = title;
        this.theory = theory;
        this.orderIndex = orderIndex;
        this.expReward = expReward;
        this.module = module;
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

    public String getTheory() {
        return theory;
    }

    public void setTheory(String theory) {
        this.theory = theory;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public int getExpReward() {
        return expReward;
    }

    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }
}
