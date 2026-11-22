# Diagrama de clases de LevelUp Code

Clases principales de la versión actual del proyecto. El diagrama se presenta en tres vistas para seguir el dominio, el flujo de la aplicación y el acceso a Oracle por separado. Se muestran atributos y operaciones representativos.

## Modelo de dominio

```mermaid
classDiagram
    direction TB

    class User {
        -int id
        -String email
        -int experience
        -int level
        -int lives
    }
    class Role {
        -int id
        -String name
    }
    class Language {
        -int id
        -String name
    }
    class CourseModule {
        -String title
        -int orderIndex
    }
    class Lesson {
        -String title
        -String theory
        -int expReward
    }
    class Challenge {
        <<abstract>>
        -int id
        -String title
        -int revision
        -int expReward
    }
    class CodeChallenge {
        -String initialTemplate
        -int timeLimitSeconds
    }
    class QuizChallenge {
        -String correctAnswer
        -int correctOptionIndex
    }
    class OutputPredictionChallenge {
        -String codeSnippet
    }
    class CodeOrderingChallenge
    class DebuggingChallenge
    class ChallengeGuidance
    class TestCase {
        -String input
        -String expectedOutput
        -boolean hidden
    }
    class Enrollment
    class LessonProgress {
        -ProgressStatus status
        -LocalDateTime approvedAt
    }
    class Attempt {
        -AttemptResult result
        -String requestId
        -int challengeRevision
        -int earnedExperience
        +isCorrect() boolean
    }
    class DailyActivity {
        -LocalDate activityDate
        -int experienceGained
    }
    class Achievement {
        <<abstract>>
        -String name
    }
    class StreakAchievement
    class ExpAchievement
    class ExperienceAchievement
    class ChallengeCountAchievement
    class UserAchievement
    class ProfessionalMission {
        +String key
        +int challengeId
        +String client
    }
    class MissionRequirement {
        +String text
        +automaticallyVerified() boolean
    }
    class AuthoredChallenge {
        +PublicationState state
        +int revision
    }

    User --> Role : tiene
    Language "1" o-- "0..*" CourseModule : módulos
    CourseModule "1" o-- "0..*" Lesson : lecciones
    Lesson "1" o-- "0..*" Challenge : retos

    Challenge <|-- CodeChallenge
    Challenge <|-- QuizChallenge
    Challenge <|-- CodeOrderingChallenge
    CodeChallenge <|-- DebuggingChallenge
    QuizChallenge <|-- OutputPredictionChallenge
    Challenge --> ChallengeGuidance : pistas y explicación
    CodeChallenge o-- "0..*" TestCase : casos

    Enrollment --> User
    Enrollment --> Language
    LessonProgress --> User
    LessonProgress --> Lesson
    Attempt --> User
    Attempt --> Challenge
    DailyActivity --> User

    Achievement <|-- StreakAchievement
    Achievement <|-- ExpAchievement
    ExpAchievement <|-- ExperienceAchievement
    Achievement <|-- ChallengeCountAchievement
    UserAchievement --> User
    UserAchievement --> Achievement

    AuthoredChallenge --> Challenge : edición administrativa
    ProfessionalMission "1" o-- "0..*" MissionRequirement : requisitos
    ProfessionalMission ..> Challenge : challengeId
    MissionRequirement ..> TestCase : testCaseIds
```

`Enrollment` vincula usuario y lenguaje. `LessonProgress` relaciona usuario y lección. `Attempt` conserva cada envío y su revisión. Las misiones referencian el reto y los casos mediante identificadores.

## Inicio, pantallas y evaluación

```mermaid
classDiagram
    direction TB

    class Main
    class LevelUpFxApp
    class AppContext {
        +getInstance() AppContext
        +getUserService() UserService
        +getChallengeService() ChallengeService
        +getProgressService() ProgressService
    }
    class LoginFrame
    class AuthController {
        +login(String email, String password) Optional~User~
    }
    class DashboardFrame
    class DashboardController
    class LearningWorkspace
    class LessonFrame
    class ChallengeFrame
    class CodeOrderingFrame
    class ProfessionalMissionsPane
    class ChallengeAuthoringPane

    class UserService {
        +authenticate(String email, String password) Optional~User~
    }
    class ContentService
    class EnrollmentService
    class ProgressService {
        +startLesson(User user, int lessonId) LessonProgress
        +completeLesson(User user, int lessonId) LessonProgress
    }
    class ChallengeService {
        +submitAttempt(User user, int challengeId, String submission) Attempt
    }
    class GamificationService
    class AchievementService
    class AuthoringService

    class ChallengeEvaluator {
        <<interface>>
        +evaluate(Challenge challenge, String submission) ChallengeEvaluation
    }
    class CodeChallengeEvaluator
    class QuizChallengeEvaluator
    class CodeOrderingEvaluator
    class CodeRunner {
        <<interface>>
    }
    class SimulatedCodeRunner

    Main ..> LevelUpFxApp : lanza
    LevelUpFxApp ..> AppContext : inicializa
    LevelUpFxApp ..> LoginFrame : abre
    LoginFrame --> AuthController
    LoginFrame ..> DashboardFrame : autenticación exitosa
    AuthController --> UserService
    DashboardFrame --> DashboardController
    DashboardFrame ..> LearningWorkspace : abre
    DashboardFrame ..> ProfessionalMissionsPane
    DashboardFrame ..> ChallengeAuthoringPane
    DashboardController --> ContentService
    DashboardController --> EnrollmentService
    LearningWorkspace ..> LessonFrame
    LearningWorkspace ..> ChallengeFrame
    LearningWorkspace ..> CodeOrderingFrame
    LessonFrame ..> ProgressService : inicio y finalización
    ChallengeFrame ..> ChallengeService : envío
    CodeOrderingFrame ..> ChallengeService : envío
    ChallengeAuthoringPane ..> AuthoringService : guarda borrador o publicación

    AppContext --> UserService : configura
    AppContext --> ProgressService : configura
    AppContext --> ChallengeService : configura
    ProgressService --> GamificationService
    ChallengeService --> GamificationService
    ChallengeService --> AchievementService
    ChallengeService --> "1..*" ChallengeEvaluator : elige por tipo
    ChallengeEvaluator <|.. CodeChallengeEvaluator
    ChallengeEvaluator <|.. QuizChallengeEvaluator
    ChallengeEvaluator <|.. CodeOrderingEvaluator
    CodeChallengeEvaluator --> CodeRunner
    CodeRunner <|.. SimulatedCodeRunner
```

Al enviar un reto, `ChallengeService` elige el evaluador según su tipo. El evaluador devuelve el veredicto en Java. El repositorio guarda el envío y Oracle aplica las reglas de gamificación.

## Repositorios y conexión con Oracle

```mermaid
classDiagram
    direction TB

    class UserRepository {
        <<interface>>
    }
    class ContentRepository {
        <<interface>>
    }
    class EnrollmentRepository {
        <<interface>>
    }
    class ProgressRepository {
        <<interface>>
    }
    class ChallengeRepository {
        <<interface>>
    }
    class AttemptRepository {
        <<interface>>
    }
    class AchievementRepository {
        <<interface>>
    }
    class ActivityRepository {
        <<interface>>
    }
    class ProfessionalMissionRepository {
        <<interface>>
    }
    class GenericDAO {
        <<interface>>
    }
    class OracleUserRepository
    class UserDAO
    class OracleContentRepository
    class OracleEnrollmentRepository
    class OracleProgressRepository
    class OracleChallengeRepository
    class OracleAttemptRepository
    class OracleAchievementRepository
    class OracleActivityRepository
    class OracleAuthoringRepository
    class OracleChallengeVersionRepository
    class OracleProfessionalMissionRepository
    class DemoProfessionalMissionRepository
    class OracleCalls {
        +cursor(Connection connection, String procedure, Object... inputs) CallableStatement
        +rows(CallableStatement statement) ResultSet
    }
    class DatabaseConnection {
        +getConnection() Connection
        +isDatabaseReady() boolean
    }
    class DatabaseDiagnostics

    UserService --> UserRepository
    ContentService --> ContentRepository
    EnrollmentService --> EnrollmentRepository
    ProgressService --> ProgressRepository
    ChallengeService --> ChallengeRepository
    ChallengeService --> AttemptRepository
    AchievementService --> AchievementRepository
    GamificationService --> ActivityRepository
    ProfessionalMissionsPane --> ProfessionalMissionRepository
    AuthoringService ..> OracleAuthoringRepository : guarda revisión
    OracleAuthoringRepository ..> OracleChallengeVersionRepository : registra versión

    UserRepository <|.. OracleUserRepository
    GenericDAO <|.. UserDAO
    OracleUserRepository --> UserDAO : delega
    ContentRepository <|.. OracleContentRepository
    EnrollmentRepository <|.. OracleEnrollmentRepository
    ProgressRepository <|.. OracleProgressRepository
    ChallengeRepository <|.. OracleChallengeRepository
    AttemptRepository <|.. OracleAttemptRepository
    AchievementRepository <|.. OracleAchievementRepository
    ActivityRepository <|.. OracleActivityRepository
    ProfessionalMissionRepository <|.. OracleProfessionalMissionRepository
    ProfessionalMissionRepository <|.. DemoProfessionalMissionRepository

    UserDAO ..> OracleCalls : consultas de usuario
    OracleContentRepository ..> OracleCalls : consultas de catálogo
    OracleChallengeRepository ..> OracleCalls : consultas de retos
    OracleProfessionalMissionRepository ..> OracleCalls : misiones publicadas
    OracleChallengeVersionRepository ..> OracleCalls : guarda y consulta versiones
    OracleProgressRepository ..> OracleCalls : consulta de progreso
    OracleAttemptRepository ..> OracleCalls : historial de intentos
    UserDAO ..> DatabaseConnection : obtiene conexión
    OracleAttemptRepository ..> DatabaseConnection : obtiene conexión
    OracleProgressRepository ..> DatabaseConnection : obtiene conexión
    DatabaseDiagnostics ..> DatabaseConnection
    DatabaseDiagnostics ..> OracleCalls
```

Los repositorios pasan una conexión JDBC a `OracleCalls` para consultar los paquetes de Oracle. Las operaciones de gamificación también se llaman mediante JDBC. [Guía de scripts y paquetes](../database/README.md).

| Otra pantalla | Clase de acceso a datos |
|---|---|
| `ChallengeAuthoringPane` | `OracleAuthoringRepository` y `OracleChallengeVersionRepository` |
| `CurriculumAuthoringPane` | `OracleCurriculumRepository` |
| `LearningAnalyticsPane` | `OracleLearningAnalyticsRepository` |
| `LearningGoalsPane` | `OracleGoalRepository` |
| `NotebookPane` | `OracleNotebookRepository` |
| `ProfilePane` | `OraclePreferenceRepository` para las preferencias del editor |
