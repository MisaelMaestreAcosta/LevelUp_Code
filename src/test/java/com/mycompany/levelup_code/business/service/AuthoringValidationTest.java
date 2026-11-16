package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.enums.PublicationState;
import com.mycompany.levelup_code.domain.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthoringValidationTest {
    private CodeChallenge code() {
        Lesson lesson = new Lesson();
        lesson.setId(1);
        return new CodeChallenge(0, "Reto", "Resuelve el problema", lesson, 50, "// Completa aquí", 2);
    }

    @Test void allowsDraftWithoutSolutionButRequiresReferenceAndCasesForPublication() {
        CodeChallenge challenge = code();
        assertDoesNotThrow(() -> AuthoringService.validate(new AuthoredChallenge(challenge, "", PublicationState.BORRADOR, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> AuthoringService.validate(new AuthoredChallenge(challenge, "", PublicationState.PUBLICADO, 0)));
        challenge.addTestCase(new TestCase(0, challenge, "", "", false));
        assertDoesNotThrow(() -> AuthoringService.validate(new AuthoredChallenge(challenge,
                "class Solution {}", PublicationState.PUBLICADO, 0)));
        assertEquals("// Completa aquí", challenge.getInitialTemplate());
    }
}
