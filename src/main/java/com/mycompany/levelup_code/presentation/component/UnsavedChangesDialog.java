package com.mycompany.levelup_code.presentation.component;

import javafx.stage.Window;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

final class UnsavedChangesDialog {
    static final ButtonType SAVE = new ButtonType("Guardar", ButtonBar.ButtonData.YES);
    static final ButtonType DISCARD = new ButtonType("Descartar", ButtonBar.ButtonData.NO);

    private UnsavedChangesDialog() { }

    static ButtonType choose(Window owner, String message) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, message, SAVE, DISCARD, ButtonType.CANCEL);
        dialog.setTitle("Cambios pendientes");
        dialog.setHeaderText("Tienes cambios sin guardar");
        dialog.initOwner(owner);
        Theme.applyCss(dialog.getDialogPane().getScene());
        return dialog.showAndWait().orElse(ButtonType.CANCEL);
    }

    static void showBusy(Window owner) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION, "Espera a que termine la operación en curso y vuelve a intentarlo.");
        dialog.initOwner(owner);
        Theme.applyCss(dialog.getDialogPane().getScene());
        dialog.showAndWait();
    }
}
