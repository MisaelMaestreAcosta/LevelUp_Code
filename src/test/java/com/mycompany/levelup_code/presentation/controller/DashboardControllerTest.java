package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.business.service.AchievementService;
import com.mycompany.levelup_code.business.service.ContentService;
import com.mycompany.levelup_code.business.service.EnrollmentService;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeAchievementRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeActivityRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeAttemptRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeContentRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeEnrollmentRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeProgressRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeUserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardControllerTest {

    @Test
    void oracleModeShowsPendingProgressEmptyHeatmapAndRealEnrollmentsForNewUser() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();
        FakeEnrollmentRepository enrollmentRepo = new FakeEnrollmentRepository();
        FakeProgressRepository progressRepo = new FakeProgressRepository(true);
        FakeActivityRepository activityRepo = new FakeActivityRepository();
        FakeAttemptRepository attemptRepo = new FakeAttemptRepository(true);
        FakeAchievementRepository achRepo = new FakeAchievementRepository(userRepo, attemptRepo);

        Language javaLang = contentRepo.saveLanguage(new Language(1, "Java", "Java 21", true));
        Language pyLang = contentRepo.saveLanguage(new Language(2, "Python", "Python 3.12", true));
        Language cppLang = contentRepo.saveLanguage(new Language(3, "C++", "C++ 20", true));

        CourseModule mod = contentRepo.saveModule(new CourseModule(1, "POO", "Desc", 1, javaLang));
        Lesson lesson1 = contentRepo.saveLesson(new Lesson(1, "Lección 1", "Contenido", 1, 60, mod));
        Lesson lesson2 = contentRepo.saveLesson(new Lesson(2, "Lección 2", "Contenido", 2, 75, mod));

        Achievement challengeAch = new ChallengeCountAchievement(5, "5 Retos", "Supera 5 retos", "CODE", 5);
        achRepo.registerAchievement(challengeAch);

        User newStudent = new User();
        newStudent.setId(42);
        newStudent.setUsername("Nuevo Estudiante");
        newStudent.setEmail("nuevo@levelup.edu.co");
        newStudent.setRole(new Role(1, "Estudiante", "Estudiante"));
        newStudent.setExperience(2500); // Incluso con mucha XP, tiene 0 retos resueltos
        userRepo.save(newStudent);

        DashboardController controller = new DashboardController(
                true,
                newStudent,
                new ContentService(contentRepo, userRepo),
                new EnrollmentService(enrollmentRepo, contentRepo),
                new AchievementService(achRepo),
                progressRepo,
                activityRepo,
                achRepo,
                userRepo
        );

        // 1. Sin registros en Progreso_Leccion -> ambas lecciones son PENDIENTE (no COMPLETADA ni EN_CURSO)
        assertEquals(ProgressStatus.PENDING, controller.resolveLessonProgressStatus(lesson1, 0));
        assertEquals(ProgressStatus.PENDING, controller.resolveLessonProgressStatus(lesson2, 1));

        // 2. Sin registros en Actividad_Diaria -> mapa vacío (sin datos de demostración)
        assertTrue(controller.loadRecentActivityHistory().isEmpty());

        // 3. Sin filas en Inscripciones -> conjunto vacío; al inscribirse persiste en Inscripciones
        assertTrue(controller.loadEnrolledLanguageKeys(javaLang, pyLang, cppLang).isEmpty());
        controller.enrollInLanguage(cppLang);
        assertEquals(Set.of("CPP"), controller.loadEnrolledLanguageKeys(javaLang, pyLang, cppLang));

        // 4. Con 2500 XP pero 0 retos aprobados -> NO desbloquea el logro de 5 retos
        Set<Integer> unlocked = controller.resolveUnlockedAchievementIds(List.of(challengeAch), 0);
        assertFalse(unlocked.contains(5));
    }
}

