package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.data.connection.DatabaseDiagnostics;
import com.mycompany.levelup_code.data.oracle.OracleNotebookRepository;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.User;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Contextual access to the existing notebook, shared by every exercise type. */
final class ChallengeNotebookActions extends FlowPane {
    private final OracleNotebookRepository repository = new OracleNotebookRepository();
    private final User user;
    private final boolean available;
    private final Supplier<String> source;
    private final Button favorite = WorkspaceUi.button("Guardar en favoritos", "btn-secondary", Theme.IconKind.STAR);
    private final Button note = WorkspaceUi.button("Crear apunte", "btn-secondary", Theme.IconKind.NOTE);
    private final Label status = WorkspaceUi.label("", "muted-text");
    private Challenge challenge;
    private boolean selected;
    private boolean loaded;
    private long revision;

    ChallengeNotebookActions(User user, boolean preview, Supplier<String> source) {
        super(10, 8);
        this.user = user;
        this.source = source;
        available = !preview && user != null && user.getId() > 0 && AppContext.getInstance().isOracleConnected();
        getChildren().addAll(favorite, note, status);
        favorite.setDisable(!available);
        note.setDisable(!available);
        if (!available) status.setText("Entra con tu cuenta de Oracle para guardar apuntes y favoritos.");
        favorite.setOnAction(event -> {
            if (!loaded) { loadFavorite(); return; }
            boolean next = !selected;
            int challengeId = challenge.getId();
            long expected = revision;
            favorite.setDisable(true);
            run(() -> { repository.favorite(user.getId(), challengeId, next); return next; }, value -> {
                if (expected != revision) return;
                favorite.setDisable(false);
                selected = value;
                updateFavorite();
                status.setText(value ? "Reto guardado en tu cuaderno." : "Reto retirado de favoritos.");
            }, message -> {
                if (expected != revision) return;
                favorite.setDisable(false); status.setText(message);
            });
        });
        note.setOnAction(event -> showNote());
        sceneProperty().addListener((observable, old, scene) -> { if (scene != null) loadFavorite(); });
    }

    void setChallenge(Challenge challenge) {
        this.challenge = challenge;
        revision++;
        selected = false;
        loaded = false;
        updateFavorite();
        if (available && getScene() != null) loadFavorite();
    }

    private void updateFavorite() { favorite.setText(selected ? "Quitar de favoritos" : "Guardar en favoritos"); }

    private void loadFavorite() {
        if (!available || challenge == null) return;
        int challengeId = challenge.getId();
        long expected = revision;
        favorite.setDisable(true);
        run(() -> repository.isFavorite(user.getId(), challengeId), value -> {
            if (expected != revision) return;
            loaded = true; selected = value; favorite.setDisable(false); updateFavorite(); status.setText("");
        }, message -> {
            if (expected != revision) return;
            favorite.setDisable(false); loaded = false; favorite.setText("Reintentar favoritos"); status.setText(message);
        });
    }

    private void showNote() {
        Challenge related = challenge;
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Apunte del reto");
        TextField title = new TextField(related.getTitle());
        title.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= 100 ? change : null));
        TextArea content = new TextArea();
        content.setPromptText("¿Qué aprendiste o qué necesitas repasar?");
        content.setWrapText(true); content.setPrefRowCount(10);
        Label message = WorkspaceUi.label("El apunte quedará asociado a «" + related.getTitle() + "».", "body-text");
        VBox fields = new VBox(12, message, WorkspaceUi.field("Título", title), WorkspaceUi.field("Apuntes", content));
        String code = source == null ? "" : source.get();
        if (code != null && !code.isBlank()) {
            Button include = WorkspaceUi.button("Incluir mi código", "btn-secondary", Theme.IconKind.CODE);
            include.setOnAction(event -> content.appendText((content.getText().isEmpty() ? "" : "\n\n") + code));
            fields.getChildren().add(include);
        }
        fields.setPrefWidth(540);
        fields.getStyleClass().add("learning-workspace");
        ButtonType saveType = new ButtonType("Guardar apunte", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CLOSE);
        dialog.getDialogPane().setContent(fields);
        dialog.setResizable(true);
        Theme.applyCss(dialog.getDialogPane().getScene());
        Button save = (Button) dialog.getDialogPane().lookupButton(saveType);
        save.getStyleClass().add("btn-primary");
        final boolean[] busy = { false };
        final boolean[] dirty = { false };
        title.textProperty().addListener((observable, old, value) -> dirty[0] = true);
        content.textProperty().addListener((observable, old, value) -> dirty[0] = true);
        final int[] savedId = { 0 };
        save.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            if (busy[0]) return;
            var captured = new OracleNotebookRepository.Note(savedId[0], related.getId(), title.getText(), content.getText());
            busy[0] = true;
            fields.setDisable(true); save.setDisable(true); message.setText("Guardando tu apunte…");
            run(() -> repository.save(user.getId(), captured), saved -> {
                savedId[0] = saved.id(); busy[0] = false; dirty[0] = false;
                fields.setDisable(false); save.setDisable(false);
                message.setText("Apunte guardado. Lo encontrarás en Mi cuaderno.");
                status.setText("Apunte guardado en tu cuaderno.");
            }, failure -> {
                busy[0] = false; fields.setDisable(false); save.setDisable(false); message.setText(failure);
            });
        });
        dialog.setOnCloseRequest(event -> {
            if (busy[0]) { event.consume(); return; }
            if (!dirty[0]) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Descartar los cambios de este apunte?", ButtonType.OK, ButtonType.CANCEL);
            confirm.initOwner(dialog.getDialogPane().getScene().getWindow());
            Theme.applyCss(confirm.getDialogPane().getScene());
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) event.consume();
        });
        dialog.show();
    }

    private <T> void run(Callable<T> operation, Consumer<T> success, Consumer<String> failure) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception {
                try { return operation.call(); }
                catch (java.sql.SQLException ex) { updateMessage(DatabaseDiagnostics.inspect().message()); throw ex; }
            }
        };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> failure.accept(task.getException() instanceof IllegalArgumentException
                ? task.getException().getMessage() : "No se completó la operación. " + task.getMessage()));
        Thread worker = new Thread(task, "challenge-notebook");
        worker.setDaemon(true); worker.start();
    }
}
