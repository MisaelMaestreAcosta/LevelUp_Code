package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.business.evaluator.MissionEvaluation;
import com.mycompany.levelup_code.business.evaluator.MissionEvaluation.Status;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The client request and acceptance criteria, read before opening the editor. */
final class MissionBriefPane implements LearningPage {
    private static final Logger LOGGER = Logger.getLogger(MissionBriefPane.class.getName());

    private final ScrollPane scroll;
    private final Button start = WorkspaceUi.button("Abrir reto", "btn-primary", Theme.IconKind.PLAY);
    private final Label status = new Label();
    private final LupoGuide reviewGuide = new LupoGuide(Lupo.Expression.THINKING, "Última evaluación de esta sesión", "");
    private final List<Label> requirementStatuses = new ArrayList<>();
    private Task<Optional<Challenge>> loading;

    MissionBriefPane(ProfessionalMission mission, Supplier<Optional<Challenge>> lookup,
            BiConsumer<MissionBriefPane, Challenge> open) {
        Label client = WorkspaceUi.label("Cliente · " + mission.client(), "section-caption");
        Label title = WorkspaceUi.label(mission.title(), "page-title");
        Label description = WorkspaceUi.label(mission.brief(), "body-text");
        VBox brief = new VBox(10, client, title, description);
        brief.getStyleClass().addAll("surface-card", "mission-card");

        reviewGuide.setVisible(false);
        reviewGuide.managedProperty().bind(reviewGuide.visibleProperty());
        VBox body = new VBox(16, brief, reviewGuide);
        body.setMaxWidth(900);
        if (!mission.requirements().isEmpty()) {
            VBox requirements = new VBox(10, WorkspaceUi.label("Requisitos de entrega", "card-title"));
            for (MissionRequirement requirement : mission.requirements()) {
                requirements.getChildren().add(requirementRow(requirement));
            }
            body.getChildren().add(requirements);
        }
        WorkspaceUi.status(status, "", "");
        body.getChildren().addAll(start, status);
        VBox page = new VBox(body);
        page.getStyleClass().add("learning-workspace");
        page.setMinWidth(0);
        scroll = WorkspaceUi.scroll(page);
        start.setOnAction(event -> loadChallenge(lookup, open));
    }

    void showReview(MissionEvaluation review) {
        if (requirementStatuses.isEmpty()) return;
        List<Status> results = review.requirements();
        for (int i = 0; i < results.size(); i++) {
            Label label = requirementStatuses.get(i);
            Status result = results.get(i);
            label.setText(switch (result) {
                case PASSED -> "Cumplido";
                case NEEDS_WORK -> "Por corregir";
                case UNVERIFIED -> "Sin verificar";
                case SELF_REVIEW -> "Revisión personal";
            });
            label.getStyleClass().setAll(switch (result) {
                case PASSED -> "text-success";
                case NEEDS_WORK -> "text-warning";
                case UNVERIFIED, SELF_REVIEW -> "muted-text";
            });
        }
        long automatic = results.stream().filter(result -> result != Status.SELF_REVIEW).count();
        long passed = results.stream().filter(result -> result == Status.PASSED).count();
        boolean allPassed = automatic > 0 && passed == automatic;
        String message;
        if (automatic == 0) {
            message = "Repasa los criterios de revisión personal antes de dar por terminado el encargo.";
        } else if (results.stream().noneMatch(result -> result == Status.NEEDS_WORK || result == Status.PASSED)) {
            message = "No hay resultados de pruebas para esta evaluación. Vuelve al editor para comprobar los detalles.";
        } else {
            message = "Requisitos automáticos cumplidos · " + passed + " de " + automatic + ". "
                    + (allPassed ? "Repasa la entrega antes de cerrar el encargo."
                            : "Revisa los requisitos pendientes y ajusta el código.");
        }
        reviewGuide.setExpression(allPassed ? Lupo.Expression.CELEBRATING : Lupo.Expression.THINKING);
        reviewGuide.messageLabel().setText(message);
        reviewGuide.setVisible(true);
        scroll.setVvalue(0);
    }

    void resumeWith(Runnable openEditor) {
        start.setText("Volver al editor");
        start.setOnAction(event -> openEditor.run());
    }

    private void loadChallenge(Supplier<Optional<Challenge>> lookup, BiConsumer<MissionBriefPane, Challenge> open) {
        start.setDisable(true);
        WorkspaceUi.status(status, "Abriendo el reto…", "");
        Task<Optional<Challenge>> task = new Task<>() {
            @Override protected Optional<Challenge> call() { return lookup.get(); }
        };
        loading = task;
        task.setOnSucceeded(event -> {
            finishLoading();
            if (scroll.getScene() == null) return;
            task.getValue().filter(CodeChallenge.class::isInstance).ifPresentOrElse(challenge -> {
                WorkspaceUi.status(status, "", "");
                open.accept(this, challenge);
            }, () -> WorkspaceUi.status(status,
                    "El reto ya no está disponible. Vuelve a Misiones y actualiza el catálogo.", "status-error"));
        });
        task.setOnFailed(event -> {
            finishLoading();
            LOGGER.log(Level.WARNING, "Could not open the mission challenge", task.getException());
            WorkspaceUi.status(status, "No pudimos abrir el reto. Comprueba la conexión y vuelve a intentarlo.",
                    "status-error");
        });
        Thread worker = new Thread(task, "mission-challenge");
        worker.setDaemon(true);
        worker.start();
    }

    private void finishLoading() {
        loading = null;
        start.setDisable(false);
    }

    private HBox requirementRow(MissionRequirement requirement) {
        var icon = Theme.createIcon(requirement.automaticallyVerified() ? Theme.IconKind.CODE : Theme.IconKind.NOTE,
                18, Theme.HEX_INDIGO);
        Label text = WorkspaceUi.label(requirement.text(), "body-text");
        Label validation = WorkspaceUi.label(requirement.automaticallyVerified()
                ? "Se verifica con pruebas" : "Revisión personal", "muted-text");
        requirementStatuses.add(validation);
        VBox copy = new VBox(4, text, validation);
        copy.setMinWidth(0);
        HBox.setHgrow(copy, Priority.ALWAYS);
        HBox row = new HBox(12, icon, copy);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("mission-requirement");
        return row;
    }

    @Override public Region content() { return scroll; }
    @Override public boolean canLeave() { return true; }
    @Override public void disposePage() {
        if (loading != null) loading.cancel();
    }
    @Override public void setNavigator(LearningNavigation navigator) { }
}
