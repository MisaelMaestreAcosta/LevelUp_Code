package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleLearningAnalyticsRepository;
import com.mycompany.levelup_code.domain.model.LearningAnalytics;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/** Read-only analytics, fetched off the JavaFX thread. */
public final class LearningAnalyticsPane extends VBox {
    private final Label status = new Label();
    private final TilePane cards = new TilePane(16, 16);
    private final FlowPane summary = new FlowPane(12, 12);
    private final Button refresh = WorkspaceUi.button("Actualizar", "btn-secondary", Theme.IconKind.REFRESH);

    public LearningAnalyticsPane(int userId, boolean oracle) {
        getStyleClass().add("learning-workspace");
        refresh.setOnAction(event -> load(userId));
        cards.setPrefColumns(3);
        cards.setPrefTileWidth(280);
        cards.setTileAlignment(Pos.TOP_LEFT);
        cards.widthProperty().addListener((observable, old, value) -> {
            int columns = Math.max(1, Math.min(3, (int) ((value.doubleValue() + 16) / 276)));
            cards.setPrefTileWidth(Math.max(0, Math.floor((value.doubleValue() - 16 * (columns - 1) - 2) / columns)));
        });
        VBox body = new VBox(18, summary,
                WorkspaceUi.label("Tu avance por lenguaje", "card-title"), cards);
        getChildren().addAll(WorkspaceUi.header("Mi progreso",
                "Consulta lo que has aprendido y tu actividad de los últimos 7 días.", refresh),
                status, WorkspaceUi.scroll(body));
        if (oracle) {
            load(userId);
        } else {
            refresh.setDisable(true);
            WorkspaceUi.status(status, "Inicia sesión con tu cuenta para consultar el progreso guardado.", "");
            cards.getChildren().add(WorkspaceUi.empty(Theme.IconKind.CHART, "Tu progreso empieza aquí",
                    "Los retos y las lecturas que completes aparecerán en esta sección."));
        }
    }

    private VBox metric(String value, String caption, Theme.IconKind icon) {
        HBox heading = new HBox(10, Theme.createIcon(icon, 18, Theme.HEX_INDIGO),
                WorkspaceUi.label(value, "metric-value"));
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox tile = new VBox(6, heading, WorkspaceUi.label(caption, "muted-text"));
        tile.getStyleClass().add("metric-tile");
        tile.setMinWidth(180);
        return tile;
    }

    private void show(LearningAnalytics data) {
        int solved = data.languages().stream().mapToInt(LearningAnalytics.LanguageResult::solved).sum();
        summary.getChildren().setAll(
                metric(data.weeklyXp() + " XP", "Ganados en los últimos 7 días", Theme.IconKind.BOLT),
                metric(data.activeDays() + " de 7", "Días con experiencia", Theme.IconKind.FLAME),
                metric(String.valueOf(solved), "Retos superados en total", Theme.IconKind.CHECK));
        cards.getChildren().clear();
        for (var result : data.languages()) {
            String key = result.language().toLowerCase(java.util.Locale.ROOT);
            String languageStyle = key.contains("python") ? "python" : key.contains("c++") ? "cpp" : "java";
            String color = switch (languageStyle) {
                case "python" -> Theme.HEX_PYTHON;
                case "cpp" -> Theme.HEX_CPP;
                default -> Theme.HEX_JAVA;
            };
            int percent = result.total() == 0 ? 0 : (int) Math.round(100.0 * result.solved() / result.total());
            Label heading = WorkspaceUi.label(result.language(), "card-title");
            Label percentage = WorkspaceUi.label(percent + "%", "metric-value");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox top = new HBox(10, Theme.createIcon(Theme.IconKind.CODE, 20, color), heading, spacer, percentage);
            top.setAlignment(Pos.CENTER_LEFT);
            ProgressBar progress = new ProgressBar(result.total() == 0 ? 0 : (double) result.solved() / result.total());
            progress.getStyleClass().add("progress-" + languageStyle);
            progress.setMaxWidth(Double.MAX_VALUE);
            progress.setAccessibleText(result.language() + ", " + percent + "% de retos superados");
            VBox card = new VBox(14, top,
                    WorkspaceUi.label(result.solved() + " de " + result.total() + " retos superados", "body-text"),
                    progress, new javafx.scene.control.Separator(),
                    WorkspaceUi.label(result.completedLessons() + " de " + result.totalLessons() + " lecturas completadas", "body-text"),
                    WorkspaceUi.label(result.attempts() + " intentos · " + result.accuracy() + "% de aciertos", "muted-text"));
            card.getStyleClass().add("surface-card");
            card.setMaxWidth(Double.MAX_VALUE);
            cards.getChildren().add(card);
        }
        if (data.languages().isEmpty()) {
            cards.getChildren().add(WorkspaceUi.empty(Theme.IconKind.BOOK, "Todavía no hay cursos disponibles",
                    "El progreso aparecerá cuando se publique contenido."));
        }
        WorkspaceUi.status(status, "", "");
    }

    private void load(int userId) {
        refresh.setDisable(true);
        WorkspaceUi.status(status, "Consultando tu actividad…", "");
        Task<LearningAnalytics> task = new Task<>() {
            @Override protected LearningAnalytics call() throws Exception {
                return new OracleLearningAnalyticsRepository().load(userId);
            }
        };
        task.setOnSucceeded(event -> {
            refresh.setDisable(false);
            show(task.getValue());
        });
        task.setOnFailed(event -> {
            refresh.setDisable(false);
            WorkspaceUi.status(status, "No se pudo actualizar el progreso. Comprueba la conexión y pulsa Actualizar.", "status-error");
        });
        Thread thread = new Thread(task, "learning-analytics");
        thread.setDaemon(true);
        thread.start();
    }
}
