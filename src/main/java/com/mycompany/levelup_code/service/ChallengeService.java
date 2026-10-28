package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.Attempt;
import com.mycompany.levelup_code.model.AttemptResult;
import com.mycompany.levelup_code.model.Challenge;
import com.mycompany.levelup_code.model.ChallengeType;
import com.mycompany.levelup_code.model.User;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Coordina la evaluación, el registro y la recompensa de los intentos. */
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final AttemptRepository attemptRepository;
    private final Map<ChallengeType, ChallengeEvaluator> evaluators;
    private final GamificationService gamificationService;

    public ChallengeService(
            ChallengeRepository challengeRepository,
            AttemptRepository attemptRepository,
            Map<ChallengeType, ChallengeEvaluator> evaluators,
            GamificationService gamificationService) {
        if (challengeRepository == null || attemptRepository == null
                || evaluators == null || evaluators.isEmpty()
                || gamificationService == null) {
            throw new IllegalArgumentException(
                    "Los repositorios, los evaluadores y el servicio de gamificación son obligatorios.");
        }

        EnumMap<ChallengeType, ChallengeEvaluator> evaluatorCopy =
                new EnumMap<>(ChallengeType.class);
        evaluatorCopy.putAll(evaluators);
        if (evaluatorCopy.containsValue(null)) {
            throw new IllegalArgumentException("Los evaluadores no pueden ser nulos.");
        }

        this.challengeRepository = challengeRepository;
        this.attemptRepository = attemptRepository;
        this.evaluators = Collections.unmodifiableMap(evaluatorCopy);
        this.gamificationService = gamificationService;
    }

    /**
     * Evalúa y registra un envío. Cada envío queda en el historial; la XP del reto
     * se concede una sola vez, en el primer resultado correcto del usuario.
     */
    public Attempt submitAttempt(User user, int challengeId, String submission) {
        validateSubmission(user, challengeId, submission);

        Optional<Challenge> challengeResult = challengeRepository.findById(challengeId);
        if (challengeResult == null) {
            throw new IllegalStateException("El repositorio devolvió una búsqueda inválida.");
        }
        Challenge challenge = challengeResult.orElseThrow(() ->
                new IllegalArgumentException("El reto no existe."));

        if (challenge.getType() == null || challenge.getExpReward() < 0) {
            throw new IllegalStateException("El reto tiene un tipo o recompensa inválidos.");
        }

        ChallengeEvaluator evaluator = evaluators.get(challenge.getType());
        if (evaluator == null) {
            throw new IllegalStateException(
                    "No hay un evaluador registrado para el tipo " + challenge.getType() + ".");
        }

        ChallengeEvaluation evaluation = evaluator.evaluate(challenge, submission.trim());
        if (evaluation == null) {
            throw new IllegalStateException("El evaluador no devolvió un resultado.");
        }

        boolean firstCorrectAttempt = evaluation.getResult() == AttemptResult.CORRECT
                && !attemptRepository.hasSuccessfulAttempt(user.getId(), challengeId);

        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setChallenge(challenge);
        attempt.setSubmittedContent(submission.trim());
        attempt.setResult(evaluation.getResult());
        attempt.setExecutionTimeMs(evaluation.getExecutionTimeMs());
        attempt.setEarnedExperience(firstCorrectAttempt ? challenge.getExpReward() : 0);

        Attempt savedAttempt = attemptRepository.save(attempt);
        if (savedAttempt == null) {
            throw new IllegalStateException("El repositorio no devolvió el intento guardado.");
        }

        if (attempt.getEarnedExperience() > 0) {
            gamificationService.awardExperience(user, attempt.getEarnedExperience());
        }
        return savedAttempt;
    }

    /** Consulta el historial de intentos para un usuario y un reto. */
    public List<Attempt> getAttemptHistory(int userId, int challengeId) {
        if (userId <= 0 || challengeId <= 0) {
            throw new IllegalArgumentException("El usuario y el reto deben ser válidos.");
        }

        List<Attempt> history =
                attemptRepository.findByUserAndChallenge(userId, challengeId);
        if (history == null) {
            throw new IllegalStateException("El repositorio devolvió un historial inválido.");
        }
        return history;
    }

    private void validateSubmission(User user, int challengeId, String submission) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("El usuario debe ser válido.");
        }
        if (challengeId <= 0) {
            throw new IllegalArgumentException("El identificador del reto debe ser válido.");
        }
        if (submission == null || submission.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido enviado es obligatorio.");
        }
    }
}
