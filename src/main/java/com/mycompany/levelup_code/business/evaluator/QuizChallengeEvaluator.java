package com.mycompany.levelup_code.business.evaluator;

import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import java.util.List;

/** Evalúa respuestas textuales de retos de selección múltiple. */
public class QuizChallengeEvaluator implements ChallengeEvaluator {

    /**
     * Evalúa una respuesta y devuelve el resultado del intento.
     * El tiempo de ejecución de un quiz se registra como cero.
     */
    @Override
    public ChallengeEvaluation evaluate(Challenge challenge, String submission) {
        // Este evaluador solo puede procesar retos de tipo QuizChallenge.
        if (!(challenge instanceof QuizChallenge)) {
            throw new IllegalArgumentException(
                    "El evaluador de quiz requiere un reto de tipo QuizChallenge.");
        }

        // El envío debe contener una respuesta.
        if (submission == null || submission.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La respuesta enviada es obligatoria.");
        }

        QuizChallenge quiz = (QuizChallenge) challenge;

        // Si el quiz está mal configurado, reporta un error de evaluación.
        // Así ChallengeService puede registrar el intento con resultado ERROR.
        if (!hasValidAnswerConfiguration(quiz)) {
            return new ChallengeEvaluation(AttemptResult.ERROR, 0);
        }

        // El modelo compara la respuesta ignorando espacios externos y mayúsculas.
        AttemptResult result = quiz.isCorrectOption(submission.trim())
                ? AttemptResult.CORRECT
                : AttemptResult.INCORRECT;

        return new ChallengeEvaluation(result, 0);
    }

    /**
     * Comprueba que haya opciones y que la respuesta correcta figure entre ellas.
     */
    private boolean hasValidAnswerConfiguration(QuizChallenge quiz) {
        List<String> options = quiz.getOptions();
        String correctAnswer = quiz.getCorrectAnswer();

        // Un quiz sin opciones o sin respuesta correcta no puede evaluarse.
        if (options == null || options.isEmpty()
                || correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return false;
        }

        // La respuesta correcta debe coincidir con alguna opción,
        // sin distinguir mayúsculas ni espacios al inicio o al final.
        String normalizedCorrectAnswer = correctAnswer.trim();
        return options.stream()
                .filter(option -> option != null)
                .anyMatch(option ->
                        option.trim().equalsIgnoreCase(normalizedCorrectAnswer));
    }
}