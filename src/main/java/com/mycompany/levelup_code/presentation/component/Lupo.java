package com.mycompany.levelup_code.presentation.component;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Shared artwork and brand lockup for Lupo, the LevelUp Code fox. */
public final class Lupo {
    public enum Expression { WELCOME, THINKING, CELEBRATING }

    private static final Image[] EXPRESSIONS = loadExpressions();

    private Lupo() { }

    private static Image[] loadExpressions() {
        var resource = Objects.requireNonNull(Lupo.class.getResource("/images/lupo/expressions.png"),
                "Falta el recurso de Lupo.");
        Image strip = new Image(resource.toExternalForm());
        if (strip.isError()) {
            throw new IllegalStateException("No se pudo cargar la imagen de Lupo.", strip.getException());
        }
        int width = (int) strip.getWidth() / Expression.values().length;
        int height = (int) strip.getHeight();
        Image[] expressions = new Image[Expression.values().length];
        for (Expression expression : Expression.values()) {
            expressions[expression.ordinal()] = new WritableImage(strip.getPixelReader(),
                    expression.ordinal() * width, 0, width, height);
        }
        return expressions;
    }

    public static Image image(Expression expression) {
        return EXPRESSIONS[expression.ordinal()];
    }

    public static ImageView portrait(Expression expression, double size) {
        ImageView portrait = new ImageView(image(expression));
        portrait.setFitWidth(size);
        portrait.setFitHeight(size);
        portrait.setPreserveRatio(true);
        portrait.setSmooth(true);
        portrait.setMouseTransparent(true);
        portrait.setAccessibleText("Lupo, el zorro de LevelUp Code");
        return portrait;
    }

    public static HBox brand() {
        Label name = new Label("LevelUp Code");
        name.getStyleClass().add("lupo-brand-name");
        Label tagline = new Label("Aprende. Practica. Avanza.");
        tagline.getStyleClass().add("lupo-brand-tagline");
        VBox words = new VBox(3, name, tagline);
        HBox brand = new HBox(9, portrait(Expression.WELCOME, 48), words);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("lupo-brand");
        return brand;
    }
}
