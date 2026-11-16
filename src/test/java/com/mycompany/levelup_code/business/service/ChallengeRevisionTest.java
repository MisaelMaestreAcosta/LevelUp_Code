package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.business.evaluator.*;
import com.mycompany.levelup_code.domain.enums.*;
import com.mycompany.levelup_code.domain.model.*;
import com.mycompany.levelup_code.support.InMemoryTestRepositories.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChallengeRevisionTest {
    @Test void rejectsStaleContentBeforeEvaluationAndReplaysConfirmedRequestAtZeroLives() {
        var users = new FakeUserRepository();
        var attempts = new FakeAttemptRepository();
        var challenges = new FakeChallengeRepository();
        var achievements = new AchievementService(new FakeAchievementRepository(users, attempts));
        var gamification = new GamificationService(users, new FakeActivityRepository(), new StandardLevelPolicy(), achievements);
        int[] evaluations = {0};
        ChallengeEvaluator evaluator = (challenge, answer) -> {
            evaluations[0]++;
            return new ChallengeEvaluation(AttemptResult.CORRECT, 0);
        };
        var service = new ChallengeService(challenges, attempts, Map.of(ChallengeType.QUIZ, evaluator), gamification, achievements);
        QuizChallenge quiz = new QuizChallenge(100, "Quiz", "Pregunta", null, 20, List.of("A", "B"), "A");
        quiz.setRevision(3);
        challenges.register(quiz);
        User user = new User();
        users.save(user);
        assertThrows(IllegalStateException.class, () -> service.submitAttemptDetailed(user, 100, "A", "first", 2));
        assertEquals(0, evaluations[0]);
        var first = service.submitAttemptDetailed(user, 100, "A", "first", 3);
        user.setLives(0);
        var replay = service.submitAttemptDetailed(user, 100, "A", "first", 2);
        assertEquals(first.attempt().getId(), replay.attempt().getId());
        assertEquals(1, evaluations[0]);
        assertEquals(20, user.getExperience());
        assertThrows(IllegalArgumentException.class, () -> service.submitAttemptDetailed(user, 100, "B", "first", 3));
    }
}
