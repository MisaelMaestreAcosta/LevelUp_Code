package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.data.dao.ContentRepository;
import com.mycompany.levelup_code.data.dao.EnrollmentRepository;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.User;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Caso de uso de negocio para consultar y registrar inscripciones de estudiantes
 * en rutas de lenguajes de programación ({@code Inscripciones}).
 */
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ContentRepository contentRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            ContentRepository contentRepository) {
        if (enrollmentRepository == null) {
            throw new IllegalArgumentException("El repositorio de inscripciones es obligatorio.");
        }
        if (contentRepository == null) {
            throw new IllegalArgumentException("El repositorio de contenido es obligatorio.");
        }
        this.enrollmentRepository = enrollmentRepository;
        this.contentRepository = contentRepository;
    }

    /** Devuelve los identificadores de lenguajes en los que el usuario está inscrito. */
    public Set<Integer> listEnrolledLanguageIds(User user) {
        validateUser(user);
        Set<Integer> ids = enrollmentRepository.findEnrolledLanguageIds(user.getId());
        if (ids == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(ids));
    }

    /** Indica si el usuario ya está inscrito en el lenguaje indicado. */
    public boolean isEnrolled(User user, int languageId) {
        validateUser(user);
        if (languageId <= 0) {
            throw new IllegalArgumentException("El identificador del lenguaje debe ser positivo.");
        }
        return enrollmentRepository.isEnrolled(user.getId(), languageId);
    }

    /** Inscribe al usuario de forma idempotente en un lenguaje activo existente. */
    public void enroll(User user, int languageId) {
        validateUser(user);
        if (languageId <= 0) {
            throw new IllegalArgumentException("El identificador del lenguaje debe ser positivo.");
        }

        Optional<Language> languageResult = contentRepository.findLanguageById(languageId);
        if (languageResult == null || !languageResult.isPresent()) {
            throw new IllegalArgumentException("El lenguaje solicitado no existe.");
        }
        if (!languageResult.get().isActive()) {
            throw new IllegalStateException("No es posible inscribirse en un lenguaje inactivo.");
        }

        enrollmentRepository.enroll(user.getId(), languageId);
    }

    private void validateUser(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("El usuario debe ser válido y estar persistido.");
        }
    }
}

