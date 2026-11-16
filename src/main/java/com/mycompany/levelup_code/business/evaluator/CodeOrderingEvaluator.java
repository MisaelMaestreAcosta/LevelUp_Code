package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeOrderingChallenge;

public final class CodeOrderingEvaluator implements ChallengeEvaluator {
    @Override public ChallengeEvaluation evaluate(Challenge challenge, String submission) {
        if (!(challenge instanceof CodeOrderingChallenge ordering)) {
            throw new IllegalArgumentException("El evaluador requiere un reto de ordenamiento.");
        }
        boolean correct;
        try { correct = ordering.getCorrectOrder().equals(CodeOrderingChallenge.parseOrder(submission)); }
        catch (IllegalArgumentException ex) { correct = false; }
        return new ChallengeEvaluation(correct ? AttemptResult.CORRECT : AttemptResult.INCORRECT, 0);
    }
}
