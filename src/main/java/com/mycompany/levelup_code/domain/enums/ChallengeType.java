package com.mycompany.levelup_code.domain.enums;

/**
 * Enumeración que define los tipos de retos soportados por la plataforma.
 * Según la especificación del sistema, los tipos iniciales son código y quiz.
 */
public enum ChallengeType {
    /**
     * Reto de programación en código fuente evaluado contra casos de prueba.
     */
    CODE("CODIGO", "Código"),

    /**
     * Reto conceptual de selección múltiple con opciones y respuesta única.
     */
    QUIZ("QUIZ", "Quiz"),
    OUTPUT_PREDICTION("PREDICCION", "Predicción de salida"),
    DEBUGGING("DEPURACION", "Depuración"),
    CODE_ORDERING("ORDENAR", "Ordenar fragmentos");

    private final String databaseValue;
    private final String label;

    ChallengeType(String databaseValue, String label) {
        this.databaseValue = databaseValue;
        this.label = label;
    }

    public String databaseValue() { return databaseValue; }
    public String label() { return label; }
}
