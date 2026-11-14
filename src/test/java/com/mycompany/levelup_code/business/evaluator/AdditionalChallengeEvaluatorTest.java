package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalChallengeEvaluatorTest {
    @Test void orderingRejectsMissingAndRepeatedFragmentsAndRecognizesCorrectOrder() {
        var challenge = new CodeOrderingChallenge(1, "Ordena", "Pregunta", null, 20,
                List.of("print(value)", "value = 1", "value += 1"), List.of(2, 3, 1));
        var evaluator = new CodeOrderingEvaluator();
        assertEquals(AttemptResult.CORRECT, evaluator.evaluate(challenge, "2, 3, 1").getResult());
        assertEquals(AttemptResult.INCORRECT, evaluator.evaluate(challenge, "2,3").getResult());
        assertEquals(AttemptResult.INCORRECT, evaluator.evaluate(challenge, "2,3,3").getResult());
        assertThrows(IllegalArgumentException.class, () -> new CodeOrderingChallenge(1, "Ordena", "Pregunta", null, 20,
                List.of("A", "B"), List.of(1, 1)));
    }

    @Test void predictionAnswersPreserveOutputCase() {
        var challenge = new OutputPredictionChallenge(1, "Salida", "Pregunta", null, 20,
                List.of("Hola", "HOLA"), "Hola", "print('Hola')");
        var evaluator = new QuizChallengeEvaluator();
        assertEquals(AttemptResult.CORRECT, evaluator.evaluate(challenge, "Hola").getResult());
        assertEquals(AttemptResult.INCORRECT, evaluator.evaluate(challenge, "HOLA").getResult());
    }
}
