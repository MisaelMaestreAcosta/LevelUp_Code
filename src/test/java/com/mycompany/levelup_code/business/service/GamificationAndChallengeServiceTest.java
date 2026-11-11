package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.business.evaluator.ChallengeEvaluator;
import com.mycompany.levelup_code.business.evaluator.ChallengeEvaluation;
import com.mycompany.levelup_code.business.evaluator.CodeChallengeEvaluator;
import com.mycompany.levelup_code.business.evaluator.QuizChallengeEvaluator;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.Attempt;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.domain.model.UserAchievement;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeAchievementRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeActivityRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeAttemptRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeChallengeRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeContentRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeProgressRepository;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.FakeUserRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamificationAndChallengeServiceTest {

    @Test
    void evaluatorErrorsAreRecordedWithoutTakingAStudentLife() {
        FakeUserRepository users = new FakeUserRepository();
        FakeAttemptRepository attempts = new FakeAttemptRepository(false);
        FakeChallengeRepository challenges = new FakeChallengeRepository();
        AchievementService achievements = new AchievementService(new FakeAchievementRepository(users, attempts));
        GamificationService gamification = new GamificationService(users,
                new FakeActivityRepository(), new StandardLevelPolicy(), achievements);
        Map<ChallengeType, ChallengeEvaluator> evaluators = new EnumMap<>(ChallengeType.class);
        evaluators.put(ChallengeType.QUIZ,
                (challenge, submission) -> new ChallengeEvaluation(AttemptResult.ERROR, 0));
        ChallengeService service = new ChallengeService(challenges, attempts,
                evaluators, gamification, achievements);

        QuizChallenge quiz = new QuizChallenge(91, "Quiz", "Pregunta", null, 20,
                List.of("A", "B"), "A");
        challenges.register(quiz);
        User student = new User();
        student.setId(91);
        student.setLives(3);
        users.save(student);

        Attempt saved = service.submitAttempt(student, 91, "A");

        assertEquals(AttemptResult.ERROR, saved.getResult());
        assertEquals(3, student.getLives(), "Un error del evaluador no debe castigar al estudiante");
    }

    @Test
    void detailedSubmissionReturnsTheSingleEvaluationUsedForPersistence() {
        FakeUserRepository users = new FakeUserRepository();
        FakeAttemptRepository attempts = new FakeAttemptRepository(false);
        FakeChallengeRepository challenges = new FakeChallengeRepository();
        AchievementService achievements = new AchievementService(new FakeAchievementRepository(users, attempts));
        GamificationService gamification = new GamificationService(users,
                new FakeActivityRepository(), new StandardLevelPolicy(), achievements);
        int[] evaluations = {0};
        SimulatedCodeRunner runner = new SimulatedCodeRunner() {
            @Override public ExecutionSummary runAndValidate(CodeChallenge challenge, String source) {
                evaluations[0]++;
                return new ExecutionSummary(1, 1, 0.01,
                        List.of(new CaseOutcome(true, "Caso #1 aprobado")), true);
            }
        };
        Map<ChallengeType, ChallengeEvaluator> evaluators = new EnumMap<>(ChallengeType.class);
        evaluators.put(ChallengeType.CODE, new CodeChallengeEvaluator(runner));
        ChallengeService service = new ChallengeService(challenges, attempts,
                evaluators, gamification, achievements);

        User student = new User();
        student.setId(5);
        student.setLives(5);
        users.save(student);
        Language language = new Language(1, "Java", "Java", true);
        CourseModule module = new CourseModule(1, "Módulo", "Desc", 1, language);
        Lesson lesson = new Lesson(1, "Lección", "Desc", 1, 0, module);
        CodeChallenge challenge = new CodeChallenge(7, "Reto", "Desc", lesson, 50, "", 2);
        challenge.addTestCase(new TestCase(1, challenge, "entrada", "salida", false));
        challenges.register(challenge);

        ChallengeService.SubmissionResult saved = service.submitAttemptDetailed(student, 7, "código");
        assertEquals(1, evaluations[0]);
        assertTrue(saved.attempt().isCorrect());
        assertTrue(saved.evaluation().getExecutionSummary().isAllPassed());
        assertEquals(50, saved.attempt().getEarnedExperience());
    }

    @Test
    void highExperienceFromLessonsNeverUnlocksChallengeCountAchievementWithoutSolvedChallenges() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeAttemptRepository attemptRepo = new FakeAttemptRepository(false);
        FakeAchievementRepository achRepo = new FakeAchievementRepository(userRepo, attemptRepo);
        FakeActivityRepository activityRepo = new FakeActivityRepository();
        FakeContentRepository contentRepo = new FakeContentRepository();
        FakeProgressRepository progressRepo = new FakeProgressRepository(false);

        achRepo.registerAchievement(new ExperienceAchievement(1, "1500 XP", "Meta XP", "BOLT", 1500));
        achRepo.registerAchievement(new ChallengeCountAchievement(2, "2 Retos", "Meta Retos", "CODE", 2));

        AchievementService achievementService = new AchievementService(achRepo);
        GamificationService gamificationService = new GamificationService(
                userRepo, activityRepo, new StandardLevelPolicy(), achievementService);
        ProgressService progressService = new ProgressService(progressRepo, contentRepo, gamificationService);

        User student = new User();
        student.setId(10);
        student.setUsername("Ana");
        student.setEmail("ana@levelup.edu.co");
        student.setRole(new Role(1, "Estudiante", "Estudiante"));
        userRepo.save(student);

        Language javaLang = contentRepo.saveLanguage(new Language(1, "Java", "Java", true));
        CourseModule mod = contentRepo.saveModule(new CourseModule(1, "M1", "Desc", 1, javaLang));
        Lesson bigLesson = contentRepo.saveLesson(new Lesson(1, "Lección Magistral", "Teoría", 1, 1800, mod));

        progressService.startLesson(student, bigLesson.getId());
        progressService.completeLesson(student, bigLesson.getId());

        assertEquals(1800, student.getTotalExp());
        List<UserAchievement> unlocked = achievementService.listUnlockedBy(student);
        Set<Integer> unlockedIds = unlocked.stream()
                .map(ua -> ua.getAchievement().getId())
                .collect(Collectors.toSet());

        assertTrue(unlockedIds.contains(1), "Debe desbloquear el logro de experiencia (1800 >= 1500)");
        assertFalse(unlockedIds.contains(2), "NO debe desbloquear el logro de retos sin intentos aprobados");
    }

    @Test
    void challengeSubmissionAwardsXpOnceDeductsLivesOnFailureAndBlocksAtZeroLives() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeAttemptRepository attemptRepo = new FakeAttemptRepository(false);
        FakeChallengeRepository challengeRepo = new FakeChallengeRepository();
        FakeAchievementRepository achRepo = new FakeAchievementRepository(userRepo, attemptRepo);
        FakeActivityRepository activityRepo = new FakeActivityRepository();

        achRepo.registerAchievement(new ChallengeCountAchievement(5, "1 Reto", "Supera 1 reto", "CODE", 1));

        AchievementService achievementService = new AchievementService(achRepo);
        GamificationService gamificationService = new GamificationService(
                userRepo, activityRepo, new StandardLevelPolicy(), achievementService);

        Map<ChallengeType, ChallengeEvaluator> evaluators = new EnumMap<>(ChallengeType.class);
        evaluators.put(ChallengeType.CODE, new CodeChallengeEvaluator(new SimulatedCodeRunner()));
        evaluators.put(ChallengeType.QUIZ, new QuizChallengeEvaluator());

        ChallengeService challengeService = new ChallengeService(
                challengeRepo, attemptRepo, evaluators, gamificationService, achievementService);

        QuizChallenge quiz = new QuizChallenge(
                50, "Quiz POO", "¿Qué es vtable?", null, 100,
                Arrays.asList("Opción A", "Tabla virtual"), "Tabla virtual"
        );
        challengeRepo.register(quiz);

        User student = new User();
        student.setId(1);
        student.setUsername("Carlos");
        student.setEmail("carlos@levelup.edu.co");
        student.setLives(2);
        userRepo.save(student);

        // 1. Intento fallido -> descuenta 1 vida, 0 XP
        Attempt failed = challengeService.submitAttempt(student, 50, "Opción A");
        assertFalse(failed.isCorrect());
        assertEquals(0, failed.getEarnedExperience());
        assertEquals(1, student.getLives());
        assertEquals(0, student.getTotalExp());

        // 2. Primer intento correcto -> +100 XP y desbloquea logro de 1 reto distinto
        Attempt passed1 = challengeService.submitAttempt(student, 50, "Tabla virtual");
        assertTrue(passed1.isCorrect());
        assertEquals(100, passed1.getEarnedExperience());
        assertEquals(100, student.getTotalExp());
        assertEquals(1, achievementService.listUnlockedBy(student).size());

        // 3. Segundo intento correcto al mismo reto -> 0 XP adicional
        Attempt passed2 = challengeService.submitAttempt(student, 50, "Tabla virtual");
        assertTrue(passed2.isCorrect());
        assertEquals(0, passed2.getEarnedExperience());
        assertEquals(100, student.getTotalExp());

        // 4. Pierde la última vida restante -> bloquea envíos posteriores con 0 vidas
        challengeService.submitAttempt(student, 50, "Opción A");
        assertEquals(0, student.getLives());
        assertThrows(IllegalStateException.class, () -> challengeService.submitAttempt(student, 50, "Tabla virtual"));
    }

    @Test
    void transactionalRepositoryDoesNotTriggerDuplicateJavaGamification() {
        FakeUserRepository userRepo = new FakeUserRepository();
        FakeAttemptRepository transactionalAttemptRepo = new FakeAttemptRepository(true);
        FakeChallengeRepository challengeRepo = new FakeChallengeRepository();
        FakeAchievementRepository achRepo = new FakeAchievementRepository(userRepo, transactionalAttemptRepo);
        FakeActivityRepository activityRepo = new FakeActivityRepository();

        AchievementService achievementService = new AchievementService(achRepo);
        GamificationService gamificationService = new GamificationService(
                userRepo, activityRepo, new StandardLevelPolicy(), achievementService);

        Map<ChallengeType, ChallengeEvaluator> evaluators = new EnumMap<>(ChallengeType.class);
        evaluators.put(ChallengeType.QUIZ, new QuizChallengeEvaluator());

        ChallengeService challengeService = new ChallengeService(
                challengeRepo, transactionalAttemptRepo, evaluators, gamificationService, achievementService);

        QuizChallenge quiz = new QuizChallenge(
                77, "Quiz Transaccional", "Pregunta", null, 80,
                Arrays.asList("Correcta", "Incorrecta"), "Correcta"
        );
        challengeRepo.register(quiz);

        User student = new User();
        student.setId(3);
        student.setUsername("Elena");
        student.setEmail("elena@levelup.edu.co");
        student.setLives(5);
        userRepo.save(student);

        challengeService.submitAttempt(student, 77, "Correcta");
        assertEquals(80, student.getTotalExp(), "La XP debe sumarse exactamente una vez por el motor transaccional");

        challengeService.submitAttempt(student, 77, "Incorrecta");
        assertEquals(4, student.getLives(), "La vida debe descontarse exactamente una vez por el motor transaccional");

        student.setLives(0);
        assertThrows(IllegalStateException.class,
                () -> challengeService.submitAttempt(student, 77, "Correcta"),
                "Oracle tampoco debe recibir intentos sin vidas");
        assertEquals(5, gamificationService.recoverLivesIfEligible(student));
        assertEquals(5, student.getLives());
    }
}

