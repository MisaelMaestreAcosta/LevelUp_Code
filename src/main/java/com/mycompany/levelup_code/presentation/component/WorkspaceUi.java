package com.mycompany.levelup_code.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Shared presentation elements for the student's learning workspaces. */
final class WorkspaceUi {
    private WorkspaceUi() { }

    static VBox header(String title, String description, Node... actions) {
        Label heading = label(title, "page-title");
        Label subtitle = label(description, "muted-text");
        HBox row = new HBox(12, heading);
        row.setAlignment(Pos.CENTER_LEFT);
        if (actions.length > 0) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().add(spacer);
            row.getChildren().addAll(actions);
        }
        return new VBox(5, row, subtitle);
    }

    static Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        label.setWrapText(true);
        return label;
    }

    static Button button(String text, String style, Theme.IconKind icon) {
        Button button = new Button(text);
        button.getStyleClass().add(style);
        if (icon != null) {
            button.setGraphic(Theme.createIcon(icon, 14,
                    "btn-primary".equals(style) ? Theme.HEX_WHITE : Theme.HEX_INDIGO));
            button.setGraphicTextGap(7);
        }
        return button;
    }

    static VBox field(String text, Control control) {
        Label label = label(text, "field-label");
        label.setLabelFor(control);
        control.setMaxWidth(Double.MAX_VALUE);
        return new VBox(6, label, control);
    }

    static ScrollPane scroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("scroll-pane-clean");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(0);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    static VBox empty(Theme.IconKind icon, String title, String description) {
        VBox empty = new VBox(10, Theme.createIcon(icon, 26, Theme.HEX_MUTED),
                label(title, "card-title"), label(description, "muted-text"));
        empty.getStyleClass().add("workspace-empty");
        empty.setAlignment(Pos.CENTER);
        return empty;
    }

    static void status(Label label, String message, String state) {
        label.setText(message);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().removeAll("workspace-status", "status-error", "status-success");
        label.getStyleClass().add("workspace-status");
        if (!state.isEmpty()) label.getStyleClass().add(state);
        label.setVisible(!message.isBlank());
        label.setManaged(!message.isBlank());
    }
}
