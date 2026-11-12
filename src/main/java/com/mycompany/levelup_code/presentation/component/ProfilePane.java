package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.data.oracle.OraclePreferenceRepository;
import com.mycompany.levelup_code.domain.model.EditorPreferences;
import com.mycompany.levelup_code.domain.model.User;
import java.util.concurrent.Callable;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class ProfilePane extends VBox {
    private final Label status = new Label();
    private final VBox controls = new VBox(12);
    private final PasswordField currentPassword = new PasswordField();
    private final PasswordField newPassword = new PasswordField();
    private final PasswordField confirmation = new PasswordField();

    public ProfilePane(User user, boolean oracle, Runnable profileChanged) {
        super(14);
        Label title = new Label("Mi perfil");
        title.getStyleClass().add("page-title");
        status.setWrapText(true);
        TextField firstName = new TextField(user.getFirstName());
        TextField lastName = new TextField(user.getLastName());
        firstName.setPromptText("Nombre");
        lastName.setPromptText("Apellido");
        firstName.setAccessibleText("Nombre");
        lastName.setAccessibleText("Apellido");
        Button saveProfile = new Button("Guardar nombre");
        saveProfile.getStyleClass().add("btn-primary");
        saveProfile.setOnAction(event -> {
            String first = firstName.getText();
            String last = lastName.getText();
            run(() -> { AppContext.getInstance().getUserService().updateProfile(user.getId(), first, last); return true; }, () -> {
                user.setFirstName(first.strip());
                user.setLastName(last.strip());
                user.setUsername(first.strip() + " " + last.strip());
                profileChanged.run();
            }, "Nombre actualizado.");
        });
        currentPassword.setPromptText("Contraseña actual");
        newPassword.setPromptText("Nueva contraseña, mínimo 8 caracteres");
        confirmation.setPromptText("Repite la nueva contraseña");
        currentPassword.setAccessibleText("Contraseña actual");
        newPassword.setAccessibleText("Nueva contraseña");
        confirmation.setAccessibleText("Confirmación de contraseña");
        Button changePassword = new Button("Cambiar contraseña");
        changePassword.setOnAction(event -> {
            String current = currentPassword.getText();
            String replacement = newPassword.getText();
            if (!replacement.equals(confirmation.getText())) {
                status.setText("Las nuevas contraseñas no coinciden.");
                return;
            }
            run(() -> { AppContext.getInstance().getUserService().changePassword(user.getId(), current, replacement); return true; },
                    () -> { }, "Contraseña actualizada.");
        });
        VBox account = new VBox(10, new Label(user.getEmail()), firstName, lastName, saveProfile,
                currentPassword, newPassword, confirmation, changePassword);
        account.getStyleClass().add("surface-card");
        account.setDisable(!oracle);
        EditorPreferences preferences = EditorPreferences.forUser(user.getId());
        ComboBox<String> language = new ComboBox<>();
        language.getItems().setAll("JAVA", "PYTHON", "CPP");
        language.setValue(preferences.language());
        Spinner<Integer> size = new Spinner<>(12, 24, preferences.fontSize());
        size.setAccessibleText("Tamaño de letra del editor");
        ComboBox<Integer> indent = new ComboBox<>();
        indent.getItems().setAll(2, 4, 8);
        indent.setValue(preferences.indentSpaces());
        indent.setAccessibleText("Espacios de indentación");
        CheckBox reduceMotion = new CheckBox("Reducir animaciones");
        reduceMotion.setSelected(preferences.reducedMotion());
        Button savePreferences = new Button("Guardar preferencias");
        savePreferences.getStyleClass().add("btn-primary");
        savePreferences.setOnAction(event -> {
            EditorPreferences selected = new EditorPreferences(language.getValue(), size.getValue(),
                    indent.getValue(), reduceMotion.isSelected());
            run(() -> {
                if (oracle) new OraclePreferenceRepository().save(user.getId(), selected);
                return true;
            }, () -> {
                EditorPreferences.remember(user.getId(), selected);
                Theme.setReducedMotion(selected.reducedMotion());
            }, oracle ? "Preferencias guardadas. Se aplicarán a los próximos editores y al iniciar sesión."
                    : "Preferencias aplicadas durante esta sesión de prueba.");
        });
        VBox settings = new VBox(10, new Label("Lenguaje inicial"), language,
                new Label("Tamaño de letra"), size, new Label("Espacios al pulsar Tab"), indent, reduceMotion, savePreferences);
        settings.getStyleClass().add("surface-card");
        controls.getChildren().addAll(account, settings);
        getChildren().addAll(title, status, controls);
        if (!oracle) status.setText("La edición de la cuenta requiere Oracle. Puedes probar las preferencias durante esta sesión.");
    }

    private void clearPasswords() {
        currentPassword.clear();
        newPassword.clear();
        confirmation.clear();
    }

    private void run(Callable<Boolean> action, Runnable success, String message) {
        controls.setDisable(true);
        status.setText("Guardando…");
        Task<Boolean> task = new Task<>() { protected Boolean call() throws Exception { return action.call(); } };
        task.setOnSucceeded(event -> {
            controls.setDisable(false);
            clearPasswords();
            success.run();
            status.setText(message);
        });
        task.setOnFailed(event -> {
            controls.setDisable(false);
            clearPasswords();
            status.setText(task.getException() instanceof IllegalArgumentException
                    ? task.getException().getMessage() : "No se pudo guardar. Comprueba la conexión y la instalación de las preferencias e inténtalo de nuevo.");
        });
        Thread worker = new Thread(task, "profile-save");
        worker.setDaemon(true);
        worker.start();
    }
}
