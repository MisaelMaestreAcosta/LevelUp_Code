package com.mycompany.levelup_code.presentation.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Tracks secondary windows that belong to the currently authenticated session. */
final class SessionWindowRegistry {

    private static final Set<Stage> WINDOWS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private SessionWindowRegistry() {
    }

    static void track(Stage window) {
        if (window == null) return;
        WINDOWS.add(window);
        window.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> WINDOWS.add(window));
        window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> WINDOWS.remove(window));
    }

    static void closeAll() {
        for (Stage window : new ArrayList<>(WINDOWS)) {
            window.hide();
        }
        WINDOWS.clear();
    }
}
