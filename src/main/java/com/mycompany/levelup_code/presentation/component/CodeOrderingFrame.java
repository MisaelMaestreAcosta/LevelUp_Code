package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.evaluator.CodeOrderingEvaluator;
import com.mycompany.levelup_code.data.local.ChallengeDraftStore;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.model.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.css.PseudoClass;
import javafx.stage.Stage;

public final class CodeOrderingFrame extends Stage implements LearningPage {
    private ScrollPane view;
    private LearningNavigation navigator;
    @Override public Region content() { return view; }
    @Override public void setNavigator(LearningNavigation value) { navigator = value; }
    @Override public boolean canLeave() {
        if (busy) status.setText("Espera a que termine el envío.");
        return !busy;
    }
    @Override public void disposePage() { saveDraft(); drafts.shutdown(); }
    private static final DataFormat FRAGMENT_DATA = new DataFormat("application/x-levelup-fragment");
    private final String dragToken = UUID.randomUUID().toString();
    private record Fragment(int number, String code) {
        @Override public String toString() { return number + "   " + code; }
    }
    private record Result(boolean correct, Attempt saved) { }
    private final ListView<Fragment> fragments = new ListView<>();
    private final LupoGuide lupoFeedback = new LupoGuide(Lupo.Expression.THINKING, "Ordena con Lupo",
            "Busca qué instrucción inicia el programa y qué necesita cada fragmento para continuar.");
    private final Label status = lupoFeedback.messageLabel();
    private final Label draftStatus = new Label();
    private final VBox controls = new VBox(12);
    private final ChallengeDraftStore store = new ChallengeDraftStore();
    private final ExecutorService drafts = Executors.newSingleThreadExecutor();
    private final CodeOrderingChallenge challenge;
    private final User user;
    private final Runnable callback;
    private final boolean preview;
    private final String mode;
    private final String fingerprint;
    private boolean busy;
    private String pendingId;
    private String pendingPayload;
    private boolean uncertain;
    private boolean celebrated;
    private boolean showResult;
    private int hintStage;

    public CodeOrderingFrame(User user, CodeOrderingChallenge challenge, Runnable callback, boolean preview) {
        this.user = user;
        this.challenge = challenge;
        this.callback = callback;
        this.preview = preview;
        mode = AppContext.getInstance().isOracleConnected() ? "oracle" : "demo";
        fingerprint = fingerprint(challenge);
        Label title = new Label(challenge.getTitle());
        title.getStyleClass().add("page-title"); title.setWrapText(true);
        Label statement = new Label(challenge.getDescription()); statement.setWrapText(true);
        status.setWrapText(true); draftStatus.setWrapText(true);
        fragments.setPrefHeight(320);
        fragments.getStyleClass().add("ordering-list");
        fragments.setAccessibleText("Fragmentos de código. Arrastra para ordenar o usa Alt más flecha arriba o abajo.");
        fragments.setCellFactory(list -> new FragmentCell());
        fragments.setOnDragOver(event -> {
            if (draggedIndex(event.getDragboard()) >= 0 && !busy) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });
        fragments.setOnDragDropped(event -> {
            boolean moved = drop(event.getDragboard(), fragments.getItems().size());
            event.setDropCompleted(moved);
            event.consume();
        });
        for (int i = 0; i < challenge.getFragments().size(); i++) fragments.getItems().add(new Fragment(i + 1, challenge.getFragments().get(i)));
        Collections.shuffle(fragments.getItems());
        if (payload().equals(CodeOrderingChallenge.formatOrder(challenge.getCorrectOrder()))) Collections.rotate(fragments.getItems(), 1);
        Button up = new Button("Subir fragmento"); up.setOnAction(event -> move(-1));
        Button down = new Button("Bajar fragmento"); down.setOnAction(event -> move(1));
        up.getStyleClass().add("btn-secondary");
        down.getStyleClass().add("btn-secondary");
        up.disableProperty().bind(fragments.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0));
        down.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(() ->
                fragments.getSelectionModel().getSelectedIndex() < 0
                        || fragments.getSelectionModel().getSelectedIndex() >= fragments.getItems().size() - 1,
                fragments.getSelectionModel().selectedIndexProperty(), fragments.getItems()));
        Button submit = new Button("Comprobar orden"); submit.getStyleClass().add("btn-primary");
        submit.setOnAction(event -> submit());
        Button history = new Button("Ver intentos");
        history.setDisable(user == null || !AppContext.getInstance().isOracleConnected() || preview);
        history.setOnAction(event -> history());
        Button hint = new Button("Ver pista");
        hint.getStyleClass().add("btn-secondary");
        hint.setOnAction(event -> {
            hintStage = Math.min(3, hintStage + 1);
            status.setText(challenge.getGuidance().hint(hintStage, challenge));
            hint.setText(hintStage == 3 ? "Pistas completas" : "Ver pista " + (hintStage + 1));
        });
        controls.getChildren().addAll(title, statement, WorkspaceUi.label("Arrastra los fragmentos para ordenarlos. También puedes seleccionar uno y usar Subir/Bajar o Alt + ↑/↓.", "body-text"),
                fragments, new FlowPane(8, 8, up, down, submit, hint, history));
        ChallengeNotebookActions notebook = new ChallengeNotebookActions(user, preview, () ->
                String.join("\n", fragments.getItems().stream().map(Fragment::code).toList()));
        notebook.setChallenge(challenge);
        VBox root = new VBox(12, controls, notebook, lupoFeedback, draftStatus);
        root.getStyleClass().addAll("root-shell", "learning-workspace", "ordering-workspace");
        root.setPadding(new javafx.geometry.Insets(20));
        ScrollPane scroll = new ScrollPane(root); scroll.setFitToWidth(true);
        view = scroll;
        Scene scene = new Scene(scroll, 850, 650); Theme.applyCss(scene); setScene(scene);
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isAltDown() && (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN)) {
                if (!busy && !controls.isDisabled()) move(event.getCode() == KeyCode.UP ? -1 : 1);
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                if (!controls.isDisabled()) submit();
                event.consume();
            }
        });
        setTitle("LevelUp Code · Ordenar fragmentos");
        SessionWindowRegistry.track(this);
        setOnCloseRequest(event -> {
            if (busy) { event.consume(); status.setText("Espera a que termine el envío."); }
        });
        setOnHidden(event -> { saveDraft(); drafts.shutdown(); });
        restoreDraft();
    }

    private static String fingerprint(CodeOrderingChallenge challenge) {
        try {
            String source = challenge.getId() + ":" + challenge.getRevision() + ":" + challenge.getDescription() + challenge.getFragments();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }

    private String payload() {
        return CodeOrderingChallenge.formatOrder(fragments.getItems().stream().map(Fragment::number).toList());
    }

    private boolean persistDraft() { return !preview && user != null && user.getEmail() != null && challenge.getId() > 0; }

    private ChallengeDraftStore.DraftData snapshot() {
        return new ChallengeDraftStore.DraftData(payload(), -1, 0, 0, uncertain, pendingId, pendingPayload, fingerprint);
    }

    private void restoreDraft() {
        if (!persistDraft()) return;
        controls.setDisable(true);
        drafts.submit(() -> {
            try {
                var saved = store.load(mode, user.getEmail(), challenge.getId());
                Platform.runLater(() -> {
                    controls.setDisable(false);
                    saved.filter(value -> fingerprint.equals(value.challengeFingerprint())).ifPresent(value -> {
                        try {
                            List<Integer> order = CodeOrderingChallenge.parseOrder(value.code());
                            if (!order.stream().sorted().toList().equals(java.util.stream.IntStream.rangeClosed(1, fragments.getItems().size()).boxed().toList())) return;
                            Map<Integer, Fragment> indexed = new HashMap<>();
                            fragments.getItems().forEach(fragment -> indexed.put(fragment.number(), fragment));
                            fragments.getItems().setAll(order.stream().map(indexed::get).toList());
                            pendingId = value.pendingRequestId(); pendingPayload = value.pendingPayloadHash(); uncertain = value.saveUncertain();
                            draftStatus.setText("Orden recuperado de tu borrador.");
                        } catch (IllegalArgumentException ignored) { draftStatus.setText("El borrador anterior no se pudo recuperar."); }
                    });
                });
            } catch (Exception ex) { Platform.runLater(() -> { controls.setDisable(false); draftStatus.setText("No se pudo recuperar el borrador."); }); }
        });
    }

    private void move(int distance) {
        int index = fragments.getSelectionModel().getSelectedIndex();
        int target = index + distance;
        if (index < 0 || target < 0 || target >= fragments.getItems().size()) return;
        moveFragment(index, target);
    }

    private void moveFragment(int index, int target) {
        if (index == target) return;
        Fragment fragment = fragments.getItems().remove(index);
        fragments.getItems().add(target, fragment);
        fragments.getSelectionModel().select(target);
        fragments.scrollTo(target);
        showResult = false;
        fragments.refresh();
        status.setText("Orden actualizado. Compruébalo cuando estés listo.");
        lupoFeedback.setExpression(Lupo.Expression.THINKING);
        saveDraft();
    }

    private int draggedIndex(Dragboard board) {
        Object value = board.getContent(FRAGMENT_DATA);
        if (!(value instanceof String text) || !text.startsWith(dragToken + ":")) return -1;
        for (int index = 0; index < fragments.getItems().size(); index++) {
            if (text.equals(dragToken + ":" + fragments.getItems().get(index).number())) return index;
        }
        return -1;
    }

    private boolean drop(Dragboard board, int insertion) {
        int index = draggedIndex(board);
        if (index < 0 || busy || controls.isDisabled()) return false;
        int target = Math.min(fragments.getItems().size() - 1, index < insertion ? insertion - 1 : insertion);
        moveFragment(index, target);
        return true;
    }

    private final class FragmentCell extends ListCell<Fragment> {
        FragmentCell() {
            setOnDragDetected(event -> {
                if (isEmpty() || busy || controls.isDisabled()) return;
                fragments.getSelectionModel().select(getIndex());
                Dragboard board = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(FRAGMENT_DATA, dragToken + ":" + getItem().number());
                board.setContent(content);
                event.consume();
            });
            setOnDragOver(event -> {
                if (isEmpty() || draggedIndex(event.getDragboard()) < 0 || busy) return;
                event.acceptTransferModes(TransferMode.MOVE);
                pseudoClassStateChanged(PseudoClass.getPseudoClass("drop-before"), event.getY() < getHeight() / 2);
                pseudoClassStateChanged(PseudoClass.getPseudoClass("drop-after"), event.getY() >= getHeight() / 2);
                event.consume();
            });
            setOnDragExited(event -> clearDropMark());
            setOnDragDone(event -> clearDropMark());
            setOnDragDropped(event -> {
                int insertion = isEmpty() ? fragments.getItems().size()
                        : getIndex() + (event.getY() >= getHeight() / 2 ? 1 : 0);
                event.setDropCompleted(drop(event.getDragboard(), insertion));
                clearDropMark();
                event.consume();
            });
        }

        private void clearDropMark() {
            pseudoClassStateChanged(PseudoClass.getPseudoClass("drop-before"), false);
            pseudoClassStateChanged(PseudoClass.getPseudoClass("drop-after"), false);
        }

        @Override protected void updateItem(Fragment fragment, boolean empty) {
            super.updateItem(fragment, empty);
            clearDropMark();
            setText(null);
            setGraphic(null);
            if (empty || fragment == null) return;
            Label code = WorkspaceUi.label(fragment.code(), "fragment-code");
            code.setMinWidth(0);
            HBox.setHgrow(code, Priority.ALWAYS);
            Label position = WorkspaceUi.label(String.valueOf(getIndex() + 1), "fragment-position");
            HBox row = new HBox(12, position, code);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.prefWidthProperty().bind(fragments.widthProperty().subtract(48));
            if (showResult && getIndex() >= 0 && getIndex() < challenge.getCorrectOrder().size()) {
                boolean correct = challenge.getCorrectOrder().get(getIndex()) == fragment.number();
                row.getChildren().add(Theme.createIcon(correct ? Theme.IconKind.CHECK : Theme.IconKind.REFRESH,
                        16, correct ? Theme.HEX_GREEN : Theme.HEX_AMBER));
            }
            setGraphic(row);
            setAccessibleText("Posición " + (getIndex() + 1) + ", " + fragment.code());
        }
    }

    private void saveDraft() {
        if (!persistDraft() || drafts.isShutdown()) return;
        var snapshot = snapshot();
        drafts.submit(() -> {
            try { store.save(mode, user.getEmail(), challenge.getId(), snapshot); }
            catch (Exception ex) { Platform.runLater(() -> draftStatus.setText("No se pudo guardar el borrador en este equipo.")); }
        });
    }

    private void submit() {
        if (busy) return;
        String answer = payload();
        int previousLevel = user == null ? 1 : user.getLevel();
        boolean persistent = !preview && user != null && user.getId() > 0 && user.getLives() > 0
                && AppContext.getInstance().isOracleConnected() && AppContext.getInstance().isOracleAvailable();
        if (!answer.equals(pendingPayload) || pendingId == null) pendingId = UUID.randomUUID().toString();
        pendingPayload = answer;
        uncertain = persistent;
        String requestId = pendingId;
        var draft = snapshot();
        User detached = new User();
        if (user != null) {
            detached.setId(user.getId()); detached.setExperience(user.getExperience()); detached.setLives(user.getLives());
        }
        busy = true; controls.setDisable(true); status.setText("Comprobando el orden…");
        lupoFeedback.setExpression(Lupo.Expression.THINKING);
        Task<Result> task = new Task<>() {
            protected Result call() throws Exception {
                if (persistent) {
                    drafts.submit(() -> { store.save(mode, user.getEmail(), challenge.getId(), draft); return true; }).get();
                    var saved = AppContext.getInstance().getChallengeService().submitAttemptDetailed(detached, challenge.getId(), answer, requestId, challenge.getRevision());
                    return new Result(saved.attempt().isCorrect(), saved.attempt());
                }
                return new Result(new CodeOrderingEvaluator().evaluate(challenge, answer).getResult() == AttemptResult.CORRECT, null);
            }
        };
        task.setOnSucceeded(event -> {
            busy = false; controls.setDisable(false); uncertain = false; pendingId = null; pendingPayload = null;
            Result result = task.getValue();
            showResult = true;
            fragments.refresh();
            lupoFeedback.setExpression(result.correct() ? Lupo.Expression.CELEBRATING : Lupo.Expression.THINKING);
            if (result.saved() != null) {
                User fresh = result.saved().getUser();
                user.setExperience(fresh.getExperience()); user.setLevel(fresh.getLevel()); user.setLives(fresh.getLives());
                user.setCurrentStreak(fresh.getCurrentStreak()); user.setMaxStreak(fresh.getMaxStreak());
            }
            status.setText((result.correct() ? "¡Orden correcto!" : "Ese orden aún no resuelve el ejercicio. Prueba de nuevo.")
                    + (result.saved() == null ? " Práctica local sin XP." : " Intento guardado · +" + result.saved().getEarnedExperience() + " XP."));
            if (result.correct() && !challenge.getGuidance().explanation().isBlank()) {
                status.setText(status.getText() + "\n" + challenge.getGuidance().explanation());
            }
            saveDraft();
            if (result.correct() && !celebrated) {
                celebrated = true;
                CompletionDialog.show(ownerWindow(), challenge.getTitle(), result.saved() == null ? 0
                        : result.saved().getEarnedExperience(), user, previousLevel, result.saved() != null, null);
            }
            if (navigator != null) navigator.evaluated(new com.mycompany.levelup_code.domain.model.PracticeSession.Outcome(
                    challenge.getId(), result.correct(), result.saved() != null,
                    result.saved() == null ? 0 : result.saved().getEarnedExperience()));
            if (result.saved() != null && callback != null) callback.run();
        });
        task.setOnFailed(event -> {
            busy = false; controls.setDisable(false);
            lupoFeedback.setExpression(Lupo.Expression.THINKING);
            status.setText("No se confirmó el envío. Tu orden se conserva. Puedes reintentar con la misma respuesta o abrir la versión actual del reto.");
            saveDraft();
        });
        new Thread(task, "ordering-submit").start();
    }

    private void history() {
        controls.setDisable(true);
        Task<List<Attempt>> task = new Task<>() {
            protected List<Attempt> call() { return AppContext.getInstance().getChallengeService().getAttemptHistory(user.getId(), challenge.getId()); }
        };
        task.setOnSucceeded(event -> {
            controls.setDisable(false);
            StringBuilder content = new StringBuilder();
            for (Attempt attempt : task.getValue()) content.append(attempt.getAttemptDate()).append(" · ")
                    .append(attempt.getResult()).append(" · v").append(attempt.getChallengeRevision())
                    .append("\nOrden enviado ").append(attempt.getSubmittedContent()).append("\n\n");
            TextArea text = new TextArea(content.isEmpty() ? "Todavía no hay intentos guardados." : content.toString());
            text.setEditable(false);
            Alert dialog = new Alert(Alert.AlertType.INFORMATION); dialog.initOwner(ownerWindow());
            dialog.setHeaderText("Últimos 50 intentos"); dialog.getDialogPane().setContent(text); dialog.setResizable(true); dialog.show();
        });
        task.setOnFailed(event -> { controls.setDisable(false); status.setText("No se pudo consultar el historial."); });
        Thread worker = new Thread(task, "ordering-history"); worker.setDaemon(true); worker.start();
    }
}
