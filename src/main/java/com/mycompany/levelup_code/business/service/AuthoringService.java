package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.data.oracle.OracleAuthoringRepository;
import com.mycompany.levelup_code.domain.enums.PublicationState;
import com.mycompany.levelup_code.domain.model.*;
import java.sql.SQLException;

public final class AuthoringService {
    public static void validate(AuthoredChallenge draft) {
        Challenge challenge = draft.challenge();
        if (challenge == null || draft.state() == null || challenge.getTitle() == null
                || challenge.getTitle().isBlank() || challenge.getTitle().length() > 100
                || challenge.getDescription() == null || challenge.getDescription().isBlank()
                || challenge.getLessonId() <= 0 || challenge.getExpReward() < 0) {
            throw new IllegalArgumentException("Completa título, enunciado, lección y recompensa válida.");
        }
        if (challenge instanceof CodeChallenge code) {
            if (code.getTimeLimitSeconds() <= 0) throw new IllegalArgumentException("Usa un límite de tiempo positivo.");
            if (draft.state() == PublicationState.PUBLICADO
                    && (code.getTestCases().isEmpty() || draft.referenceSolution() == null || draft.referenceSolution().isBlank())) {
                throw new IllegalArgumentException("Para publicar añade casos y una solución de referencia.");
            }
        }
        if (challenge instanceof QuizChallenge quiz && (quiz.getOptions().size() < 2
                || quiz.getOptions().stream().anyMatch(option -> option == null || option.isBlank())
                || quiz.getOptions().stream().map(option -> quiz instanceof OutputPredictionChallenge
                        ? option.strip() : option.strip().toLowerCase(java.util.Locale.ROOT)).distinct().count() != quiz.getOptions().size()
                || !quiz.getOptions().contains(quiz.getCorrectAnswer()) || quiz.getCorrectAnswer().length() > 200)) {
            throw new IllegalArgumentException("Añade al menos dos opciones distintas y selecciona la respuesta correcta, de hasta 200 caracteres.");
        }
        if (challenge instanceof OutputPredictionChallenge prediction
                && (prediction.getCodeSnippet() == null || prediction.getCodeSnippet().isBlank())) {
            throw new IllegalArgumentException("Añade el código cuya salida debe predecir el estudiante.");
        }
    }

    public AuthoredChallenge save(int administratorId, AuthoredChallenge draft) throws SQLException {
        OracleAuthoringRepository repository = new OracleAuthoringRepository();
        repository.checkAccess(administratorId);
        validate(draft);
        if (draft.state() == PublicationState.PUBLICADO && draft.challenge() instanceof CodeChallenge code) {
            var validation = new SimulatedCodeRunner().runAndValidate(code, draft.referenceSolution());
            if (!validation.isAllPassed()) {
                throw new IllegalArgumentException("La solución de referencia no supera todos los casos de prueba.");
            }
        }
        return repository.save(administratorId, draft);
    }
}
