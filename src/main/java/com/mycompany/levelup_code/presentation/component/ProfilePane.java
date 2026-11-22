package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.User;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Perfil del estudiante, edición de cuenta y vitrina de logros obtenidos. */
public final class ProfilePane extends VBox {
    private final User user;
    private final Label status = new Label();
    private final VBox editableControls = new VBox(14);
    private final PasswordField currentPassword = new PasswordField();
    private final PasswordField newPassword = new PasswordField();
    private final PasswordField confirmation = new PasswordField();
    private final Label profileDisplayName = new Label();
    private final Label achievementsCount = new Label();
    private final ProgressBar achievementsProgress = new ProgressBar(0);
    private final FlowPane achievementsGallery = new FlowPane(12, 12);

    public ProfilePane(User user, boolean oracle, Runnable profileChanged,
            List<Achievement> catalog, Set<Integer> unlockedIds, Runnable openAllAchievements) {
        super(18);
        this.user = user;
        getStyleClass().add("profile-pane");
        setFillWidth(true);

        Label title = new Label("Mi perfil");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Administra tu información y celebra todo lo que has conseguido.");
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        status.getStyleClass().add("profile-status");
        status.setWrapText(true);
        status.setVisible(false);
        status.setManaged(false);

        getChildren().addAll(new VBox(4, title, subtitle), buildHero(),
                buildAccountCard(oracle, profileChanged), status,
                buildAchievementsCard(openAllAchievements));
        refreshAchievements(catalog, unlockedIds);
    }

    private HBox buildHero() {
        StackPane avatar = new StackPane(Theme.createIcon(Theme.IconKind.USER, 30, Theme.HEX_WHITE));
        avatar.getStyleClass().add("profile-avatar");
        avatar.setAccessibleText("Avatar de " + user.getUsername());

        profileDisplayName.setText(user.getUsername());
        profileDisplayName.getStyleClass().add("profile-name");
        Label email = new Label(user.getEmail());
        email.getStyleClass().add("profile-email");

        HBox stats = new HBox(8,
                metric("NIVEL " + user.getLevel(), "badge-indigo"),
                metric(user.getTotalExp() + " XP", "badge-amber"),
                metric(user.getCurrentStreak() + " DÍAS DE RACHA", "badge-green"));
        stats.setAlignment(Pos.CENTER_LEFT);

        VBox identity = new VBox(4, profileDisplayName, email, stats);
        identity.setMinWidth(0);
        HBox.setHgrow(identity, Priority.ALWAYS);

        HBox hero = new HBox(16, avatar, identity);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("profile-hero");
        return hero;
    }

    private Label metric(String text, String styleClass) {
        Label label = Theme.createPillBadge(text, styleClass);
        label.getStyleClass().add("profile-metric");
        return label;
    }

    private VBox buildAccountCard(boolean oracle, Runnable profileChanged) {
        Label heading = new Label("Información personal");
        heading.getStyleClass().add("card-title");
        Label helper = new Label("Así aparecerá tu nombre dentro de LevelUp Code.");
        helper.getStyleClass().add("muted-text");

        TextField firstName = new TextField(user.getFirstName());
        TextField lastName = new TextField(user.getLastName());
        firstName.setPromptText("Nombre");
        lastName.setPromptText("Apellido");
        firstName.setAccessibleText("Nombre");
        lastName.setAccessibleText("Apellido");

        VBox firstField = field("NOMBRE", firstName);
        VBox lastField = field("APELLIDO", lastName);
        HBox nameFields = new HBox(12, firstField, lastField);
        HBox.setHgrow(firstField, Priority.ALWAYS);
        HBox.setHgrow(lastField, Priority.ALWAYS);
        firstField.setMaxWidth(Double.MAX_VALUE);
        lastField.setMaxWidth(Double.MAX_VALUE);

        Button saveProfile = new Button("Guardar cambios");
        saveProfile.getStyleClass().add("btn-primary");
        saveProfile.setGraphic(Theme.createIcon(Theme.IconKind.CHECK, 15, Theme.HEX_WHITE));
        saveProfile.setOnAction(event -> {
            String first = firstName.getText();
            String last = lastName.getText();
            run(() -> {
                AppContext.getInstance().getUserService().updateProfile(user.getId(), first, last);
                return true;
            }, () -> {
                user.setFirstName(first.strip());
                user.setLastName(last.strip());
                user.setUsername(first.strip() + " " + last.strip());
                profileDisplayName.setText(user.getUsername());
                profileChanged.run();
            }, "Nombre actualizado correctamente.");
        });

        TitledPane passwordPane = buildPasswordPane();
        firstName.setDisable(!oracle);
        lastName.setDisable(!oracle);
        saveProfile.setDisable(!oracle);
        passwordPane.setDisable(!oracle);

        editableControls.getChildren().setAll(nameFields, saveProfile, passwordPane);
        VBox card = new VBox(6, heading, helper, editableControls);
        VBox.setMargin(editableControls, new javafx.geometry.Insets(10, 0, 0, 0));
        if (!oracle) {
            Label offline = new Label("La edición de la cuenta estará disponible al conectarte con Oracle.");
            offline.getStyleClass().add("profile-offline-note");
            offline.setWrapText(true);
            card.getChildren().add(offline);
        }
        card.getStyleClass().addAll("surface-card", "profile-account-card");
        return card;
    }

    private VBox field(String caption, TextField input) {
        Label label = new Label(caption);
        label.getStyleClass().add("field-caption");
        input.setMaxWidth(Double.MAX_VALUE);
        return new VBox(6, label, input);
    }

    private TitledPane buildPasswordPane() {
        currentPassword.setPromptText("Contraseña actual");
        newPassword.setPromptText("Nueva contraseña, mínimo 8 caracteres");
        confirmation.setPromptText("Repite la nueva contraseña");
        currentPassword.setAccessibleText("Contraseña actual");
        newPassword.setAccessibleText("Nueva contraseña");
        confirmation.setAccessibleText("Confirmación de contraseña");

        Button changePassword = new Button("Actualizar contraseña");
        changePassword.getStyleClass().add("btn-secondary");
        changePassword.setOnAction(event -> {
            String current = currentPassword.getText();
            String replacement = newPassword.getText();
            if (!replacement.equals(confirmation.getText())) {
                showStatus("Las nuevas contraseñas no coinciden.", true);
                return;
            }
            run(() -> {
                AppContext.getInstance().getUserService().changePassword(user.getId(), current, replacement);
                return true;
            }, () -> { }, "Contraseña actualizada correctamente.");
        });

        VBox content = new VBox(10, currentPassword, newPassword, confirmation, changePassword);
        content.getStyleClass().add("password-content");
        TitledPane pane = new TitledPane("Seguridad y contraseña", content);
        pane.getStyleClass().add("profile-password-pane");
        pane.setExpanded(false);
        pane.setAnimated(false);
        return pane;
    }

    private VBox buildAchievementsCard(Runnable openAllAchievements) {
        Label heading = new Label("Logros obtenidos");
        heading.getStyleClass().add("card-title");
        Label helper = new Label("Tus insignias cuentan la historia de tu progreso.");
        helper.getStyleClass().add("muted-text");

        achievementsCount.getStyleClass().add("profile-achievement-count");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, new VBox(4, heading, helper), spacer, achievementsCount);
        header.setAlignment(Pos.CENTER_LEFT);

        achievementsProgress.setMaxWidth(Double.MAX_VALUE);
        achievementsProgress.getStyleClass().add("profile-achievement-progress");

        achievementsGallery.getStyleClass().add("profile-achievements-gallery");
        achievementsGallery.setAlignment(Pos.TOP_LEFT);

        Button viewAll = new Button("Ver todos los logros");
        viewAll.getStyleClass().add("btn-ghost");
        viewAll.setGraphic(Theme.createIcon(Theme.IconKind.TROPHY, 15, Theme.HEX_AMBER));
        viewAll.setOnAction(event -> openAllAchievements.run());

        VBox card = new VBox(14, header, achievementsProgress, achievementsGallery, viewAll);
        card.getStyleClass().addAll("surface-card", "profile-achievements-card");
        return card;
    }

    public void refreshAchievements(List<Achievement> catalog, Set<Integer> unlockedIds) {
        List<Achievement> safeCatalog = catalog == null ? List.of() : catalog;
        Set<Integer> safeUnlocked = unlockedIds == null ? Set.of() : unlockedIds;
        List<Achievement> unlocked = safeCatalog.stream()
                .filter(achievement -> safeUnlocked.contains(achievement.getId()))
                .sorted(Comparator.comparingInt(Achievement::getId))
                .toList();

        achievementsCount.setText(unlocked.size() + " de " + safeCatalog.size());
        achievementsProgress.setProgress(safeCatalog.isEmpty() ? 0 : unlocked.size() / (double) safeCatalog.size());
        achievementsProgress.setAccessibleText(unlocked.size() + " de " + safeCatalog.size() + " logros obtenidos");
        achievementsGallery.getChildren().clear();

        if (unlocked.isEmpty()) {
            Label emptyTitle = new Label("Tu primera insignia está cerca");
            emptyTitle.getStyleClass().add("card-title");
            Label emptyText = new Label("Completa retos, gana XP y mantén tu racha para desbloquear logros.");
            emptyText.getStyleClass().add("muted-text");
            emptyText.setWrapText(true);
            VBox empty = new VBox(6, emptyTitle, emptyText);
            empty.getStyleClass().add("profile-achievements-empty");
            achievementsGallery.getChildren().add(empty);
            return;
        }

        for (Achievement achievement : unlocked) {
            Label name = new Label(achievement.getName());
            name.getStyleClass().add("profile-badge-name");
            name.setWrapText(true);
            Label description = new Label(achievement.getDescription());
            description.getStyleClass().add("muted-text");
            description.setWrapText(true);
            VBox text = new VBox(3, name, description);
            text.setMinWidth(0);
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox badge = new HBox(10, AchievementArt.badge(achievement, true, 48), text);
            badge.setAlignment(Pos.CENTER_LEFT);
            badge.getStyleClass().add("profile-achievement-badge");
            achievementsGallery.getChildren().add(badge);
        }
    }

    private void clearPasswords() {
        currentPassword.clear();
        newPassword.clear();
        confirmation.clear();
    }

    private void showStatus(String message, boolean error) {
        status.setText(message);
        status.getStyleClass().removeAll("profile-status-success", "profile-status-error");
        status.getStyleClass().add(error ? "profile-status-error" : "profile-status-success");
        status.setVisible(true);
        status.setManaged(true);
    }

    private void run(Callable<Boolean> action, Runnable success, String message) {
        editableControls.setDisable(true);
        showStatus("Guardando cambios…", false);
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return action.call();
            }
        };
        task.setOnSucceeded(event -> {
            editableControls.setDisable(false);
            clearPasswords();
            success.run();
            showStatus(message, false);
        });
        task.setOnFailed(event -> {
            editableControls.setDisable(false);
            clearPasswords();
            showStatus(task.getException() instanceof IllegalArgumentException
                    ? task.getException().getMessage()
                    : "No se pudo guardar. Comprueba la conexión e inténtalo de nuevo.", true);
        });
        Thread worker = new Thread(task, "profile-save");
        worker.setDaemon(true);
        worker.start();
    }
}
