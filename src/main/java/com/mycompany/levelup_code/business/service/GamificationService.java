package com.mycompany.levelup_code.business.service;

import com.mycompany.levelup_code.data.dao.ActivityRepository;
import com.mycompany.levelup_code.data.dao.UserRepository;

import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.User;
import java.time.LocalDate;
import java.util.Optional;

/** Actualiza experiencia, nivel, actividad diaria y logros del usuario. */
public class GamificationService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final LevelPolicy levelPolicy;
    private final AchievementService achievementService;

    /** Recibe los contratos y servicios necesarios para aplicar la gamificación. */
    public GamificationService(
            UserRepository userRepository,
            ActivityRepository activityRepository,
            LevelPolicy levelPolicy,
            AchievementService achievementService) {

        if (userRepository == null
                || activityRepository == null
                || levelPolicy == null
                || achievementService == null) {
            throw new IllegalArgumentException(
                    "Los repositorios y servicios de gamificación son obligatorios."
            );
        }

        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.levelPolicy = levelPolicy;
        this.achievementService = achievementService;
    }

    /** Otorga experiencia, actualiza la actividad diaria y evalúa logros. */
    public void awardExperience(User user, int points) {
        validateAward(user, points);

        LocalDate today = LocalDate.now();
        Optional<DailyActivity> todayActivity =
                activityRepository.findByUserAndDate(user.getId(), today);

        if (todayActivity.isPresent()) {
            // Suma la experiencia a la actividad que ya existe hoy.
            DailyActivity activity = todayActivity.get();
            activity.setExperienceGained(
                    activity.getExperienceGained() + points
            );
            activityRepository.saveOrUpdate(activity);
        } else {
            // Registra la primera actividad del día y actualiza la racha.
            updateStreak(user, today);

            DailyActivity activity = new DailyActivity();
            activity.setUser(user);
            activity.setActivityDate(today);
            activity.setExperienceGained(points);
            activityRepository.saveOrUpdate(activity);
        }

        // Actualiza el total de XP y calcula el nivel correspondiente.
        int totalExperience = user.getExperience() + points;
        user.setExperience(totalExperience);
        user.setLevel(levelPolicy.levelForExperience(totalExperience));
        userRepository.update(user);

        // Evalúa los logros con las estadísticas ya actualizadas.
        achievementService.evaluateAndUnlock(user);
    }

    /** Continúa la racha si hubo actividad ayer; si no, la inicia de nuevo. */
    private void updateStreak(User user, LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        Optional<DailyActivity> previousActivity =
                activityRepository.findByUserAndDate(user.getId(), yesterday);

        if (previousActivity.isPresent()) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else {
            user.setCurrentStreak(1);
        }

        if (user.getCurrentStreak() > user.getMaxStreak()) {
            user.setMaxStreak(user.getCurrentStreak());
        }
    }

    /** Valida el usuario y que la recompensa sea positiva. */
    private void validateAward(User user, int points) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("El usuario debe ser válido.");
        }

        if (points <= 0) {
            throw new IllegalArgumentException(
                    "La experiencia otorgada debe ser mayor que cero."
            );
        }
    }
}
