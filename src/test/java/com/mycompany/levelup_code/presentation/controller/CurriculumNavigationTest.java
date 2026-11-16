package com.mycompany.levelup_code.presentation.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurriculumNavigationTest {
    @Test void comparisonFollowsTheSameLessonAfterReorderingOrRenaming() {
        var java = DemoSeedCatalog.createSeedLanguages().getFirst();
        var lessons = new ArrayList<>(java.getModules().stream().flatMap(module -> module.getLessons().stream()).toList());
        Collections.reverse(lessons);
        var binary = DemoSeedCatalog.comparativeLesson(lessons, "JAVA", 2);
        assertEquals(2, binary.getId());
        binary.setTitle("Nuevo título del módulo de búsqueda");
        assertTrue(DemoSeedCatalog.resolveComparativeSnippet(binary, "JAVA")[0].contains("mid"));
        assertNull(DemoSeedCatalog.comparativeLesson(List.of(), "JAVA", 2));
    }
}
