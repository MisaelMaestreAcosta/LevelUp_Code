package com.mycompany.levelup_code.domain.model;

import com.mycompany.levelup_code.domain.enums.ChallengeType;
import java.util.List;

public final class OutputPredictionChallenge extends QuizChallenge {
    private final String codeSnippet;

    public OutputPredictionChallenge(int id, String title, String statement, Lesson lesson, int reward,
            List<String> options, String answer, String codeSnippet) {
        super(id, title, statement, lesson, reward, options, answer);
        this.codeSnippet = codeSnippet;
    }
    public String getCodeSnippet() { return codeSnippet; }
    @Override public ChallengeType getType() { return ChallengeType.OUTPUT_PREDICTION; }
    @Override public boolean isCorrectOption(String answer) {
        return answer != null && getCorrectAnswer() != null && getCorrectAnswer().strip().equals(answer.strip());
    }
}
