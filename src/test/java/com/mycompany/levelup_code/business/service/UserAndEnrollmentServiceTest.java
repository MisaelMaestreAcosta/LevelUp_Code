package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeContentRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeEnrollmentRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeUserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAndEnrollmentServiceTest {

    @Test
    void rejectsEmailLongerThanOracleColumnBeforeSaving() {
        UserService service = new UserService(new FakeUserRepository(),
                new Role(1, "Estudiante", "Estudiante"));
        String tooLong = "a".repeat(110) + "@example.com";
        assertTrue(tooLong.length() > UserService.MAX_EMAIL_LENGTH);
        assertThrows(IllegalArgumentException.class,
                () -> service.register("Estudiante", tooLong, "ClaveSegura2026"));
    }

    @Test
    void registersUserWithPbkdf2HashAndAuthenticatesValidCredentials() {
        FakeUserRepository userRepo = new FakeUserRepository();
        Role studentRole = new Role(1, "Estudiante", "Estudiante");
        UserService userService = new UserService(userRepo, studentRole);

        User created = userService.register("Misael Maestre", "misael@unicesar.edu.co", "ClaveSegura2026");

        assertTrue(created.getId() > 0);
        assertEquals("Misael", created.getFirstName());
        assertEquals("Maestre", created.getLastName());
        assertNotEquals("ClaveSegura2026", created.getPasswordHash());
        assertTrue(created.getPasswordHash().startsWith("pbkdf2$"));

        Optional<User> ok = userService.authenticate("MISAEL@unicesar.edu.co", "ClaveSegura2026");
        assertTrue(ok.isPresent());

        Optional<User> badPass = userService.authenticate("misael@unicesar.edu.co", "OtraClave123");
        assertFalse(badPass.isPresent());

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("Otro", "misael@unicesar.edu.co", "ClaveSegura2026"));
    }

    @Test
    void enrollmentServicePersistsActiveLanguagesAndRejectsInactiveOnes() {
        FakeEnrollmentRepository enrollmentRepo = new FakeEnrollmentRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();

        Language javaLang = contentRepo.saveLanguage(new Language(1, "Java", "Java 21", true));
        Language archivedLang = contentRepo.saveLanguage(new Language(2, "Pascal", "Histórico", false));

        EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepo, contentRepo);

        User student = new User();
        student.setId(7);
        student.setUsername("Laura");

        assertTrue(enrollmentService.listEnrolledLanguageIds(student).isEmpty());

        enrollmentService.enroll(student, javaLang.getId());
        assertTrue(enrollmentService.isEnrolled(student, javaLang.getId()));
        assertEquals(Set.of(1), enrollmentService.listEnrolledLanguageIds(student));

        assertThrows(IllegalStateException.class,
                () -> enrollmentService.enroll(student, archivedLang.getId()));
    }
}
