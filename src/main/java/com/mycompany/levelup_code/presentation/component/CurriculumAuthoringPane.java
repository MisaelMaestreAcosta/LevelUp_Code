package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.connection.DatabaseDiagnostics;
import com.mycompany.levelup_code.data.oracle.OracleCurriculumRepository;
import com.mycompany.levelup_code.data.oracle.OracleCurriculumRepository.*;
import com.mycompany.levelup_code.domain.model.User;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** The same form creates or updates curriculum content and previews it before saving. */
final class CurriculumAuthoringPane extends VBox {
    private final OracleCurriculumRepository repository = new OracleCurriculumRepository();
    private final int administratorId;
    private final Runnable savedCallback;
    private final ComboBox<Course> course = new ComboBox<>();
    private final ComboBox<Kind> kind = new ComboBox<>();
    private final ComboBox<Entry> parentModule = new ComboBox<>();
    private final ComboBox<Entry> entries = new ComboBox<>();
    private final TextField title = new TextField();
    private final TextArea content = new TextArea();
    private final Spinner<Integer> position = new Spinner<>(1, 100000, 1);
    private final Spinner<Integer> xp = new Spinner<>(0, 10000, 50);
    private final Label status = WorkspaceUi.label("", "body-text");
    private final Label contentLabel = WorkspaceUi.label("Descripción", "body-text");
    private final VBox moduleField = WorkspaceUi.field("Módulo", parentModule);
    private final VBox xpField = WorkspaceUi.field("Recompensa XP", xp);
    private final VBox form = new VBox(12);
    private Entry original;
    private List<String> baseline = List.of();
    private boolean changing;
    private boolean busy;

    CurriculumAuthoringPane(User administrator, Runnable savedCallback) {
        super(14);
        this.administratorId = administrator.getId();
        this.savedCallback = savedCallback;
        getStyleClass().add("learning-workspace");
        course.setPromptText("Selecciona un curso");
        parentModule.setPromptText("Selecciona un módulo");
        entries.setPromptText("Selecciona contenido para editar");
        for (ComboBox<?> selector : List.of(course, parentModule, entries, kind)) selector.setMaxWidth(Double.MAX_VALUE);
        kind.getItems().setAll(Kind.MODULE, Kind.LESSON);
        kind.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Kind value) { return value == Kind.LESSON ? "Lecciones" : "Módulos"; }
            public Kind fromString(String value) { return null; }
        });
        kind.setValue(Kind.MODULE);
        content.setWrapText(true); content.setPrefRowCount(12);
        title.setPromptText("Título del contenido");
        Button fresh = WorkspaceUi.button("Crear nuevo", "btn-secondary", null);
        fresh.setOnAction(event -> requestLeave(getScene().getWindow(), this::newEntry));
        Button save = WorkspaceUi.button("Guardar contenido", "btn-primary", Theme.IconKind.CHECK);
        save.setOnAction(event -> save(() -> { }));
        Button preview = WorkspaceUi.button("Vista previa", "btn-secondary", Theme.IconKind.BOOK);
        preview.setOnAction(event -> preview());
        Button refresh = WorkspaceUi.button("Actualizar lista", "btn-secondary", Theme.IconKind.REFRESH);
        refresh.setOnAction(event -> requestLeave(getScene().getWindow(), () -> loadModules(parentModule.getValue() == null ? 0 : parentModule.getValue().id())));
        form.getChildren().addAll(WorkspaceUi.field("Curso", course), WorkspaceUi.field("Contenido", kind), moduleField,
                entries, new FlowPane(10, 8, fresh, refresh), WorkspaceUi.field("Título", title),
                new VBox(6, contentLabel, content), new FlowPane(12, 8, WorkspaceUi.field("Posición en la ruta", position), xpField),
                new FlowPane(10, 8, save, preview));
        form.getStyleClass().add("surface-card");
        getChildren().addAll(WorkspaceUi.header("Módulos y lecciones", "Organiza la ruta y prepara el contenido de tus cursos."), status, form);
        course.valueProperty().addListener((observable, old, value) -> {
            if (changing || value == old) return;
            changing = true; course.setValue(old); changing = false;
            requestLeave(getScene().getWindow(), () -> {
                changing = true; course.setValue(value); changing = false; loadModules(0);
            });
        });
        kind.valueProperty().addListener((observable, old, value) -> {
            if (changing || value == old) return;
            changing = true; kind.setValue(old); changing = false;
            requestLeave(getScene().getWindow(), () -> {
                changing = true; kind.setValue(value); changing = false; updateKind(); loadEntries(0);
            });
        });
        parentModule.valueProperty().addListener((observable, old, value) -> {
            if (changing || kind.getValue() != Kind.LESSON || value == old) return;
            changing = true; parentModule.setValue(old); changing = false;
            requestLeave(getScene().getWindow(), () -> {
                changing = true; parentModule.setValue(value); changing = false; loadEntries(0);
            });
        });
        entries.valueProperty().addListener((observable, old, value) -> {
            if (changing || value == null || value == old) return;
            changing = true; entries.setValue(old); changing = false;
            requestLeave(getScene().getWindow(), () -> show(value));
        });
        baseline = fields();
        updateKind();
        run(() -> repository.courses(administratorId), courses -> {
            changing = true; course.getItems().setAll(courses); course.getSelectionModel().selectFirst(); changing = false;
            loadModules(0);
        });
    }

    private void updateKind() {
        boolean lesson = kind.getValue() == Kind.LESSON;
        moduleField.setVisible(lesson); moduleField.setManaged(lesson);
        xpField.setVisible(lesson); xpField.setManaged(lesson);
        contentLabel.setText(lesson ? "Contenido de la lección" : "Descripción del módulo");
        title.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= (lesson ? 100 : 80) ? change : null));
    }

    private int parentId() {
        return kind.getValue() == Kind.MODULE ? course.getValue() == null ? 0 : course.getValue().id()
                : parentModule.getValue() == null ? 0 : parentModule.getValue().id();
    }

    private void loadModules(int selectedId) {
        if (course.getValue() == null) { clearEntries(); return; }
        int courseId = course.getValue().id();
        run(() -> repository.list(administratorId, Kind.MODULE, courseId), modules -> {
            changing = true;
            parentModule.getItems().setAll(modules);
            parentModule.setValue(modules.stream().filter(module -> module.id() == selectedId).findFirst().orElse(modules.isEmpty() ? null : modules.getFirst()));
            changing = false;
            loadEntries(0);
        });
    }

    private void loadEntries(int selectedId) {
        Kind selectedKind = kind.getValue();
        int parentId = parentId();
        if (parentId == 0) { clearEntries(); return; }
        run(() -> repository.list(administratorId, selectedKind, parentId), values -> {
            changing = true; entries.getItems().setAll(values); changing = false;
            Entry selected = values.stream().filter(entry -> entry.id() == selectedId).findFirst().orElse(values.isEmpty() ? null : values.getFirst());
            if (selected == null) newEntry(); else show(selected);
        });
    }

    private void clearEntries() {
        changing = true; entries.getItems().clear(); parentModule.getItems().clear(); changing = false; newEntry();
    }

    private void show(Entry entry) {
        original = entry;
        changing = true; entries.setValue(entry); changing = false;
        title.setText(entry.title()); content.setText(entry.content());
        position.getValueFactory().setValue(entry.order()); xp.getValueFactory().setValue(entry.xp());
        baseline = fields();
    }

    private void newEntry() {
        original = null;
        changing = true; entries.setValue(null); changing = false;
        title.clear(); content.clear();
        position.getValueFactory().setValue(entries.getItems().size() + 1);
        xp.getValueFactory().setValue(50);
        baseline = fields();
    }

    private List<String> fields() {
        return List.of(title.getText(), content.getText(), position.getValue().toString(), xp.getValue().toString());
    }

    void requestLeave(javafx.stage.Window owner, Runnable continuation) {
        if (busy) { UnsavedChangesDialog.showBusy(owner); return; }
        if (baseline.equals(fields())) { continuation.run(); return; }
        ButtonType choice = UnsavedChangesDialog.choose(owner, "¿Guardar los cambios del contenido del curso?");
        if (choice == UnsavedChangesDialog.SAVE) save(continuation);
        else if (choice == UnsavedChangesDialog.DISCARD) continuation.run();
    }

    private void save(Runnable continuation) {
        Kind selectedKind = kind.getValue();
        Entry draft = new Entry(original == null ? 0 : original.id(), parentId(), title.getText(), content.getText(), position.getValue(),
                selectedKind == Kind.LESSON ? xp.getValue() : 0);
        Entry expected = original;
        try { OracleCurriculumRepository.validate(selectedKind, draft); }
        catch (IllegalArgumentException ex) { status.setText(ex.getMessage()); return; }
        run(() -> repository.save(administratorId, selectedKind, draft, expected), saved -> {
            // Record the committed identity before attempting any later refresh.
            original = saved; baseline = fields();
            var reordered = new java.util.ArrayList<>(entries.getItems());
            reordered.removeIf(entry -> entry.id() == saved.id());
            reordered.add(Math.min(saved.order() - 1, reordered.size()), saved);
            for (int i = 0; i < reordered.size(); i++) {
                Entry value = reordered.get(i);
                reordered.set(i, new Entry(value.id(), value.parentId(), value.title(), value.content(), i + 1, value.xp()));
            }
            changing = true;
            entries.getItems().setAll(reordered);
            if (selectedKind == Kind.MODULE) { parentModule.getItems().setAll(reordered); parentModule.setValue(saved); }
            changing = false;
            show(saved);
            status.setText("Contenido guardado. La ruta se actualizará en Mis cursos.");
            savedCallback.run();
            continuation.run();
        });
    }

    private void preview() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Vista previa del contenido");
        Label text = WorkspaceUi.label(content.getText(), "body-text");
        text.setWrapText(true); text.setMaxWidth(650);
        VBox page = new VBox(16, WorkspaceUi.label(title.getText(), "page-title"), text);
        page.getStyleClass().add("surface-card");
        dialog.getDialogPane().setContent(WorkspaceUi.scroll(page));
        dialog.getDialogPane().setPrefSize(700, 600);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true); Theme.applyCss(dialog.getDialogPane().getScene()); dialog.show();
    }

    private <T> void run(Callable<T> operation, Consumer<T> success) {
        if (busy) return;
        busy = true; form.setDisable(true); status.setText("Actualizando el contenido…");
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception {
                try { return operation.call(); }
                catch (java.sql.SQLException ex) { updateMessage(DatabaseDiagnostics.inspect().message()); throw ex; }
            }
        };
        task.setOnSucceeded(event -> { busy = false; form.setDisable(false); status.setText(""); success.accept(task.getValue()); });
        task.setOnFailed(event -> {
            busy = false; form.setDisable(false);
            Throwable error = task.getException();
            status.setText(error instanceof IllegalArgumentException || error instanceof IllegalStateException || error instanceof SecurityException
                    ? error.getMessage() : "No se completó la operación. " + task.getMessage());
        });
        Thread worker = new Thread(task, "curriculum-authoring"); worker.setDaemon(true); worker.start();
    }
}
