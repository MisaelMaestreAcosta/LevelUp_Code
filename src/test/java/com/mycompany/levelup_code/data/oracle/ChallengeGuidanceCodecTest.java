package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.domain.model.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChallengeGuidanceCodecTest {
    @Test void preservesBlankHintsUnicodeAndOptionExplanations() {
        var guidance = new ChallengeGuidance(List.of("Primera pista\nñ", "", "Tercera | pista"), "Por qué funciona", List.of("Bien", "Revisa el límite"));
        assertEquals(guidance, ChallengeGuidanceCodec.decode(ChallengeGuidanceCodec.encode(guidance)));
        assertEquals(ChallengeGuidance.EMPTY, ChallengeGuidanceCodec.decode(null));
    }

    @Test void hintsForNewExercisesDoNotDependOnTheirTitle() {
        var quiz = new QuizChallenge(1, "Otro tema", "Identifica una transacción", null, 10, List.of("A", "B"), "A");
        assertTrue(quiz.getGuidance().hint(1, quiz).contains("transacción"));
        assertFalse(quiz.getGuidance().hint(3, quiz).contains("polimorfismo"));
        quiz.setGuidance(new ChallengeGuidance(List.of("Mira el COMMIT"), "Confirmación", List.of("Correcta", "Eso revierte")));
        assertEquals("Eso revierte", quiz.getGuidance().feedback(1, false));
        assertEquals("Mira el COMMIT", quiz.getGuidance().hint(1, quiz));
    }
}
