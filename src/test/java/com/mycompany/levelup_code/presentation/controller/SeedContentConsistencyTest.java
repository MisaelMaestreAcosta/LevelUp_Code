package com.mycompany.levelup_code.presentation.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeedContentConsistencyTest {
    @Test
    void oracleAndDemoStartWithTheSameIncompleteTemplates() throws Exception {
        String seed = read("02_seed.sql");
        var entries = Pattern.compile("INSERT INTO Reto_Codigo \\(id_reto, codigo_inicial, limite_tiempo_ms\\)"
                + "\\s*VALUES\\s*\\(\\s*(\\d+),\\s*(.*?)\\s*,\\s*2000\\s*\\);", Pattern.DOTALL).matcher(seed);
        int count = 0;
        while (entries.find()) {
            int id = Integer.parseInt(entries.group(1));
            assertEquals(SeedChallengeContent.forId(id).starter(), decode(entries.group(2)), "Reto " + id);
            count++;
        }
        assertEquals(18, count);
    }

    @Test
    void migrationContainsMatchingReferenceSolutionsAndIsIncludedInMaster() throws Exception {
        String migration = read("12_seed_starter_templates.sql");
        var entries = Pattern.compile("separate_template\\((\\d+),\\s*(.*?)\\n    \\);", Pattern.DOTALL).matcher(migration);
        int count = 0;
        while (entries.find()) {
            var content = SeedChallengeContent.forId(Integer.parseInt(entries.group(1)));
            String[] arguments = entries.group(2).split(",\\n        (?=')", 2);
            assertEquals(2, arguments.length);
            assertEquals(content.reference().stripTrailing(), decode(arguments[0]).stripTrailing());
            assertEquals(content.starter(), decode(arguments[1]));
            count++;
        }
        assertEquals(18, count);
        String master = read("11_master_database.sql");
        assertTrue(master.contains(read("02_seed.sql").strip()));
        assertTrue(master.contains(migration.strip()));
        assertTrue(master.contains(read("13_challenge_guidance.sql").strip()));
        assertTrue(master.contains(read("14_module_finals.sql").strip()));
        assertTrue(master.contains(read("00_drop.sql").strip()));
    }

    private String read(String filename) throws Exception {
        return Files.readString(Path.of("database", filename)).replace("\r\n", "\n");
    }

    private String decode(String expression) {
        var pieces = Pattern.compile("'((?:''|[^'])*)'|CHR\\(10\\)").matcher(expression);
        var code = new StringBuilder();
        while (pieces.find()) code.append(pieces.group(1) == null ? "\n" : pieces.group(1).replace("''", "'"));
        return code.toString();
    }
}
