package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository;
import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository.Note;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

public final class NotebookPane extends VBox {
    private final OracleNotebookRepository repository = new OracleNotebookRepository();
    private final ListView<Note> notes = new ListView<>();
    private final List<Note> savedNotes = new ArrayList<>();
    private final TextField search = new TextField();
    private final TextField title = new TextField();
    private final TextArea content = new TextArea();
    private final ComboBox<Challenge> related = new ComboBox<>();
    private final VBox favorites = new VBox(10);
    private final Label status = new Label();
    private final Label editorTitle = WorkspaceUi.label("Nueva nota", "card-title");
    private final Label noteCount = WorkspaceUi.label("Tus notas", "card-title");
    private final GridPane controls = new GridPane();
    private final Button refresh = WorkspaceUi.button("Actualizar", "btn-secondary", Theme.IconKind.REFRESH);
    private final Button remove = WorkspaceUi.button("Eliminar nota", "btn-danger", null);
    private final Button favorite = WorkspaceUi.button("Guardar en favoritos", "btn-ghost", Theme.IconKind.STAR);
    private final int userId;
    private final User user;
    private final Runnable callback;
    private final List<Challenge> challenges;
    private Note editingNote;
    private boolean busy;
    private boolean dirty;
    private boolean changingSelection;
    private boolean loadingEditor;
    private boolean wide;

    public NotebookPane(User user, boolean oracle, List<Challenge> challenges, Runnable callback) {
        this.user = user;
        this.userId = user.getId();
        this.challenges = challenges;
        this.callback = callback;
        getStyleClass().add("learning-workspace");
        title.setPromptText("Por ejemplo, cómo funciona un JOIN");
        title.setTextFormatter(new TextFormatter<String>(
                change -> change.getControlNewText().length() <= 100 ? change : null));
        content.setPromptText("Escribe tus apuntes o pega un fragmento de código…");
        content.setPrefRowCount(12);
        content.setWrapText(true);
        content.setAccessibleText("Contenido de la nota");
        related.getItems().addAll(challenges);
        related.setPromptText("Seleccionar reto (opcional)");
        related.setMinWidth(0);
        related.setConverter(new StringConverter<>() {
            @Override public String toString(Challenge challenge) {
                return challenge == null ? "" : challenge.getTitle();
            }
            @Override public Challenge fromString(String value) { return null; }
        });
        title.textProperty().addListener((observable, old, value) -> markDirty());
        content.textProperty().addListener((observable, old, value) -> markDirty());
        related.valueProperty().addListener((observable, old, value) -> {
            favorite.setDisable(value == null);
            markDirty();
        });
        favorite.setDisable(true);
        remove.setDisable(true);
        notes.setPrefHeight(170);
        notes.setMinHeight(150);
        notes.getStyleClass().add("notes-list");
        notes.setPlaceholder(WorkspaceUi.label("Tus notas aparecerán aquí.", "muted-text"));
        notes.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                setText(null);
                setGraphic(null);
                if (empty || note == null) return;
                String preview = note.content().replaceAll("\\s+", " ").strip();
                if (preview.length() > 75) preview = preview.substring(0, 75) + "…";
                VBox text = new VBox(5, WorkspaceUi.label(note.title(), "card-title"),
                        WorkspaceUi.label(preview, "muted-text"));
                text.setMaxWidth(Double.MAX_VALUE);
                text.prefWidthProperty().bind(widthProperty().subtract(28));
                setGraphic(text);
            }
        });
        notes.getSelectionModel().selectedItemProperty().addListener((observable, old, note) -> {
            if (changingSelection || note == null) return;
            if (!confirmReplacement()) {
                changingSelection = true;
                notes.getSelectionModel().select(old);
                changingSelection = false;
                return;
            }
            edit(note);
        });
        search.setPromptText("Buscar en tus notas");
        search.setAccessibleText("Buscar notas por título o contenido");
        search.textProperty().addListener((observable, old, value) -> filterNotes());

        Button create = WorkspaceUi.button("Nueva nota", "btn-secondary", Theme.IconKind.NOTE);
        create.setOnAction(event -> { if (confirmReplacement()) clear(); });
        HBox libraryHeader = new HBox(10, noteCount, create);
        libraryHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(noteCount, Priority.ALWAYS);
        noteCount.setMaxWidth(Double.MAX_VALUE);
        VBox library = new VBox(12, libraryHeader, search, notes);
        library.getStyleClass().add("surface-card");
        VBox favoriteCard = new VBox(12,
                WorkspaceUi.label("Retos favoritos", "card-title"), favorites);
        favoriteCard.getStyleClass().add("surface-card");
        favorites.getChildren().add(WorkspaceUi.label("Asocia un reto a tu nota para guardarlo aquí.", "muted-text"));

        Button save = WorkspaceUi.button("Guardar nota", "btn-primary", Theme.IconKind.CHECK);
        save.setOnAction(event -> save());
        remove.setOnAction(event -> remove());
        favorite.setOnAction(event -> {
            Challenge challenge = related.getValue();
            if (challenge == null) return;
            run(() -> {
                repository.favorite(userId, challenge.getId(), true);
                return repository.load(userId);
            }, data -> {
                show(data);
                WorkspaceUi.status(status, "Reto añadido a tus favoritos.", "status-success");
            });
        });
        Button unlink = WorkspaceUi.button("Quitar asociación", "btn-secondary", null);
        unlink.disableProperty().bind(related.valueProperty().isNull());
        unlink.setOnAction(event -> related.setValue(null));
        FlowPane relatedActions = new FlowPane(8, 8, favorite, unlink);
        FlowPane actions = new FlowPane(10, 10, save, remove);
        VBox editor = new VBox(14, editorTitle,
                WorkspaceUi.field("Título", title), WorkspaceUi.field("Tus apuntes", content),
                WorkspaceUi.field("Reto relacionado", related), relatedActions, new Separator(), actions);
        editor.getStyleClass().add("surface-card");
        editor.setMinWidth(0);
        library.setMinWidth(0);
        favoriteCard.setMinWidth(0);
        GridPane.setValignment(library, javafx.geometry.VPos.TOP);
        GridPane.setValignment(favoriteCard, javafx.geometry.VPos.TOP);
        GridPane.setValignment(editor, javafx.geometry.VPos.TOP);
        GridPane.setFillHeight(library, false);
        GridPane.setFillHeight(favoriteCard, false);
        controls.setHgap(16);
        controls.setVgap(16);
        controls.add(library, 0, 0);
        controls.add(editor, 0, 1);
        controls.add(favoriteCard, 0, 2);
        ColumnConstraints single = new ColumnConstraints();
        single.setPercentWidth(100);
        controls.getColumnConstraints().add(single);
        widthProperty().addListener((observable, old, value) -> {
            boolean useColumns = value.doubleValue() >= 780;
            if (useColumns == wide) return;
            wide = useColumns;
            notes.setPrefHeight(wide ? 270 : 170);
            controls.getColumnConstraints().clear();
            ColumnConstraints left = new ColumnConstraints();
            left.setPercentWidth(wide ? 36 : 100);
            controls.getColumnConstraints().add(left);
            if (wide) {
                ColumnConstraints right = new ColumnConstraints();
                right.setPercentWidth(64);
                controls.getColumnConstraints().add(right);
            }
            GridPane.setColumnIndex(editor, wide ? 1 : 0);
            GridPane.setRowIndex(editor, wide ? 0 : 1);
            GridPane.setRowSpan(editor, wide ? 2 : 1);
            GridPane.setRowIndex(favoriteCard, wide ? 1 : 2);
        });
        refresh.setOnAction(event -> run(() -> repository.load(userId), this::show));
        getChildren().addAll(WorkspaceUi.header("Mi cuaderno",
                "Conserva tus apuntes y vuelve a los retos que quieres practicar.", refresh),
                status, WorkspaceUi.scroll(controls));
        if (oracle) {
            run(() -> repository.load(userId), this::show);
        } else {
            controls.setDisable(true);
            refresh.setDisable(true);
            WorkspaceUi.status(status, "Inicia sesión con tu cuenta para guardar tus notas y favoritos.", "");
        }
    }

    private void markDirty() {
        if (!loadingEditor) dirty = true;
    }

    private boolean confirmReplacement() {
        if (!dirty) return true;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Tienes cambios sin guardar. ¿Descartarlos y abrir otra nota?", ButtonType.OK, ButtonType.CANCEL);
        confirm.initOwner(getScene().getWindow());
        Theme.applyCss(confirm.getDialogPane().getScene());
        return confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void edit(Note note) {
        loadingEditor = true;
        editingNote = note;
        title.setText(note.title());
        content.setText(note.content());
        related.setValue(challenges.stream()
                .filter(challenge -> Objects.equals(challenge.getId(), note.challengeId())).findFirst().orElse(null));
        editorTitle.setText("Editar nota");
        remove.setDisable(false);
        loadingEditor = false;
        dirty = false;
    }

    private void clear() {
        changingSelection = true;
        notes.getSelectionModel().clearSelection();
        changingSelection = false;
        loadingEditor = true;
        editingNote = null;
        title.clear();
        content.clear();
        related.setValue(null);
        editorTitle.setText("Nueva nota");
        remove.setDisable(true);
        loadingEditor = false;
        dirty = false;
        title.requestFocus();
    }

    private void save() {
        Note note = new Note(editingNote == null ? 0 : editingNote.id(),
                related.getValue() == null ? null : related.getValue().getId(), title.getText(), content.getText());
        run(() -> {
            repository.save(userId, note);
            return repository.load(userId);
        }, data -> {
            show(data);
            clear();
            WorkspaceUi.status(status, "Nota guardada en tu cuenta.", "status-success");
        });
    }

    private void remove() {
        if (editingNote == null) return;
        Note selected = editingNote;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la nota «" + selected.title() + "»?", ButtonType.OK, ButtonType.CANCEL);
        confirm.initOwner(getScene().getWindow());
        Theme.applyCss(confirm.getDialogPane().getScene());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            run(() -> {
                repository.delete(userId, selected.id());
                return repository.load(userId);
            }, data -> {
                show(data);
                clear();
                WorkspaceUi.status(status, "Nota eliminada.", "status-success");
            });
        }
    }

    private void filterNotes() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        changingSelection = true;
        notes.getItems().setAll(savedNotes.stream().filter(note ->
                (note.title() + " " + note.content()).toLowerCase(Locale.ROOT).contains(query)).toList());
        if (editingNote != null) {
            notes.getItems().stream().filter(note -> note.id() == editingNote.id())
                    .findFirst().ifPresent(note -> notes.getSelectionModel().select(note));
        }
        changingSelection = false;
        notes.setPlaceholder(WorkspaceUi.label(savedNotes.isEmpty()
                ? "Todavía no tienes notas.\nCrea una para empezar." : "No hay notas que coincidan.", "muted-text"));
    }

    private void show(OracleNotebookRepository.Notebook data) {
        savedNotes.clear();
        savedNotes.addAll(data.notes());
        noteCount.setText("Tus notas · " + savedNotes.size());
        filterNotes();
        favorites.getChildren().clear();
        for (Challenge challenge : challenges) {
            if (!data.favorites().contains(challenge.getId())) continue;
            Label name = WorkspaceUi.label(challenge.getTitle(), "body-text");
            Button open = WorkspaceUi.button("Practicar", "btn-ghost", Theme.IconKind.PLAY);
            open.setAccessibleText("Practicar " + challenge.getTitle());
            open.setOnAction(event -> ChallengeFrame.open(user, challenge, callback));
            Button removeFavorite = WorkspaceUi.button("Quitar", "btn-secondary", null);
            removeFavorite.setAccessibleText("Quitar " + challenge.getTitle() + " de favoritos");
            removeFavorite.setOnAction(event -> run(() -> {
                repository.favorite(userId, challenge.getId(), false);
                return repository.load(userId);
            }, this::show));
            VBox row = new VBox(8, name, new FlowPane(8, 8, open, removeFavorite));
            row.getStyleClass().add("surface-subtle");
            favorites.getChildren().add(row);
        }
        if (favorites.getChildren().isEmpty()) {
            favorites.getChildren().add(WorkspaceUi.label("Asocia un reto a tu nota y añádelo a favoritos para encontrarlo aquí.", "muted-text"));
        }
        WorkspaceUi.status(status, "", "");
    }

    private <T> void run(Callable<T> action, Consumer<T> success) {
        if (busy) return;
        busy = true;
        controls.setDisable(true);
        refresh.setDisable(true);
        WorkspaceUi.status(status, "Actualizando tu cuaderno…", "");
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return action.call(); }
        };
        task.setOnSucceeded(event -> {
            busy = false;
            controls.setDisable(false);
            refresh.setDisable(false);
            success.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            busy = false;
            controls.setDisable(false);
            refresh.setDisable(false);
            String message = task.getException() instanceof IllegalArgumentException
                    ? task.getException().getMessage()
                    : "No pudimos acceder al cuaderno. Comprueba la conexión y pulsa Actualizar. Tus campos se conservan. Si continúa, contacta con el administrador.";
            WorkspaceUi.status(status, message, "status-error");
        });
        Thread thread = new Thread(task, "student-notebook");
        thread.setDaemon(true);
        thread.start();
    }
}
