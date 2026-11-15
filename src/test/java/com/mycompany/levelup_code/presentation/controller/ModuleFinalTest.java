package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.domain.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModuleFinalTest {
    @Test void eachModuleHasOneFinalWithFeedbackAndAnUnambiguousAnswer() {
        for (var language : DemoSeedCatalog.createSeedLanguages()) for (var module : language.getModules()) {
            var finals = module.getLessons().stream().flatMap(lesson -> lesson.getChallenges().stream()).filter(Challenge::isModuleFinal).toList();
            assertEquals(1, finals.size());
            var challenge = (OutputPredictionChallenge) finals.getFirst();
            assertEquals(1, challenge.getOptions().stream().filter(challenge.getCorrectAnswer()::equals).count());
            assertEquals(3, challenge.getGuidance().hints().size());
            assertEquals(challenge.getOptions().size(), challenge.getGuidance().optionFeedback().size());
            assertFalse(challenge.getCodeSnippet().isBlank());
        }
    }

    @Test void javaFinalsProduceTheirExpectedOutputsWhenExecuted() throws Exception {
        var java = DemoSeedCatalog.createSeedLanguages().getFirst();
        for (var module : java.getModules()) {
            var challenge = (OutputPredictionChallenge) module.getLessons().stream()
                    .flatMap(lesson -> lesson.getChallenges().stream()).filter(Challenge::isModuleFinal).findFirst().orElseThrow();
            assertEquals(challenge.getCorrectAnswer(), new SimulatedCodeRunner().run("Java", challenge.getCodeSnippet(), "5", 3).strip());
        }
    }
}
