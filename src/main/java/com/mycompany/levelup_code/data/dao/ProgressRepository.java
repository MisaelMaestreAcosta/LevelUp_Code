package com.mycompany.levelup_code.data.dao;

import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import java.util.Map;
import java.util.Optional;

/**
 * Contrato de persistencia para el progreso de las lecciones.
 * La implementación concreta corresponde a la capa de acceso a datos.
 */
public interface ProgressRepository {

    /**
     * Busca el progreso de una lección para un usuario.
     * Devuelve Optional.empty() si aún no existe un registro.
     */
    Optional<LessonProgress> findByUserAndLesson(int userId, int lessonId);

    /** Returns every saved lesson status with one repository call. Missing lessons are pending. */
    Map<Integer, ProgressStatus> findStatusesByUser(int userId);

    /**
     * Guarda un progreso nuevo o actualiza el existente.
     */
    LessonProgress saveOrUpdate(LessonProgress progress);

    /**
     * Indica si el repositorio aplica de forma atómica la recompensa y gamificación
     * al completar una lección (ej. mediante PL/SQL).
     */
    default boolean managesGamificationTransactionally() {
        return false;
    }
}
