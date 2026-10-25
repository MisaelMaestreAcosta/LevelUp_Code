package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.Lesson;
import com.mycompany.levelup_code.model.LessonProgress;
import com.mycompany.levelup_code.model.ProgressStatus;
import com.mycompany.levelup_code.model.User;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Coordina el seguimiento del avance de un usuario en sus lecciones.
 */
public class ProgressService {

    // Repositorio para consultar y guardar el progreso.
    private final ProgressRepository progressRepository;

    // Repositorio para obtener las lecciones.
    private final ContentRepository contentRepository;

    // Servicio que aplica recompensas de experiencia.
    private final GamificationService gamificationService;

    /**
     * Recibe las dependencias necesarias para gestionar el progreso.
     */
    public ProgressService(
            ProgressRepository progressRepository,
            ContentRepository contentRepository,
            GamificationService gamificationService) {

        if (progressRepository == null
                || contentRepository == null
                || gamificationService == null) {
            throw new IllegalArgumentException(
                    "Los repositorios y el servicio de gamificación son obligatorios."
            );
        }

        this.progressRepository = progressRepository;
        this.contentRepository = contentRepository;
        this.gamificationService = gamificationService;
    }

    /**
     * Marca una lección como iniciada.
     * Si ya estaba completada, conserva ese estado.
     */
    public LessonProgress startLesson(User user, int lessonId) {
        validateUser(user);

        Lesson lesson = findLesson(lessonId);
        Optional<LessonProgress> existing = progressRepository
                .findByUserAndLesson(user.getId(), lessonId);

        if (existing.isPresent()) {
            LessonProgress progress = existing.get();

            // No retrocede una lección que el usuario ya completó.
            if (progress.getStatus() == ProgressStatus.COMPLETED) {
                return progress;
            }

            progress.setStatus(ProgressStatus.IN_PROGRESS);
            return progressRepository.saveOrUpdate(progress);
        }

        // Crea el primer registro de avance para esta combinación usuario-lección.
        LessonProgress progress = new LessonProgress();
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setStatus(ProgressStatus.IN_PROGRESS);

        return progressRepository.saveOrUpdate(progress);
    }

    /**
     * Completa una lección iniciada y otorga su experiencia una sola vez.
     */
    public LessonProgress completeLesson(User user, int lessonId) {
        validateUser(user);

        Lesson lesson = findLesson(lessonId);
        LessonProgress progress = progressRepository
                .findByUserAndLesson(user.getId(), lessonId)
                .orElseThrow(() -> new IllegalStateException(
                        "La lección debe iniciarse antes de completarse."
                ));

        // Evita volver a otorgar experiencia si se repite la solicitud.
        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            return progress;
        }

        if (progress.getStatus() != ProgressStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "La lección no está en curso."
            );
        }

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setApprovedAt(LocalDateTime.now());

        LessonProgress savedProgress =
                progressRepository.saveOrUpdate(progress);

        // La gamificación aplica la recompensa y actualiza XP, nivel y actividad.
        if (lesson.getExpReward() > 0) {
            gamificationService.awardExperience(
                    user.getId(), lesson.getExpReward()
            );
        }

        return savedProgress;
    }

    /**
     * Busca la lección y genera un error claro si no existe.
     */
    private Lesson findLesson(int lessonId) {
        if (lessonId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la lección debe ser válido."
            );
        }

        return contentRepository.findLessonById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La lección no existe."
                ));
    }

    /**
     * Comprueba que se haya recibido un usuario válido.
     */
    private void validateUser(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El usuario debe ser válido."
            );
        }
    }
}
