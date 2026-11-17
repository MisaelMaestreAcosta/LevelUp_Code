package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import java.util.Optional;
import java.util.function.Consumer;
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
    private Task<Optional<Challenge>> loading;

    MissionBriefPane(ProfessionalMission mission, Supplier<Optional<Challenge>> lookup, Consumer<Challenge> open) {
        Label client = WorkspaceUi.label("Cliente · " + mission.client(), "section-caption");
        Label title = WorkspaceUi.label(mission.title(), "page-title");
        Label description = WorkspaceUi.label(mission.brief(), "body-text");
        VBox brief = new VBox(10, client, title, description);
        brief.getStyleClass().addAll("surface-card", "mission-card");

        VBox body = new VBox(16, brief);
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

    private void loadChallenge(Supplier<Optional<Challenge>> lookup, Consumer<Challenge> open) {
        start.setDisable(true);
        WorkspaceUi.status(status, "Abriendo el reto…", "");
        Task<Optional<Challenge>> task = new Task<>() {
            @Override protected Optional<Challenge> call() { return lookup.get(); }
        };
        loading = task;
        task.setOnSucceeded(event -> {
            finishLoading();
            if (scroll.getScene() == null) return;
            task.getValue().ifPresentOrElse(challenge -> {
                WorkspaceUi.status(status, "", "");
                open.accept(challenge);
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

    private static HBox requirementRow(MissionRequirement requirement) {
        var icon = Theme.createIcon(requirement.automaticallyVerified() ? Theme.IconKind.CODE : Theme.IconKind.NOTE,
                18, Theme.HEX_INDIGO);
        Label text = WorkspaceUi.label(requirement.text(), "body-text");
        Label validation = WorkspaceUi.label(requirement.automaticallyVerified()
                ? "Se verifica con pruebas" : "Revisión personal", "muted-text");
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
