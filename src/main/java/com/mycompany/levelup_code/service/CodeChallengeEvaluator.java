package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.AttemptResult;
import com.mycompany.levelup_code.model.Challenge;
import com.mycompany.levelup_code.model.CodeChallenge;
import com.mycompany.levelup_code.model.Language;
import com.mycompany.levelup_code.model.TestCase;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/** Evalúa un reto de código comparando la salida con sus casos de prueba. */
public class CodeChallengeEvaluator implements ChallengeEvaluator {

    private final CodeRunner codeRunner;

    /** Recibe el ejecutor que realizará cada prueba. */
    public CodeChallengeEvaluator(CodeRunner codeRunner) {
        if (codeRunner == null) {
            throw new IllegalArgumentException(
                    "El ejecutor de código es obligatorio.");
        }
        this.codeRunner = codeRunner;
    }

    /** Ejecuta el envío contra los casos y devuelve un resultado agregado. */
    @Override
    public ChallengeEvaluation evaluate(
            Challenge challenge,
            String submission) {
        // Este evaluador solo procesa retos de código.
        if (!(challenge instanceof CodeChallenge)) {
            throw new IllegalArgumentException(
                    "Se requiere un reto de tipo CodeChallenge.");
        }

        // El envío debe contener código.
        if (submission == null || submission.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código enviado es obligatorio.");
        }

        CodeChallenge codeChallenge = (CodeChallenge) challenge;
        Language language = findLanguage(codeChallenge);
        List<TestCase> testCases = codeChallenge.getTestCases();

        // Un reto sin lenguaje, tiempo válido o casos no puede evaluarse.
        if (language == null
                || codeChallenge.getTimeLimitSeconds() <= 0
                || testCases == null
                || testCases.isEmpty()) {
            return new ChallengeEvaluation(AttemptResult.ERROR, 0);
        }

        long totalExecutionTimeMs = 0;

        // Ejecuta cada caso y compara la salida con la esperada.
        for (TestCase testCase : testCases) {
            if (testCase == null || testCase.getExpectedOutput() == null) {
                return new ChallengeEvaluation(
                        AttemptResult.ERROR, totalExecutionTimeMs);
            }

            long startTime = System.nanoTime();
            try {
                String output = codeRunner.run(
                        language.getName(),
                        submission,
                        testCase.getInput(),
                        codeChallenge.getTimeLimitSeconds());

                totalExecutionTimeMs += elapsedMilliseconds(startTime);

                // La primera salida distinta marca el intento como incorrecto.
                if (!testCase.matches(output)) {
                    return new ChallengeEvaluation(
                            AttemptResult.INCORRECT, totalExecutionTimeMs);
                }
            } catch (TimeoutException exception) {
                totalExecutionTimeMs += elapsedMilliseconds(startTime);
                return new ChallengeEvaluation(
                        AttemptResult.TIMEOUT, totalExecutionTimeMs);
            } catch (IOException exception) {
                totalExecutionTimeMs += elapsedMilliseconds(startTime);
                return new ChallengeEvaluation(
                        AttemptResult.ERROR, totalExecutionTimeMs);
            } catch (InterruptedException exception) {
                // Conserva la señal de interrupción para quien llamó al servicio.
                Thread.currentThread().interrupt();
                totalExecutionTimeMs += elapsedMilliseconds(startTime);
                return new ChallengeEvaluation(
                        AttemptResult.ERROR, totalExecutionTimeMs);
            }
        }

        // El reto es correcto si todos sus casos pasaron.
        return new ChallengeEvaluation(
                AttemptResult.CORRECT, totalExecutionTimeMs);
    }

    /** Obtiene el lenguaje asociado a la lección del reto. */
    private Language findLanguage(CodeChallenge challenge) {
        if (challenge.getLesson() == null
                || challenge.getLesson().getModule() == null) {
            return null;
        }
        return challenge.getLesson().getModule().getLanguage();
    }

    /** Convierte el tiempo transcurrido desde nanoTime a milisegundos. */
    private long elapsedMilliseconds(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
