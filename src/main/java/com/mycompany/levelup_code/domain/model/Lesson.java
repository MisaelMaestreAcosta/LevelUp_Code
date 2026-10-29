package com.mycompany.levelup_code.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una lección dentro de un módulo de aprendizaje.
 * Según RF-04, contiene teoría explicativa, un orden secuencial dentro del módulo,
 * una recompensa de puntos de experiencia al ser completada y sus retos de evaluación asociados.
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

    // Lista de retos de evaluación asociados a esta lección (RF-06)
    private List<Challenge> challenges;

    /**
     * Constructor por defecto
     */
    public Lesson() {
        this.expReward = 0;
        this.challenges = new ArrayList<>();
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
        this.challenges = new ArrayList<>();
    }

    /**
     * Agrega un reto de evaluación a la lección y establece la relación.
     *
     * @param challenge Reto a agregar
     */
    public void addChallenge(Challenge challenge) {
        if (challenge != null) {
            challenge.setLesson(this);
            this.challenges.add(challenge);
        }
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

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges != null ? challenges : new ArrayList<>();
    }
}
