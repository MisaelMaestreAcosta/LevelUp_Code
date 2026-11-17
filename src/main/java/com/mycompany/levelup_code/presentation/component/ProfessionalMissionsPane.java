package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.data.dao.ProfessionalMissionRepository;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

/** The student-facing catalog of published client missions. */
final class ProfessionalMissionsPane extends VBox {
    private static final Logger LOGGER = Logger.getLogger(ProfessionalMissionsPane.class.getName());

    private final ProfessionalMissionRepository repository;
    private final Consumer<ProfessionalMission> openBrief;
    private final VBox catalog = new VBox(12);
    private final Label status = new Label();
    private final ProgressIndicator loading = new ProgressIndicator();
    private final Button refresh = WorkspaceUi.button("Actualizar", "btn-secondary", Theme.IconKind.REFRESH);

    ProfessionalMissionsPane(ProfessionalMissionRepository repository, Consumer<ProfessionalMission> openBrief) {
        this.repository = repository;
        this.openBrief = openBrief;
        getStyleClass().add("learning-workspace");
        setMinWidth(0);

        VBox header = WorkspaceUi.header("Misiones profesionales",
                "Practica con encargos de clientes ficticios.", refresh);
        LupoGuide guide = new LupoGuide(Lupo.Expression.WELCOME, "Tu primer día con Lupo",
                "Cada cliente te deja un encargo y te explica qué espera de tu entrega.");
        loading.setMaxSize(22, 22);
        loading.setAccessibleText("Cargando misiones");
        loading.managedProperty().bind(loading.visibleProperty());
        catalog.setMaxWidth(900);
        refresh.setOnAction(event -> load());
        getChildren().addAll(header, guide, status, loading, catalog);
        load();
    }

    private void load() {
        refresh.setDisable(true);
        loading.setVisible(true);
        WorkspaceUi.status(status, "Cargando misiones…", "");
        Task<List<ProfessionalMission>> task = new Task<>() {
            @Override protected List<ProfessionalMission> call() {
                return repository.findPublished();
            }
        };
        task.setOnSucceeded(event -> {
            finishLoading("Actualizar");
            WorkspaceUi.status(status, "", "");
            showMissions(task.getValue());
        });
        task.setOnFailed(event -> {
            finishLoading("Reintentar");
            LOGGER.log(Level.WARNING, "Could not load professional missions", task.getException());
            WorkspaceUi.status(status, "No pudimos actualizar las misiones. Comprueba la conexión y vuelve a intentarlo.",
                    "status-error");
        });
        Thread worker = new Thread(task, "professional-missions");
        worker.setDaemon(true);
        worker.start();
    }

    private void finishLoading(String action) {
        loading.setVisible(false);
        refresh.setText(action);
        refresh.setDisable(false);
    }

    private void showMissions(List<ProfessionalMission> missions) {
        catalog.getChildren().clear();
        if (missions.isEmpty()) {
            catalog.getChildren().add(WorkspaceUi.empty(Theme.IconKind.TARGET, "Aún no hay misiones",
                    "Los encargos publicados aparecerán aquí."));
            return;
        }
        for (ProfessionalMission mission : missions) {
            Label title = WorkspaceUi.label(mission.title(), "card-title");
            Label client = WorkspaceUi.label(mission.client(), "muted-text");
            title.setGraphic(Theme.createIcon(Theme.IconKind.TARGET, 19, Theme.HEX_INDIGO));
            title.setGraphicTextGap(10);
            Button details = WorkspaceUi.button("Ver encargo", "btn-primary", Theme.IconKind.BOOK);
            details.setAccessibleText("Ver encargo · " + mission.title());
            details.setOnAction(event -> openBrief.accept(mission));
            VBox card = new VBox(8, title, client, details);
            card.getStyleClass().addAll("surface-card", "mission-card");
            catalog.getChildren().add(card);
        }
    }
}
