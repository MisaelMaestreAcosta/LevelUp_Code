package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.PracticeSession;
import com.mycompany.levelup_code.domain.model.User;
import javafx.scene.control.*;
import javafx.scene.layout.*;

final class GuidedPracticePane extends VBox {
    private final PracticeSession session;
    private final LearningWorkspace workspace;
    private final Label position = WorkspaceUi.label("", "card-title");
    private final Label reason = WorkspaceUi.label("", "body-text");
    private final ProgressBar progress = new ProgressBar();
    private final Button next = WorkspaceUi.button("Saltar reto", "btn-secondary", null);
    private final VBox body = new VBox(12);

    GuidedPracticePane(User user, PracticeSession session, Runnable refresh) {
        this(user, session, refresh, outcome -> { });
    }

    GuidedPracticePane(User user, PracticeSession session, Runnable refresh, java.util.function.Consumer<PracticeSession.Outcome> results) {
        super(14);
        this.session = session;
        getStyleClass().add("learning-workspace");
        VBox heading = new VBox(8, WorkspaceUi.label("Tu sesión de práctica", "page-title"), position, reason, progress);
        heading.getStyleClass().add("surface-card");
        progress.setMaxWidth(Double.MAX_VALUE);
        workspace = new LearningWorkspace(user, refresh, refresh, this::showPaused, focused -> {
            heading.setVisible(!focused); heading.setManaged(!focused);
        }, outcome -> { session.record(outcome); results.accept(outcome); updateNext(); });
        VBox.setVgrow(body, Priority.ALWAYS);
        next.setOnAction(event -> {
            if (!workspace.clear()) return;
            session.advance();
            showCurrent();
        });
        Button finish = WorkspaceUi.button("Terminar sesión", "btn-secondary", null);
        finish.setOnAction(event -> {
            if (!workspace.clear()) return;
            while (!session.finished()) session.advance();
            showCurrent();
            finish.setVisible(false); finish.setManaged(false);
        });
        next.visibleProperty().addListener((observable, old, visible) -> {
            if (session.finished()) { finish.setVisible(false); finish.setManaged(false); }
        });
        getChildren().addAll(heading, body, new FlowPane(10, 8, next, finish));
        showCurrent();
    }

    boolean finished() { return session.finished(); }
    boolean canLeave() { return workspace.canLeave(); }
    boolean dispose() { return workspace.clear(); }

    private void updateNext() {
        var outcome = session.outcome(session.current().challenge().getId());
        next.setText(outcome == null ? "Saltar reto" : outcome.correct() ? "Continuar" : "Dejar para repasar");
    }

    private void showCurrent() {
        progress.setProgress(session.steps().isEmpty() ? 1 : (double) session.position() / session.steps().size());
        if (session.finished()) { showSummary(); return; }
        position.setText("Reto " + (session.position() + 1) + " de " + session.steps().size());
        reason.setText(session.current().reason());
        body.getChildren().setAll(workspace);
        workspace.openChallenge(session.current().challenge());
        next.setVisible(true); next.setManaged(true);
        updateNext();
    }

    private void showPaused() {
        var resume = WorkspaceUi.button("Retomar este reto", "btn-primary", Theme.IconKind.PLAY);
        resume.setOnAction(event -> showCurrent());
        body.getChildren().setAll(WorkspaceUi.label("Tu sesión está en pausa. Puedes retomarla desde aquí mientras la aplicación siga abierta.", "body-text"), resume);
        next.setVisible(false); next.setManaged(false);
    }

    private void showSummary() {
        position.setText("Sesión terminada");
        reason.setText(session.correctCount() + " de " + session.steps().size() + " retos resueltos · "
                + session.savedCount() + " con intentos guardados · +" + session.earnedXp() + " XP guardados");
        body.getChildren().clear();
        for (var step : session.steps()) {
            var outcome = session.outcome(step.challenge().getId());
            String result = outcome == null ? "Pendiente de practicar" : outcome.correct()
                    ? outcome.correctSaved() ? "Superado y guardado" : "Resuelto en práctica local" : "Conviene repasar";
            VBox card = new VBox(7, WorkspaceUi.label(step.challenge().getTitle(), "card-title"), WorkspaceUi.label(result, "body-text"));
            card.getStyleClass().add("surface-card");
            body.getChildren().add(card);
        }
        next.setVisible(false); next.setManaged(false);
    }
}
