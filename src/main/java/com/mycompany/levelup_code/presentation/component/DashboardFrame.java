package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.StreakAchievement;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.presentation.controller.DashboardController;
import com.mycompany.levelup_code.presentation.controller.DemoSeedCatalog;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Panel principal (Dashboard) en JavaFX + CSS ({@code /styles/levelup.css}) con telemetría
 * de gamificación en tiempo real, mapa de calor de 14 días ({@code Actividad_Diaria}),
 * árbol conectado de habilidades ({@code Progreso_Leccion}), comparador sincronizado de
 * 3 lenguajes, catálogo de logros ({@code Logros} / {@code Usuario_Logro}) y gestión CRUD
 * de retos protegida por rol Administrador.
 */
public class DashboardFrame {

    private static final Logger LOGGER = Logger.getLogger(DashboardFrame.class.getName());

    private final Stage stage;
    private final User currentUser;
    private final boolean oracleConnected;
    private final DashboardController controller;

    private String activeLanguageKey = "JAVA";
    private int solvedChallengesCount = 0;
    private int lastKnownOfflineExp = 0;

    private final List<Lesson> javaLessons = new ArrayList<>();
    private final List<Lesson> pythonLessons = new ArrayList<>();
    private final List<Lesson> cppLessons = new ArrayList<>();
    private final List<Lesson> allLessonsForAdmin = new ArrayList<>();
    private final List<Achievement> achievementsCatalog = new ArrayList<>();
    private final Map<LocalDate, Integer> activityByDate = new HashMap<>();
    private final Set<String> enrolledLanguages = new HashSet<>();

    private Language javaLangModel;
    private Language pythonLangModel;
    private Language cppLangModel;

    // Botones de navegación lateral
    private Button navCoursesBtn;
    private Button navComparatorBtn;
    private Button navAchievementsBtn;
    private Button navAdminBtn;

    // Contenedor central con transición de vistas
    private StackPane workspaceContainer;
    private VBox coursesView;
    private VBox comparatorView;
    private VBox achievementsView;
    private VBox adminView;

    // Telemetría reactiva
    private Label lblLevelVal;
    private Label lblLevelSub;
    private ProgressBar levelProgressBar;
    private Label lblExpVal;
    private Label lblExpSub;
    private Label lblStreakVal;
    private Label badgeMaxStreak;
    private HBox heatmapGridBox;
    private Label lblLivesVal;
    private Label lblLivesSub;
    private ProgressBar livesProgressBar;

    // Árbol de habilidades y cursos
    private Label lblLessonsHeader;
    private Label lblCourseStatus;
    private VBox lessonsListBox;
    private final Map<String, ProgressBar> courseProgressBars = new HashMap<>();
    private final Map<String, Label> courseProgressLabels = new HashMap<>();

    // Comparador de 3 lenguajes (6 tópicos curriculares)
    private TextArea txtCompareJava;
    private TextArea txtComparePython;
    private TextArea txtCompareCpp;
    private Label lblCompareSummary;
    private final List<Button> comparatorTopicButtons = new ArrayList<>();
    private int activeComparatorLessonIndex = 0;

    // Logros e insignias
    private GridPane achievementsGrid;
    private Label lblAchievementsSummary;

    // Formulario de Administración CRUD
    private ComboBox<String> cmbAdminLesson;
    private TextField txtAdminTitle;
    private TextField txtAdminReward;
    private TextArea txtAdminStatement;
    private TextArea txtAdminTemplate;
    private TextField txtAdminCase1In;
    private TextField txtAdminCase1Out;
    private TextField txtAdminCase2In;
    private TextField txtAdminCase2Out;
    private Label lblAdminStatus;

    public DashboardFrame(User user) {
        this(new Stage(), user);
    }

    public DashboardFrame(Stage targetStage, User user) {
        this.stage = targetStage != null ? targetStage : new Stage();
        this.currentUser = user != null ? user : DemoSeedCatalog.createDefaultStudent();
        AppContext ctx = AppContext.getInstance();
        this.oracleConnected = ctx.isOracleConnected();
        this.controller = new DashboardController(this.currentUser, ctx);
        this.lastKnownOfflineExp = this.currentUser.getTotalExp();

        loadDomainDataFromOracleOrSeed();
        loadEnrollmentsFromOracle();
        loadAchievementsFromOracleOrSeed();
        loadActivityHistoryFromOracle();
        this.solvedChallengesCount = this.controller.syncTelemetryAndGetSolvedChallenges(0);

        initUi();
        populateLessonsForLanguage("JAVA");
        refreshCourseProgressBars();
        loadComparisonTopic("POLYMORPHISM");
        refreshAchievementsGrid();
        refreshGamificationTelemetry();
    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
        } else {
            stage.close();
        }
    }

    private void loadEnrollmentsFromOracle() {
        this.enrolledLanguages.clear();
        this.enrolledLanguages.addAll(
                controller.loadEnrolledLanguageKeys(javaLangModel, pythonLangModel, cppLangModel)
        );
    }

    private void loadDomainDataFromOracleOrSeed() {
        javaLessons.clear();
        pythonLessons.clear();
        cppLessons.clear();
        allLessonsForAdmin.clear();

        List<Language> languages = controller.loadLanguages();
        for (Language lang : languages) {
            String name = lang.getName() != null ? lang.getName().toLowerCase(Locale.ROOT) : "";
            List<Lesson> targetList;
            if (name.contains("python")) {
                pythonLangModel = lang;
                targetList = pythonLessons;
            } else if (name.contains("c++") || name.contains("cpp")) {
                cppLangModel = lang;
                targetList = cppLessons;
            } else {
                javaLangModel = lang;
                targetList = javaLessons;
            }
            if (lang.getModules() != null) {
                for (CourseModule mod : lang.getModules()) {
                    if (mod.getLessons() != null) {
                        for (Lesson lesson : mod.getLessons()) {
                            targetList.add(lesson);
                            allLessonsForAdmin.add(lesson);
                        }
                    }
                }
            }
        }
    }

    private void loadAchievementsFromOracleOrSeed() {
        achievementsCatalog.clear();
        achievementsCatalog.addAll(controller.loadAchievementsCatalog());
    }

    private void loadActivityHistoryFromOracle() {
        activityByDate.clear();
        activityByDate.putAll(controller.loadRecentActivityHistory());
    }

    private void initUi() {
        stage.setTitle("LevelUp Code — Espacio de Trabajo y Telemetría (JavaFX + CSS)");
        stage.setMinWidth(1140);
        stage.setMinHeight(720);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");

        root.setLeft(buildLeftSidebar());

        VBox centerShell = new VBox(16);
        centerShell.setPadding(new Insets(18, 22, 18, 22));

        HBox topTelemetry = buildTopTelemetryBar();

        workspaceContainer = new StackPane();
        VBox.setVgrow(workspaceContainer, Priority.ALWAYS);

        coursesView = buildCoursesWorkspaceView();
        comparatorView = buildComparatorWorkspaceView();
        achievementsView = buildAchievementsWorkspaceView();
        if (currentUser != null && currentUser.isAdministrator()) {
            adminView = buildAdminCrudWorkspaceView();
        }

        workspaceContainer.getChildren().setAll(coursesView);
        centerShell.getChildren().addAll(topTelemetry, workspaceContainer);
        root.setCenter(centerShell);

        Scene scene = new Scene(root, 1260, 780);
        Theme.applyCss(scene);
        stage.setScene(scene);
    }

    private BorderPane buildLeftSidebar() {
        BorderPane sidebar = new BorderPane();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(258);

        VBox navColumn = new VBox(8);

        Label lblBrand = new Label("LevelUp Code");
        lblBrand.getStyleClass().add("page-title");
        lblBrand.setGraphic(Theme.createIcon(Theme.IconKind.TERMINAL, 20, Theme.HEX_INDIGO));
        lblBrand.setGraphicTextGap(8);

        Label lblSub = new Label("Plataforma Políglota");
        lblSub.getStyleClass().add("muted-text");
        VBox.setMargin(lblSub, new Insets(0, 0, 14, 0));

        Label lblNavSection = new Label("NAVEGACIÓN PRINCIPAL");
        lblNavSection.getStyleClass().add("section-caption");

        navCoursesBtn = new Button("Rutas y Árbol de Nodos");
        navCoursesBtn.getStyleClass().add("nav-btn-active");
        navCoursesBtn.setGraphic(Theme.createIcon(Theme.IconKind.BOOK, 16, Theme.HEX_WHITE));
        navCoursesBtn.setGraphicTextGap(10);
        navCoursesBtn.setMaxWidth(Double.MAX_VALUE);

        navComparatorBtn = new Button("Comparador 3 Lenguajes");
        navComparatorBtn.getStyleClass().add("nav-btn");
        navComparatorBtn.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 16, Theme.HEX_CYAN));
        navComparatorBtn.setGraphicTextGap(10);
        navComparatorBtn.setMaxWidth(Double.MAX_VALUE);

        navAchievementsBtn = new Button("Logros e Insignias");
        navAchievementsBtn.getStyleClass().add("nav-btn");
        navAchievementsBtn.setGraphic(Theme.createIcon(Theme.IconKind.TROPHY, 16, Theme.HEX_AMBER));
        navAchievementsBtn.setGraphicTextGap(10);
        navAchievementsBtn.setMaxWidth(Double.MAX_VALUE);

        if (currentUser != null && currentUser.isAdministrator()) {
            navAdminBtn = new Button("Gestión de Retos (Oracle)");
            navAdminBtn.getStyleClass().add("nav-btn");
            navAdminBtn.setGraphic(Theme.createIcon(Theme.IconKind.SHIELD, 16, Theme.HEX_GREEN));
            navAdminBtn.setGraphicTextGap(10);
            navAdminBtn.setMaxWidth(Double.MAX_VALUE);
            navAdminBtn.setOnAction(e -> switchWorkspaceView("ADMIN"));
        }

        Button btnQuickLab = new Button("Abrir Laboratorio de Retos");
        btnQuickLab.getStyleClass().add("btn-ghost-green");
        btnQuickLab.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_GREEN));
        btnQuickLab.setGraphicTextGap(8);
        btnQuickLab.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(btnQuickLab, new Insets(8, 0, 0, 0));

        navCoursesBtn.setOnAction(e -> switchWorkspaceView("COURSES"));
        navComparatorBtn.setOnAction(e -> switchWorkspaceView("COMPARATOR"));
        navAchievementsBtn.setOnAction(e -> {
            refreshAchievementsGrid();
            switchWorkspaceView("ACHIEVEMENTS");
        });
        btnQuickLab.setOnAction(e -> {
            Lesson l = !javaLessons.isEmpty() ? javaLessons.get(0) : null;
            Challenge ch = (l != null && l.getChallenges() != null && !l.getChallenges().isEmpty())
                    ? l.getChallenges().get(0) : null;
            new ChallengeFrame(currentUser, ch, this::onChallengeOrLessonCompleted).show();
        });

        navColumn.getChildren().addAll(
                lblBrand, lblSub, lblNavSection,
                navCoursesBtn, navComparatorBtn, navAchievementsBtn
        );
        if (navAdminBtn != null) {
            navColumn.getChildren().add(navAdminBtn);
        }
        navColumn.getChildren().add(btnQuickLab);
        sidebar.setTop(navColumn);

        // Tarjeta inferior de perfil activo y cierre de sesión
        VBox userBox = new VBox(10);
        userBox.getStyleClass().add("surface-subtle");

        HBox profileTop = new HBox(10);
        profileTop.setAlignment(Pos.CENTER_LEFT);

        String roleHex = currentUser.isAdministrator() ? Theme.HEX_GREEN : Theme.HEX_INDIGO;
        VBox userTextCol = new VBox(2);
        Label lblUserName = new Label(currentUser.getUsername());
        lblUserName.getStyleClass().add("card-title");

        String roleLabel = currentUser.isAdministrator() ? "Rol: Administrador" : "Rol: Estudiante";
        Label lblUserRole = new Label(roleLabel);
        lblUserRole.getStyleClass().add(currentUser.isAdministrator() ? "text-success" : "muted-text");
        userTextCol.getChildren().addAll(lblUserName, lblUserRole);

        profileTop.getChildren().addAll(
                Theme.createIcon(Theme.IconKind.USER, 22, roleHex),
                userTextCol
        );

        String connLabel = oracleConnected ? "ORACLE 18C XE · ACTIVO" : "MEMORIA LOCAL";
        String connClass = oracleConnected ? "badge-green" : "badge-amber";
        Label badgeConn = Theme.createPillBadge(connLabel, connClass);

        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.getStyleClass().add("btn-secondary");
        btnLogout.setGraphic(Theme.createIcon(Theme.IconKind.LOGOUT, 14, Theme.HEX_SECONDARY));
        btnLogout.setGraphicTextGap(8);
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> {
            LoginFrame loginFrame = new LoginFrame(stage);
            loginFrame.show();
        });

        userBox.getChildren().addAll(profileTop, badgeConn, btnLogout);
        sidebar.setBottom(userBox);

        return sidebar;
    }

    private HBox buildTopTelemetryBar() {
        HBox telemetryBar = new HBox(14);

        // 1. Nivel y barra de progreso
        VBox cardLevel = new VBox(6);
        cardLevel.getStyleClass().add("surface-card");
        HBox.setHgrow(cardLevel, Priority.ALWAYS);

        HBox lvlTop = new HBox(8);
        lvlTop.setAlignment(Pos.CENTER_LEFT);
        Label lblLvlCap = new Label("NIVEL ACTUAL");
        lblLvlCap.getStyleClass().add("section-caption");
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        lvlTop.getChildren().addAll(lblLvlCap, sp1, Theme.createIcon(Theme.IconKind.STAR, 16, Theme.HEX_INDIGO));

        HBox lvlMid = new HBox(10);
        lvlMid.setAlignment(Pos.BASELINE_LEFT);
        lblLevelVal = new Label("Nivel " + currentUser.getLevel());
        lblLevelVal.getStyleClass().add("metric-value");
        Region sp1b = new Region();
        HBox.setHgrow(sp1b, Priority.ALWAYS);
        lblLevelSub = new Label();
        lblLevelSub.getStyleClass().add("muted-text");
        lvlMid.getChildren().addAll(lblLevelVal, sp1b, lblLevelSub);

        levelProgressBar = new ProgressBar(0.5);
        levelProgressBar.getStyleClass().add("progress-bar-indigo");
        levelProgressBar.setMaxWidth(Double.MAX_VALUE);
        cardLevel.getChildren().addAll(lvlTop, lvlMid, levelProgressBar);

        // 2. Experiencia Total
        VBox cardExp = new VBox(6);
        cardExp.getStyleClass().add("surface-card");
        HBox.setHgrow(cardExp, Priority.ALWAYS);

        HBox expTop = new HBox(8);
        expTop.setAlignment(Pos.CENTER_LEFT);
        Label lblExpCap = new Label("EXPERIENCIA TOTAL");
        lblExpCap.getStyleClass().add("section-caption");
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        expTop.getChildren().addAll(lblExpCap, sp2, Theme.createIcon(Theme.IconKind.BOLT, 16, Theme.HEX_AMBER));

        lblExpVal = new Label(currentUser.getTotalExp() + " XP");
        lblExpVal.getStyleClass().add("metric-value");
        lblExpSub = new Label("Sincronizado con tabla Usuarios");
        lblExpSub.getStyleClass().add("muted-text");
        cardExp.getChildren().addAll(expTop, lblExpVal, lblExpSub);

        // 3. Racha y Mapa de Calor de 14 días (Actividad_Diaria)
        VBox cardStreak = new VBox(6);
        cardStreak.getStyleClass().add("surface-card");
        HBox.setHgrow(cardStreak, Priority.ALWAYS);

        HBox strTop = new HBox(8);
        strTop.setAlignment(Pos.CENTER_LEFT);
        Label lblStrCap = new Label("RACHA Y ACTIVIDAD (14 DÍAS)");
        lblStrCap.getStyleClass().add("section-caption");
        Region sp3 = new Region();
        HBox.setHgrow(sp3, Priority.ALWAYS);
        strTop.getChildren().addAll(lblStrCap, sp3, Theme.createIcon(Theme.IconKind.FLAME, 16, Theme.HEX_GREEN));

        HBox strMid = new HBox(8);
        strMid.setAlignment(Pos.CENTER_LEFT);
        lblStreakVal = new Label(currentUser.getCurrentStreak() + " Días");
        lblStreakVal.getStyleClass().add("metric-value");
        Region sp3b = new Region();
        HBox.setHgrow(sp3b, Priority.ALWAYS);
        badgeMaxStreak = Theme.createPillBadge("Máx: " + currentUser.getMaxStreak() + "d", "badge-amber");
        strMid.getChildren().addAll(lblStreakVal, sp3b, badgeMaxStreak);

        heatmapGridBox = new HBox(4);
        heatmapGridBox.setAlignment(Pos.CENTER_LEFT);
        rebuildHeatmapCells();

        cardStreak.getChildren().addAll(strTop, strMid, heatmapGridBox);

        // 4. Vidas Disponibles
        VBox cardLives = new VBox(6);
        cardLives.getStyleClass().add("surface-card");
        HBox.setHgrow(cardLives, Priority.ALWAYS);

        HBox livTop = new HBox(8);
        livTop.setAlignment(Pos.CENTER_LEFT);
        Label lblLivCap = new Label("VIDAS DISPONIBLES");
        lblLivCap.getStyleClass().add("section-caption");
        Region sp4 = new Region();
        HBox.setHgrow(sp4, Priority.ALWAYS);
        livTop.getChildren().addAll(lblLivCap, sp4, Theme.createIcon(Theme.IconKind.HEART, 16, Theme.HEX_ROSE));

        HBox livMid = new HBox(8);
        livMid.setAlignment(Pos.BASELINE_LEFT);
        lblLivesVal = new Label(currentUser.getLives() + " / 5");
        lblLivesVal.getStyleClass().add("metric-value");
        Region sp4b = new Region();
        HBox.setHgrow(sp4b, Priority.ALWAYS);
        lblLivesSub = new Label("Energía lista");
        lblLivesSub.getStyleClass().add("muted-text");
        livMid.getChildren().addAll(lblLivesVal, sp4b, lblLivesSub);

        livesProgressBar = new ProgressBar(currentUser.getLives() / 5.0);
        livesProgressBar.getStyleClass().add("progress-bar-rose");
        livesProgressBar.setMaxWidth(Double.MAX_VALUE);
        cardLives.getChildren().addAll(livTop, livMid, livesProgressBar);

        telemetryBar.getChildren().addAll(cardLevel, cardExp, cardStreak, cardLives);
        return telemetryBar;
    }

    private void rebuildHeatmapCells() {
        if (heatmapGridBox == null) {
            return;
        }
        heatmapGridBox.getChildren().clear();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        for (int i = 13; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int xp = activityByDate.getOrDefault(day, 0);
            Region cell = new Region();
            cell.setPrefSize(13, 13);
            cell.setMinSize(13, 13);
            cell.setMaxSize(13, 13);
            String styleClass = xp <= 0 ? "heatmap-cell-0"
                    : (xp <= 70 ? "heatmap-cell-1" : (xp <= 150 ? "heatmap-cell-2" : "heatmap-cell-3"));
            cell.getStyleClass().add(styleClass);
            Tooltip.install(cell, new Tooltip(day.format(fmt) + ": +" + xp + " XP (Actividad_Diaria)"));
            heatmapGridBox.getChildren().add(cell);
        }
    }

    private VBox buildCoursesWorkspaceView() {
        VBox view = new VBox(16);

        HBox secHeader = Theme.createSectionHeader(
                "Rutas de Especialización Multilenguaje (Inscripciones)",
                "3 Lenguajes Activos", "badge-indigo"
        );

        HBox langGrid = new HBox(16);
        Node cardJava = createLanguageCard(
                "JAVA", "Java 21 LTS",
                "POO, Clases Abstractas, Interfaces y Colecciones en JVM.",
                "lang-card-java", "btn-java", javaLessons.size()
        );
        Node cardPython = createLanguageCard(
                "PYTHON", "Python 3.12",
                "Protocolos ABC, Comprensión de Listas y Búsqueda Binaria.",
                "lang-card-python", "btn-python", pythonLessons.size()
        );
        Node cardCpp = createLanguageCard(
                "CPP", "ISO C++ 20",
                "Funciones Virtuales Puras, Memoria RAII y STL std::vector.",
                "lang-card-cpp", "btn-cpp", cppLessons.size()
        );
        HBox.setHgrow(cardJava, Priority.ALWAYS);
        HBox.setHgrow(cardPython, Priority.ALWAYS);
        HBox.setHgrow(cardCpp, Priority.ALWAYS);
        langGrid.getChildren().addAll(cardJava, cardPython, cardCpp);

        VBox lessonsWrapper = new VBox(12);
        lessonsWrapper.getStyleClass().add("surface-card");
        VBox.setVgrow(lessonsWrapper, Priority.ALWAYS);

        lblCourseStatus = new Label(oracleConnected
                ? "Inscripciones sincronizadas con la tabla Inscripciones en Oracle."
                : "Modo demostración local sin conexión a Oracle.");
        lblCourseStatus.getStyleClass().add("muted-text");

        HBox listHeader = new HBox(10);
        listHeader.setAlignment(Pos.CENTER_LEFT);
        lblLessonsHeader = new Label("ÁRBOL DE HABILIDADES Y PROGRESO · JAVA");
        lblLessonsHeader.getStyleClass().add("section-caption");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label badgeHint = Theme.createPillBadge("Nodos conectados con Progreso_Leccion", "badge-green");
        listHeader.getChildren().addAll(lblLessonsHeader, sp, lblCourseStatus, badgeHint);

        lessonsListBox = new VBox(12);
        ScrollPane scroll = new ScrollPane(lessonsListBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-clean");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        lessonsWrapper.getChildren().addAll(listHeader, scroll);
        view.getChildren().addAll(secHeader, langGrid, lessonsWrapper);
        return view;
    }

    private VBox createLanguageCard(
            final String langKey,
            String title,
            String description,
            String cardStyleClass,
            String btnStyleClass,
            int lessonCount
    ) {
        VBox card = new VBox(8);
        card.getStyleClass().add(cardStyleClass);

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("card-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        boolean isEnrolled = enrolledLanguages.contains(langKey);
        final Label enrollBadge = Theme.createPillBadge(
                isEnrolled ? "INSCRITO ✓" : "DISPONIBLE",
                isEnrolled ? "badge-green" : "badge-neutral"
        );
        topRow.getChildren().addAll(lblTitle, sp, enrollBadge);

        Label lblDesc = new Label(description);
        lblDesc.getStyleClass().add("muted-text");
        lblDesc.setWrapText(true);

        ProgressBar langProgress = new ProgressBar(0.0);
        langProgress.getStyleClass().add("progress-bar-green");
        langProgress.setMaxWidth(Double.MAX_VALUE);
        courseProgressBars.put(langKey, langProgress);

        Label lblProgressInfo = new Label("0 / " + lessonCount + " completadas (0%)");
        lblProgressInfo.getStyleClass().add("muted-text");
        courseProgressLabels.put(langKey, lblProgressInfo);

        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Label badgeCount = Theme.createPillBadge(lessonCount + " Lecciones · 3 Módulos", "badge-indigo");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        final Button btnEnroll = new Button(isEnrolled ? "Abrir Ruta" : "Inscribirme");
        btnEnroll.getStyleClass().add(btnStyleClass);
        btnEnroll.setOnAction(e -> {
            if (!enrolledLanguages.contains(langKey)) {
                if (persistEnrollmentInOracle(langKey)) {
                    enrolledLanguages.add(langKey);
                    enrollBadge.setText("INSCRITO ✓");
                    enrollBadge.getStyleClass().setAll("badge-green");
                    btnEnroll.setText("Abrir Ruta");
                } else {
                    return;
                }
            }
            populateLessonsForLanguage(langKey);
        });

        card.setOnMouseClicked(e -> populateLessonsForLanguage(langKey));
        bottomRow.getChildren().addAll(badgeCount, sp2, btnEnroll);
        card.getChildren().addAll(topRow, lblDesc, langProgress, lblProgressInfo, bottomRow);
        return card;
    }

    private void refreshCourseProgressBars() {
        updateSingleCourseProgress("JAVA", javaLessons);
        updateSingleCourseProgress("PYTHON", pythonLessons);
        updateSingleCourseProgress("CPP", cppLessons);
    }

    private void updateSingleCourseProgress(String langKey, List<Lesson> lessons) {
        ProgressBar bar = courseProgressBars.get(langKey);
        Label label = courseProgressLabels.get(langKey);
        if (bar == null || label == null) {
            return;
        }
        int total = lessons != null ? lessons.size() : 0;
        int completed = 0;
        for (int i = 0; i < total; i++) {
            if (resolveLessonProgressStatus(lessons.get(i), i) == ProgressStatus.COMPLETED) {
                completed++;
            }
        }
        double ratio = total > 0 ? (double) completed / total : 0.0;
        int pct = (int) Math.round(ratio * 100.0);
        bar.setProgress(ratio);
        label.setText(completed + " / " + total + " lecciones completadas (" + pct + "%)");
    }

    private boolean persistEnrollmentInOracle(String langKey) {
        if (!oracleConnected || currentUser.getId() <= 0) {
            return true;
        }
        Language targetLang = "PYTHON".equals(langKey) ? pythonLangModel
                : ("CPP".equals(langKey) ? cppLangModel : javaLangModel);
        try {
            controller.enrollInLanguage(targetLang);
            if (lblCourseStatus != null) {
                lblCourseStatus.getStyleClass().setAll("text-success");
                lblCourseStatus.setText("Inscripción guardada en tabla Inscripciones (" + langKey + ").");
            }
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al guardar inscripción de lenguaje " + langKey + " en Oracle", ex);
            if (lblCourseStatus != null) {
                lblCourseStatus.getStyleClass().setAll("text-danger");
                lblCourseStatus.setText("Error al guardar inscripción en Oracle: " + ex.getMessage());
            }
            return false;
        }
    }

    private void populateLessonsForLanguage(String langKey) {
        this.activeLanguageKey = langKey;
        lessonsListBox.getChildren().clear();

        List<Lesson> selectedList;
        String langLabel;
        if ("PYTHON".equals(langKey)) {
            selectedList = pythonLessons;
            langLabel = "PYTHON 3.12";
        } else if ("CPP".equals(langKey)) {
            selectedList = cppLessons;
            langLabel = "ISO C++ 20";
        } else {
            selectedList = javaLessons;
            langLabel = "JAVA 21 LTS";
        }

        lblLessonsHeader.setText("ÁRBOL DE HABILIDADES Y PROGRESO (PROGRESO_LECCION) · " + langLabel);

        String lastModuleTitle = "";
        for (int i = 0; i < selectedList.size(); i++) {
            Lesson lesson = selectedList.get(i);
            CourseModule module = lesson.getModule();
            String modTitle = (module != null && module.getTitle() != null)
                    ? module.getTitle()
                    : "Módulo Curricular";
            if (!modTitle.equals(lastModuleTitle)) {
                lastModuleTitle = modTitle;
                String modDesc = (module != null && module.getDescription() != null)
                        ? module.getDescription()
                        : "";
                lessonsListBox.getChildren().add(createModuleHeaderRow(modTitle, modDesc));
            }
            ProgressStatus status = resolveLessonProgressStatus(lesson, i);
            lessonsListBox.getChildren().add(createSkillNodeRow(i + 1, lesson, status));
        }
        Theme.playFadeIn(lessonsListBox);
    }

    private HBox createModuleHeaderRow(String moduleTitle, String moduleDescription) {
        HBox header = new HBox(12);
        header.getStyleClass().add("module-header-card");
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblModTitle = new Label(moduleTitle);
        lblModTitle.getStyleClass().add("card-title");
        lblModTitle.setGraphic(Theme.createIcon(Theme.IconKind.SPARK, 15, Theme.HEX_INDIGO));
        lblModTitle.setGraphicTextGap(8);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblModDesc = new Label(moduleDescription);
        lblModDesc.getStyleClass().add("muted-text");

        header.getChildren().addAll(lblModTitle, sp, lblModDesc);
        return header;
    }

    private ProgressStatus resolveLessonProgressStatus(Lesson lesson, int indexInList) {
        return controller.resolveLessonProgressStatus(lesson, indexInList);
    }

    private HBox createSkillNodeRow(int orderNum, final Lesson lesson, ProgressStatus status) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        String nodeStyleClass;
        String statusText;
        String badgeStyleClass;
        Theme.IconKind iconKind;
        String iconHex;

        if (status == ProgressStatus.COMPLETED) {
            nodeStyleClass = "skill-node-completed";
            statusText = "COMPLETADA";
            badgeStyleClass = "badge-green";
            iconKind = Theme.IconKind.CHECK;
            iconHex = Theme.HEX_GREEN;
        } else if (status == ProgressStatus.IN_PROGRESS) {
            nodeStyleClass = "skill-node-active";
            statusText = "EN CURSO";
            badgeStyleClass = "badge-indigo";
            iconKind = Theme.IconKind.PLAY;
            iconHex = Theme.HEX_INDIGO;
        } else {
            nodeStyleClass = "skill-node-pending";
            statusText = "PENDIENTE";
            badgeStyleClass = "badge-neutral";
            iconKind = Theme.IconKind.BOOK;
            iconHex = Theme.HEX_MUTED;
        }
        row.getStyleClass().add(nodeStyleClass);

        VBox textColumn = new VBox(4);
        HBox.setHgrow(textColumn, Priority.ALWAYS);

        HBox titleLine = new HBox(10);
        titleLine.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("Nodo #" + orderNum + ": " + lesson.getTitle());
        lblTitle.getStyleClass().add("card-title");

        Label statusBadge = Theme.createPillBadge(statusText, badgeStyleClass);
        Label xpBadge = Theme.createPillBadge("+" + lesson.getExpReward() + " XP", "badge-amber");

        int challengeCount = lesson.getChallenges() != null ? lesson.getChallenges().size() : 0;
        Label chBadge = Theme.createPillBadge(challengeCount + " Retos", "badge-violet");

        titleLine.getChildren().addAll(lblTitle, statusBadge, xpBadge, chBadge);

        String preview = lesson.getTheory() != null ? lesson.getTheory().replace("\n", " ") : "";
        if (preview.length() > 115) {
            preview = preview.substring(0, 115) + "...";
        }
        Label lblPreview = new Label(preview);
        lblPreview.getStyleClass().add("muted-text");

        textColumn.getChildren().addAll(titleLine, lblPreview);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnTheory = new Button("Estudiar Teoría");
        btnTheory.getStyleClass().add("btn-secondary");
        btnTheory.setOnAction(e -> new LessonFrame(currentUser, lesson, this::onLessonStateChanged).show());
        actions.getChildren().add(btnTheory);

        List<Challenge> lessonChallenges = lesson.getChallenges();
        if (lessonChallenges != null && !lessonChallenges.isEmpty()) {
            for (final Challenge ch : lessonChallenges) {
                boolean isQuiz = ch.getType() == ChallengeType.QUIZ;
                Button btnChallenge = new Button(isQuiz ? "Reto Quiz" : "Reto Código");
                btnChallenge.getStyleClass().add(isQuiz ? "btn-ghost" : "btn-primary");
                btnChallenge.setOnAction(e -> new ChallengeFrame(currentUser, ch, this::onChallengeOrLessonCompleted).show());
                actions.getChildren().add(btnChallenge);
            }
        } else {
            Button btnSolve = new Button("Resolver Reto");
            btnSolve.getStyleClass().add("btn-primary");
            btnSolve.setOnAction(e -> new ChallengeFrame(currentUser, null, this::onChallengeOrLessonCompleted).show());
            actions.getChildren().add(btnSolve);
        }

        row.getChildren().addAll(
                Theme.createIcon(iconKind, 20, iconHex),
                textColumn,
                actions
        );
        return row;
    }

    private VBox buildComparatorWorkspaceView() {
        VBox view = new VBox(14);

        VBox topToolbar = new VBox(10);
        topToolbar.getStyleClass().add("surface-card");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("Comparador Espejo Sincronizado · 6 Tópicos Curriculares (Java · Python · C++)");
        lblTitle.getStyleClass().add("page-title");
        lblTitle.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 20, Theme.HEX_CYAN));
        lblTitle.setGraphicTextGap(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badgeTopics = Theme.createPillBadge("6 Tópicos Interactivos", "badge-cyan");
        titleRow.getChildren().addAll(lblTitle, spacer, badgeTopics);

        HBox topicsRow = new HBox(8);
        topicsRow.setAlignment(Pos.CENTER_LEFT);
        comparatorTopicButtons.clear();

        String[][] topics = {
            {"POLYMORPHISM", "1. Polimorfismo POO"},
            {"INTERFACES", "2. Contratos SOLID / Dunder"},
            {"BINARY_SEARCH", "3. Búsqueda Binaria O(log n)"},
            {"TWO_POINTERS", "4. Dos Punteros y Slicing"},
            {"COLLECTIONS", "5. Colecciones Dinámicas"},
            {"HASH_SETS", "6. Tablas Hash O(1)"}
        };

        for (final String[] topic : topics) {
            Button btnTopic = new Button(topic[1]);
            btnTopic.getStyleClass().add("btn-secondary");
            btnTopic.setUserData(topic[0]);
            btnTopic.setOnAction(e -> loadComparisonTopic(topic[0]));
            comparatorTopicButtons.add(btnTopic);
            topicsRow.getChildren().add(btnTopic);
        }

        topToolbar.getChildren().addAll(titleRow, topicsRow);

        txtCompareJava = createComparisonTextArea();
        txtComparePython = createComparisonTextArea();
        txtCompareCpp = createComparisonTextArea();

        HBox threeColumns = new HBox(14);
        VBox.setVgrow(threeColumns, Priority.ALWAYS);

        VBox colJava = wrapCodeColumn("JAVA 21 LTS", "Tipado Estático · JVM", "lang-card-java", "badge-java", txtCompareJava);
        VBox colPy = wrapCodeColumn("PYTHON 3.12", "Tipado Dinámico · ABC", "lang-card-python", "badge-python", txtComparePython);
        VBox colCpp = wrapCodeColumn("ISO C++ 20", "Compilado Nativo · STL", "lang-card-cpp", "badge-cpp", txtCompareCpp);

        HBox.setHgrow(colJava, Priority.ALWAYS);
        HBox.setHgrow(colPy, Priority.ALWAYS);
        HBox.setHgrow(colCpp, Priority.ALWAYS);
        threeColumns.getChildren().addAll(colJava, colPy, colCpp);

        HBox bottomCallout = new HBox(12);
        bottomCallout.getStyleClass().add("surface-card");
        bottomCallout.setAlignment(Pos.CENTER_LEFT);

        lblCompareSummary = new Label();
        lblCompareSummary.getStyleClass().add("body-text");
        lblCompareSummary.setWrapText(true);
        lblCompareSummary.setGraphic(Theme.createIcon(Theme.IconKind.BOLT, 18, Theme.HEX_GREEN));
        lblCompareSummary.setGraphicTextGap(10);
        HBox.setHgrow(lblCompareSummary, Priority.ALWAYS);

        Button btnPractice = new Button("Practicar este Tópico en el Evaluador");
        btnPractice.getStyleClass().add("btn-primary");
        btnPractice.setOnAction(e -> {
            List<Lesson> targetList = "PYTHON".equals(activeLanguageKey) ? pythonLessons
                    : ("CPP".equals(activeLanguageKey) ? cppLessons : javaLessons);
            Lesson l = (!targetList.isEmpty())
                    ? targetList.get(Math.min(activeComparatorLessonIndex, targetList.size() - 1))
                    : null;
            Challenge ch = (l != null && l.getChallenges() != null && !l.getChallenges().isEmpty())
                    ? l.getChallenges().get(0) : null;
            new ChallengeFrame(currentUser, ch, this::onChallengeOrLessonCompleted).show();
        });

        bottomCallout.getChildren().addAll(lblCompareSummary, btnPractice);
        view.getChildren().addAll(topToolbar, threeColumns, bottomCallout);
        return view;
    }

    private TextArea createComparisonTextArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.getStyleClass().add("code-editor");
        VBox.setVgrow(area, Priority.ALWAYS);
        return area;
    }

    private VBox wrapCodeColumn(String langName, String paradigmTag, String cardClass, String badgeClass, TextArea editor) {
        VBox col = new VBox(10);
        col.getStyleClass().add(cardClass);

        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        Label lblLang = new Label(langName);
        lblLang.getStyleClass().add("card-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label badge = Theme.createPillBadge(paradigmTag, badgeClass);
        head.getChildren().addAll(lblLang, sp, badge);

        col.getChildren().addAll(head, editor);
        return col;
    }

    private void loadComparisonTopic(String topicKey) {
        for (Button btn : comparatorTopicButtons) {
            boolean active = topicKey.equals(btn.getUserData());
            btn.getStyleClass().setAll(active ? "btn-primary" : "btn-secondary");
        }

        int lessonIdx = 0;
        if ("INTERFACES".equals(topicKey)) {
            lessonIdx = 1;
        } else if ("BINARY_SEARCH".equals(topicKey)) {
            lessonIdx = 2;
        } else if ("TWO_POINTERS".equals(topicKey)) {
            lessonIdx = 3;
        } else if ("COLLECTIONS".equals(topicKey)) {
            lessonIdx = 4;
        } else if ("HASH_SETS".equals(topicKey)) {
            lessonIdx = 5;
        }
        this.activeComparatorLessonIndex = lessonIdx;

        Lesson sampleLesson = (!javaLessons.isEmpty() && lessonIdx < javaLessons.size())
                ? javaLessons.get(lessonIdx)
                : null;
        String[] javaSnippet = DemoSeedCatalog.resolveComparativeSnippet(sampleLesson, "JAVA");
        String[] pySnippet = DemoSeedCatalog.resolveComparativeSnippet(sampleLesson, "PYTHON");
        String[] cppSnippet = DemoSeedCatalog.resolveComparativeSnippet(sampleLesson, "CPP");

        txtCompareJava.setText(javaSnippet[0]);
        txtComparePython.setText(pySnippet[0]);
        txtCompareCpp.setText(cppSnippet[0]);
        lblCompareSummary.setText("Análisis Comparativo · " + javaSnippet[1] + " | " + pySnippet[1] + " | " + cppSnippet[1]);
        Theme.playFadeIn(txtCompareJava);
        Theme.playFadeIn(txtComparePython);
        Theme.playFadeIn(txtCompareCpp);
    }

    private VBox buildAchievementsWorkspaceView() {
        VBox view = new VBox(14);

        HBox topHeader = new HBox(12);
        topHeader.getStyleClass().add("surface-card");
        topHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Catálogo de Logros e Insignias (Logros y Usuario_Logro)");
        lblTitle.getStyleClass().add("page-title");
        lblTitle.setGraphic(Theme.createIcon(Theme.IconKind.TROPHY, 20, Theme.HEX_AMBER));
        lblTitle.setGraphicTextGap(8);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblAchievementsSummary = new Label();
        lblAchievementsSummary.getStyleClass().add("text-success");

        topHeader.getChildren().addAll(lblTitle, spacer, lblAchievementsSummary);

        achievementsGrid = new GridPane();
        achievementsGrid.setHgap(14);
        achievementsGrid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        achievementsGrid.getColumnConstraints().addAll(col1, col2);

        ScrollPane scroll = new ScrollPane(achievementsGrid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-clean");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        view.getChildren().addAll(topHeader, scroll);
        return view;
    }

    private void refreshAchievementsGrid() {
        if (achievementsGrid == null) {
            return;
        }
        achievementsGrid.getChildren().clear();

        Set<Integer> unlockedIds = controller.resolveUnlockedAchievementIds(
                achievementsCatalog,
                solvedChallengesCount
        );

        int unlockedCount = 0;
        for (int i = 0; i < achievementsCatalog.size(); i++) {
            Achievement ach = achievementsCatalog.get(i);
            boolean unlocked = unlockedIds.contains(ach.getId());
            if (unlocked) {
                unlockedCount++;
            }
            int col = i % 2;
            int row = i / 2;
            achievementsGrid.add(createAchievementCard(ach, unlocked), col, row);
        }

        lblAchievementsSummary.setText("Desbloqueados: " + unlockedCount + " de " + achievementsCatalog.size() + " logros");
    }

    private HBox createAchievementCard(Achievement ach, boolean unlocked) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add(unlocked ? "achievement-card-unlocked" : "achievement-card-locked");

        String iconHex = unlocked ? Theme.HEX_AMBER : Theme.HEX_MUTED;
        Theme.IconKind kind = Theme.IconKind.TROPHY;
        if (ach instanceof StreakAchievement) {
            kind = Theme.IconKind.FLAME;
        } else if (ach instanceof ExperienceAchievement) {
            kind = Theme.IconKind.BOLT;
        } else if (ach instanceof ChallengeCountAchievement) {
            kind = Theme.IconKind.CODE;
        }

        VBox body = new VBox(5);
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label lblName = new Label(ach.getName());
        lblName.getStyleClass().add("card-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label statusBadge = Theme.createPillBadge(
                unlocked ? "DESBLOQUEADO ✓" : "EN PROGRESO",
                unlocked ? "badge-green" : "badge-neutral"
        );
        top.getChildren().addAll(lblName, sp, statusBadge);

        Label lblDesc = new Label(ach.getDescription());
        lblDesc.getStyleClass().add("body-text");
        lblDesc.setWrapText(true);

        String ruleText = "Meta del logro";
        if (ach instanceof StreakAchievement) {
            ruleText = "Meta Logro_Racha: " + ((StreakAchievement) ach).getRequiredDays() + " días consecutivos";
        } else if (ach instanceof ExperienceAchievement) {
            ruleText = "Meta Logro_Exp: " + ((ExperienceAchievement) ach).getRequiredExp() + " XP acumulados";
        } else if (ach instanceof ChallengeCountAchievement) {
            ruleText = "Meta Logro_Retos: " + ((ChallengeCountAchievement) ach).getRequiredChallenges() + " retos superados";
        }
        Label lblRule = new Label(ruleText);
        lblRule.getStyleClass().add(unlocked ? "text-success" : "muted-text");

        body.getChildren().addAll(top, lblDesc, lblRule);
        card.getChildren().addAll(Theme.createIcon(kind, 24, iconHex), body);
        return card;
    }

    private VBox buildAdminCrudWorkspaceView() {
        VBox view = new VBox(14);

        HBox headerCard = new HBox(12);
        headerCard.getStyleClass().add("surface-card");
        headerCard.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Panel de Administración CRUD de Retos (Retos, Reto_Codigo y Casos_Prueba)");
        lblTitle.getStyleClass().add("page-title");
        lblTitle.setGraphic(Theme.createIcon(Theme.IconKind.SHIELD, 20, Theme.HEX_GREEN));
        lblTitle.setGraphicTextGap(8);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeRole = Theme.createPillBadge("ROL ADMINISTRADOR VERIFICADO", "badge-green");
        headerCard.getChildren().addAll(lblTitle, spacer, badgeRole);

        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("surface-card");
        VBox.setVgrow(formCard, Priority.ALWAYS);

        HBox row1 = new HBox(14);
        cmbAdminLesson = new ComboBox<>();
        cmbAdminLesson.getStyleClass().add("combo-box-modern");
        cmbAdminLesson.setMaxWidth(Double.MAX_VALUE);
        for (Lesson l : allLessonsForAdmin) {
            String langPrefix = (l.getModule() != null && l.getModule().getLanguage() != null)
                    ? l.getModule().getLanguage().getName() + " · " : "";
            cmbAdminLesson.getItems().add(langPrefix + "Lección #" + l.getOrderIndex() + ": " + l.getTitle());
        }
        if (!cmbAdminLesson.getItems().isEmpty()) {
            cmbAdminLesson.getSelectionModel().selectFirst();
        }

        txtAdminTitle = new TextField("Reto Personalizado: Invertir Cadena");
        txtAdminTitle.getStyleClass().add("input-field");

        txtAdminReward = new TextField("90");
        txtAdminReward.getStyleClass().add("input-field");

        VBox colLesson = wrapLabeledField("LECCIÓN DESTINO (ID_LECCION)", cmbAdminLesson);
        VBox colTitle = wrapLabeledField("TÍTULO DEL RETO", txtAdminTitle);
        VBox colReward = wrapLabeledField("RECOMPENSA XP", txtAdminReward);
        HBox.setHgrow(colLesson, Priority.ALWAYS);
        HBox.setHgrow(colTitle, Priority.ALWAYS);
        colReward.setPrefWidth(150);
        row1.getChildren().addAll(colLesson, colTitle, colReward);

        HBox row2 = new HBox(14);
        VBox.setVgrow(row2, Priority.ALWAYS);

        txtAdminStatement = new TextArea(
                "Implementa el método solve(String input) para que retorne la cadena invertida o procesada según los casos de prueba.");
        txtAdminStatement.setWrapText(true);
        txtAdminStatement.getStyleClass().add("code-editor");
        VBox.setVgrow(txtAdminStatement, Priority.ALWAYS);

        txtAdminTemplate = new TextArea(
                "public class CustomSolver {\n"
                + "    public static String solve(String input) {\n"
                + "        if (\"Java\".equalsIgnoreCase(input)) return \"avaJ\";\n"
                + "        if (\"UPC\".equalsIgnoreCase(input)) return \"CPU\";\n"
                + "        return \"\";\n"
                + "    }\n"
                + "}");
        txtAdminTemplate.getStyleClass().add("code-editor");
        VBox.setVgrow(txtAdminTemplate, Priority.ALWAYS);

        VBox colStatement = wrapLabeledField("ENUNCIADO DEL PROBLEMA", txtAdminStatement);
        VBox colTemplate = wrapLabeledField("PLANTILLA INICIAL DE CÓDIGO (PLANTILLA_CODIGO)", txtAdminTemplate);
        HBox.setHgrow(colStatement, Priority.ALWAYS);
        HBox.setHgrow(colTemplate, Priority.ALWAYS);
        VBox.setVgrow(colStatement, Priority.ALWAYS);
        VBox.setVgrow(colTemplate, Priority.ALWAYS);
        row2.getChildren().addAll(colStatement, colTemplate);

        HBox casesRow = new HBox(12);
        txtAdminCase1In = new TextField("Java");
        txtAdminCase1In.getStyleClass().add("input-field");
        txtAdminCase1Out = new TextField("avaJ");
        txtAdminCase1Out.getStyleClass().add("input-field");
        txtAdminCase2In = new TextField("UPC");
        txtAdminCase2In.getStyleClass().add("input-field");
        txtAdminCase2Out = new TextField("CPU");
        txtAdminCase2Out.getStyleClass().add("input-field");

        VBox c1In = wrapLabeledField("CASO #1 · ENTRADA", txtAdminCase1In);
        VBox c1Out = wrapLabeledField("CASO #1 · SALIDA ESPERADA", txtAdminCase1Out);
        VBox c2In = wrapLabeledField("CASO #2 · ENTRADA", txtAdminCase2In);
        VBox c2Out = wrapLabeledField("CASO #2 · SALIDA ESPERADA", txtAdminCase2Out);
        HBox.setHgrow(c1In, Priority.ALWAYS);
        HBox.setHgrow(c1Out, Priority.ALWAYS);
        HBox.setHgrow(c2In, Priority.ALWAYS);
        HBox.setHgrow(c2Out, Priority.ALWAYS);
        casesRow.getChildren().addAll(c1In, c1Out, c2In, c2Out);

        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        lblAdminStatus = new Label("Los retos creados se insertan en Retos, Reto_Codigo y Casos_Prueba en Oracle.");
        lblAdminStatus.getStyleClass().add("muted-text");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnCreateAndTest = new Button("Guardar Reto en Oracle y Probar en Evaluador");
        btnCreateAndTest.getStyleClass().add("btn-success");
        btnCreateAndTest.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_WHITE));
        btnCreateAndTest.setGraphicTextGap(8);
        btnCreateAndTest.setOnAction(e -> handleCreateAdminChallenge());

        actionRow.getChildren().addAll(lblAdminStatus, sp, btnCreateAndTest);

        formCard.getChildren().addAll(row1, row2, casesRow, actionRow);
        view.getChildren().addAll(headerCard, formCard);
        return view;
    }

    private VBox wrapLabeledField(String caption, Node control) {
        VBox box = new VBox(5);
        Label lbl = new Label(caption);
        lbl.getStyleClass().add("section-caption");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private void handleCreateAdminChallenge() {
        if (currentUser == null || !currentUser.isAdministrator()) {
            lblAdminStatus.getStyleClass().setAll("text-danger");
            lblAdminStatus.setText("Acceso denegado: Se requiere rol de Administrador.");
            return;
        }

        int lessonIdx = cmbAdminLesson.getSelectionModel().getSelectedIndex();
        Lesson targetLesson = (lessonIdx >= 0 && lessonIdx < allLessonsForAdmin.size())
                ? allLessonsForAdmin.get(lessonIdx)
                : (!javaLessons.isEmpty() ? javaLessons.get(0) : null);

        String title = txtAdminTitle.getText() != null ? txtAdminTitle.getText().trim() : "";
        String statement = txtAdminStatement.getText() != null ? txtAdminStatement.getText().trim() : "";
        String template = txtAdminTemplate.getText() != null ? txtAdminTemplate.getText() : "";
        int reward;
        try {
            reward = Integer.parseInt(txtAdminReward.getText().trim());
        } catch (NumberFormatException ex) {
            lblAdminStatus.getStyleClass().setAll("text-danger");
            lblAdminStatus.setText("La recompensa XP debe ser un número entero válido.");
            return;
        }

        CodeChallenge newCh = new CodeChallenge(
                0,
                title,
                statement,
                targetLesson,
                reward,
                template,
                2
        );
        newCh.addTestCase(new TestCase(1, newCh, txtAdminCase1In.getText().trim(), txtAdminCase1Out.getText().trim(), false));
        newCh.addTestCase(new TestCase(2, newCh, txtAdminCase2In.getText().trim(), txtAdminCase2Out.getText().trim(), false));

        try {
            Challenge saved = controller.createAdminChallenge(targetLesson, newCh);
            if (targetLesson != null) {
                targetLesson.addChallenge(saved);
            }
            lblAdminStatus.getStyleClass().setAll("text-success");
            lblAdminStatus.setText(oracleConnected && saved.getId() > 0
                    ? "¡Reto #" + saved.getId() + " guardado en Retos, Reto_Codigo y Casos_Prueba en Oracle!"
                    : "Reto creado localmente (modo demostración sin Oracle) y abierto en el evaluador.");
            populateLessonsForLanguage(activeLanguageKey);
            new ChallengeFrame(currentUser, saved, this::onChallengeOrLessonCompleted).show();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al crear reto desde el panel de administración", ex);
            lblAdminStatus.getStyleClass().setAll("text-danger");
            lblAdminStatus.setText("Error al guardar el reto: " + ex.getMessage());
        }
    }

    private void switchWorkspaceView(String cardKey) {
        if ("ADMIN".equals(cardKey) && (currentUser == null || !currentUser.isAdministrator())) {
            return;
        }

        Node target = coursesView;
        if ("COMPARATOR".equals(cardKey)) {
            target = comparatorView;
        } else if ("ACHIEVEMENTS".equals(cardKey)) {
            target = achievementsView;
        } else if ("ADMIN".equals(cardKey) && adminView != null) {
            target = adminView;
        }

        workspaceContainer.getChildren().setAll(target);
        Theme.playFadeIn(target);

        navCoursesBtn.getStyleClass().setAll("COURSES".equals(cardKey) ? "nav-btn-active" : "nav-btn");
        navCoursesBtn.setGraphic(Theme.createIcon(
                Theme.IconKind.BOOK, 16, "COURSES".equals(cardKey) ? Theme.HEX_WHITE : Theme.HEX_INDIGO));

        navComparatorBtn.getStyleClass().setAll("COMPARATOR".equals(cardKey) ? "nav-btn-active" : "nav-btn");
        navComparatorBtn.setGraphic(Theme.createIcon(
                Theme.IconKind.CODE, 16, "COMPARATOR".equals(cardKey) ? Theme.HEX_WHITE : Theme.HEX_CYAN));

        navAchievementsBtn.getStyleClass().setAll("ACHIEVEMENTS".equals(cardKey) ? "nav-btn-active" : "nav-btn");
        navAchievementsBtn.setGraphic(Theme.createIcon(
                Theme.IconKind.TROPHY, 16, "ACHIEVEMENTS".equals(cardKey) ? Theme.HEX_WHITE : Theme.HEX_AMBER));

        if (navAdminBtn != null) {
            navAdminBtn.getStyleClass().setAll("ADMIN".equals(cardKey) ? "nav-btn-active" : "nav-btn");
            navAdminBtn.setGraphic(Theme.createIcon(
                    Theme.IconKind.SHIELD, 16, "ADMIN".equals(cardKey) ? Theme.HEX_WHITE : Theme.HEX_GREEN));
        }
    }

    private void onLessonStateChanged() {
        this.lastKnownOfflineExp = currentUser.getTotalExp();
        refreshAllDashboardState();
    }

    private void onChallengeOrLessonCompleted() {
        if (!oracleConnected && currentUser.getTotalExp() > lastKnownOfflineExp) {
            solvedChallengesCount++;
        }
        this.lastKnownOfflineExp = currentUser.getTotalExp();
        refreshAllDashboardState();
    }

    private void refreshAllDashboardState() {
        this.solvedChallengesCount = controller.syncTelemetryAndGetSolvedChallenges(this.solvedChallengesCount);
        this.lastKnownOfflineExp = currentUser.getTotalExp();
        loadActivityHistoryFromOracle();
        rebuildHeatmapCells();
        refreshGamificationTelemetry();
        populateLessonsForLanguage(activeLanguageKey);
        refreshCourseProgressBars();
        refreshAchievementsGrid();
    }

    private void refreshGamificationTelemetry() {
        int exp = currentUser.getTotalExp();
        int computedLevel = Math.max(currentUser.getLevel(), 1 + exp / 300);
        currentUser.setLevel(computedLevel);

        int expInCurrentBracket = exp % 300;
        int remainingToNext = 300 - expInCurrentBracket;

        lblLevelVal.setText("Nivel " + currentUser.getLevel());
        lblLevelSub.setText("Faltan " + remainingToNext + " XP para Nivel " + (currentUser.getLevel() + 1));
        levelProgressBar.setProgress(expInCurrentBracket / 300.0);

        lblExpVal.setText(exp + " XP");
        lblExpSub.setText(solvedChallengesCount > 0
                ? solvedChallengesCount + " retos aprobados verificados"
                : (oracleConnected ? "Sincronizado con tabla Usuarios" : "Modo demostración local"));

        lblStreakVal.setText(currentUser.getCurrentStreak() + " Días");
        badgeMaxStreak.setText("Máx: " + currentUser.getMaxStreak() + "d");

        lblLivesVal.setText(currentUser.getLives() + " / 5");
        lblLivesSub.setText(currentUser.getLives() > 0 ? "Energía lista" : "Sin vidas disponibles");
        livesProgressBar.setProgress(currentUser.getLives() / 5.0);
    }
}
