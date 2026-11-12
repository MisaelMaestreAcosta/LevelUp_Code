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
import javafx.stage.Stage;

public final class CodeOrderingFrame extends Stage {
    private record Fragment(int number, String code) {
        @Override public String toString() { return number + "   " + code; }
    }
    private record Result(boolean correct, Attempt saved) { }
    private final ListView<Fragment> fragments = new ListView<>();
    private final Label status = new Label();
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
        for (int i = 0; i < challenge.getFragments().size(); i++) fragments.getItems().add(new Fragment(i + 1, challenge.getFragments().get(i)));
        Collections.shuffle(fragments.getItems());
        if (payload().equals(CodeOrderingChallenge.formatOrder(challenge.getCorrectOrder()))) Collections.rotate(fragments.getItems(), 1);
        Button up = new Button("Subir fragmento"); up.setOnAction(event -> move(-1));
        Button down = new Button("Bajar fragmento"); down.setOnAction(event -> move(1));
        Button submit = new Button("Comprobar orden"); submit.getStyleClass().add("btn-primary");
        submit.setOnAction(event -> submit());
        Button history = new Button("Ver intentos");
        history.setDisable(user == null || !AppContext.getInstance().isOracleConnected() || preview);
        history.setOnAction(event -> history());
        controls.getChildren().addAll(title, statement, new Label("Selecciona un fragmento y usa Subir o Bajar para colocarlo."),
                fragments, new FlowPane(8, 8, up, down, submit, history));
        VBox root = new VBox(12, controls, status, draftStatus);
        root.setPadding(new javafx.geometry.Insets(20));
        ScrollPane scroll = new ScrollPane(root); scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 850, 650); Theme.applyCss(scene); setScene(scene);
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
        Collections.swap(fragments.getItems(), index, target);
        fragments.getSelectionModel().select(target);
        saveDraft();
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
            if (result.saved() != null) {
                User fresh = result.saved().getUser();
                user.setExperience(fresh.getExperience()); user.setLevel(fresh.getLevel()); user.setLives(fresh.getLives());
                user.setCurrentStreak(fresh.getCurrentStreak()); user.setMaxStreak(fresh.getMaxStreak());
            }
            status.setText((result.correct() ? "¡Orden correcto!" : "Ese orden aún no resuelve el ejercicio. Prueba de nuevo.")
                    + (result.saved() == null ? " Práctica local sin XP." : " Intento guardado · +" + result.saved().getEarnedExperience() + " XP."));
            saveDraft();
            if (result.saved() != null && callback != null) callback.run();
        });
        task.setOnFailed(event -> {
            busy = false; controls.setDisable(false);
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
            Alert dialog = new Alert(Alert.AlertType.INFORMATION); dialog.initOwner(this);
            dialog.setHeaderText("Últimos 50 intentos"); dialog.getDialogPane().setContent(text); dialog.setResizable(true); dialog.show();
        });
        task.setOnFailed(event -> { controls.setDisable(false); status.setText("No se pudo consultar el historial."); });
        Thread worker = new Thread(task, "ordering-history"); worker.setDaemon(true); worker.start();
    }
}
