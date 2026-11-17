package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.MissionRequirement;
import com.mycompany.levelup_code.domain.model.ProfessionalMission;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The client request and acceptance criteria, read before opening the editor. */
final class MissionBriefPane implements LearningPage {
    private final ScrollPane scroll;

    MissionBriefPane(ProfessionalMission mission) {
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
        VBox page = new VBox(body);
        page.getStyleClass().add("learning-workspace");
        page.setMinWidth(0);
        scroll = WorkspaceUi.scroll(page);
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
    @Override public void disposePage() { }
    @Override public void setNavigator(LearningNavigation navigator) { }
}
