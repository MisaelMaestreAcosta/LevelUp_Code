package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Displays the completed outcome after evaluation and persistence have finished. */
final class CompletionDialog {
    private CompletionDialog() { }

    static void show(Window owner, String challenge, int xp, User user, int previousLevel,
            boolean saved, Runnable next) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("LevelUp Code · ¡Buen trabajo!");
        javafx.scene.layout.HBox artwork = new javafx.scene.layout.HBox(12,
                Lupo.portrait(Lupo.Expression.CELEBRATING, 115), AchievementArt.medal(80));
        artwork.setAlignment(Pos.CENTER);
        VBox content = new VBox(12, artwork,
                WorkspaceUi.label(saved ? "¡Reto superado!" : "¡Práctica completada!", "home-greeting"),
                WorkspaceUi.label(challenge, "card-title"));
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(18));
        content.setPrefWidth(400);
        content.getStyleClass().addAll("learning-workspace", "completion-content");
        if (saved && user != null) {
            content.getChildren().add(WorkspaceUi.label("Resultado guardado · +" + xp + " XP recibidos", "completion-reward"));
            if (user.getLevel() > previousLevel) {
                content.getChildren().add(WorkspaceUi.label("¡Alcanzaste el nivel " + user.getLevel() + "!", "card-title"));
            }
            int remaining = 300 - user.getTotalExp() % 300;
            ProgressBar progress = new ProgressBar((user.getTotalExp() % 300) / 300.0);
            progress.setMaxWidth(Double.MAX_VALUE);
            progress.setAccessibleText("Faltan " + remaining + " XP para el siguiente nivel");
            content.getChildren().addAll(progress, WorkspaceUi.label("Faltan " + remaining
                    + " XP para el nivel " + (user.getLevel() + 1), "muted-text"));
        } else {
            content.getChildren().add(WorkspaceUi.label("Resolviste el ejercicio en práctica local.", "body-text"));
        }
        dialog.getDialogPane().setContent(content);
        ButtonType done = new ButtonType("Seguir practicando", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType continueButton = new ButtonType("Siguiente reto", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(done);
        if (next != null) dialog.getDialogPane().getButtonTypes().add(continueButton);
        Theme.applyCss(dialog.getDialogPane().getScene());
        dialog.getDialogPane().lookupButton(done).getStyleClass().add(next == null ? "btn-primary" : "btn-secondary");
        if (next != null) dialog.getDialogPane().lookupButton(continueButton).getStyleClass().add("btn-primary");
        dialog.setOnShown(event -> Theme.playPulse(content));
        dialog.setOnHidden(event -> { if (dialog.getResult() == continueButton && next != null) next.run(); });
        dialog.show();
    }
}
