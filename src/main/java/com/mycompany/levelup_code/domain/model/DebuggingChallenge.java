package com.mycompany.levelup_code.domain.model;

import com.mycompany.levelup_code.domain.enums.ChallengeType;

public final class DebuggingChallenge extends CodeChallenge {
    public DebuggingChallenge(int id, String title, String statement, Lesson lesson, int reward, String brokenCode, int timeout) {
        super(id, title, statement, lesson, reward, brokenCode, timeout);
    }
    @Override public ChallengeType getType() { return ChallengeType.DEBUGGING; }
}
