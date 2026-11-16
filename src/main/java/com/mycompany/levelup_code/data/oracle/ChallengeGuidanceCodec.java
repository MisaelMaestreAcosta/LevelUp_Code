package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.domain.model.ChallengeGuidance;
import java.util.ArrayList;
import java.util.List;

final class ChallengeGuidanceCodec {
    private ChallengeGuidanceCodec() { }

    static String encode(ChallengeGuidance guidance) {
        var values = new ArrayList<String>();
        for (int i = 0; i < 3; i++) values.add(i < guidance.hints().size() ? guidance.hints().get(i) : "");
        values.add(guidance.explanation());
        values.addAll(guidance.optionFeedback());
        return QuizOptionCodec.encode(values);
    }

    static ChallengeGuidance decode(String stored) {
        if (stored == null || stored.isBlank()) return ChallengeGuidance.EMPTY;
        List<String> values = QuizOptionCodec.decode(stored);
        if (values.size() < 4) throw new IllegalArgumentException("La guía del reto tiene un formato inválido.");
        return new ChallengeGuidance(values.subList(0, 3), values.get(3), values.subList(4, values.size()));
    }
}
