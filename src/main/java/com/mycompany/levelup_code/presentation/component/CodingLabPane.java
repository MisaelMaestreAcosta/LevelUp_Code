package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.data.local.LabDraftStore;
import com.mycompany.levelup_code.domain.model.EditorPreferences;
import com.mycompany.levelup_code.domain.model.User;
import java.nio.file.Path;
import java.util.Optional;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

public final class CodingLabPane extends VBox {
    private final LabDraftStore store = new LabDraftStore();
    private final String identity;
    private final ComboBox<String> language = new ComboBox<>();
    private final CodeEditor code = new CodeEditor();
    private final TextArea input = new TextArea();
    private final TextArea output = new TextArea();
    private final Label status = new Label();
    private final Label savedState = WorkspaceUi.label("Guardado en este equipo", "muted-text");
    private final Label executionMode = WorkspaceUi.label("", "muted-text");
    private final VBox controls = new VBox(16);
    private final GridPane console = new GridPane();
    private String activeLanguage;
    private boolean dirty;
    private boolean loading;
    private boolean busy;
    private boolean wide;
    private final LabDraftStore recovery = new LabDraftStore(
            Path.of(System.getProperty("user.home"), ".levelup-code", "laboratory-recovery"));
    private final PauseTransition draftDelay = new PauseTransition(Duration.millis(900));
    private final Object draftLock = new Object();
    private volatile long draftRevision;

    public CodingLabPane(User user, boolean oracle) {
        getStyleClass().add("learning-workspace");
        identity = (oracle ? "oracle:" : "demo:") + user.getEmail();
        draftDelay.setOnFinished(event -> saveRecovery());
        EditorPreferences preferences = EditorPreferences.forUser(user.getId());
        activeLanguage = switch (preferences.language()) {
            case "PYTHON" -> "Python";
            case "CPP" -> "C++";
            default -> "Java";
        };
        language.getItems().setAll("Java", "Python", "C++");
        language.setValue(activeLanguage);
        language.setPrefWidth(135);
        language.setAccessibleText("Lenguaje del laboratorio");
        code.setStyle("-fx-font-size: " + preferences.fontSize() + "px;");
        code.setPrefHeight(280);
        code.setMinHeight(210);
        code.setAccessibleText("Código del experimento");
        code.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && !event.isShiftDown() && !event.isControlDown() && !event.isAltDown()) {
                code.replaceSelection(" ".repeat(EditorPreferences.forUser(user.getId()).indentSpaces()));
                event.consume();
            }
        });
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (!event.isControlDown() || event.isAltDown() || busy) return;
            if (event.getCode() == KeyCode.ENTER) {
                execute();
                event.consume();
            } else if (event.getCode() == KeyCode.S) {
                save();
                event.consume();
            }
        });
        input.setPrefRowCount(4);
        input.setPromptText("Datos que recibirá solve(input)");
        input.setAccessibleText("Entrada del experimento");
        input.getStyleClass().add("console-output");
        output.setPrefRowCount(4);
        output.setEditable(false);
        output.setPromptText("Ejecuta tu código para ver el resultado.");
        output.setAccessibleText("Resultado del experimento");
        output.getStyleClass().add("console-output");
        code.textProperty().addListener((observable, old, value) -> { if (!loading) setDirty(true); });
        input.textProperty().addListener((observable, old, value) -> { if (!loading) setDirty(true); });
        language.valueProperty().addListener((observable, old, value) -> {
            if (loading || value == null || value.equals(activeLanguage)) return;
            loading = true;
            language.setValue(activeLanguage);
            loading = false;
            requestLeave(() -> {
                if (!removeRecovery()) return;
                activeLanguage = value;
                loading = true;
                language.setValue(value);
                loading = false;
                restore();
            });
        });

        Button run = WorkspaceUi.button("Ejecutar", "btn-primary", Theme.IconKind.PLAY);
        run.setTooltip(new Tooltip("Ejecutar código · Ctrl+Enter"));
        run.setOnAction(event -> execute());
        Button save = WorkspaceUi.button("Guardar", "btn-secondary", Theme.IconKind.CHECK);
        save.setTooltip(new Tooltip("Guardar en este equipo · Ctrl+S"));
        save.setOnAction(event -> save());
        Button template = WorkspaceUi.button("Restablecer plantilla", "btn-secondary", null);
        template.setOnAction(event -> {
            requestLeave(() -> {
                if (!removeRecovery()) return;
                replace(template(), "");
                setDirty(true);
                WorkspaceUi.status(status, "Plantilla restablecida. Guarda para conservarla en este equipo.", "");
            });
        });
        FlowPane toolbar = new FlowPane(10, 10, language, run, save, template);
        toolbar.getStyleClass().add("workspace-toolbar");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox editorHeader = new HBox(10, Theme.createIcon(Theme.IconKind.TERMINAL, 18, Theme.HEX_INDIGO),
                WorkspaceUi.label("Editor", "editor-heading"), spacer, savedState);
        editorHeader.setAlignment(Pos.CENTER_LEFT);
        VBox editor = new VBox(12, toolbar, editorHeader, code, executionMode,
                WorkspaceUi.label("Ctrl+Enter para ejecutar · Ctrl+S para guardar", "muted-text"));
        editor.getStyleClass().add("surface-card");

        VBox inputCard = new VBox(10, WorkspaceUi.field("Entrada", input));
        VBox outputCard = new VBox(10, WorkspaceUi.field("Resultado", output));
        inputCard.getStyleClass().add("surface-card");
        outputCard.getStyleClass().add("surface-card");
        inputCard.setMinWidth(0);
        outputCard.setMinWidth(0);
        console.setHgap(16);
        console.setVgap(16);
        console.add(inputCard, 0, 0);
        console.add(outputCard, 0, 1);
        ColumnConstraints single = new ColumnConstraints();
        single.setPercentWidth(100);
        console.getColumnConstraints().add(single);
        widthProperty().addListener((observable, old, value) -> {
            boolean useColumns = value.doubleValue() >= 700;
            if (useColumns == wide) return;
            wide = useColumns;
            console.getColumnConstraints().clear();
            ColumnConstraints first = new ColumnConstraints();
            first.setPercentWidth(wide ? 45 : 100);
            console.getColumnConstraints().add(first);
            if (wide) {
                ColumnConstraints second = new ColumnConstraints();
                second.setPercentWidth(55);
                console.getColumnConstraints().add(second);
            }
            GridPane.setColumnIndex(outputCard, wide ? 1 : 0);
            GridPane.setRowIndex(outputCard, wide ? 0 : 1);
        });
        controls.getChildren().addAll(editor, console);
        getChildren().addAll(WorkspaceUi.header("Laboratorio de código",
                "Prueba tus ideas y conserva tus experimentos en este equipo."), status, WorkspaceUi.scroll(controls));
        restore();
    }

    private void setDirty(boolean value) {
        dirty = value;
        draftRevision++;
        if (value) draftDelay.playFromStart();
        else draftDelay.stop();
        savedState.setText(value ? "Cambios sin guardar" : "Sin cambios pendientes");
    }

    private void save() { save(() -> { }); }

    private void save(Runnable continuation) {
        if (busy) return;
        String selected = activeLanguage;
        LabDraftStore.Experiment experiment = new LabDraftStore.Experiment(code.getText(), input.getText());
        background(() -> {
            store.save(identity, selected, experiment);
            return true;
        }, ignored -> {
            setDirty(false);
            if (!removeRecovery()) return;
            WorkspaceUi.status(status, "Experimento guardado en este equipo.", "status-success");
            continuation.run();
        });
    }

    private void restore() {
        String selected = activeLanguage;
        code.setLanguage(selected);
        executionMode.setText("Java".equals(selected)
                ? "Java · Ejecución local · Método solve(String input) · Límite de 3 segundos"
                : selected + " · Ejecución simulada de demostración");
        background(() -> new RestoredExperiment(store.load(identity, selected), recovery.load(identity, selected)), restored -> {
            Optional<LabDraftStore.Experiment> saved = restored.recovery().or(() -> restored.saved());
            replace(saved.map(LabDraftStore.Experiment::code).orElseGet(this::template),
                    saved.map(LabDraftStore.Experiment::input).orElse(""));
            if (restored.recovery().isPresent()) {
                setDirty(true);
                WorkspaceUi.status(status, "Recuperamos tus cambios pendientes. Guarda cuando quieras conservar esta versión.", "");
            } else {
                WorkspaceUi.status(status, saved.isPresent()
                        ? "Experimento recuperado de este equipo."
                        : "Tu experimento se conservará cuando pulses Guardar.", "");
            }
        });
    }

    private record RestoredExperiment(Optional<LabDraftStore.Experiment> saved,
            Optional<LabDraftStore.Experiment> recovery) { }

    private void replace(String source, String data) {
        loading = true;
        code.setText(source);
        input.setText(data);
        output.clear();
        loading = false;
        setDirty(false);
    }

    private void requestLeave(Runnable continuation) { requestLeave(getScene().getWindow(), continuation); }

    void requestLeave(javafx.stage.Window owner, Runnable continuation) {
        if (busy) {
            UnsavedChangesDialog.showBusy(owner);
            return;
        }
        if (!dirty) { continuation.run(); return; }
        ButtonType choice = UnsavedChangesDialog.choose(owner, "¿Guardar el experimento de " + activeLanguage + " en este equipo?");
        if (choice == UnsavedChangesDialog.SAVE) save(continuation);
        else if (choice == UnsavedChangesDialog.DISCARD) continuation.run();
    }

    private void saveRecovery() {
        if (!dirty) return;
        String selected = activeLanguage;
        var experiment = new LabDraftStore.Experiment(code.getText(), input.getText());
        long revision = draftRevision;
        Thread writer = new Thread(() -> {
            try {
                synchronized (draftLock) {
                    if (revision != draftRevision) return;
                    recovery.save(identity, selected, experiment);
                }
            } catch (java.io.IOException ex) {
                javafx.application.Platform.runLater(() -> WorkspaceUi.status(status,
                        "No se pudo guardar la copia de recuperación. Guarda tu experimento.", "status-error"));
            }
        }, "laboratory-recovery");
        writer.setDaemon(true);
        writer.start();
    }

    boolean removeRecovery() {
        draftDelay.stop();
        draftRevision++;
        try {
            synchronized (draftLock) { recovery.remove(identity, activeLanguage); }
            return true;
        } catch (java.io.IOException ex) {
            WorkspaceUi.status(status, "No se pudo borrar la copia de recuperación. Vuelve a intentarlo.", "status-error");
            return false;
        }
    }

    private String template() {
        return switch (activeLanguage) {
            case "Python" -> "def solve(input):\n    return input.upper()\n";
            case "C++" -> "#include <string>\nstd::string solve(std::string input) {\n    return input;\n}\n";
            default -> "public class Experiment {\n    public static String solve(String input) {\n        return input.toUpperCase();\n    }\n}\n";
        };
    }

    private void execute() {
        if (busy) return;
        String selected = activeLanguage;
        String source = code.getText();
        String data = input.getText();
        if (source.isBlank()) {
            WorkspaceUi.status(status, "Escribe código antes de ejecutar.", "status-error");
            code.requestFocus();
            return;
        }
        boolean real = AppContext.getInstance().getCodeRunner().supportsRealIsolatedExecution(selected);
        background(() -> AppContext.getInstance().getCodeRunner().run(selected, source, data, 3), result -> {
            output.setText(result);
            WorkspaceUi.status(status, real
                    ? "Ejecución Java finalizada. La práctica del laboratorio no otorga XP."
                    : "Resultado simulado para " + selected + ". La práctica del laboratorio no otorga XP.", "");
        }, failure -> {
            output.setText(failure.getMessage() == null ? "La ejecución terminó con un error." : failure.getMessage());
            WorkspaceUi.status(status, "No se pudo ejecutar el código. Revisa el detalle en Resultado.", "status-error");
        });
    }

    private <T> void background(Callable<T> action, Consumer<T> success) {
        background(action, success, failure -> WorkspaceUi.status(status,
                "No se completó la operación. Vuelve a intentarlo. Tu código se conserva.", "status-error"));
    }

    private <T> void background(Callable<T> action, Consumer<T> success, Consumer<Throwable> failure) {
        if (busy) return;
        busy = true;
        controls.setDisable(true);
        WorkspaceUi.status(status, "Procesando…", "");
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return action.call(); }
        };
        task.setOnSucceeded(event -> {
            busy = false;
            controls.setDisable(false);
            success.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            busy = false;
            controls.setDisable(false);
            failure.accept(task.getException());
        });
        Thread worker = new Thread(task, "coding-lab");
        worker.setDaemon(true);
        worker.start();
    }
}
