package com.mycompany.levelup_code.data.oracle;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizOptionCodecTest {

    @Test
    void preservesPipesUnicodeAndLineBreaksWhileReadingLegacyValues() {
        List<String> options = List.of(
                "a | b",
                "System.out.println(\"¡Hola!\");\nreturn true;",
                "");

        assertEquals(options, QuizOptionCodec.decode(QuizOptionCodec.encode(options)));
        assertEquals(List.of("uno", "dos", "tres"),
                QuizOptionCodec.decode("uno|dos|tres"));
    }
}
