package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.Challenge;
import com.mycompany.levelup_code.model.CourseModule;
import com.mycompany.levelup_code.model.Language;
import com.mycompany.levelup_code.model.Lesson;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para las operaciones de contenido.
 * Su implementación corresponde a la capa de acceso a datos.
 */
public interface ContentRepository {

    // Comprueba si ya existe un lenguaje con ese nombre.
    boolean existsLanguageByName(String name);

    // Comprueba si otro lenguaje usa ese nombre, excluyendo el indicado.
    boolean existsLanguageByNameExceptId(String name, int excludedId);

    // Busca contenido por identificador.
    Optional<Language> findLanguageById(int id);
    Optional<CourseModule> findModuleById(int id);
    Optional<Lesson> findLessonById(int id);

    // Devuelve los lenguajes disponibles para los estudiantes.
    List<Language> findActiveLanguages();

    // Guarda un elemento nuevo.
    Language saveLanguage(Language language);
    CourseModule saveModule(CourseModule module);
    Lesson saveLesson(Lesson lesson);
    Challenge saveChallenge(Challenge challenge);

    // Actualiza un elemento existente.
    Language updateLanguage(Language language);
    CourseModule updateModule(CourseModule module);
    Lesson updateLesson(Lesson lesson);
    Challenge updateChallenge(Challenge challenge);
}
