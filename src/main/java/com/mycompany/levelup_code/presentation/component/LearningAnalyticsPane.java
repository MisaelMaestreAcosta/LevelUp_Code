package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleLearningAnalyticsRepository;
import com.mycompany.levelup_code.domain.model.LearningAnalytics;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.app.AppContext;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
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
    private final VBox history = new VBox(14);
    private final VBox review = new VBox(10);
    private final Consumer<Challenge> openChallenge;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"));

    public LearningAnalyticsPane(int userId, boolean oracle) {
        this(userId, oracle, challenge -> ChallengeFrame.open(null, challenge, null));
    }

    LearningAnalyticsPane(int userId, boolean oracle, Consumer<Challenge> openChallenge) {
        this.openChallenge = openChallenge;
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
                history, review, WorkspaceUi.label("Tu avance por lenguaje", "card-title"), cards);
        history.setVisible(false);
        history.setManaged(false);
        review.setVisible(false);
        review.setManaged(false);
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
        showHistory(data);
        showReview(data);
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

    private void showHistory(LearningAnalytics data) {
        history.getChildren().clear();
        history.setVisible(!data.dailyActivity().isEmpty());
        history.setManaged(history.isVisible());
        if (data.dailyActivity().isEmpty()) return;
        CategoryAxis dates = new CategoryAxis();
        NumberAxis xp = new NumberAxis();
        xp.setLabel("XP");
        xp.setMinorTickVisible(false);
        BarChart<String, Number> chart = new BarChart<>(dates, xp);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setMinHeight(230);
        chart.setPrefHeight(260);
        chart.getStyleClass().add("xp-history");
        ComboBox<String> period = new ComboBox<>();
        period.getItems().addAll("Últimos 7 días", "Últimos 28 días");
        period.getSelectionModel().selectFirst();
        period.setAccessibleText("Periodo del gráfico de experiencia");
        Runnable render = () -> {
            int count = period.getSelectionModel().getSelectedIndex() == 0 ? 7 : 28;
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            var days = data.dailyActivity().subList(Math.max(0, data.dailyActivity().size() - count), data.dailyActivity().size());
            for (var day : days) {
                var point = new XYChart.Data<String, Number>(DATE.format(day.date()), day.xp());
                point.nodeProperty().addListener((observable, old, node) -> {
                    if (node != null) {
                        Tooltip.install(node, new Tooltip(DATE.format(day.date()) + " · " + day.xp() + " XP"));
                    }
                });
                series.getData().add(point);
            }
            chart.getData().setAll(java.util.List.of(series));
            dates.setTickLabelRotation(count == 7 ? 0 : -45);
        };
        period.setOnAction(event -> render.run());
        render.run();
        GridPane calendar = new GridPane();
        calendar.setHgap(6);
        calendar.setVgap(6);
        String[] weekdays = { "L", "M", "X", "J", "V", "S", "D" };
        for (int column = 0; column < 7; column++) {
            var constraint = new javafx.scene.layout.ColumnConstraints();
            constraint.setPercentWidth(100.0 / 7);
            calendar.getColumnConstraints().add(constraint);
            calendar.add(WorkspaceUi.label(weekdays[column], "muted-text"), column, 0);
        }
        Label selectedDay = WorkspaceUi.label("Selecciona un día para consultar la XP obtenida.", "body-text");
        int offset = data.dailyActivity().getFirst().date().getDayOfWeek().getValue() - 1;
        int max = data.dailyActivity().stream().mapToInt(LearningAnalytics.DayActivity::xp).max().orElse(0);
        int index = 0;
        for (var day : data.dailyActivity()) {
            int intensity = day.xp() == 0 ? 0 : day.xp() >= max * 0.66 ? 3 : day.xp() >= max * 0.33 ? 2 : 1;
            Button cell = new Button(String.valueOf(day.date().getDayOfMonth()));
            cell.getStyleClass().addAll("activity-day", "heatmap-cell-" + intensity);
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setAccessibleText(DATE.format(day.date()) + ", " + day.xp() + " XP");
            cell.setTooltip(new Tooltip(cell.getAccessibleText()));
            cell.setOnAction(event -> selectedDay.setText(DATE.format(day.date()) + " · " + day.xp() + " XP obtenidos"));
            calendar.add(cell, (index + offset) % 7, 1 + (index + offset) / 7);
            index++;
        }
        VBox chartCard = new VBox(12, WorkspaceUi.label("Tu experiencia día a día", "card-title"), period, chart);
        chartCard.getStyleClass().add("surface-card");
        VBox calendarCard = new VBox(12, WorkspaceUi.label("Actividad de las últimas 4 semanas", "card-title"), calendar, selectedDay);
        calendarCard.getStyleClass().add("surface-card");
        history.getChildren().addAll(chartCard, calendarCard);
    }

    private void showReview(LearningAnalytics data) {
        review.getChildren().setAll(WorkspaceUi.label("Retos para repasar", "card-title"));
        review.setVisible(true);
        review.setManaged(true);
        if (data.reviewChallenges().isEmpty()) {
            review.getChildren().add(WorkspaceUi.label("Tus retos intentados y aún pendientes aparecerán aquí.", "body-text"));
        }
        for (var challenge : data.reviewChallenges()) {
            Button open = WorkspaceUi.button("Volver a intentar", "btn-secondary", Theme.IconKind.PLAY);
            open.setOnAction(event -> openReview(challenge.id(), open));
            VBox card = new VBox(10, WorkspaceUi.label(challenge.title(), "card-title"),
                    WorkspaceUi.label(challenge.language(), "muted-text"), open);
            card.getStyleClass().add("surface-card");
            review.getChildren().add(card);
        }
    }

    private void openReview(int id, Button button) {
        button.setDisable(true);
        Task<Challenge> task = new Task<>() {
            @Override protected Challenge call() {
                return AppContext.getInstance().getChallengeRepository().findById(id).orElseThrow();
            }
        };
        task.setOnSucceeded(event -> {
            button.setDisable(false);
            if (getScene() != null) openChallenge.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            button.setDisable(false);
            WorkspaceUi.status(status, "No pudimos abrir el reto. Actualiza el progreso e inténtalo de nuevo.", "status-error");
        });
        Thread thread = new Thread(task, "analytics-review");
        thread.setDaemon(true);
        thread.start();
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
