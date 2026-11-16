package com.mycompany.levelup_code.presentation.component;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class LessonTraceTest {
    @Test void binarySearchFindsBoundariesAndTerminatesForMissingValues() {
        assertEquals(0, LessonTrace.binarySearch(2).getLast().result());
        assertEquals(6, LessonTrace.binarySearch(38).getLast().result());
        assertEquals(3, LessonTrace.binarySearch(12).getLast().result());
        assertEquals(-1, LessonTrace.binarySearch(7).getLast().result());
        for (var step : LessonTrace.binarySearch(7)) {
            assertTrue(step.highlighted().stream().allMatch(index -> index >= 0 && index < step.cells().size()));
        }
    }

    @Test void palindromeTraceHandlesMatchesMismatchesAndUnicodeCharacters() {
        assertEquals(1, LessonTrace.palindrome("radar").getLast().result());
        assertEquals(0, LessonTrace.palindrome("casa").getLast().result());
        assertEquals(1, LessonTrace.palindrome("😀a😀").getLast().result());
        assertEquals(1, LessonTrace.palindrome("").getLast().result());
    }

    @Test void collectionExamplesCalculateTheirDisplayedResults() {
        assertEquals(5, LessonTrace.firstDuplicate().getLast().result());
        assertEquals(12, LessonTrace.sumEven().getLast().result());
    }
}
