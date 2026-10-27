package com.mycompany.levelup_code.model;

/**
 * Enumeración que define los tipos de retos soportados por la plataforma.
 * Según la especificación del sistema, los tipos iniciales son código y quiz.
 */
public enum ChallengeType {
    /**
     * Reto de programación en código fuente evaluado contra casos de prueba.
     */
    CODE,

    /**
     * Reto conceptual de selección múltiple con opciones y respuesta única.
     */
    QUIZ
}
