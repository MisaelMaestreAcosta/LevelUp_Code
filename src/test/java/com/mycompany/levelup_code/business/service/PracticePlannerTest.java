package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.enums.ChallengeProgress;
import com.mycompany.levelup_code.domain.model.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PracticePlannerTest {
    private Challenge challenge(int id) { return new QuizChallenge(id, "Reto " + id, "Pregunta", null, 10, List.of("A", "B"), "A"); }

    @Test void mixesErrorsNewExercisesAndDueReviewWithoutDuplicates() {
        LocalDate today = LocalDate.of(2026, 11, 15);
        List<Challenge> catalog = List.of(challenge(1), challenge(2), challenge(3), challenge(4), challenge(5), challenge(6), challenge(2));
        var session = new PracticePlanner().plan(catalog,
                Map.of(1, ChallengeProgress.ATTEMPTED, 3, ChallengeProgress.SOLVED, 4, ChallengeProgress.SOLVED),
                Map.of(3, today.minusDays(4), 4, today), today, challenge -> challenge.getId() != 6);
        assertEquals(5, session.steps().size());
        assertEquals(List.of(1, 2, 3), session.steps().subList(0, 3).stream().map(step -> step.challenge().getId()).toList());
        assertEquals(5, session.steps().stream().map(step -> step.challenge().getId()).distinct().count());
        assertTrue(new PracticePlanner().plan(List.of(), Map.of(), Map.of(), today, challenge -> true).finished());
    }

    @Test void retriedResultsDoNotDuplicateXpOrClaimAnUnsavedSuccessWasSaved() {
        var session = new PracticeSession(List.of(new PracticeSession.Step(challenge(1), "Pendiente")));
        session.record(new PracticeSession.Outcome(1, true, false, 0));
        session.record(new PracticeSession.Outcome(1, false, true, 0));
        assertTrue(session.outcome(1).correct());
        assertFalse(session.outcome(1).correctSaved());
        session.record(new PracticeSession.Outcome(1, true, true, 10));
        session.record(new PracticeSession.Outcome(1, true, true, 10));
        assertEquals(10, session.earnedXp());
        assertTrue(session.outcome(1).correctSaved());
        session.advance();
        assertTrue(session.finished());
    }
}
