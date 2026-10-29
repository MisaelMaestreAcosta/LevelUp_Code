package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.model.Challenge;

/** Evalúa un envío para un tipo de reto sin encargarse de persistencia ni XP. */
public interface ChallengeEvaluator {

    ChallengeEvaluation evaluate(Challenge challenge, String submission);
}
