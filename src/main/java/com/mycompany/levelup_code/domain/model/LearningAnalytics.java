package com.mycompany.levelup_code.domain.model;

import java.util.List;
import java.time.LocalDate;

public record LearningAnalytics(List<LanguageResult> languages, int weeklyXp, int activeDays,
        List<DayActivity> dailyActivity, List<ReviewChallenge> reviewChallenges) {
    public LearningAnalytics {
        languages = List.copyOf(languages);
        dailyActivity = List.copyOf(dailyActivity);
        reviewChallenges = List.copyOf(reviewChallenges);
    }
    public LearningAnalytics(List<LanguageResult> languages, int weeklyXp, int activeDays) {
        this(languages, weeklyXp, activeDays, List.of(), List.of());
    }
    public record DayActivity(LocalDate date, int xp) { }
    public record ReviewChallenge(int id, String title, String language) { }
    public record LanguageResult(String language, int attempts, int correct,
            int solved, int total, int completedLessons, int totalLessons) {
        public int accuracy() { return attempts == 0 ? 0 : (int) Math.round(100.0 * correct / attempts); }
    }
}
