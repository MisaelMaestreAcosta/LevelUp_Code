package com.mycompany.levelup_code.support;

import com.mycompany.levelup_code.data.dao.AchievementRepository;
import com.mycompany.levelup_code.data.dao.ActivityRepository;
import com.mycompany.levelup_code.data.dao.AttemptRepository;
import com.mycompany.levelup_code.data.dao.ChallengeRepository;
import com.mycompany.levelup_code.data.dao.ContentRepository;
import com.mycompany.levelup_code.data.dao.EnrollmentRepository;
import com.mycompany.levelup_code.data.dao.ProgressRepository;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.Attempt;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.domain.model.UserAchievement;
import com.mycompany.levelup_code.domain.model.UserStats;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repositorios falsos (Fake Repositories) en memoria para pruebas unitarias deterministas
 * sin requerir una instancia activa de Oracle Database.
 */
public final class InMemoryTestRepositories {

    private InMemoryTestRepositories() {
    }

    public static final class FakeUserRepository implements UserRepository {
        private final Map<Integer, User> byId = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();
        private int seq = 1;

        @Override
        public Optional<User> findById(int id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            if (email == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(byEmail.get(email.trim().toLowerCase(Locale.ROOT)));
        }

        @Override
        public User save(User user) {
            if (user.getId() <= 0) {
                user.setId(seq++);
            }
            byId.put(user.getId(), user);
            if (user.getEmail() != null) {
                byEmail.put(user.getEmail().trim().toLowerCase(Locale.ROOT), user);
            }
            return user;
        }

        @Override
        public User update(User user) {
            return save(user);
        }
    }

    public static final class FakeContentRepository implements ContentRepository {
        private final Map<Integer, Language> languages = new HashMap<>();
        private final Map<Integer, CourseModule> modules = new HashMap<>();
        private final Map<Integer, Lesson> lessons = new HashMap<>();
        private final Map<Integer, Challenge> challenges = new HashMap<>();
        private int seq = 100;

        @Override
        public boolean existsLanguageByName(String name) {
            return languages.values().stream()
                    .anyMatch(l -> l.getName().equalsIgnoreCase(name));
        }

        @Override
        public boolean existsLanguageByNameExceptId(String name, int excludedId) {
            return languages.values().stream()
                    .anyMatch(l -> l.getId() != excludedId && l.getName().equalsIgnoreCase(name));
        }

        @Override
        public Optional<Language> findLanguageById(int id) {
            return Optional.ofNullable(languages.get(id));
        }

        @Override
        public Optional<CourseModule> findModuleById(int id) {
            return Optional.ofNullable(modules.get(id));
        }

        @Override
        public Optional<Lesson> findLessonById(int id) {
            return Optional.ofNullable(lessons.get(id));
        }

        @Override
        public List<Language> findActiveLanguages() {
            return languages.values().stream()
                    .filter(Language::isActive)
                    .collect(Collectors.toList());
        }

        @Override
        public Language saveLanguage(Language language) {
            if (language.getId() <= 0) {
                language.setId(seq++);
            }
            languages.put(language.getId(), language);
            return language;
        }

        @Override
        public CourseModule saveModule(CourseModule module) {
            if (module.getId() <= 0) {
                module.setId(seq++);
            }
            modules.put(module.getId(), module);
            return module;
        }

        @Override
        public Lesson saveLesson(Lesson lesson) {
            if (lesson.getId() <= 0) {
                lesson.setId(seq++);
            }
            lessons.put(lesson.getId(), lesson);
            return lesson;
        }

        @Override
        public Challenge saveChallenge(Challenge challenge) {
            if (challenge.getId() <= 0) {
                challenge.setId(seq++);
            }
            challenges.put(challenge.getId(), challenge);
            return challenge;
        }

        @Override
        public Language updateLanguage(Language language) {
            languages.put(language.getId(), language);
            return language;
        }

        @Override
        public CourseModule updateModule(CourseModule module) {
            modules.put(module.getId(), module);
            return module;
        }

        @Override
        public Lesson updateLesson(Lesson lesson) {
            lessons.put(lesson.getId(), lesson);
            return lesson;
        }

        @Override
        public Challenge updateChallenge(Challenge challenge) {
            challenges.put(challenge.getId(), challenge);
            return challenge;
        }
    }

    public static final class FakeChallengeRepository implements ChallengeRepository {
        private final Map<Integer, Challenge> byId = new HashMap<>();

        public void register(Challenge challenge) {
            byId.put(challenge.getId(), challenge);
        }

        @Override
        public Optional<Challenge> findById(int challengeId) {
            return Optional.ofNullable(byId.get(challengeId));
        }
    }

    public static final class FakeAttemptRepository implements AttemptRepository {
        private final List<Attempt> attempts = new ArrayList<>();
        private final boolean transactionalGamification;
        private int seq = 1;

        public FakeAttemptRepository() {
            this(false);
        }

        public FakeAttemptRepository(boolean transactionalGamification) {
            this.transactionalGamification = transactionalGamification;
        }

        @Override
        public boolean managesGamificationTransactionally() {
            return transactionalGamification;
        }

        @Override
        public boolean hasSuccessfulAttempt(int userId, int challengeId) {
            return attempts.stream()
                    .anyMatch(a -> a.getUser().getId() == userId
                            && a.getChallenge().getId() == challengeId
                            && a.isCorrect());
        }

        @Override
        public Attempt save(Attempt attempt) {
            if (attempt.getId() <= 0) {
                attempt.setId(seq++);
            }
            if (transactionalGamification && attempt.getUser() != null) {
                if (attempt.isCorrect() && attempt.getEarnedExperience() > 0) {
                    attempt.getUser().addExperience(attempt.getEarnedExperience());
                } else if (!attempt.isCorrect()) {
                    attempt.getUser().loseLife();
                }
            }
            attempts.add(attempt);
            return attempt;
        }

        @Override
        public List<Attempt> findByUserAndChallenge(int userId, int challengeId) {
            return attempts.stream()
                    .filter(a -> a.getUser().getId() == userId && a.getChallenge().getId() == challengeId)
                    .collect(Collectors.toList());
        }

        public int countDistinctSolvedChallenges(int userId) {
            return (int) attempts.stream()
                    .filter(a -> a.getUser().getId() == userId && a.isCorrect())
                    .map(a -> a.getChallenge().getId())
                    .distinct()
                    .count();
        }
    }

    public static final class FakeProgressRepository implements ProgressRepository {
        private final Map<String, LessonProgress> map = new HashMap<>();
        private final boolean transactionalGamification;

        public FakeProgressRepository() {
            this(false);
        }

        public FakeProgressRepository(boolean transactionalGamification) {
            this.transactionalGamification = transactionalGamification;
        }

        @Override
        public boolean managesGamificationTransactionally() {
            return transactionalGamification;
        }

        @Override
        public Optional<LessonProgress> findByUserAndLesson(int userId, int lessonId) {
            return Optional.ofNullable(map.get(userId + ":" + lessonId));
        }

        @Override
        public LessonProgress saveOrUpdate(LessonProgress progress) {
            map.put(progress.getUser().getId() + ":" + progress.getLesson().getId(), progress);
            return progress;
        }
    }

    public static final class FakeEnrollmentRepository implements EnrollmentRepository {
        private final Map<Integer, Set<Integer>> byUser = new HashMap<>();

        @Override
        public Set<Integer> findEnrolledLanguageIds(int userId) {
            return new HashSet<>(byUser.getOrDefault(userId, Collections.emptySet()));
        }

        @Override
        public boolean isEnrolled(int userId, int languageId) {
            return byUser.getOrDefault(userId, Collections.emptySet()).contains(languageId);
        }

        @Override
        public void enroll(int userId, int languageId) {
            byUser.computeIfAbsent(userId, k -> new HashSet<>()).add(languageId);
        }
    }

    public static final class FakeActivityRepository implements ActivityRepository {
        private final Map<String, DailyActivity> byUserDate = new HashMap<>();

        @Override
        public Optional<DailyActivity> findByUserAndDate(int userId, LocalDate date) {
            return Optional.ofNullable(byUserDate.get(userId + ":" + date));
        }

        @Override
        public DailyActivity saveOrUpdate(DailyActivity activity) {
            byUserDate.put(activity.getUser().getId() + ":" + activity.getActivityDate(), activity);
            return activity;
        }

        @Override
        public List<DailyActivity> findRecentByUser(int userId, int days) {
            LocalDate cutoff = LocalDate.now().minusDays(days);
            return byUserDate.values().stream()
                    .filter(a -> a.getUser().getId() == userId && !a.getActivityDate().isBefore(cutoff))
                    .collect(Collectors.toList());
        }
    }

    public static final class FakeAchievementRepository implements AchievementRepository {
        private final FakeUserRepository userRepo;
        private final FakeAttemptRepository attemptRepo;
        private final List<Achievement> catalog = new ArrayList<>();
        private final Map<Integer, List<UserAchievement>> unlockedByUser = new HashMap<>();

        public FakeAchievementRepository(FakeUserRepository userRepo, FakeAttemptRepository attemptRepo) {
            this.userRepo = userRepo;
            this.attemptRepo = attemptRepo;
        }

        public void registerAchievement(Achievement achievement) {
            catalog.add(achievement);
        }

        @Override
        public List<Achievement> findAllCatalog() {
            return new ArrayList<>(catalog);
        }

        @Override
        public Optional<UserStats> findStatsByUserId(int userId) {
            return userRepo.findById(userId).map(u -> new UserStats(
                    u.getCurrentStreak(),
                    u.getMaxStreak(),
                    u.getTotalExp(),
                    attemptRepo.countDistinctSolvedChallenges(userId)
            ));
        }

        @Override
        public List<Achievement> findNotUnlockedByUser(int userId) {
            Set<Integer> unlockedIds = findUnlockedByUser(userId).stream()
                    .map(ua -> ua.getAchievement().getId())
                    .collect(Collectors.toSet());
            return catalog.stream()
                    .filter(a -> !unlockedIds.contains(a.getId()))
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<UserAchievement> saveIfNotUnlocked(UserAchievement userAchievement) {
            int userId = userAchievement.getUser().getId();
            int achId = userAchievement.getAchievement().getId();
            List<UserAchievement> list = unlockedByUser.computeIfAbsent(userId, k -> new ArrayList<>());
            boolean exists = list.stream().anyMatch(ua -> ua.getAchievement().getId() == achId);
            if (exists) {
                return Optional.empty();
            }
            if (userAchievement.getUnlockedAt() == null) {
                userAchievement.setUnlockedAt(LocalDateTime.now());
            }
            list.add(userAchievement);
            return Optional.of(userAchievement);
        }

        @Override
        public List<UserAchievement> findUnlockedByUser(int userId) {
            return new ArrayList<>(unlockedByUser.getOrDefault(userId, Collections.emptyList()));
        }
    }
}

