package com.mycompany.levelup_code.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** A compact, accessible message from Lupo with a consistent portrait. */
public final class LupoGuide extends HBox {
    private final ImageView portrait;
    private final Label message;

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
        getChildren().addAll(portrait, words);
    }

    public void setExpression(Lupo.Expression expression) {
        portrait.setImage(Lupo.image(expression));
    }

    Label messageLabel() {
        return message;
    }
}
