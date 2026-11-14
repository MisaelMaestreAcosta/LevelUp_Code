package com.mycompany.levelup_code.presentation.fx;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.presentation.component.LoginFrame;
import com.mycompany.levelup_code.presentation.component.Theme;
import com.mycompany.levelup_code.presentation.component.Lupo;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aplicación principal en JavaFX 21 + CSS ({@code /styles/levelup.css}).
 * Coordina el ciclo de vida del {@link Stage} primario y lanza la ventana de
 * autenticación ({@link LoginFrame}), el panel de telemetría ({@code DashboardFrame}),
 * el visor de lecciones ({@code LessonFrame}) y el evaluador dinámico ({@code ChallengeFrame}).
 */
public class LevelUpFxApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(LevelUpFxApp.class.getName());

    public static void launchApp(String[] args) {
        Application.launch(LevelUpFxApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        showConnectingScreen(primaryStage);
    }

    private void showConnectingScreen(Stage stage) {
        stage.setTitle("LevelUp Code · Conectando");
        var brand = Lupo.brand();
        brand.setAlignment(Pos.CENTER);
        Label message = new Label("Preparando tu espacio de aprendizaje…");
        message.getStyleClass().add("muted-text");
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setMaxSize(36, 36);
        Button retry = new Button("Volver a intentar");
        retry.getStyleClass().add("btn-primary");
        retry.setVisible(false);
        retry.setManaged(false);
        retry.setOnAction(event -> showConnectingScreen(stage));
        VBox content = new VBox(16, brand, Lupo.portrait(Lupo.Expression.THINKING, 112), indicator, message, retry);
        content.getStyleClass().add("root-shell");
        content.setAlignment(Pos.CENTER);
        Scene scene = new Scene(content, 680, 460);
        Theme.applyCss(scene);
        stage.setScene(scene);
        stage.show();

        Task<AppContext> connect = new Task<>() {
            @Override protected AppContext call() {
                return AppContext.getInstance();
            }
        };
        connect.setOnSucceeded(event -> {
            if (stage.getScene() == scene) new LoginFrame(stage).show();
        });
        connect.setOnFailed(event -> {
            LOGGER.log(Level.WARNING, "No se pudo iniciar LevelUp Code", connect.getException());
            indicator.setVisible(false);
            message.setText("No pudimos iniciar la aplicación. Inténtalo de nuevo.");
            retry.setVisible(true);
            retry.setManaged(true);
        });
        Thread worker = new Thread(connect, "levelup-initial-connection");
        worker.setDaemon(true);
        worker.start();
    }
}
