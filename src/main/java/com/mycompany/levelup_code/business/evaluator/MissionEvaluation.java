package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner.CaseOutcome;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner.ExecutionSummary;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Summarizes the client's requirements without carrying individual case inputs or outputs. */
public record MissionEvaluation(List<Status> requirements) {
    public enum Status { PASSED, NEEDS_WORK, UNVERIFIED, SELF_REVIEW }

    public MissionEvaluation {
        requirements = List.copyOf(requirements);
    }

    public static MissionEvaluation from(ProfessionalMission mission, ExecutionSummary summary) {
        Map<Integer, CaseOutcome> cases = new HashMap<>();
        if (summary != null && summary.isSandboxedExecution()) {
            for (CaseOutcome outcome : summary.getCaseOutcomes()) {
                if (outcome.getTestCaseId() > 0) cases.put(outcome.getTestCaseId(), outcome);
            }
        }
        return new MissionEvaluation(mission.requirements().stream()
                .map(requirement -> evaluate(requirement, cases)).toList());
    }

    private static Status evaluate(MissionRequirement requirement, Map<Integer, CaseOutcome> cases) {
        if (!requirement.automaticallyVerified()) return Status.SELF_REVIEW;
        boolean allPassed = true;
        for (int id : requirement.testCaseIds()) {
            CaseOutcome result = cases.get(id);
            if (result != null && result.getFailureResult() == AttemptResult.INCORRECT) return Status.NEEDS_WORK;
            allPassed &= result != null && result.isPassed();
        }
        return allPassed ? Status.PASSED : Status.UNVERIFIED;
    }
}
