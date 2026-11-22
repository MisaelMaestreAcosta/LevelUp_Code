package com.mycompany.levelup_code.domain.model;

import java.util.List;
import java.util.Objects;

/** A client brief attached to the challenge used for evaluation and attempt history. */
public record ProfessionalMission(String key, int challengeId, String title, String client, String brief,
        List<MissionRequirement> requirements) {
    public ProfessionalMission {
        Objects.requireNonNull(key);
        Objects.requireNonNull(title);
        Objects.requireNonNull(client);
        Objects.requireNonNull(brief);
        if (key.isBlank() || challengeId <= 0 || title.isBlank() || client.isBlank() || brief.isBlank()) {
            throw new IllegalArgumentException("Completa el encargo, el cliente y el reto de la misión.");
        }
        requirements = List.copyOf(requirements);
    }
}
