package com.mycompany.levelup_code.model;

/**
 * Clase abstracta que representa la entidad base para un reto de evaluación.
 * Modela la jerarquía y especialización ES-UN entre retos genéricos y sus subtipos
 * (CodeChallenge y QuizChallenge), aplicando herencia y polimorfismo.
 * Cumple con RF-06 y permite la extensibilidad requerida por RNF-05.
 */
public abstract class Challenge {

    // Identificador único del reto
    private int id;

    // Título descriptivo del reto
    private String title;

    // Enunciado o descripción del problema a resolver (statement)
    private String description;

    // Lección a la cual está asociado el reto (RF-06)
    private Lesson lesson;

    // Puntos de experiencia otorgados por resolver exitosamente el reto
    private int expReward;

    /**
     * Constructor por defecto
     */
    public Challenge() {
        this.expReward = 0;
    }

    /**
     * Constructor con parámetros
     *
     * @param id Identificador único
     * @param title Título del reto
     * @param description Enunciado o descripción
     * @param lesson Lección asociada
     * @param expReward Puntos de experiencia otorgados
     */
    public Challenge(int id, String title, String description, Lesson lesson, int expReward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lesson = lesson;
        this.expReward = expReward;
    }

    /**
     * Método polimórfico que retorna el tipo enumerado del reto.
     *
     * @return Tipo de reto (CODE o QUIZ)
     */
    public abstract ChallengeType getType();

    /**
     * Retorna un resumen descriptivo del reto para listados y vistas.
     *
     * @return Resumen en texto legible
     */
    public abstract String getSummary();

    /**
     * Método polimórfico de compatibilidad que retorna el tipo identificador del reto como String.
     *
     * @return Nombre textual del tipo de reto
     */
    public String getChallengeType() {
        ChallengeType type = getType();
        return type != null ? type.name() : null;
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

    public String getStatement() {
        return description;
    }

    public void setStatement(String statement) {
        this.description = statement;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public int getLessonId() {
        return lesson != null ? lesson.getId() : 0;
    }

    public int getExpReward() {
        return expReward;
    }

    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }
}
