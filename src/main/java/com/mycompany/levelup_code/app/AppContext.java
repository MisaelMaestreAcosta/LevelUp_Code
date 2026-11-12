package com.mycompany.levelup_code.app;

import com.mycompany.levelup_code.business.evaluator.ChallengeEvaluator;
import com.mycompany.levelup_code.business.evaluator.CodeChallengeEvaluator;
import com.mycompany.levelup_code.business.evaluator.QuizChallengeEvaluator;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.business.service.AchievementService;
import com.mycompany.levelup_code.business.service.ChallengeService;
import com.mycompany.levelup_code.business.service.ContentService;
import com.mycompany.levelup_code.business.service.EnrollmentService;
import com.mycompany.levelup_code.business.service.GamificationService;
import com.mycompany.levelup_code.business.service.LevelPolicy;
import com.mycompany.levelup_code.business.service.ProgressService;
import com.mycompany.levelup_code.business.service.StandardLevelPolicy;
import com.mycompany.levelup_code.business.service.UserService;
import com.mycompany.levelup_code.data.connection.DatabaseConnection;
import com.mycompany.levelup_code.data.dao.AchievementRepository;
import com.mycompany.levelup_code.data.dao.ActivityRepository;
import com.mycompany.levelup_code.data.dao.AttemptRepository;
import com.mycompany.levelup_code.data.dao.ChallengeRepository;
import com.mycompany.levelup_code.data.dao.ContentRepository;
import com.mycompany.levelup_code.data.dao.EnrollmentRepository;
import com.mycompany.levelup_code.data.dao.ProgressRepository;
import com.mycompany.levelup_code.data.dao.UserRepository;
import com.mycompany.levelup_code.data.oracle.OracleAchievementRepository;
import com.mycompany.levelup_code.data.oracle.OracleActivityRepository;
import com.mycompany.levelup_code.data.oracle.OracleAttemptRepository;
import com.mycompany.levelup_code.data.oracle.OracleChallengeRepository;
import com.mycompany.levelup_code.data.oracle.OracleContentRepository;
import com.mycompany.levelup_code.data.oracle.OracleEnrollmentRepository;
import com.mycompany.levelup_code.data.oracle.OracleProgressRepository;
import com.mycompany.levelup_code.data.oracle.OracleUserRepository;
import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.model.Role;
import java.util.EnumMap;
import java.util.Map;

/**
 * Contenedor de ensamblaje de dependencias de la aplicación (Inversión de Dependencias).
 * Conecta automáticamente los repositorios Oracle JDBC con todos los servicios de negocio
 * cuando la base de datos está disponible.
 */
public final class AppContext {

    private static AppContext instance;

    private final boolean oracleConnected;
    private volatile boolean oracleAvailable;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ChallengeRepository challengeRepository;
    private final AttemptRepository attemptRepository;
    private final ProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ActivityRepository activityRepository;
    private final AchievementRepository achievementRepository;

    private final UserService userService;
    private final ContentService contentService;
    private final EnrollmentService enrollmentService;
    private final AchievementService achievementService;
    private final GamificationService gamificationService;
    private final ProgressService progressService;
    private final ChallengeService challengeService;
    private final SimulatedCodeRunner codeRunner;

    private AppContext() {
        this(DatabaseConnection.isDatabaseReady());
    }

    private AppContext(boolean oracleConnected) {
        this.oracleConnected = oracleConnected;
        this.oracleAvailable = oracleConnected;
        this.userRepository = new OracleUserRepository();
        this.contentRepository = new OracleContentRepository();
        this.challengeRepository = new OracleChallengeRepository();
        this.attemptRepository = new OracleAttemptRepository();
        this.progressRepository = new OracleProgressRepository();
        this.enrollmentRepository = new OracleEnrollmentRepository();
        this.activityRepository = new OracleActivityRepository();
        this.achievementRepository = new OracleAchievementRepository();

        Role studentRole = new Role(1, "Estudiante", "Acceso a cursos, retos y seguimiento de progreso");
        LevelPolicy levelPolicy = new StandardLevelPolicy();
        this.codeRunner = new SimulatedCodeRunner();

        this.userService = new UserService(userRepository, studentRole);
        this.contentService = new ContentService(contentRepository, oracleConnected ? userRepository : null);
        this.enrollmentService = new EnrollmentService(enrollmentRepository, contentRepository);
        this.achievementService = new AchievementService(achievementRepository);
        this.gamificationService = new GamificationService(
                userRepository, activityRepository, levelPolicy, achievementService);
        this.progressService = new ProgressService(
                progressRepository, contentRepository, gamificationService);

        Map<ChallengeType, ChallengeEvaluator> evaluators = new EnumMap<>(ChallengeType.class);
        evaluators.put(ChallengeType.CODE, new CodeChallengeEvaluator(codeRunner));
        evaluators.put(ChallengeType.QUIZ, new QuizChallengeEvaluator());
        evaluators.put(ChallengeType.OUTPUT_PREDICTION, new QuizChallengeEvaluator());
        evaluators.put(ChallengeType.DEBUGGING, new CodeChallengeEvaluator(codeRunner));
        evaluators.put(ChallengeType.CODE_ORDERING, new com.mycompany.levelup_code.business.evaluator.CodeOrderingEvaluator());

        this.challengeService = new ChallengeService(
                challengeRepository,
                attemptRepository,
                evaluators,
                gamificationService,
                achievementService
        );
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /** Rebuilds the demo context only after Oracle responds, before a real login. */
    public static synchronized boolean reconnectOracle() {
        if (!DatabaseConnection.isDatabaseReady()) return false;
        if (instance == null || !instance.oracleConnected) instance = new AppContext(true);
        else instance.setOracleAvailable(true);
        return true;
    }

    public boolean isOracleConnected() {
        return oracleConnected;
    }

    public boolean isOracleAvailable() {
        return oracleAvailable;
    }

    public void setOracleAvailable(boolean available) {
        this.oracleAvailable = oracleConnected && available;
    }

    public UserService getUserService() {
        return userService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public EnrollmentService getEnrollmentService() {
        return enrollmentService;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public AchievementService getAchievementService() {
        return achievementService;
    }

    public GamificationService getGamificationService() {
        return gamificationService;
    }

    public ProgressService getProgressService() {
        return progressService;
    }

    public ChallengeService getChallengeService() {
        return challengeService;
    }

    public ChallengeRepository getChallengeRepository() {
        return challengeRepository;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public ProgressRepository getProgressRepository() {
        return progressRepository;
    }

    public EnrollmentRepository getEnrollmentRepository() {
        return enrollmentRepository;
    }

    public ActivityRepository getActivityRepository() {
        return activityRepository;
    }

    public AchievementRepository getAchievementRepository() {
        return achievementRepository;
    }

    public OracleActivityRepository getOracleActivityRepository() {
        return (OracleActivityRepository) activityRepository;
    }

    public OracleAchievementRepository getOracleAchievementRepository() {
        return (OracleAchievementRepository) achievementRepository;
    }

    public SimulatedCodeRunner getCodeRunner() {
        return codeRunner;
    }
}
