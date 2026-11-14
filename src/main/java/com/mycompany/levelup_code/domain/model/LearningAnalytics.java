package com.mycompany.levelup_code.domain.model;

import java.util.List;

public record LearningAnalytics(List<LanguageResult> languages, int weeklyXp, int activeDays) {
    public record LanguageResult(String language, int attempts, int correct,
            int solved, int total, int completedLessons, int totalLessons) {
        public int accuracy() { return attempts == 0 ? 0 : (int) Math.round(100.0 * correct / attempts); }
    }
}
