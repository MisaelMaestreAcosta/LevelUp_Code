package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.enums.AttemptResult;

/** Resultado de evaluación que mantiene el tiempo separado de la entidad Attempt. */
public final class ChallengeEvaluation {

    private final AttemptResult result;
    private final long executionTimeMs;
    private final SimulatedCodeRunner.ExecutionSummary executionSummary;

    public ChallengeEvaluation(AttemptResult result, long executionTimeMs) {
        this(result, executionTimeMs, null);
    }

    public ChallengeEvaluation(AttemptResult result, long executionTimeMs,
            SimulatedCodeRunner.ExecutionSummary executionSummary) {
        if (result == null) {
            throw new IllegalArgumentException("El resultado de evaluación es obligatorio.");
        }
        if (executionTimeMs < 0) {
            throw new IllegalArgumentException("El tiempo de ejecución no puede ser negativo.");
        }
        this.result = result;
        this.executionTimeMs = executionTimeMs;
        this.executionSummary = executionSummary;
    }

    public AttemptResult getResult() {
        return result;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public SimulatedCodeRunner.ExecutionSummary getExecutionSummary() {
        return executionSummary;
    }
}
