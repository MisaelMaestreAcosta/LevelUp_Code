package com.mycompany.levelup_code.domain.model;

import java.util.List;
import java.util.Objects;

/** Authored teaching content, kept in the same revision as the exercise. */
public record ChallengeGuidance(List<String> hints, String explanation, List<String> optionFeedback) {
    public static final ChallengeGuidance EMPTY = new ChallengeGuidance(List.of(), "", List.of());

    public ChallengeGuidance {
        hints = List.copyOf(hints);
        explanation = Objects.requireNonNullElse(explanation, "");
        optionFeedback = List.copyOf(optionFeedback);
        if (hints.size() > 3) throw new IllegalArgumentException("Usa hasta tres pistas progresivas.");
    }

    public boolean isEmpty() {
        return hints.stream().allMatch(String::isBlank) && explanation.isBlank()
                && optionFeedback.stream().allMatch(String::isBlank);
    }

    public String hint(int stage, Challenge challenge) {
        if (stage > 0 && stage <= hints.size() && !hints.get(stage - 1).isBlank()) return hints.get(stage - 1);
        return switch (Math.min(3, Math.max(1, stage))) {
            case 1 -> "Identifica los datos de entrada y lo que pide el enunciado.\n\n" + challenge.getDescription();
            case 2 -> "Prueba tu razonamiento con un ejemplo pequeño y comprueba cada paso antes de responder.";
            default -> challenge.getLesson() == null || challenge.getLesson().getTheory() == null
                    ? "Revisa los casos de prueba y contrasta tu respuesta con el resultado esperado."
                    : challenge.getLesson().getTheory();
        };
    }

    public String feedback(int option, boolean correct) {
        if (option >= 0 && option < optionFeedback.size() && !optionFeedback.get(option).isBlank()) return optionFeedback.get(option);
        if (correct && !explanation.isBlank()) return explanation;
        return correct ? "Respuesta correcta. Puedes continuar con el siguiente ejercicio."
                : "Revisa la pista y comprueba qué parte del enunciado no cumple tu respuesta.";
    }
}
