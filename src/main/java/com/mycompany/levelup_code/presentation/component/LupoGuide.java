package com.mycompany.levelup_code.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A compact, accessible message from Lupo with a consistent portrait. */
public final class LupoGuide extends HBox {
    private final ImageView portrait;
    private final Label message;
    private final Button action = WorkspaceUi.button("", "btn-secondary", null);
    private final Button toggle = WorkspaceUi.button("Contraer guía", "btn-ghost", null);
    private boolean compact;

    public LupoGuide(Lupo.Expression expression, String title, String text) {
        super(12);
        setAlignment(Pos.CENTER_LEFT);
        setMinWidth(0);
        getStyleClass().add("lupo-guide");
        portrait = Lupo.portrait(expression, 68);
        Label heading = new Label(title);
        heading.getStyleClass().add("lupo-guide-title");
        heading.setWrapText(true);
        message = new Label(text);
        message.getStyleClass().add("body-text");
        message.setWrapText(true);
        message.setMinHeight(Region.USE_PREF_SIZE);
        message.setMaxWidth(Double.MAX_VALUE);
        VBox words = new VBox(4, heading, message);
        words.setMinWidth(0);
        HBox.setHgrow(words, Priority.ALWAYS);
        action.setVisible(false);
        action.setManaged(false);
        action.setWrapText(true);
        words.getChildren().add(action);
        toggle.setOnAction(event -> {
            compact = !compact;
            portrait.setVisible(!compact);
            portrait.setManaged(!compact);
            heading.setVisible(!compact);
            heading.setManaged(!compact);
            toggle.setText(compact ? "Ampliar guía" : "Contraer guía");
        });
        toggle.setWrapText(true);
        toggle.setMaxWidth(100);
        getChildren().addAll(portrait, words, toggle);
    }

    public void setExpression(Lupo.Expression expression) {
        var next = Lupo.image(expression);
        if (portrait.getImage() != next) {
            portrait.setImage(next);
            Theme.playPulse(portrait);
        }
    }

    public void setAction(String text, Runnable callback) {
        boolean available = callback != null;
        action.setText(text);
        action.setOnAction(event -> { if (callback != null) callback.run(); });
        action.setVisible(available);
        action.setManaged(available);
    }

    public void setActionDisabled(boolean disabled) { action.setDisable(disabled); }

    Label messageLabel() {
        return message;
    }
}
