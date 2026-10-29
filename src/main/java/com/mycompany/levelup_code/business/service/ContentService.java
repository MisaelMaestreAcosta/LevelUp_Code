package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.data.dao.ContentRepository;

import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.User;
import java.util.List;

/**
 * Contiene reglas de negocio para administrar el contenido educativo.
 * La persistencia se delega a ContentRepository.
 */
public class ContentService {

    // Abstracción usada para consultar y guardar contenido.
    // La implementación concreta la proporcionará la capa DAO.
    private final ContentRepository contentRepository;

    /**
     * Recibe el repositorio requerido por este servicio.
     * Falla de inmediato si no se proporciona.
     */
    public ContentService(ContentRepository contentRepository) {
        if (contentRepository == null) {
            throw new IllegalArgumentException(
                    "El repositorio de contenido es obligatorio."
            );
        }
        this.contentRepository = contentRepository;
    }

    /**
     * Crea un lenguaje si los datos son válidos y su nombre no está duplicado.
     */
    public Language createLanguage(User administrator, Language language) {
        requireAdministrator(administrator);
        validateLanguage(language);

        if (contentRepository.existsLanguageByName(language.getName().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe un lenguaje con ese nombre."
            );
        }

        language.setName(language.getName().trim());
        return contentRepository.saveLanguage(language);
    }

    /**
     * Actualiza un lenguaje existente.
     * También impide que se repita el nombre de otro lenguaje.
     */
    public Language updateLanguage(User administrator, Language language) {
        requireAdministrator(administrator);
        validateLanguage(language);

        if (language.getId() <= 0
                || !contentRepository.findLanguageById(language.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "El lenguaje que intenta actualizar no existe."
            );
        }

        if (contentRepository.existsLanguageByNameExceptId(
                language.getName().trim(), language.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe otro lenguaje con ese nombre."
            );
        }

        language.setName(language.getName().trim());
        return contentRepository.updateLanguage(language);
    }

    /**
     * Desactiva un lenguaje sin borrarlo, para conservar su contenido e historial.
     */
    public Language deactivateLanguage(User administrator, int languageId) {
        requireAdministrator(administrator);

        Language language = contentRepository.findLanguageById(languageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El lenguaje no existe."
                ));

        language.setActive(false);
        return contentRepository.updateLanguage(language);
    }

    /**
     * Crea un módulo después de verificar sus datos y el lenguaje asociado.
     * No permite agregar módulos a un lenguaje desactivado.
     */
    public CourseModule createModule(
            User administrator, CourseModule module) {
        requireAdministrator(administrator);
        validateModule(module);

        Language language = contentRepository
                .findLanguageById(module.getLanguage().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El lenguaje asociado no existe."
                ));

        if (!language.isActive()) {
            throw new IllegalArgumentException(
                    "No se pueden agregar módulos a un lenguaje inactivo."
            );
        }

        module.setTitle(module.getTitle().trim());
        module.setLanguage(language);
        return contentRepository.saveModule(module);
    }

    /**
     * Crea una lección si su módulo asociado existe y sus datos son válidos.
     */
    public Lesson createLesson(User administrator, Lesson lesson) {
        requireAdministrator(administrator);
        validateLesson(lesson);

        CourseModule module = contentRepository
                .findModuleById(lesson.getModule().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El módulo asociado no existe."
                ));

        lesson.setTitle(lesson.getTitle().trim());
        lesson.setModule(module);
        return contentRepository.saveLesson(lesson);
    }

    /**
     * Crea un reto y valida los campos específicos de su subtipo:
     * código o quiz.
     */
    public Challenge createChallenge(
            User administrator, Challenge challenge) {
        requireAdministrator(administrator);
        validateChallenge(challenge);

        Lesson lesson = contentRepository
                .findLessonById(challenge.getLesson().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La lección asociada no existe."
                ));

        challenge.setTitle(challenge.getTitle().trim());
        challenge.setLesson(lesson);
        return contentRepository.saveChallenge(challenge);
    }

    /**
     * Devuelve los lenguajes activos que pueden mostrarse a los estudiantes.
     */
    public List<Language> listActiveLanguages() {
        return contentRepository.findActiveLanguages();
    }

    /**
     * Comprueba que la persona que intenta administrar el contenido tenga
     * el rol Administrador.
     */
    private void requireAdministrator(User user) {
        if (user == null || user.getRole() == null
                || user.getRole().getName() == null
                || !"Administrador".equalsIgnoreCase(
                        user.getRole().getName().trim())) {
            throw new SecurityException(
                    "Se requiere el rol de administrador."
            );
        }
    }

    /**
     * Comprueba que el lenguaje recibido y su nombre sean válidos.
     */
    private void validateLanguage(Language language) {
        if (language == null || isBlank(language.getName())) {
            throw new IllegalArgumentException(
                    "El nombre del lenguaje es obligatorio."
            );
        }
    }

    /**
     * Valida título, lenguaje asociado y posición del módulo.
     */
    private void validateModule(CourseModule module) {
        if (module == null || isBlank(module.getTitle())
                || module.getLanguage() == null
                || module.getLanguage().getId() <= 0
                || module.getOrderIndex() < 1) {
            throw new IllegalArgumentException(
                    "El módulo debe tener título, lenguaje y orden válidos."
            );
        }
    }

    /**
     * Valida contenido, módulo, posición y recompensa de experiencia
     * de una lección.
     */
    private void validateLesson(Lesson lesson) {
        if (lesson == null || isBlank(lesson.getTitle())
                || isBlank(lesson.getTheory())
                || lesson.getModule() == null
                || lesson.getModule().getId() <= 0
                || lesson.getOrderIndex() < 1
                || lesson.getExpReward() < 0) {
            throw new IllegalArgumentException(
                    "La lección tiene datos obligatorios inválidos."
            );
        }
    }

    /**
     * Valida los datos generales del reto y luego los campos de su subtipo.
     * En los quizzes, la respuesta correcta debe estar entre las opciones.
     */
    private void validateChallenge(Challenge challenge) {
        if (challenge == null || isBlank(challenge.getTitle())
                || isBlank(challenge.getDescription())
                || challenge.getLesson() == null
                || challenge.getLesson().getId() <= 0
                || challenge.getExpReward() < 0) {
            throw new IllegalArgumentException(
                    "El reto tiene datos obligatorios inválidos."
            );
        }

        if (challenge instanceof CodeChallenge) {
            CodeChallenge codeChallenge = (CodeChallenge) challenge;

            if (codeChallenge.getTimeLimitSeconds() <= 0) {
                throw new IllegalArgumentException(
                        "El límite de ejecución debe ser mayor que cero."
                );
            }
        } else if (challenge instanceof QuizChallenge) {
            QuizChallenge quizChallenge = (QuizChallenge) challenge;

            if (quizChallenge.getOptions() == null
                    || quizChallenge.getOptions().isEmpty()
                    || isBlank(quizChallenge.getCorrectAnswer())
                    || !quizChallenge.getOptions().contains(
                            quizChallenge.getCorrectAnswer())) {
                throw new IllegalArgumentException(
                        "El quiz debe tener opciones y una respuesta correcta válida."
                );
            }
        }
    }

    /**
     * Utilidad para detectar textos nulos, vacíos o compuestos solo por espacios.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}