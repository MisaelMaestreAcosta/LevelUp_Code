package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.domain.model.LearningGoal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LearningGoalTest {
    @Test void rejectsImpossibleActiveDayTargetsAndInvalidDurations() {
        assertThrows(IllegalArgumentException.class,()->LearningGoal.validate(LearningGoal.Type.DIAS,8,7));
        assertThrows(IllegalArgumentException.class,()->LearningGoal.validate(LearningGoal.Type.RETOS,0,7));
        assertThrows(IllegalArgumentException.class,()->LearningGoal.validate(LearningGoal.Type.XP,100,2));
        assertDoesNotThrow(()->LearningGoal.validate(LearningGoal.Type.DIAS,1,1));
    }
    @Test void capsVisibleProgressAfterTargetIsExceeded() {
        LocalDate date=LocalDate.of(2026,11,12);
        var goal=new LearningGoal(1,LearningGoal.Type.RETOS,3,date,date.plusDays(6),5);
        assertTrue(goal.completed());
        assertEquals(1.0,goal.fraction());
        assertFalse(new LearningGoal(2,LearningGoal.Type.RETOS,3,date,date,1).completed());
    }
}
