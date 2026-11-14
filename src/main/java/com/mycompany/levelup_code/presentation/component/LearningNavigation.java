package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.Lesson;

interface LearningNavigation {
    void openLesson(Lesson lesson);
    void openChallenge(Challenge challenge);
    void challengeChanged(Challenge challenge);
    void back();
}
