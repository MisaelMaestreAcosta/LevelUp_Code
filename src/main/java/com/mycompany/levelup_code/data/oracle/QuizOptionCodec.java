package com.mycompany.levelup_code.data.oracle;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Versioned storage format for quiz options, with legacy pipe support. */
final class QuizOptionCodec {

    private static final String PREFIX = "LEVELUP_OPTIONS_V2\n";

    private QuizOptionCodec() {
    }

    static String encode(List<String> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("El quiz debe incluir opciones.");
        }
        StringBuilder encoded = new StringBuilder(PREFIX);
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) encoded.append('\n');
            String option = options.get(i) == null ? "" : options.get(i);
            encoded.append(Base64.getEncoder().encodeToString(
                    option.getBytes(StandardCharsets.UTF_8)));
        }
        return encoded.toString();
    }

    static List<String> decode(String stored) {
        if (stored == null || stored.isEmpty()) return new ArrayList<>();
        if (!stored.startsWith(PREFIX)) {
            return new ArrayList<>(List.of(stored.split("\\|", -1)));
        }
        String body = stored.substring(PREFIX.length());
        List<String> options = new ArrayList<>();
        for (String encoded : body.split("\n", -1)) {
            options.add(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8));
        }
        return options;
    }
}
