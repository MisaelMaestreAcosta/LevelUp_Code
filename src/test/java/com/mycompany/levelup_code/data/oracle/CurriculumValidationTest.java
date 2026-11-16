package com.mycompany.levelup_code.data.oracle;

import com.mycompany.levelup_code.data.oracle.OracleCurriculumRepository.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurriculumValidationTest {
    @Test void respectsDatabaseLimitsAndRequiresLessonContent() {
        assertDoesNotThrow(() -> OracleCurriculumRepository.validate(Kind.MODULE, new Entry(0, 1, "Módulo", "", 1, 0)));
        assertThrows(IllegalArgumentException.class, () -> OracleCurriculumRepository.validate(Kind.MODULE, new Entry(0, 1, "A".repeat(81), "", 1, 0)));
        assertThrows(IllegalArgumentException.class, () -> OracleCurriculumRepository.validate(Kind.MODULE, new Entry(0, 1, "Título", "A".repeat(201), 1, 0)));
        assertThrows(IllegalArgumentException.class, () -> OracleCurriculumRepository.validate(Kind.LESSON, new Entry(0, 1, "Lección", " ", 1, 0)));
        assertThrows(IllegalArgumentException.class, () -> OracleCurriculumRepository.validate(Kind.LESSON, new Entry(0, 1, "Lección", "Teoría", 0, 50)));
        assertThrows(IllegalArgumentException.class, () -> OracleCurriculumRepository.validate(Kind.LESSON, new Entry(0, 1, "Lección", "Teoría", 1, -1)));
    }
}
