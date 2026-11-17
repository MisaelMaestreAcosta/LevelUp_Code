package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.business.evaluator.MissionEvaluation.Status;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner.CaseOutcome;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner.ExecutionSummary;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MissionEvaluationTest {
    private final ProfessionalMission mission = new ProfessionalMission("descuentos", 1, "Descuentos",
            "Tienda Horizonte", "Corrige el precio final.", List.of(
                    new MissionRequirement("Aplica el descuento", List.of(3, 7)),
                    new MissionRequirement("Valida el rango", List.of(14)),
                    new MissionRequirement("Redondea el centavo", List.of(21, 30)),
                    new MissionRequirement("Revisa el estilo", List.of())));

    @Test
    void distinguishesPassedFailedAndUnavailableCriteriaByCaseId() {
        var outcomes = List.of(
                new CaseOutcome(21, false, "Error de compilación", AttemptResult.ERROR, null),
                new CaseOutcome(7, true, "Oculto", null, null),
                new CaseOutcome(14, false, "Oculto", AttemptResult.INCORRECT, null),
                new CaseOutcome(3, true, "Público", null, "9000"));
        assertEquals(List.of(Status.PASSED, Status.NEEDS_WORK, Status.UNVERIFIED, Status.SELF_REVIEW),
                MissionEvaluation.from(mission, new ExecutionSummary(2, 4, 0.1, outcomes, true)).requirements());
    }

    @Test
    void doesNotMarkSimulatedOrRecoveredAggregateResultsAsVerified() {
        var summary = new ExecutionSummary(1, 1, 0.1,
                List.of(new CaseOutcome(14, true, "Simulado", null, null)), false);
        var unverified = List.of(Status.UNVERIFIED, Status.UNVERIFIED, Status.UNVERIFIED, Status.SELF_REVIEW);
        assertEquals(unverified, MissionEvaluation.from(mission, summary).requirements());
        assertEquals(unverified, MissionEvaluation.from(mission, null).requirements());
    }
}
