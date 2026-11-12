package com.mycompany.levelup_code.presentation.component;

import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

/**
 * Sistema de diseño centralizado en JavaFX + CSS ({@code /styles/levelup.css}).
 * Proporciona iconos vectoriales renderizados en {@link Canvas}, insignias tipo píldora,
 * cabeceras de sección y transiciones animadas sin dependencias de AWT/Swing.
 */
public final class Theme {

    // Paleta cromática hexadecimal de referencia para iconos vectoriales
    public static final String HEX_PRIMARY = "#0F172A";
    public static final String HEX_SECONDARY = "#334155";
    public static final String HEX_MUTED = "#64748B";
    public static final String HEX_INDIGO = "#2563EB";
    public static final String HEX_GREEN = "#16A34A";
    public static final String HEX_AMBER = "#D97706";
    public static final String HEX_ROSE = "#DC2626";
    public static final String HEX_VIOLET = "#7C3AED";
    public static final String HEX_CYAN = "#0284C7";
    public static final String HEX_WHITE = "#FFFFFF";

    // Identidad cromática por lenguaje
    public static final String HEX_JAVA = "#E76F00";
    public static final String HEX_PYTHON = "#3776AB";
    public static final String HEX_CPP = "#00599C";

    public enum IconKind {
        TERMINAL, CODE, BOOK, TROPHY, FLAME, HEART, SHIELD,
        CHECK, LOCK, PLAY, SPARK, USER, LOGOUT, STAR, BOLT
    }

    private static boolean reducedMotion;

    public static void setReducedMotion(boolean value) { reducedMotion = value; }

    private Theme() {
    }

    /**
     * Vincula la hoja de estilos oficial {@code /styles/levelup.css} a una escena JavaFX.
     */
    public static void applyCss(Scene scene) {
        if (scene == null) {
            return;
        }
        URL cssUrl = Theme.class.getResource("/styles/levelup.css");
        if (cssUrl != null) {
            String ext = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(ext)) {
                scene.getStylesheets().add(ext);
            }
        }
    }

    /**
     * Crea una insignia tipo píldora (Pill Badge) estilizada por clase CSS.
     */
    public static Label createPillBadge(String text, String badgeStyleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().add(badgeStyleClass != null ? badgeStyleClass : "badge-indigo");
        return badge;
    }

    /**
     * Construye un encabezado de sección con título a la izquierda e insignia opcional a la derecha.
     */
    public static HBox createSectionHeader(String titleText, String badgeText, String badgeStyleClass) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(titleText != null ? titleText.toUpperCase() : "");
        lblTitle.getStyleClass().add("section-caption");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(lblTitle, spacer);
        if (badgeText != null && !badgeText.trim().isEmpty()) {
            header.getChildren().add(createPillBadge(badgeText, badgeStyleClass));
        }
        return header;
    }

    /**
     * Ejecuta una transición suave de opacidad al cambiar de vista en el espacio de trabajo.
     */
    public static void playFadeIn(Node node) {
        if (node == null || reducedMotion) {
            return;
        }
        FadeTransition ft = new FadeTransition(Duration.millis(210), node);
        ft.setFromValue(0.28);
        ft.setToValue(1.0);
        ft.play();
    }

    /**
     * Ejecuta un pulso suave de escala y opacidad para retroalimentación interactiva al aprobar retos.
     */
    public static void playPulse(Node node) {
        if (node == null || reducedMotion) {
            return;
        }
        ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
        st.setFromX(0.96);
        st.setFromY(0.96);
        st.setToX(1.0);
        st.setToY(1.0);

        FadeTransition ft = new FadeTransition(Duration.millis(160), node);
        ft.setFromValue(0.65);
        ft.setToValue(1.0);

        new ParallelTransition(st, ft).play();
    }

    /**
     * Crea una insignia de dificultad según la recompensa de experiencia del reto.
     */
    public static Label createDifficultyBadge(int expReward) {
        if (expReward <= 65) {
            return createPillBadge("NIVEL FÁCIL", "badge-green");
        } else if (expReward <= 85) {
            return createPillBadge("NIVEL MEDIO", "badge-amber");
        }
        return createPillBadge("NIVEL DIFÍCIL", "badge-rose");
    }

    /**
     * Renderiza un icono vectorial nítido sobre un {@link Canvas} de JavaFX.
     */
    public static Canvas createIcon(IconKind kind, double size, String hexColor) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Color color = Color.web(hexColor != null ? hexColor : HEX_INDIGO);

        gc.setStroke(color);
        gc.setFill(color);
        gc.setLineWidth(Math.max(1.7, size / 8.5));
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double s = size;
        switch (kind) {
            case TERMINAL:
                gc.strokeRoundRect(2, 2, s - 4, s - 4, 4, 4);
                gc.strokeLine(5, s / 2.0 - 3, 8, s / 2.0);
                gc.strokeLine(8, s / 2.0, 5, s / 2.0 + 3);
                gc.strokeLine(10, s / 2.0 + 3, s - 5, s / 2.0 + 3);
                break;

            case CODE:
                gc.strokeLine(s / 3.0, 4, 2, s / 2.0);
                gc.strokeLine(2, s / 2.0, s / 3.0, s - 4);
                gc.strokeLine(s - s / 3.0, 4, s - 2, s / 2.0);
                gc.strokeLine(s - 2, s / 2.0, s - s / 3.0, s - 4);
                break;

            case BOOK:
                gc.strokeRoundRect(3, 2, s - 6, s - 4, 3, 3);
                gc.strokeLine(7, 2, 7, s - 2);
                gc.strokeLine(10, 6, s - 6, 6);
                gc.strokeLine(10, 10, s - 6, 10);
                break;

            case TROPHY:
                gc.strokeArc(4, 2, s - 8, s - 8, 180, 180, ArcType.OPEN);
                gc.strokeLine(4, 2, s - 4, 2);
                gc.strokeLine(s / 2.0, s - 6, s / 2.0, s - 2);
                gc.strokeLine(5, s - 2, s - 5, s - 2);
                break;

            case FLAME:
                double[] fx = {s / 2.0, s - 3, s * 0.72, s / 2.0, s * 0.28, 3};
                double[] fy = {2, s * 0.55, s - 2, s * 0.75, s - 2, s * 0.55};
                gc.fillPolygon(fx, fy, fx.length);
                break;

            case HEART:
                gc.fillOval(2, 3, s / 2.0 - 1, s / 2.0 - 1);
                gc.fillOval(s / 2.0 - 1, 3, s / 2.0 - 1, s / 2.0 - 1);
                double[] hx = {2, s - 2, s / 2.0};
                double[] hy = {s / 2.0, s / 2.0, s - 2};
                gc.fillPolygon(hx, hy, 3);
                break;

            case SHIELD:
                double[] sx = {s / 2.0, s - 3, s - 4, s / 2.0, 4, 3};
                double[] sy = {2, 5, s * 0.62, s - 2, s * 0.62, 5};
                gc.strokePolygon(sx, sy, sx.length);
                break;

            case CHECK:
                gc.strokeLine(3, s / 2.0, s / 2.0 - 1, s - 4);
                gc.strokeLine(s / 2.0 - 1, s - 4, s - 3, 4);
                break;

            case LOCK:
                gc.strokeRoundRect(3, s / 2.0 - 1, s - 6, s / 2.0 - 1, 3, 3);
                gc.strokeArc(5, 2, s - 10, s / 2.0 + 2, 0, 180, ArcType.OPEN);
                break;

            case PLAY:
                double[] px = {4, 4, s - 3};
                double[] py = {3, s - 3, s / 2.0};
                gc.fillPolygon(px, py, 3);
                break;

            case USER:
                gc.strokeOval(s / 2.0 - 3, 2, 6, 6);
                gc.strokeArc(3, s / 2.0 + 1, s - 6, s / 2.0, 0, 180, ArcType.OPEN);
                break;

            case LOGOUT:
                gc.strokeArc(2, 3, s - 6, s - 6, 90, 180, ArcType.OPEN);
                gc.strokeLine(s / 2.0 - 1, s / 2.0, s - 2, s / 2.0);
                gc.strokeLine(s - 5, s / 2.0 - 3, s - 2, s / 2.0);
                gc.strokeLine(s - 5, s / 2.0 + 3, s - 2, s / 2.0);
                break;

            case STAR:
            case SPARK:
            case BOLT:
            default:
                double[] bx = {s * 0.56, s * 0.24, s * 0.48, s * 0.42, s * 0.76, s * 0.52};
                double[] by = {2, s * 0.54, s * 0.54, s - 2, s * 0.44, s * 0.44};
                gc.fillPolygon(bx, by, bx.length);
                break;
        }
        return canvas;
    }
}
