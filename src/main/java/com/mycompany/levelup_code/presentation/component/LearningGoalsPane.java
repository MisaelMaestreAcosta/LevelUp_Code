package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleGoalRepository;
import com.mycompany.levelup_code.domain.model.LearningGoal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class LearningGoalsPane extends VBox {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"));
    private final OracleGoalRepository repository = new OracleGoalRepository();
    private final Label status = new Label();
    private final VBox controls = new VBox(18);
    private final VBox cards = new VBox(12);
    private final Button refresh = WorkspaceUi.button("Actualizar", "btn-secondary", Theme.IconKind.REFRESH);
    private final int userId;
    private boolean busy;

    public LearningGoalsPane(int userId, boolean oracle) {
        this.userId = userId;
        getStyleClass().add("learning-workspace");
        ComboBox<LearningGoal.Type> type = new ComboBox<>();
        type.getItems().setAll(LearningGoal.Type.values());
        type.getSelectionModel().selectFirst();
        type.setPrefWidth(210);
        Spinner<Integer> target = new Spinner<>(1, 10000, 3);
        target.setEditable(true);
        target.setPrefWidth(120);
        target.getEditor().setTextFormatter(new TextFormatter<String>(
                change -> change.getControlNewText().matches("\\d{0,5}") ? change : null));
        ComboBox<String> duration = new ComboBox<>();
        duration.getItems().setAll("Hoy", "7 días");
        duration.getSelectionModel().selectLast();
        duration.setPrefWidth(130);

        Button create = WorkspaceUi.button("Crear meta", "btn-primary", Theme.IconKind.TARGET);
        create.setOnAction(event -> {
            try {
                int amount = Integer.parseInt(target.getEditor().getText());
                int days = duration.getSelectionModel().getSelectedIndex() == 0 ? 1 : 7;
                LearningGoal.Type selectedType = type.getValue();
                LearningGoal.validate(selectedType, amount, days);
                run(() -> {
                    repository.create(userId, selectedType, amount, days);
                    return repository.load(userId);
                }, "Meta creada. Tu actividad del periodo ya cuenta para alcanzarla.");
            } catch (IllegalArgumentException ex) {
                WorkspaceUi.status(status, "Introduce una cantidad entre 1 y 10000. Los días con XP deben caber en el periodo elegido.", "status-error");
            }
        });
        refresh.setOnAction(event -> run(() -> repository.load(userId), "Metas actualizadas."));

        FlowPane fields = new FlowPane(12, 12,
                WorkspaceUi.field("Quiero completar", type),
                WorkspaceUi.field("Cantidad", target),
                WorkspaceUi.field("Durante", duration));
        VBox form = new VBox(14, WorkspaceUi.label("Crea tu próxima meta", "card-title"), fields, create,
                WorkspaceUi.label("Cuenta la actividad desde el inicio de hoy. Cada reto cuenta una vez por periodo. Las metas no añaden XP.", "muted-text"));
        form.getStyleClass().add("surface-card");
        controls.getChildren().addAll(form, WorkspaceUi.label("Tus metas", "card-title"), cards);
        getChildren().addAll(WorkspaceUi.header("Mis metas", "Elige un objetivo pequeño y sigue tu avance.", refresh),
                status, WorkspaceUi.scroll(controls));
        if (oracle) {
            run(() -> repository.load(userId), "");
        } else {
            controls.setDisable(true);
            refresh.setDisable(true);
            WorkspaceUi.status(status, "Inicia sesión con tu cuenta para guardar metas y seguir su avance.", "");
        }
    }

    private void show(List<LearningGoal> goals) {
        cards.getChildren().clear();
        for (LearningGoal goal : goals) {
            boolean expired = goal.end().isBefore(LocalDate.now());
            String state = goal.completed() ? "Cumplida" : expired ? "Finalizada" : "En curso";
            String badgeStyle = goal.completed() ? "badge-green" : expired ? "badge-neutral" : "badge-indigo";
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox top = new HBox(10, Theme.createIcon(Theme.IconKind.TARGET, 18, Theme.HEX_INDIGO),
                    WorkspaceUi.label(goal.type().toString(), "card-title"), spacer,
                    Theme.createPillBadge(state, badgeStyle));
            top.setAlignment(Pos.CENTER_LEFT);
            ProgressBar progress = new ProgressBar(goal.fraction());
            progress.setMaxWidth(Double.MAX_VALUE);
            progress.setAccessibleText(goal.type() + ", " + Math.min(goal.current(), goal.target()) + " de " + goal.target());
            if (goal.completed()) progress.getStyleClass().add("progress-bar-green");
            Label count = WorkspaceUi.label(Math.min(goal.current(), goal.target()) + " de " + goal.target(), "metric-value");
            Label dates = WorkspaceUi.label(DATE.format(goal.start()) + " – " + DATE.format(goal.end()), "muted-text");
            Button remove = WorkspaceUi.button("Eliminar", "btn-danger", null);
            remove.setAccessibleText("Eliminar meta de " + goal.type());
            remove.setOnAction(event -> remove(goal));
            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);
            HBox footer = new HBox(12, dates, footerSpacer, remove);
            footer.setAlignment(Pos.CENTER_LEFT);
            String message = goal.completed() ? "¡Objetivo alcanzado!"
                    : expired ? "Puedes crear otra meta para continuar."
                    : "Te faltan " + Math.max(0, goal.target() - goal.current()) + " para alcanzar tu objetivo.";
            VBox card = new VBox(12, top, count, progress, WorkspaceUi.label(message, "body-text"), footer);
            card.getStyleClass().add("surface-card");
            cards.getChildren().add(card);
        }
        if (goals.isEmpty()) {
            cards.getChildren().add(WorkspaceUi.empty(Theme.IconKind.TARGET, "Tu primera meta está por empezar",
                    "Elige qué quieres practicar y cuánto tiempo quieres dedicarle."));
        }
    }

    private void remove(LearningGoal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la meta de " + goal.type().toString().toLowerCase(Locale.ROOT) + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.initOwner(getScene().getWindow());
        Theme.applyCss(confirm.getDialogPane().getScene());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            run(() -> {
                repository.delete(userId, goal.id());
                return repository.load(userId);
            }, "Meta eliminada.");
        }
    }

    private void run(Callable<List<LearningGoal>> action, String message) {
        if (busy) return;
        busy = true;
        controls.setDisable(true);
        refresh.setDisable(true);
        WorkspaceUi.status(status, "Actualizando tus metas…", "");
        Task<List<LearningGoal>> task = new Task<>() {
            @Override protected List<LearningGoal> call() throws Exception { return action.call(); }
        };
        task.setOnSucceeded(event -> {
            busy = false;
            controls.setDisable(false);
            refresh.setDisable(false);
            show(task.getValue());
            WorkspaceUi.status(status, message, "status-success");
        });
        task.setOnFailed(event -> {
            busy = false;
            controls.setDisable(false);
            refresh.setDisable(false);
            String messageText = task.getException() instanceof IllegalArgumentException
                    ? task.getException().getMessage()
                    : "No pudimos consultar tus metas. Comprueba la conexión y pulsa Actualizar. Si continúa, contacta con el administrador.";
            WorkspaceUi.status(status, messageText, "status-error");
        });
        Thread thread = new Thread(task, "learning-goals");
        thread.setDaemon(true);
        thread.start();
    }
}
