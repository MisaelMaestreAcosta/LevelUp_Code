package com.mycompany.levelup_code.data.dao;

import java.util.Set;

/**
 * Contrato de persistencia para las inscripciones de usuarios en lenguajes
 * ({@code Inscripciones}).
 */
public interface EnrollmentRepository {

    /**
     * Devuelve los identificadores de los lenguajes en los que está inscrito el
     * usuario.
     */
    Set<Integer> findEnrolledLanguageIds(int userId);

    /** Indica si el usuario ya está inscrito en el lenguaje indicado. */
    default boolean isEnrolled(int userId, int languageId) {
        return findEnrolledLanguageIds(userId).contains(languageId);
    }

    /** Registra de forma idempotente la inscripción del usuario en un lenguaje. */
    void enroll(int userId, int languageId);
}
