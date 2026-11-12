package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.data.local.LabDraftStore;
import com.mycompany.levelup_code.domain.model.User;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class CodingLabPane extends VBox {
    private final LabDraftStore store = new LabDraftStore();
    private final String identity;
    private final ComboBox<String> language = new ComboBox<>();
    private final TextArea code = new TextArea();
    private final TextArea input = new TextArea();
    private final TextArea output = new TextArea();
    private final Label status = new Label();
    private final VBox controls = new VBox(10);
    private String activeLanguage = "Java";
    private boolean dirty;
    private boolean loading;

    public CodingLabPane(User user, boolean oracle) {
        super(12);
        identity = (oracle ? "oracle:" : "demo:") + user.getEmail();
        Label title = new Label("Laboratorio de código");
        title.getStyleClass().add("page-title");
        status.setWrapText(true);
        language.getItems().setAll("Java", "Python", "C++");
        language.setValue(activeLanguage);
        code.getStyleClass().add("code-editor");
        code.setPrefRowCount(14);
        code.setAccessibleText("Código del experimento");
        input.setPrefRowCount(2);
        input.setPromptText("Entrada personalizada");
        output.setPrefRowCount(4);
        output.setEditable(false);
        output.setAccessibleText("Resultado del experimento");
        code.textProperty().addListener((observable, old, value) -> { if (!loading) dirty = true; });
        input.textProperty().addListener((observable, old, value) -> { if (!loading) dirty = true; });
        language.valueProperty().addListener((observable, old, value) -> {
            if (value.equals(activeLanguage)) return;
            if (!confirmReplacement()) { language.setValue(activeLanguage); return; }
            activeLanguage = value;
            restore();
        });
        Button run = new Button("Ejecutar");
        run.getStyleClass().add("btn-primary");
        run.setOnAction(event -> execute());
        Button save = new Button("Guardar en este equipo");
        save.setOnAction(event -> {
            String selected = activeLanguage;
            var experiment = new LabDraftStore.Experiment(code.getText(), input.getText());
            background(() -> { store.save(identity, selected, experiment); return true; }, ignored -> {
                dirty = false;
                status.setText("Experimento guardado en este equipo.");
            });
        });
        Button template = new Button("Restablecer plantilla");
        template.setOnAction(event -> { if (confirmReplacement()) replace(template(), ""); });
        controls.getChildren().addAll(new FlowPane(10, 10, language, run, save, template), code,
                new Label("Entrada"), input, new Label("Resultado"), output);
        getChildren().addAll(title, status, controls);
        restore();
    }

    private void restore() {
        background(() -> store.load(identity, activeLanguage), saved -> {
            replace(saved.map(LabDraftStore.Experiment::code).orElseGet(this::template),
                    saved.map(LabDraftStore.Experiment::input).orElse(""));
            status.setText(saved.isPresent() ? "Experimento recuperado. Guarda para conservar nuevos cambios." : "Escribe tu código y guarda para conservarlo en este equipo.");
        });
    }

    private void replace(String source, String data) {
        loading = true;
        code.setText(source);
        input.setText(data);
        output.clear();
        loading = false;
        dirty = false;
    }

    private boolean confirmReplacement() {
        if (!dirty) return true;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hay cambios sin guardar. ¿Reemplazar el experimento?", ButtonType.OK, ButtonType.CANCEL);
        confirm.initOwner(getScene().getWindow());
        return confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private String template() {
        return switch (activeLanguage) {
            case "Python" -> "def solve(input):\n    return input.upper()\n";
            case "C++" -> "#include <string>\nstd::string solve(std::string input) {\n    return input;\n}\n";
            default -> "public class Experiment {\n    public static String solve(String input) {\n        return input.toUpperCase();\n    }\n}\n";
        };
    }

    private void execute() {
        String selected = activeLanguage;
        String source = code.getText();
        String data = input.getText();
        if (source.isBlank()) { status.setText("Escribe código antes de ejecutar."); return; }
        boolean real = AppContext.getInstance().getCodeRunner().supportsRealIsolatedExecution(selected);
        background(() -> AppContext.getInstance().getCodeRunner().run(selected, source, data, 3), result -> {
            output.setText(result);
            status.setText(real ? "Ejecución Java completada. Esta práctica no otorga XP."
                    : "Resultado simulado de demostración para " + selected + ". Esta práctica no otorga XP.");
        });
    }

    private <T> void background(Callable<T> action, Consumer<T> success) {
        controls.setDisable(true);
        status.setText("Procesando…");
        Task<T> task = new Task<>() { protected T call() throws Exception { return action.call(); } };
        task.setOnSucceeded(event -> { controls.setDisable(false); success.accept(task.getValue()); });
        task.setOnFailed(event -> {
            controls.setDisable(false);
            status.setText("No se completó la operación. " + task.getException().getMessage());
        });
        Thread worker = new Thread(task, "coding-lab");
        worker.setDaemon(true);
        worker.start();
    }
}
