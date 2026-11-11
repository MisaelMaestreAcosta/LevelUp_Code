package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeContentRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeUserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentAndAdminSecurityTest {

    @Test
    void rejectsFabricatedAdminRoleWhenPersistedUserIsStudent() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();
        ContentService contentService = new ContentService(contentRepo, userRepo);

        User persistedStudent = new User();
        persistedStudent.setId(1);
        persistedStudent.setUsername("Estudiante Real");
        persistedStudent.setEmail("estudiante@levelup.edu.co");
        persistedStudent.setRole(new Role(1, "Estudiante", "Estudiante"));
        userRepo.save(persistedStudent);

        // Intento de escalación local fabricando un objeto User con rol Administrador y el mismo ID
        User forgedLocalAdmin = new User();
        forgedLocalAdmin.setId(1);
        forgedLocalAdmin.setUsername("Adminalso");
        forgedLocalAdmin.setEmail("estudiante@levelup.edu.co");
        forgedLocalAdmin.setRole(new Role(2, "Administrador", "Forjado"));

        Language rust = new Language(0, "Rust", "Sistemas seguros", true);
        assertThrows(SecurityException.class, () -> contentService.createLanguage(forgedLocalAdmin, rust));
    }

    @Test
    void rejectsUnpersistedFabricatedAdminUser() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();
        ContentService contentService = new ContentService(contentRepo, userRepo);

        User nonExistentAdmin = new User();
        nonExistentAdmin.setId(999);
        nonExistentAdmin.setUsername("Fantasma");
        nonExistentAdmin.setRole(new Role(2, "Administrador", "No existe en BD"));

        Language go = new Language(0, "Go", "Concurrencia CSP", true);
        assertThrows(SecurityException.class, () -> contentService.createLanguage(nonExistentAdmin, go));
    }

    @Test
    void allowsVerifiedPersistedAdministratorToCreateHierarchyAndChallenge() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();
        ContentService contentService = new ContentService(contentRepo, userRepo);

        User realAdmin = new User();
        realAdmin.setId(2);
        realAdmin.setUsername("Admin Verificado");
        realAdmin.setEmail("admin@levelup.edu.co");
        realAdmin.setRole(new Role(2, "Administrador", "Administrador"));
        userRepo.save(realAdmin);

        Language savedLang = contentService.createLanguage(
                realAdmin, new Language(0, "Java", "Java 21 LTS", true));
        assertTrue(savedLang.getId() > 0);

        CourseModule savedMod = contentService.createModule(
                realAdmin, new CourseModule(0, "POO", "Herencia", 1, savedLang));
        assertTrue(savedMod.getId() > 0);

        Lesson savedLesson = contentService.createLesson(
                realAdmin, new Lesson(0, "Polimorfismo", "Teoría", 1, 60, savedMod));
        assertTrue(savedLesson.getId() > 0);

        CodeChallenge ch = new CodeChallenge(
                0, "Invertir", "Invierte cadena", savedLesson, 80,
                "public class Solver { public static String solve(String s) { return s; } }", 2);
        ch.addTestCase(new TestCase(1, ch, "ab", "ba", false));

        var savedChallenge = contentService.createChallenge(realAdmin, ch);
        assertNotNull(savedChallenge);
        assertTrue(savedChallenge.getId() > 0);
        assertEquals("Invertir", savedChallenge.getTitle());
    }

    @Test
    void updatesChallengeTemplateAndCasesOnlyForVerifiedAdministrator() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();
        ContentService service = new ContentService(contentRepo, userRepo);
        User admin = new User();
        admin.setId(7);
        admin.setUsername("Admin");
        admin.setRole(new Role(2, "Administrador", "Administrador"));
        userRepo.save(admin);

        Language language = service.createLanguage(admin,
                new Language(0, "Java", "Java", true));
        CourseModule module = service.createModule(admin,
                new CourseModule(0, "Bases", "Bases", 1, language));
        Lesson lesson = service.createLesson(admin,
                new Lesson(0, "Métodos", "Teoría", 1, 50, module));
        CodeChallenge original = new CodeChallenge(0, "Original", "Resuelve", lesson,
                70, "class Solver {}", 2);
        original.addTestCase(new TestCase(1, original, "a", "A", false));
        service.createChallenge(admin, original);

        CodeChallenge edited = new CodeChallenge(original.getId(), "Editado", "Resuelve mejor",
                lesson, 90, "class Solver { static String solve(String s) { return s; } }", 3);
        edited.addTestCase(new TestCase(0, edited, "b", "B", false));
        CodeChallenge saved = (CodeChallenge) service.updateChallenge(admin, edited);

        assertEquals("Editado", saved.getTitle());
        assertEquals(3, saved.getTimeLimitSeconds());
        assertEquals("b", saved.getTestCases().getFirst().getInput());
        assertEquals(saved, contentRepo.findChallengeById(saved.getId()).orElseThrow());
    }
}
