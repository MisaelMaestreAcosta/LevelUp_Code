package com.mycompany.levelup_code.domain.enums;

/** A student's persisted outcome for a challenge. */
public enum ChallengeProgress {
    PENDING("Pendiente"), ATTEMPTED("Intentado"), SOLVED("Superado");

    private final String label;

    ChallengeProgress(String label) { this.label = label; }

    public String label() { return label; }
}
