package com.mycompany.levelup_code.service;

import com.mycompany.levelup_code.model.Achievement;
import com.mycompany.levelup_code.model.User;
import com.mycompany.levelup_code.model.UserAchievement;
import com.mycompany.levelup_code.model.UserStats;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Evalúa los criterios de logros y registra los desbloqueos del usuario. */
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public AchievementService(AchievementRepository achievementRepository) {
        if (achievementRepository == null) {
            throw new IllegalArgumentException(
                    "El repositorio de logros es obligatorio.");
        }
        this.achievementRepository = achievementRepository;
    }

    /**
     * Comprueba las estadísticas actuales y registra cada logro nuevo que cumpla
     * el usuario. Devuelve solo los desbloqueos creados en esta llamada.
     */
    public List<UserAchievement> evaluateAndUnlock(User user) {
        validateUser(user);

        Optional<UserStats> statsResult =
                achievementRepository.findStatsByUserId(user.getId());
        if (statsResult == null || !statsResult.isPresent()) {
            throw new IllegalStateException(
                    "No se encontraron estadísticas para el usuario.");
        }

        List<Achievement> available =
                achievementRepository.findNotUnlockedByUser(user.getId());
        if (available == null) {
            throw new IllegalStateException(
                    "El repositorio devolvió una lista de logros inválida.");
        }

        UserStats stats = statsResult.get();
        List<UserAchievement> unlockedNow = new ArrayList<>();

        for (Achievement achievement : available) {
            if (achievement == null || achievement.getId() <= 0) {
                throw new IllegalStateException(
                        "El repositorio devolvió un logro inválido.");
            }

            // El subtipo de Achievement evalúa su propio criterio.
            if (!achievement.isUnlockedBy(stats)) {
                continue;
            }

            UserAchievement unlock = new UserAchievement();
            unlock.setUser(user);
            unlock.setAchievement(achievement);

            // El contrato pide guardar el desbloqueo sin crear duplicados.
            Optional<UserAchievement> savedUnlock =
                    achievementRepository.saveIfNotUnlocked(unlock);
            if (savedUnlock == null) {
                throw new IllegalStateException(
                        "El repositorio devolvió un resultado de guardado inválido.");
            }
            savedUnlock.ifPresent(unlockedNow::add);
        }

        return Collections.unmodifiableList(unlockedNow);
    }

    /** Devuelve el historial de logros desbloqueados por el usuario. */
    public List<UserAchievement> listUnlockedBy(User user) {
        validateUser(user);

        List<UserAchievement> unlocked =
                achievementRepository.findUnlockedByUser(user.getId());
        if (unlocked == null) {
            throw new IllegalStateException(
                    "El repositorio devolvió una lista de desbloqueos inválida.");
        }
        return Collections.unmodifiableList(new ArrayList<>(unlocked));
    }

    private void validateUser(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("El usuario debe ser válido.");
        }
    }
}
