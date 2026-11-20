package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.data.local.DemoProfessionalMissionRepository;
import com.mycompany.levelup_code.domain.model.DebuggingChallenge;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
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
    void migrationContainsMatchingReferenceSolutions() throws Exception {
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
    }

    @Test
    void masterRunsTheDatabaseScriptsInDependencyOrder() throws Exception {
        String master = read("11_master_database.sql");
        List<String> includes = master.lines()
                .map(String::strip)
                .filter(line -> line.startsWith("@@"))
                .map(line -> line.substring(2))
                .toList();
        assertEquals(List.of("00_drop.sql", "01_schema.sql", "02_seed.sql", "02b_seed_demo_users.sql",
                "05_student_notebook.sql", "06_learning_goals.sql", "07_user_preferences.sql",
                "08_content_publication.sql", "09_content_versions.sql", "10_challenge_types.sql",
                "03_plsql_gamificacion.sql", "12_seed_starter_templates.sql", "13_challenge_guidance.sql",
                "14_module_finals.sql", "15_expanded_curriculum.sql", "15_professional_missions.sql",
                "16_seed_discount_mission.sql", "17_data_access.sql", "04_test_plsql.sql"), includes);
        for (String include : includes) assertFalse(read(include).isBlank(), include);
        int cleanup = master.indexOf("@@00_drop.sql");
        String confirmation = master.substring(0, cleanup);
        assertTrue(confirmation.contains("DEFINE levelup_confirm_rebuild = \"NO\""));
        assertTrue(confirmation.contains("-20090"));
    }

    @Test
    void discountMissionMatchesOracleSeedAndValidatesRealJavaCode() throws Exception {
        var mission = new DemoProfessionalMissionRepository().findPublishedByKey("descuentos-tienda").orElseThrow();
        var challenge = DemoSeedCatalog.createSeedLanguages().stream()
                .flatMap(language -> language.getModules().stream())
                .flatMap(module -> module.getLessons().stream())
                .flatMap(lesson -> lesson.getChallenges().stream())
                .filter(candidate -> candidate.getId() == mission.challengeId())
                .map(DebuggingChallenge.class::cast)
                .findFirst().orElseThrow();
        String seed = read("16_seed_discount_mission.sql");
        var content = Pattern.compile("v_starter CLOB :=(.*?);\\s*v_reference CLOB :=(.*?);"
                + "\\s*v_guidance CLOB :=(.*?);\\s*v_lesson", Pattern.DOTALL).matcher(seed);
        assertTrue(content.find());
        assertEquals(challenge.getInitialTemplate(), decode(content.group(1)));
        String[] guidance = decode(content.group(3)).split("\\n");
        assertEquals("LEVELUP_OPTIONS_V2", guidance[0]);
        assertEquals(5, guidance.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(challenge.getGuidance().hints().get(i), decodeGuidanceLine(guidance[i + 1]));
        }
        assertEquals(challenge.getGuidance().explanation(), decodeGuidanceLine(guidance[4]));

        var sqlCases = Pattern.compile("add_case\\((\\d+),'([^']*)','([^']*)',(\\d)\\);").matcher(seed);
        var demoCases = challenge.getTestCases();
        assertEquals(9, demoCases.size());
        for (var testCase : demoCases) {
            assertTrue(sqlCases.find());
            var requirement = mission.requirements().get(Integer.parseInt(sqlCases.group(1)) - 1);
            assertTrue(requirement.testCaseIds().contains(testCase.getId()));
            assertEquals(testCase.getInput(), sqlCases.group(2));
            assertEquals(testCase.getExpectedOutput(), sqlCases.group(3));
            assertEquals(testCase.isHidden(), sqlCases.group(4).equals("1"));
        }
        assertFalse(sqlCases.find());
        assertEquals(List.of(4, 2, 3, 0),
                mission.requirements().stream().map(r -> r.testCaseIds().size()).toList());

        var runner = new SimulatedCodeRunner();
        var starter = runner.runAndValidate(challenge, challenge.getInitialTemplate());
        assertTrue(starter.isSandboxedExecution());
        assertFalse(starter.isAllPassed());
        var solved = runner.runAndValidate(challenge, decode(content.group(2)));
        assertTrue(solved.isSandboxedExecution());
        assertTrue(solved.isAllPassed());
    }

    private String decodeGuidanceLine(String line) {
        return new String(Base64.getDecoder().decode(line), StandardCharsets.UTF_8);
    }

    @Test
    void expandedCurriculumMigrationAndOfflineAchievementsStayAvailable() throws Exception {
        String migration = read("15_expanded_curriculum.sql");
        assertTrue(migration.contains("FOR language_index IN 1..3 LOOP"));
        assertTrue(migration.contains("FOR module_order IN 4..6 LOOP"));
        assertEquals(9, Pattern.compile("ensure_achievement\\('").matcher(migration).results().count());
        assertEquals(17, DemoSeedCatalog.createSeedAchievements().size());
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
