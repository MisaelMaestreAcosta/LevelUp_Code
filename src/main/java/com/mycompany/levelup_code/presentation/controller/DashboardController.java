package com.mycompany.levelup_code.presentation.controller;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.service.AchievementService;
import com.mycompany.levelup_code.business.service.ContentService;
import com.mycompany.levelup_code.business.service.EnrollmentService;
import com.mycompany.levelup_code.data.dao.AchievementRepository;
import com.mycompany.levelup_code.data.dao.ActivityRepository;
import com.mycompany.levelup_code.data.dao.ProgressRepository;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.domain.model.UserAchievement;
import com.mycompany.levelup_code.domain.model.UserStats;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Controlador de presentación del panel principal ({@code DashboardFrame}).
 * Separa las consultas, reglas de visualización, inscripciones, logros y administración
 * de los componentes gráficos JavaFX para permitir pruebas unitarias aisladas.
 */
public class DashboardController {

    private final boolean oracleConnected;
    private final User currentUser;
    private final ContentService contentService;
    private final EnrollmentService enrollmentService;
    private final AchievementService achievementService;
    private final ProgressRepository progressRepository;
    private final ActivityRepository activityRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public DashboardController(User currentUser, AppContext ctx) {
        this(
                ctx.isOracleConnected(),
                currentUser,
                ctx.getContentService(),
                ctx.getEnrollmentService(),
                ctx.getAchievementService(),
                ctx.getProgressRepository(),
                ctx.getActivityRepository(),
                ctx.getAchievementRepository(),
                ctx.getUserRepository()
        );
    }

    public DashboardController(
            boolean oracleConnected,
            User currentUser,
            ContentService contentService,
            EnrollmentService enrollmentService,
            AchievementService achievementService,
            ProgressRepository progressRepository,
            ActivityRepository activityRepository,
            AchievementRepository achievementRepository,
            UserRepository userRepository) {
        this.oracleConnected = oracleConnected;
        this.currentUser = currentUser != null ? currentUser : DemoSeedCatalog.createDefaultStudent();
        this.contentService = contentService;
        this.enrollmentService = enrollmentService;
        this.achievementService = achievementService;
        this.progressRepository = progressRepository;
        this.activityRepository = activityRepository;
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isOracleConnected() {
        return oracleConnected;
    }

    public List<Language> loadLanguages() {
        if (oracleConnected) {
            if (contentService == null) throw new IllegalStateException("Catálogo Oracle no configurado.");
            return contentService.listActiveLanguages();
        }
        return DemoSeedCatalog.createSeedLanguages();
    }

    public Set<String> loadEnrolledLanguageKeys(Language javaLang, Language pythonLang, Language cppLang) {
        Set<String> keys = new HashSet<>();
        if (oracleConnected && currentUser.getId() > 0 && enrollmentService != null) {
            Set<Integer> enrolledIds = enrollmentService.listEnrolledLanguageIds(currentUser);
            if (javaLang != null && enrolledIds.contains(javaLang.getId())) keys.add("JAVA");
            if (pythonLang != null && enrolledIds.contains(pythonLang.getId())) keys.add("PYTHON");
            if (cppLang != null && enrolledIds.contains(cppLang.getId())) keys.add("CPP");
            return keys;
        }

        // Valores iniciales únicamente para el modo local de demostración sin Oracle
        keys.add("JAVA");
        keys.add("PYTHON");
        return keys;
    }

    public void enrollInLanguage(Language targetLang) {
        if (!oracleConnected || currentUser.getId() <= 0) {
            return;
        }
        if (targetLang == null || targetLang.getId() <= 0) {
            throw new IllegalArgumentException("El lenguaje seleccionado no es válido en Oracle.");
        }
        enrollmentService.enroll(currentUser, targetLang.getId());
    }

    public ProgressStatus resolveLessonProgressStatus(Lesson lesson, int indexInList) {
        if (oracleConnected && currentUser.getId() > 0) {
            if (lesson == null || lesson.getId() <= 0) {
                return ProgressStatus.PENDING;
            }
            if (progressRepository == null) throw new IllegalStateException("Progreso Oracle no configurado.");
            Optional<LessonProgress> lp = progressRepository.findByUserAndLesson(currentUser.getId(), lesson.getId());
            if (lp.isPresent() && lp.get().getStatus() != null) return lp.get().getStatus();
            // En modo Oracle, la ausencia de fila se interpreta estrictamente como PENDIENTE
            return ProgressStatus.PENDING;
        }

        // Progreso demostrativo reservado exclusivamente al modo local sin Oracle
        if (indexInList == 0) {
            return ProgressStatus.COMPLETED;
        } else if (indexInList == 1) {
            return ProgressStatus.IN_PROGRESS;
        }
        return ProgressStatus.PENDING;
    }

    public Map<LocalDate, Integer> loadRecentActivityHistory() {
        if (oracleConnected && currentUser.getId() > 0) {
            Map<LocalDate, Integer> fromDb = new HashMap<>();
            if (activityRepository == null) throw new IllegalStateException("Actividad Oracle no configurada.");
            List<DailyActivity> recent = activityRepository.findRecentByUser(currentUser.getId(), 14);
            for (DailyActivity da : recent) {
                if (da.getActivityDate() != null) fromDb.put(da.getActivityDate(), da.getExperienceGained());
            }
            return fromDb;
        }
        return DemoSeedCatalog.createSampleActivityHistory();
    }

    public List<Achievement> loadAchievementsCatalog() {
        if (oracleConnected) {
            if (achievementRepository == null) throw new IllegalStateException("Logros Oracle no configurados.");
            return achievementRepository.findAllCatalog();
        }
        return DemoSeedCatalog.createSeedAchievements();
    }

    /**
     * Resuelve los logros desbloqueados sin usar jamás estimaciones artificiales (como XP / 250)
     * ni insertar directamente desde la vista. En modo Oracle delega en {@link AchievementService},
     * que consulta {@code COUNT(DISTINCT id_reto)} sobre intentos aprobados reales.
     */
    public Set<Integer> resolveUnlockedAchievementIds(List<Achievement> catalog, int offlineSolvedChallenges) {
        Set<Integer> unlockedIds = new HashSet<>();
        if (oracleConnected && currentUser.getId() > 0) {
            if (achievementService == null) throw new IllegalStateException("Servicio de logros no configurado.");
            achievementService.evaluateAndUnlock(currentUser);
            List<UserAchievement> unlockedFromDb = achievementService.listUnlockedBy(currentUser);
            for (UserAchievement ua : unlockedFromDb) {
                if (ua.getAchievement() != null) unlockedIds.add(ua.getAchievement().getId());
            }
            return unlockedIds;
        }

        // Modo local sin Oracle: usa el conteo real de retos resueltos en memoria (nunca XP / 250)
        UserStats offlineStats = new UserStats(
                currentUser.getCurrentStreak(),
                currentUser.getMaxStreak(),
                currentUser.getTotalExp(),
                Math.max(0, offlineSolvedChallenges)
        );
        if (catalog != null) {
            for (Achievement ach : catalog) {
                if (ach != null && ach.isUnlockedBy(offlineStats)) {
                    unlockedIds.add(ach.getId());
                }
            }
        }
        return unlockedIds;
    }

    /**
     * Sincroniza experiencia, nivel, racha, vidas y conteo real de retos aprobados desde Oracle.
     */
    public int syncTelemetryAndGetSolvedChallenges(int offlineFallbackCount) {
        if (oracleConnected && currentUser.getId() > 0) {
            if (userRepository == null || achievementRepository == null) {
                throw new IllegalStateException("Telemetría Oracle no configurada.");
            }
            User fresh = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalStateException("No se encontró el usuario actual en Oracle."));
            UserStats stats = achievementRepository.findStatsByUserId(currentUser.getId())
                    .orElseThrow(() -> new IllegalStateException("No se encontraron las estadísticas del usuario en Oracle."));
            currentUser.setExperience(fresh.getExperience());
            currentUser.setLevel(fresh.getLevel());
            currentUser.setCurrentStreak(fresh.getCurrentStreak());
            currentUser.setMaxStreak(fresh.getMaxStreak());
            currentUser.setLives(fresh.getLives());
            return stats.getSolvedChallenges();
        }
        return Math.max(0, offlineFallbackCount);
    }

    public Challenge createAdminChallenge(Lesson targetLesson, CodeChallenge draft) {
        if (currentUser == null || !currentUser.isAdministrator()) {
            throw new SecurityException("Acceso denegado: Se requiere rol de Administrador.");
        }
        if (oracleConnected && targetLesson != null && targetLesson.getId() > 0 && contentService != null) {
            return contentService.createChallenge(currentUser, draft);
        }
        return draft;
    }
}

