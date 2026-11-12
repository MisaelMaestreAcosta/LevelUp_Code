package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository;
import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository.Note;
import com.mycompany.levelup_code.domain.model.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

public final class NotebookPane extends VBox {
    private final OracleNotebookRepository repository = new OracleNotebookRepository();
    private final ListView<Note> notes = new ListView<>();
    private final TextField title = new TextField();
    private final TextArea content = new TextArea();
    private final ComboBox<Challenge> related = new ComboBox<>();
    private final VBox favorites = new VBox(8);
    private final Label status = new Label();
    private final VBox controls = new VBox(10);
    private final int userId;
    private final User user;
    private final Runnable callback;
    private final List<Challenge> challenges;
    private boolean busy;

    public NotebookPane(User user, boolean oracle, List<Challenge> challenges, Runnable callback) {
        super(12);
        this.user=user; this.userId=user.getId(); this.challenges=challenges; this.callback=callback;
        Label heading = new Label("Mi cuaderno"); heading.getStyleClass().add("page-title");
        status.setWrapText(true);
        related.getItems().addAll(challenges);
        related.setPromptText("Reto asociado (opcional)");
        related.setMaxWidth(Double.MAX_VALUE);
        related.setConverter(new StringConverter<>() {
            public String toString(Challenge c) { return c==null ? "" : c.getTitle(); }
            public Challenge fromString(String value) { return null; }
        });
        title.setPromptText("Título de la nota"); title.setAccessibleText("Título de la nota");
        content.setPromptText("Escribe tus apuntes o pega un fragmento de código…");
        content.setAccessibleText("Contenido de la nota"); content.setPrefRowCount(6);
        notes.setPrefHeight(130);
        notes.getSelectionModel().selectedItemProperty().addListener((o, old, note) -> {
            if (note==null) return;
            title.setText(note.title()); content.setText(note.content());
            related.setValue(challenges.stream().filter(c -> Objects.equals(c.getId(),note.challengeId())).findFirst().orElse(null));
        });
        Button create = new Button("Nueva nota");
        create.setOnAction(e -> clear());
        Button save = new Button("Guardar nota"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            Note selected=notes.getSelectionModel().getSelectedItem();
            Note note=new Note(selected==null?0:selected.id(),related.getValue()==null?null:related.getValue().getId(),title.getText(),content.getText());
            run(() -> { repository.save(userId,note); return repository.load(userId); }, data -> { show(data); clear(); status.setText("Nota guardada."); });
        });
        Button remove=new Button("Eliminar nota");
        remove.setOnAction(e -> {
            Note selected=notes.getSelectionModel().getSelectedItem();
            if (selected==null) return;
            Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"¿Eliminar esta nota?",ButtonType.OK,ButtonType.CANCEL);
            confirm.initOwner(getScene().getWindow());
            if (confirm.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK) return;
            run(() -> { repository.delete(userId,selected.id()); return repository.load(userId); }, data -> { show(data); clear(); });
        });
        Button favorite=new Button("Guardar reto en favoritos");
        favorite.setOnAction(e -> {
            Challenge challenge=related.getValue();
            if (challenge==null) { status.setText("Selecciona un reto para guardarlo."); return; }
            run(() -> { repository.favorite(userId,challenge.getId(),true); return repository.load(userId); }, this::show);
        });
        Button unlink=new Button("Sin reto asociado"); unlink.setOnAction(e -> related.setValue(null));
        Button refresh=new Button("Actualizar cuaderno");
        refresh.setOnAction(e -> run(() -> repository.load(userId),this::show));
        controls.getChildren().addAll(notes,title,content,related,new FlowPane(8,8,create,save,remove,unlink,favorite,refresh),new Label("Retos favoritos"),favorites);
        getChildren().addAll(heading,status,controls);
        if (oracle) run(() -> repository.load(userId),this::show);
        else { controls.setDisable(true); status.setText("Conecta tu cuenta a Oracle para guardar notas y favoritos."); }
    }

    private void clear() { notes.getSelectionModel().clearSelection(); title.clear(); content.clear(); related.setValue(null); }
    private void show(OracleNotebookRepository.Notebook data) {
        notes.getItems().setAll(data.notes()); favorites.getChildren().clear();
        for (Challenge c:challenges) if (data.favorites().contains(c.getId())) {
            Button open=new Button(c.getTitle()); open.setOnAction(e -> new ChallengeFrame(user,c,callback).show());
            Button remove=new Button("Quitar favorito");
            remove.setOnAction(e -> run(() -> { repository.favorite(userId,c.getId(),false); return repository.load(userId); },this::show));
            FlowPane row=new FlowPane(8,8,open,remove); favorites.getChildren().add(row);
        }
        if (favorites.getChildren().isEmpty()) favorites.getChildren().add(new Label("Todavía no guardaste retos favoritos."));
        status.setText(data.notes().size()+" notas guardadas en tu cuenta.");
    }
    private <T> void run(Callable<T> action, Consumer<T> success) {
        if (busy) return;
        busy=true; controls.setDisable(true); status.setText("Guardando o consultando tu cuaderno…");
        Task<T> task=new Task<>() { protected T call() throws Exception { return action.call(); } };
        task.setOnSucceeded(e -> { busy=false; controls.setDisable(false); success.accept(task.getValue()); });
        task.setOnFailed(e -> { busy=false; controls.setDisable(false); status.setText(task.getException() instanceof IllegalArgumentException
                ? task.getException().getMessage() : "No se pudo acceder al cuaderno. Comprueba la conexión y la instalación de la actualización del cuaderno. Tus campos se conservan."); });
        Thread thread=new Thread(task,"student-notebook"); thread.setDaemon(true); thread.start();
    }
}
