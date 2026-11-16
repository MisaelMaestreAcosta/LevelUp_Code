package com.mycompany.levelup_code.domain.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public record EditorPreferences(String language, int fontSize, int indentSpaces, boolean reducedMotion) {
    private static final Map<Integer, EditorPreferences> CACHE = new ConcurrentHashMap<>();
    public static final EditorPreferences DEFAULT = new EditorPreferences("JAVA", 14, 4, false);

    public EditorPreferences {
        if (!Set.of("JAVA", "PYTHON", "CPP").contains(language) || fontSize < 12 || fontSize > 24
                || !Set.of(2, 4, 8).contains(indentSpaces)) {
            throw new IllegalArgumentException("Las preferencias del editor no son válidas.");
        }
    }
    public static EditorPreferences forUser(int userId) { return CACHE.getOrDefault(userId, DEFAULT); }
    public static void remember(int userId, EditorPreferences preferences) { CACHE.put(userId, preferences); }
}
