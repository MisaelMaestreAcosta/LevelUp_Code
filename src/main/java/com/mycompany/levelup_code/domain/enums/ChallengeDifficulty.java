package com.mycompany.levelup_code.domain.enums;

/** Difficulty stored in Retos.dificultad. */
public enum ChallengeDifficulty {
    FACIL("Fácil"), MEDIO("Medio"), DIFICIL("Difícil");

    private final String label;

    ChallengeDifficulty(String label) { this.label = label; }

    public String label() { return label; }
}
