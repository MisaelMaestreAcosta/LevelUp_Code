package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.enums.AttemptResult;

/** Resultado de evaluación que mantiene el tiempo separado de la entidad Attempt. */
public final class ChallengeEvaluation {

    private final AttemptResult result;
    private final long executionTimeMs;

    public ChallengeEvaluation(AttemptResult result, long executionTimeMs) {
        if (result == null) {
            throw new IllegalArgumentException("El resultado de evaluación es obligatorio.");
        }
        if (executionTimeMs < 0) {
            throw new IllegalArgumentException("El tiempo de ejecución no puede ser negativo.");
        }
        this.result = result;
        this.executionTimeMs = executionTimeMs;
    }

    public AttemptResult getResult() {
        return result;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
}
