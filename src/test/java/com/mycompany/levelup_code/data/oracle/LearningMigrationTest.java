package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.presentation.controller.DemoSeedCatalog;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LearningMigrationTest {
    private static final String LITERAL = "'((?:''|[^'])*)'";

    @Test void guidanceAndFinalAssignmentsMatchTheOriginalSqlCatalog() throws Exception {
        String seed = read("02_seed.sql");
        Map<Integer, List<String>> original = new HashMap<>();
        var rows = Pattern.compile("INSERT INTO Retos .*?VALUES\\s*\\(\\s*(\\d+),\\s*\\d+,\\s*"
                + LITERAL + ",\\s*" + LITERAL, Pattern.DOTALL).matcher(seed);
        while (rows.find()) original.put(Integer.parseInt(rows.group(1)), List.of(unquote(rows.group(2)), unquote(rows.group(3))));
        assertEquals(36, original.size());

        Map<Integer, Challenge> demo = new HashMap<>();
        var languages = DemoSeedCatalog.createSeedLanguages();
        languages.forEach(language -> language.getModules().forEach(module -> module.getLessons()
                .forEach(lesson -> lesson.getChallenges().forEach(challenge -> demo.put(challenge.getId(), challenge)))));
        var guides = Pattern.compile("add_guidance\\((\\d+), " + LITERAL + ", " + LITERAL + ",\\s*(.*?)\\);", Pattern.DOTALL)
                .matcher(read("13_challenge_guidance.sql"));
        int count = 0;
        while (guides.find()) {
            int id = Integer.parseInt(guides.group(1));
            assertEquals(original.get(id), List.of(unquote(guides.group(2)), unquote(guides.group(3))), "Reto " + id);
            assertEquals(demo.get(id).getGuidance(), ChallengeGuidanceCodec.decode(expression(guides.group(4))), "Guía " + id);
            count++;
        }
        assertEquals(36, count);

        Map<Integer, String> lessonTitles = new HashMap<>();
        var lessons = Pattern.compile("INSERT INTO Lecciones .*?VALUES\\s*\\(\\s*(\\d+),\\s*\\d+,\\s*" + LITERAL, Pattern.DOTALL).matcher(seed);
        while (lessons.find()) lessonTitles.put(Integer.parseInt(lessons.group(1)), unquote(lessons.group(2)));
        var finals = Pattern.compile("create_final\\((\\d+), (\\d+), " + LITERAL).matcher(read("14_module_finals.sql"));
        Set<Integer> modules = new HashSet<>();
        while (finals.find()) {
            modules.add(Integer.parseInt(finals.group(1)));
            assertEquals(lessonTitles.get(Integer.parseInt(finals.group(2))), unquote(finals.group(3)));
        }
        assertEquals(9, modules.size());
    }

    private String read(String file) throws Exception { return Files.readString(Path.of("database", file)).replace("\r\n", "\n"); }
    private String unquote(String text) { return text.replace("''", "'"); }
    private String expression(String sql) {
        var pieces = Pattern.compile(LITERAL + "|CHR\\(10\\)").matcher(sql);
        StringBuilder result = new StringBuilder();
        while (pieces.find()) result.append(pieces.group(1) == null ? "\n" : unquote(pieces.group(1)));
        return result.toString();
    }
}
