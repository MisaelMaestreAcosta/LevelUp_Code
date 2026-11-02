package com.mycompany.levelup_code.presentation.fx;

import com.mycompany.levelup_code.presentation.component.LoginFrame;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Aplicación principal en JavaFX 21 + CSS ({@code /styles/levelup.css}).
 * Coordina el ciclo de vida del {@link Stage} primario y lanza la ventana de
 * autenticación ({@link LoginFrame}), el panel de telemetría ({@code DashboardFrame}),
 * el visor de lecciones ({@code LessonFrame}) y el evaluador dinámico ({@code ChallengeFrame}).
 */
public class LevelUpFxApp extends Application {

    public static void launchApp(String[] args) {
        Application.launch(LevelUpFxApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        LoginFrame loginFrame = new LoginFrame(primaryStage);
        loginFrame.show();
    }
}
