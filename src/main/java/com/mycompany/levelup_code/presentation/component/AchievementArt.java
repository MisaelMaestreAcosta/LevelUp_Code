package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Achievement;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/** Branded artwork for the four achievement families. */
final class AchievementArt {
    private static final Image[] BADGES = load();

    private AchievementArt() { }

    private static Image[] load() {
        var resource = java.util.Objects.requireNonNull(AchievementArt.class.getResource("/images/lupo/achievements.png"));
        Image strip = new Image(resource.toExternalForm());
        if (strip.isError()) throw new IllegalStateException("No se pudieron cargar las insignias.", strip.getException());
        int cellWidth = (int) strip.getWidth() / 4;
        int size = Math.min(cellWidth, (int) strip.getHeight());
        int top = ((int) strip.getHeight() - size) / 2;
        Image[] images = new Image[4];
        for (int index = 0; index < images.length; index++) {
            images[index] = new WritableImage(strip.getPixelReader(), index * cellWidth, top, size, size);
        }
        return images;
    }

    static ImageView badge(Achievement achievement, boolean unlocked, double size) {
        int index = switch (achievement.getAchievementType()) {
            case "CHALLENGES" -> 1;
            case "EXPERIENCE" -> 2;
            case "STREAK" -> 3;
            default -> 0;
        };
        ImageView view = image(index, size);
        view.setAccessibleText(achievement.getName() + (unlocked ? ", desbloqueado" : ", en progreso"));
        if (!unlocked) {
            ColorAdjust muted = new ColorAdjust();
            muted.setSaturation(-1);
            view.setEffect(muted);
            view.setOpacity(0.55);
        }
        return view;
    }

    static ImageView medal(double size) { return image(0, size); }

    private static ImageView image(int index, double size) {
        ImageView view = new ImageView(BADGES[index]);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }
}
