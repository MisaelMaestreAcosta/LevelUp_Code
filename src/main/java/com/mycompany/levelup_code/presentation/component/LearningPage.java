package com.mycompany.levelup_code.presentation.component;

import javafx.scene.layout.Region;
import javafx.stage.Window;

/** Lifecycle shared by standalone learning windows and embedded pages. */
interface LearningPage {
    Region content();
    boolean canLeave();
    void disposePage();
    void setNavigator(LearningNavigation navigator);

    default Window ownerWindow() {
        return content().getScene() == null ? null : content().getScene().getWindow();
    }
}
