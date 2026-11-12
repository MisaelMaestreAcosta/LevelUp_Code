package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleLearningAnalyticsRepository;
import com.mycompany.levelup_code.domain.model.LearningAnalytics;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Read-only analytics, fetched off the JavaFX thread. */
public final class LearningAnalyticsPane extends VBox {
    private final Label status = new Label();
    private final VBox cards = new VBox(12);
    private final Button refresh = new Button("Actualizar progreso");

    public LearningAnalyticsPane(int userId, boolean oracle) {
        super(14);
        Label title = new Label("Mi progreso");
        title.getStyleClass().add("page-title");
        status.setWrapText(true);
        refresh.getStyleClass().add("btn-secondary");
        refresh.setOnAction(e -> load(userId));
        getChildren().addAll(title, status, refresh, cards);
        if (oracle) load(userId);
        else {
            refresh.setDisable(true);
            status.setText("En modo de prueba no hay estadísticas guardadas. Inicia sesión con Oracle para ver tu progreso real.");
        }
    }

    private void load(int userId) {
        refresh.setDisable(true);
        status.setText("Consultando tu actividad…");
        Task<LearningAnalytics> task = new Task<>() {
            protected LearningAnalytics call() throws Exception {
                return new OracleLearningAnalyticsRepository().load(userId);
            }
        };
        task.setOnSucceeded(e -> {
            refresh.setDisable(false);
            cards.getChildren().clear();
            LearningAnalytics data = task.getValue();
            status.setText("Últimos 7 días: " + data.weeklyXp() + " XP · " + data.activeDays()
                    + " días con XP. Las lecturas completadas y los retos superados se muestran por separado.");
            for (var result : data.languages()) {
                Label heading = new Label(result.language());
                heading.getStyleClass().add("card-title");
                Label summary = new Label(result.solved() + " de " + result.total() + " retos superados · "
                        + result.attempts() + " intentos · " + result.accuracy() + "% de aciertos\n"
                        + result.completedLessons() + " de " + result.totalLessons() + " lecturas completadas");
                summary.setWrapText(true);
                ProgressBar bar = new ProgressBar(result.total() == 0 ? 0 : (double) result.solved()/result.total());
                bar.setMaxWidth(Double.MAX_VALUE);
                VBox card = new VBox(8, heading, summary, bar);
                card.getStyleClass().add("surface-card");
                cards.getChildren().add(card);
            }
        });
        task.setOnFailed(e -> {
            refresh.setDisable(false);
            status.setText("No se pudo actualizar el progreso. Revisa la conexión y vuelve a intentarlo.");
        });
        Thread thread = new Thread(task, "learning-analytics");
        thread.setDaemon(true);
        thread.start();
    }
}
