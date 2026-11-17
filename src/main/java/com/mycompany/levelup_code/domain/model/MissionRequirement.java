package com.mycompany.levelup_code.domain.model;

import java.util.List;
import java.util.Objects;

/** A criterion from the client brief. An empty test list denotes a descriptive criterion. */
public record MissionRequirement(String text, List<Integer> testCaseIds) {
    public MissionRequirement {
        Objects.requireNonNull(text);
        if (text.isBlank()) throw new IllegalArgumentException("El requisito debe tener una descripción.");
        testCaseIds = List.copyOf(testCaseIds);
        if (testCaseIds.stream().anyMatch(id -> id <= 0)
                || testCaseIds.stream().distinct().count() != testCaseIds.size()) {
            throw new IllegalArgumentException("Usa identificadores de casos de prueba válidos y distintos.");
        }
    }

    public boolean automaticallyVerified() { return !testCaseIds.isEmpty(); }
}
