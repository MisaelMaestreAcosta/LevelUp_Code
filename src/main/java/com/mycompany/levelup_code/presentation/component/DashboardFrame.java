package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.Role;
import com.mycompany.levelup_code.domain.model.StreakAchievement;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.domain.model.UserStats;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 * Panel principal interactivo conectado a Oracle 18c XE ({@link AppContext}) con:
 * - Árbol de nodos de lecciones ({@code Progreso_Leccion}),
 * - Mapa de calor de actividad diaria ({@code Actividad_Diaria}),
 * - Comparador sincronizado de 3 lenguajes ({@code Lecciones} espejo),
 * - Sistema de logros ({@code Logros} / {@code Usuario_Logro}) y
 * - Panel CRUD de administración para crear retos y {@code Casos_Prueba} en vivo.
 */
public class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final User currentUser;
    private final boolean oracleConnected;
    private final List<Language> catalogLanguages = new ArrayList<>();
    private final List<Lesson> javaLessons = new ArrayList<>();
    private final List<Lesson> pythonLessons = new ArrayList<>();
    private final List<Lesson> cppLessons = new ArrayList<>();
    private final List<Achievement> achievementsCatalog = new ArrayList<>();
    private final Map<LocalDate, Integer> activityByDate = new HashMap<>();

    private int solvedChallengesCount = 4;
    private int availableLives = 5;
    private String activeLanguageKey = "Java";
    private int currentComparatorTopic = 0;

    // Navegación principal
    private CardLayout workspaceCardLayout;
    private JPanel workspaceContainer;
    private Theme.ModernButton navCoursesBtn;
    private Theme.ModernButton navComparatorBtn;
    private Theme.ModernButton navAchievementsBtn;
    private Theme.ModernButton navAdminBtn;

    // Etiquetas de telemetría actualizables en tiempo real
    private JLabel lblExpValue;
    private JLabel lblLevelValue;
    private JLabel lblStreakValue;
    private JLabel lblLivesValue;
    private Theme.ModernProgressBar levelProgressBar;
    private JPanel heatmapStripPanel;

    // Contenedores dinámicos
    private JPanel lessonsListContainer;
    private JLabel lblSelectedLanguageHeader;
    private JPanel achievementsGridContainer;

    // Comparador de 3 columnas con resaltado equivalente
    private JTextArea txtCompJava;
    private JTextArea txtCompPython;
    private JTextArea txtCompCpp;
    private JLabel lblCompTakeaway;
    private JLabel lblCompMirrorSource;

    // Formulario CRUD de Administración
    private JComboBox<String> cmbAdminLesson;
    private final List<Lesson> allLessonsForAdmin = new ArrayList<>();
    private JTextField txtAdminTitle;
    private JTextField txtAdminStatement;
    private JTextField txtAdminReward;
    private JTextArea txtAdminTemplate;
    private JTextField txtAdminCase1In;
    private JTextField txtAdminCase1Out;
    private JTextField txtAdminCase2In;
    private JTextField txtAdminCase2Out;
    private JLabel lblAdminStatus;

    public DashboardFrame(User user) {
        this.currentUser = user != null ? user : createFallbackUser();
        this.oracleConnected = AppContext.getInstance().isOracleConnected();
        loadCurriculumFromOracleOrSeed();
        loadAchievementsFromOracleOrSeed();
        loadActivityHistoryFromOracle();
        initComponents();
        refreshGamificationTelemetry();
        populateLessonsForLanguage("Java");
        loadComparatorTopic(0);
    }

    private static User createFallbackUser() {
        User u = new User();
        u.setId(1);
        u.setUsername("Estudiante");
        u.setEmail("estudiante@levelup.edu.co");
        u.setTotalExp(1450);
        u.setLevel(5);
        u.setCurrentStreak(7);
        u.setMaxStreak(12);
        return u;
    }

    private void loadCurriculumFromOracleOrSeed() {
        catalogLanguages.clear();
        javaLessons.clear();
        pythonLessons.clear();
        cppLessons.clear();
        allLessonsForAdmin.clear();

        if (oracleConnected) {
            try {
                List<Language> dbLangs = AppContext.getInstance().getContentService().listActiveLanguages();
                if (!dbLangs.isEmpty()) {
                    catalogLanguages.addAll(dbLangs);
                    for (Language lang : dbLangs) {
                        List<Lesson> bucket = bucketForLanguage(lang.getName());
                        if (lang.getModules() != null) {
                            for (CourseModule mod : lang.getModules()) {
                                if (mod.getLessons() != null) {
                                    bucket.addAll(mod.getLessons());
                                    allLessonsForAdmin.addAll(mod.getLessons());
                                }
                            }
                        }
                    }
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        seedFallbackCurriculum();
    }

    private List<Lesson> bucketForLanguage(String langName) {
        if (langName == null) {
            return javaLessons;
        }
        String lower = langName.trim().toLowerCase();
        if (lower.contains("python")) {
            return pythonLessons;
        }
        if (lower.contains("c++") || lower.contains("cpp")) {
            return cppLessons;
        }
        return javaLessons;
    }

    private void seedFallbackCurriculum() {
        Language javaLang = new Language(1, "Java", "POO, Principios SOLID y Colecciones Tipadas", "java-icon", true);
        Language pyLang = new Language(2, "Python", "Estructuras Dinámicas, Comprensiones y Algoritmos", "python-icon", true);
        Language cppLang = new Language(3, "C++", "Gestión de Memoria, Punteros y STL de Alto Rendimiento", "cpp-icon", true);
        catalogLanguages.addAll(Arrays.asList(javaLang, pyLang, cppLang));

        CourseModule javaMod = new CourseModule(1, "Arquitectura Orientada a Objetos", "Herencia y Polimorfismo", 1, javaLang);
        Lesson jLesson1 = new Lesson(1, "Herencia, Clases Abstractas y Despacho Dinámico",
                "1. JERARQUÍAS ES-UN Y CONTRATOS\n"
                + "Las clases abstractas permiten factorizar estado y comportamiento común mientras delegan operaciones específicas a las subclases mediante métodos abstractos.",
                1, 60, javaMod);

        CodeChallenge jCode = new CodeChallenge(
                101,
                "Polimorfismo: Despacho Dinámico de Sonidos",
                "Implementa emitirSonido(String tipo) para que retorne \"Guau\" cuando reciba \"Perro\" y \"Miau\" cuando reciba \"Gato\".",
                jLesson1,
                60,
                "public class AnimalSoundSolver {\n"
                + "    public static String emitirSonido(String tipo) {\n"
                + "        if (\"Perro\".equalsIgnoreCase(tipo)) {\n"
                + "            return \"Guau\";\n"
                + "        }\n"
                + "        return \"\";\n"
                + "    }\n"
                + "}",
                2
        );
        jCode.addTestCase(new TestCase(1, jCode, "Perro", "Guau", false));
        jCode.addTestCase(new TestCase(2, jCode, "Gato", "Miau", false));
        jCode.addTestCase(new TestCase(3, jCode, "PERRO", "Guau", true));
        jLesson1.addChallenge(jCode);

        QuizChallenge jQuiz = new QuizChallenge(
                102,
                "Quiz: Enlace Dinámico y Sobrescritura",
                "¿Qué anotación en Java garantiza en compilación que un método sobrescribe realmente a uno de su superclase?",
                jLesson1,
                40,
                Arrays.asList("@Overload", "@Override", "@Virtual", "@DynamicBind"),
                "@Override"
        );
        jLesson1.addChallenge(jQuiz);
        javaLessons.add(jLesson1);
        allLessonsForAdmin.add(jLesson1);

        CourseModule pyMod = new CourseModule(2, "Estructuras Algorítmicas en Python", "Listas y Búsqueda", 1, pyLang);
        Lesson pyLesson1 = new Lesson(4, "Protocolos Abstractos (abc.ABC) y Duck Typing",
                "En Python, las clases base abstractas se definen heredando de abc.ABC y decorando métodos con @abstractmethod.",
                1, 60, pyMod);
        pyLesson1.addChallenge(jCode);
        pythonLessons.add(pyLesson1);
        allLessonsForAdmin.add(pyLesson1);

        CourseModule cppMod = new CourseModule(3, "Memoria, Referencias y Contenedores STL", "RAII y std::vector", 1, cppLang);
        Lesson cppLesson1 = new Lesson(7, "Funciones Virtuales Puras (= 0) y Tabla Virtual (vtable)",
                "En C++, el polimorfismo dinámico se activa declarando el método base como virtual.",
                1, 60, cppMod);
        cppLesson1.addChallenge(jCode);
        cppLessons.add(cppLesson1);
        allLessonsForAdmin.add(cppLesson1);
    }

    private void loadAchievementsFromOracleOrSeed() {
        achievementsCatalog.clear();
        if (oracleConnected) {
            try {
                List<Achievement> fromDb = AppContext.getInstance()
                        .getOracleAchievementRepository().findAllCatalog();
                if (!fromDb.isEmpty()) {
                    achievementsCatalog.addAll(fromDb);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        achievementsCatalog.add(new StreakAchievement(1, "Constancia de Acero", "Mantén una racha activa de 3 días seguidos practicando.", "FLAME", 3));
        achievementsCatalog.add(new StreakAchievement(2, "Hábito Algorítmico", "Alcanza una racha continua de 7 días de estudio.", "FLAME", 7));
        achievementsCatalog.add(new ExperienceAchievement(3, "Explorador Políglota", "Acumula tus primeros 1,000 puntos de experiencia (XP).", "BOLT", 1000));
        achievementsCatalog.add(new ExperienceAchievement(4, "Arquitecto Senior", "Supera la barrera de los 1,500 XP resolviendo lecciones y retos.", "TROPHY", 1500));
        achievementsCatalog.add(new ChallengeCountAchievement(5, "Domador de Casos de Prueba", "Resuelve exitosamente 5 retos de código o quiz.", "CODE", 5));
        achievementsCatalog.add(new StreakAchievement(6, "Leyenda del Semestre", "Alcanza una racha histórica de 14 días consecutivos.", "SHIELD", 14));
    }

    private void loadActivityHistoryFromOracle() {
        activityByDate.clear();
        if (oracleConnected && currentUser.getId() > 0) {
            try {
                List<DailyActivity> recent = AppContext.getInstance()
                        .getOracleActivityRepository().findRecentByUser(currentUser.getId(), 14);
                for (DailyActivity da : recent) {
                    if (da.getActivityDate() != null) {
                        activityByDate.put(da.getActivityDate(), da.getExperienceGained());
                    }
                }
                if (!activityByDate.isEmpty()) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        LocalDate today = LocalDate.now();
        int[] sampleXp = {60, 0, 120, 0, 80, 0, 0, 120, 180, 90, 210, 150, 240, 160};
        for (int i = 0; i < sampleXp.length; i++) {
            LocalDate d = today.minusDays(sampleXp.length - 1 - i);
            if (sampleXp[i] > 0) {
                activityByDate.put(d, sampleXp[i]);
            }
        }
    }

    private void initComponents() {
        setTitle("LevelUp Code — Espacio de Trabajo (" + currentUser.getUsername() + ")");
        setSize(1240, 760);
        setMinimumSize(new Dimension(1060, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);

        root.add(buildLeftSidebar(), BorderLayout.WEST);

        JPanel centerShell = new JPanel(new BorderLayout(0, 16));
        centerShell.setBackground(Theme.BG_DARK);
        centerShell.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        centerShell.add(buildTopTelemetryBar(), BorderLayout.NORTH);

        workspaceCardLayout = new CardLayout();
        workspaceContainer = new JPanel(workspaceCardLayout);
        workspaceContainer.setOpaque(false);

        workspaceContainer.add(buildCoursesWorkspaceView(), "COURSES");
        workspaceContainer.add(buildComparatorWorkspaceView(), "COMPARATOR");
        workspaceContainer.add(buildAchievementsWorkspaceView(), "ACHIEVEMENTS");
        workspaceContainer.add(buildAdminCrudWorkspaceView(), "ADMIN");

        centerShell.add(workspaceContainer, BorderLayout.CENTER);
        root.add(centerShell, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(22, 16, 20, 16)
        ));

        JPanel navColumn = new JPanel();
        navColumn.setLayout(new BoxLayout(navColumn, BoxLayout.Y_AXIS));
        navColumn.setOpaque(false);

        JLabel lblBrand = new JLabel("  LevelUp Code");
        lblBrand.setIcon(Theme.createVectorIcon(Theme.IconKind.TERMINAL, 22, Theme.ACCENT_INDIGO));
        lblBrand.setFont(Theme.FONT_TITLE);
        lblBrand.setForeground(Theme.TEXT_PRIMARY);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(oracleConnected
                ? "  Conectado a Oracle XEPDB1"
                : "  Aprendizaje Comparativo");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(oracleConnected ? Theme.ACCENT_GREEN : Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNavSection = new JLabel("  NAVEGACIÓN PRINCIPAL");
        lblNavSection.setFont(Theme.FONT_SECTION);
        lblNavSection.setForeground(Theme.TEXT_MUTED);
        lblNavSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        navCoursesBtn = new Theme.ModernButton(
                "Mapa de Nodos y Rutas", Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
        navCoursesBtn.setIcon(Theme.createVectorIcon(Theme.IconKind.BOOK, 16, Color.WHITE));
        navCoursesBtn.setHorizontalAlignment(SwingUtilities.LEFT);
        navCoursesBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        navCoursesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        navComparatorBtn = new Theme.ModernButton(
                "Comparador 3 Lenguajes", Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        navComparatorBtn.setIcon(Theme.createVectorIcon(Theme.IconKind.CODE, 16, Theme.ACCENT_CYAN));
        navComparatorBtn.setHorizontalAlignment(SwingUtilities.LEFT);
        navComparatorBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        navComparatorBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        navAchievementsBtn = new Theme.ModernButton(
                "Logros e Insignias", Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        navAchievementsBtn.setIcon(Theme.createVectorIcon(Theme.IconKind.TROPHY, 16, Theme.ACCENT_AMBER));
        navAchievementsBtn.setHorizontalAlignment(SwingUtilities.LEFT);
        navAchievementsBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        navAchievementsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        navAdminBtn = new Theme.ModernButton(
                "Gestión de Retos (Oracle)", Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        navAdminBtn.setIcon(Theme.createVectorIcon(Theme.IconKind.SHIELD, 16, Theme.ACCENT_GREEN));
        navAdminBtn.setHorizontalAlignment(SwingUtilities.LEFT);
        navAdminBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        navAdminBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnQuickLab = Theme.createGhostButton("Abrir Laboratorio de Retos", Theme.ACCENT_GREEN);
        btnQuickLab.setIcon(Theme.createVectorIcon(Theme.IconKind.PLAY, 14, Theme.ACCENT_GREEN));
        btnQuickLab.setHorizontalAlignment(SwingUtilities.LEFT);
        btnQuickLab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnQuickLab.setAlignmentX(Component.LEFT_ALIGNMENT);

        navCoursesBtn.addActionListener(e -> switchWorkspaceView("COURSES"));
        navComparatorBtn.addActionListener(e -> switchWorkspaceView("COMPARATOR"));
        navAchievementsBtn.addActionListener(e -> {
            refreshAchievementsGrid();
            switchWorkspaceView("ACHIEVEMENTS");
        });
        navAdminBtn.addActionListener(e -> switchWorkspaceView("ADMIN"));
        btnQuickLab.addActionListener(e -> {
            Lesson l = !javaLessons.isEmpty() ? javaLessons.get(0) : null;
            Challenge ch = (l != null && l.getChallenges() != null && !l.getChallenges().isEmpty())
                    ? l.getChallenges().get(0) : null;
            new ChallengeFrame(currentUser, ch, this::onChallengeOrLessonCompleted).setVisible(true);
        });

        navColumn.add(lblBrand);
        navColumn.add(Box.createVerticalStrut(4));
        navColumn.add(lblSub);
        navColumn.add(Box.createVerticalStrut(24));
        navColumn.add(lblNavSection);
        navColumn.add(Box.createVerticalStrut(10));
        navColumn.add(navCoursesBtn);
        navColumn.add(Box.createVerticalStrut(8));
        navColumn.add(navComparatorBtn);
        navColumn.add(Box.createVerticalStrut(8));
        navColumn.add(navAchievementsBtn);
        navColumn.add(Box.createVerticalStrut(8));
        navColumn.add(navAdminBtn);
        navColumn.add(Box.createVerticalStrut(16));
        navColumn.add(btnQuickLab);

        sidebar.add(navColumn, BorderLayout.NORTH);

        // Tarjeta inferior de perfil activo y cierre de sesión
        Theme.RoundedPanel userBox = new Theme.RoundedPanel(10, Theme.EDITOR_BG, Theme.BORDER_SUBTLE);
        userBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel profileTop = new JPanel(new BorderLayout(10, 0));
        profileTop.setOpaque(false);

        String initials = extractInitials(currentUser.getUsername());
        Color roleAccent = currentUser.isAdministrator() ? Theme.ACCENT_GREEN : Theme.ACCENT_INDIGO;
        profileTop.add(Theme.createAvatarBadge(initials, roleAccent, 36), BorderLayout.WEST);

        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);
        JLabel lblUName = new JLabel(currentUser.getUsername());
        lblUName.setFont(Theme.FONT_SUBTITLE);
        lblUName.setForeground(Theme.TEXT_PRIMARY);

        String roleLabel = currentUser.isAdministrator() ? "Rol: Administrador" : "Rol: Estudiante";
        JLabel lblURole = new JLabel(roleLabel);
        lblURole.setFont(Theme.FONT_SMALL);
        lblURole.setForeground(Theme.TEXT_MUTED);
        nameCol.add(lblUName);
        nameCol.add(lblURole);

        profileTop.add(nameCol, BorderLayout.CENTER);

        JButton btnLogout = Theme.createSecondaryButton("Cambiar Cuenta / Salir");
        btnLogout.setFont(Theme.FONT_SMALL);
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel userBoxContent = new JPanel(new BorderLayout(0, 10));
        userBoxContent.setOpaque(false);
        userBoxContent.add(profileTop, BorderLayout.NORTH);
        userBoxContent.add(btnLogout, BorderLayout.SOUTH);

        userBox.add(userBoxContent, BorderLayout.CENTER);
        sidebar.add(userBox, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildTopTelemetryBar() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);

        // 1. Tarjeta de Nivel y Progreso
        Theme.RoundedPanel cardLevel = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_INDIGO);
        cardLevel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 14));
        JLabel lblLvlCaption = new JLabel("NIVEL DEL ESTUDIANTE");
        lblLvlCaption.setFont(Theme.FONT_SECTION);
        lblLvlCaption.setForeground(Theme.TEXT_MUTED);
        lblLevelValue = new JLabel();
        lblLevelValue.setFont(Theme.FONT_METRIC);
        lblLevelValue.setForeground(Theme.TEXT_PRIMARY);
        levelProgressBar = Theme.createProgressBar(150, 300, Theme.ACCENT_INDIGO);
        cardLevel.add(lblLvlCaption, BorderLayout.NORTH);
        cardLevel.add(lblLevelValue, BorderLayout.CENTER);
        cardLevel.add(levelProgressBar, BorderLayout.SOUTH);

        // 2. Tarjeta de Experiencia Acumulada (XP)
        Theme.RoundedPanel cardExp = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_GREEN);
        cardExp.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 14));
        JLabel lblExpCaption = new JLabel("EXPERIENCIA ACUMULADA");
        lblExpCaption.setFont(Theme.FONT_SECTION);
        lblExpCaption.setForeground(Theme.TEXT_MUTED);
        lblExpValue = new JLabel();
        lblExpValue.setFont(Theme.FONT_METRIC);
        lblExpValue.setForeground(Theme.TEXT_PRIMARY);
        lblExpValue.setIcon(Theme.createVectorIcon(Theme.IconKind.BOLT, 20, Theme.ACCENT_GREEN));
        JLabel lblExpSub = new JLabel("Cada 300 XP subes 1 nivel");
        lblExpSub.setFont(Theme.FONT_SMALL);
        lblExpSub.setForeground(Theme.TEXT_MUTED);
        cardExp.add(lblExpCaption, BorderLayout.NORTH);
        cardExp.add(lblExpValue, BorderLayout.CENTER);
        cardExp.add(lblExpSub, BorderLayout.SOUTH);

        // 3. Tarjeta de Racha Activa + Mapa de Calor de Actividad Diaria (14 días)
        Theme.RoundedPanel cardStreak = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_AMBER);
        cardStreak.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 14));
        JLabel lblStreakCaption = new JLabel("RACHA Y MAPA DE ACTIVIDAD (14D)");
        lblStreakCaption.setFont(Theme.FONT_SECTION);
        lblStreakCaption.setForeground(Theme.TEXT_MUTED);
        lblStreakValue = new JLabel();
        lblStreakValue.setFont(Theme.FONT_METRIC);
        lblStreakValue.setForeground(Theme.TEXT_PRIMARY);
        lblStreakValue.setIcon(Theme.createVectorIcon(Theme.IconKind.FLAME, 20, Theme.ACCENT_AMBER));

        heatmapStripPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        heatmapStripPanel.setOpaque(false);
        rebuildHeatmapCells();

        cardStreak.add(lblStreakCaption, BorderLayout.NORTH);
        cardStreak.add(lblStreakValue, BorderLayout.CENTER);
        cardStreak.add(heatmapStripPanel, BorderLayout.SOUTH);

        // 4. Tarjeta de Vidas / Intentos
        Theme.RoundedPanel cardLives = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_ROSE);
        cardLives.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 14));
        JLabel lblLivesCaption = new JLabel("VIDAS DISPONIBLES");
        lblLivesCaption.setFont(Theme.FONT_SECTION);
        lblLivesCaption.setForeground(Theme.TEXT_MUTED);
        lblLivesValue = new JLabel();
        lblLivesValue.setFont(Theme.FONT_METRIC);
        lblLivesValue.setForeground(Theme.TEXT_PRIMARY);
        lblLivesValue.setIcon(Theme.createVectorIcon(Theme.IconKind.HEART, 20, Theme.ACCENT_ROSE));
        JLabel lblLivesSub = new JLabel("Recarga automática cada día");
        lblLivesSub.setFont(Theme.FONT_SMALL);
        lblLivesSub.setForeground(Theme.TEXT_MUTED);
        cardLives.add(lblLivesCaption, BorderLayout.NORTH);
        cardLives.add(lblLivesValue, BorderLayout.CENTER);
        cardLives.add(lblLivesSub, BorderLayout.SOUTH);

        grid.add(cardLevel);
        grid.add(cardExp);
        grid.add(cardStreak);
        grid.add(cardLives);
        return grid;
    }

    private void rebuildHeatmapCells() {
        if (heatmapStripPanel == null) {
            return;
        }
        heatmapStripPanel.removeAll();
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int xp = activityByDate.getOrDefault(day, 0);
            Color fill;
            if (xp <= 0) {
                fill = new Color(226, 232, 240);
            } else if (xp <= 80) {
                fill = new Color(187, 247, 208);
            } else if (xp <= 150) {
                fill = new Color(74, 222, 128);
            } else {
                fill = new Color(22, 163, 74);
            }
            JPanel cell = new JPanel();
            cell.setPreferredSize(new Dimension(11, 11));
            cell.setBackground(fill);
            cell.setBorder(BorderFactory.createLineBorder(Theme.darken(fill, 0.12f), 1));
            cell.setToolTipText("Actividad_Diaria (" + day + "): +" + xp + " XP");
            heatmapStripPanel.add(cell);
        }
        heatmapStripPanel.revalidate();
        heatmapStripPanel.repaint();
    }

    private JPanel buildCoursesWorkspaceView() {
        JPanel view = new JPanel(new BorderLayout(0, 16));
        view.setOpaque(false);

        // Parte superior: Tarjetas de los 3 lenguajes con sus colores oficiales de marca
        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);
        topSection.add(Theme.createSectionHeader(
                "Lenguajes de Estudio Inscritos (Identidad Cromática por Lenguaje)",
                "Java (#E76F00) · Python (#3776AB) · C++ (#00599C)"), BorderLayout.NORTH);

        JPanel langGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        langGrid.setOpaque(false);

        langGrid.add(createInteractiveLanguageCard(
                "Java", "Java 21 LTS", "POO, Jerarquías ES-UN, SOLID y Colecciones en JVM",
                75, Theme.LANG_JAVA, javaLessons));
        langGrid.add(createInteractiveLanguageCard(
                "Python", "Python 3.12", "Protocolos ABC, Listas Dinámicas y Búsqueda Algorítmica",
                55, Theme.LANG_PYTHON, pythonLessons));
        langGrid.add(createInteractiveLanguageCard(
                "C++", "ISO C++ 20", "Funciones Virtuales, Referencias Constantes y RAII",
                35, Theme.LANG_CPP, cppLessons));

        topSection.add(langGrid, BorderLayout.CENTER);
        view.add(topSection, BorderLayout.NORTH);

        // Parte inferior: Árbol de Habilidades (Skill-Tree) de Lecciones y Retos (Progreso_Leccion)
        Theme.RoundedPanel lessonsCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        lessonsCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel lessonsHeader = new JPanel(new BorderLayout(12, 0));
        lessonsHeader.setOpaque(false);
        lessonsHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));

        lblSelectedLanguageHeader = new JLabel("MAPA DE NODOS DE APRENDIZAJE (PROGRESO_LECCION) · JAVA");
        lblSelectedLanguageHeader.setFont(Theme.FONT_SECTION);
        lblSelectedLanguageHeader.setForeground(Theme.TEXT_MUTED);
        lessonsHeader.add(lblSelectedLanguageHeader, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("✓ Completada   ▶ En Curso   ○ Pendiente");
        lblHint.setFont(Theme.FONT_SMALL);
        lblHint.setForeground(Theme.TEXT_SECONDARY);
        lessonsHeader.add(lblHint, BorderLayout.EAST);

        lessonsListContainer = new JPanel();
        lessonsListContainer.setLayout(new BoxLayout(lessonsListContainer, BoxLayout.Y_AXIS));
        lessonsListContainer.setOpaque(false);
        lessonsListContainer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JScrollPane scrollLessons = new JScrollPane(lessonsListContainer);
        scrollLessons.setBorder(null);
        scrollLessons.setOpaque(false);
        scrollLessons.getViewport().setOpaque(false);

        lessonsCard.add(lessonsHeader, BorderLayout.NORTH);
        lessonsCard.add(scrollLessons, BorderLayout.CENTER);

        view.add(lessonsCard, BorderLayout.CENTER);
        return view;
    }

    private JPanel createInteractiveLanguageCard(
            String langKey, String versionTag, String description,
            int progressPct, Color accent, List<Lesson> lessons) {

        Theme.RoundedPanel card = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, accent);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 16));

        JPanel headerRow = new JPanel(new BorderLayout(8, 0));
        headerRow.setOpaque(false);

        JLabel lblName = new JLabel(langKey);
        lblName.setFont(Theme.FONT_TITLE);
        lblName.setForeground(accent);
        headerRow.add(lblName, BorderLayout.CENTER);
        headerRow.add(Theme.createPillBadge(versionTag, accent), BorderLayout.EAST);

        JLabel lblDesc = new JLabel("<html><body style='width:220px;color:#334155;'>" + description + "</body></html>");
        lblDesc.setFont(Theme.FONT_REGULAR);

        JPanel progressRow = new JPanel(new BorderLayout(0, 6));
        progressRow.setOpaque(false);
        JLabel lblPct = new JLabel("Avance del módulo: " + progressPct + "% (" + lessons.size() + " lecciones)");
        lblPct.setFont(Theme.FONT_SMALL);
        lblPct.setForeground(Theme.TEXT_MUTED);
        Theme.ModernProgressBar bar = Theme.createProgressBar(progressPct, 100, accent);
        progressRow.add(lblPct, BorderLayout.NORTH);
        progressRow.add(bar, BorderLayout.SOUTH);

        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 8, 0));
        actionsRow.setOpaque(false);

        JButton btnFilter = Theme.createSecondaryButton("Ver Mapa de Nodos");
        btnFilter.setFont(Theme.FONT_SMALL);
        btnFilter.addActionListener(e -> populateLessonsForLanguage(langKey));

        JButton btnStudy = Theme.createPrimaryButton("Continuar Ruta", accent);
        btnStudy.setFont(Theme.FONT_SMALL);
        btnStudy.addActionListener(e -> {
            populateLessonsForLanguage(langKey);
            if (!lessons.isEmpty()) {
                new LessonFrame(currentUser, lessons.get(0), this::onChallengeOrLessonCompleted).setVisible(true);
            }
        });

        actionsRow.add(btnFilter);
        actionsRow.add(btnStudy);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(headerRow);
        body.add(Box.createVerticalStrut(8));
        body.add(lblDesc);
        body.add(Box.createVerticalStrut(10));
        body.add(progressRow);
        body.add(Box.createVerticalStrut(12));
        body.add(actionsRow);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private ProgressStatus resolveLessonStatus(Lesson lesson, int indexInList) {
        if (oracleConnected && currentUser.getId() > 0 && lesson.getId() > 0) {
            try {
                Optional<LessonProgress> prog = AppContext.getInstance()
                        .getProgressRepository().findByUserAndLesson(currentUser.getId(), lesson.getId());
                if (prog.isPresent() && prog.get().getStatus() != null) {
                    return prog.get().getStatus();
                }
            } catch (Exception ignored) {
            }
        }
        if (indexInList == 0) {
            return ProgressStatus.COMPLETED;
        }
        if (indexInList == 1) {
            return ProgressStatus.IN_PROGRESS;
        }
        return ProgressStatus.PENDING;
    }

    private void populateLessonsForLanguage(String langKey) {
        this.activeLanguageKey = langKey;
        Color langAccent = Theme.colorForLanguage(langKey);
        lblSelectedLanguageHeader.setForeground(langAccent);
        lblSelectedLanguageHeader.setText("MAPA DE NODOS DE APRENDIZAJE (PROGRESO_LECCION) · " + langKey.toUpperCase());
        lessonsListContainer.removeAll();

        List<Lesson> targetLessons = "Python".equalsIgnoreCase(langKey)
                ? pythonLessons
                : ("C++".equalsIgnoreCase(langKey) ? cppLessons : javaLessons);

        for (int i = 0; i < targetLessons.size(); i++) {
            Lesson lesson = targetLessons.get(i);
            ProgressStatus status = resolveLessonStatus(lesson, i);
            boolean isLast = (i == targetLessons.size() - 1);

            Color statusAccent = status == ProgressStatus.COMPLETED
                    ? Theme.ACCENT_GREEN
                    : (status == ProgressStatus.IN_PROGRESS ? Theme.ACCENT_INDIGO : Theme.TEXT_MUTED);
            String statusText = status == ProgressStatus.COMPLETED
                    ? "COMPLETADA"
                    : (status == ProgressStatus.IN_PROGRESS ? "EN CURSO" : "PENDIENTE");

            JPanel nodeWrapper = new JPanel(new BorderLayout(12, 0));
            nodeWrapper.setOpaque(false);
            nodeWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

            // Indicador visual del nodo del árbol (Skill-Tree) con línea conectora
            SkillTreeNodeGraphic nodeGraphic = new SkillTreeNodeGraphic(i + 1, status, statusAccent, !isLast);
            nodeWrapper.add(nodeGraphic, BorderLayout.WEST);

            Theme.RoundedPanel row = new Theme.RoundedPanel(10, Theme.EDITOR_BG, Theme.BORDER_SUBTLE, langAccent);
            row.setLayout(new BorderLayout(16, 0));
            row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

            JPanel leftInfo = new JPanel(new BorderLayout(0, 4));
            leftInfo.setOpaque(false);

            JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            titleLine.setOpaque(false);
            JLabel lblLessonTitle = new JLabel(
                    "Nodo " + lesson.getOrderIndex() + " — " + lesson.getTitle());
            lblLessonTitle.setFont(Theme.FONT_SUBTITLE);
            lblLessonTitle.setForeground(Theme.TEXT_PRIMARY);
            titleLine.add(lblLessonTitle);
            titleLine.add(Theme.createPillBadge(statusText, statusAccent));

            int chCount = lesson.getChallenges() != null ? lesson.getChallenges().size() : 0;
            JLabel lblMeta = new JLabel(
                    "  Recompensa de lectura: +" + lesson.getExpReward() + " XP  ·  "
                    + chCount + " reto(s) práctico(s) en Casos_Prueba");
            lblMeta.setFont(Theme.FONT_SMALL);
            lblMeta.setForeground(Theme.TEXT_MUTED);

            leftInfo.add(titleLine, BorderLayout.NORTH);
            leftInfo.add(lblMeta, BorderLayout.SOUTH);

            JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightButtons.setOpaque(false);

            JButton btnCompare = Theme.createGhostButton("Comparar 3 Lenguajes", Theme.ACCENT_CYAN);
            btnCompare.setFont(Theme.FONT_SMALL);
            final int topicIdx = Math.max(0, Math.min(2, lesson.getOrderIndex() - 1));
            btnCompare.addActionListener(e -> {
                loadComparatorTopic(topicIdx);
                switchWorkspaceView("COMPARATOR");
            });

            JButton btnRead = Theme.createSecondaryButton("Estudiar Teoría");
            btnRead.setFont(Theme.FONT_SMALL);
            btnRead.addActionListener(e ->
                    new LessonFrame(currentUser, lesson, this::onChallengeOrLessonCompleted).setVisible(true));

            JButton btnChallenge = Theme.createPrimaryButton("Resolver Reto", langAccent);
            btnChallenge.setFont(Theme.FONT_SMALL);
            btnChallenge.addActionListener(e -> {
                if (lesson.getChallenges() != null && !lesson.getChallenges().isEmpty()) {
                    new ChallengeFrame(currentUser, lesson.getChallenges().get(0), this::onChallengeOrLessonCompleted)
                            .setVisible(true);
                } else {
                    new ChallengeFrame().setVisible(true);
                }
            });

            rightButtons.add(btnCompare);
            rightButtons.add(btnRead);
            rightButtons.add(btnChallenge);

            row.add(leftInfo, BorderLayout.CENTER);
            row.add(rightButtons, BorderLayout.EAST);

            nodeWrapper.add(row, BorderLayout.CENTER);
            lessonsListContainer.add(nodeWrapper);
            lessonsListContainer.add(Box.createVerticalStrut(6));
        }

        lessonsListContainer.revalidate();
        lessonsListContainer.repaint();
    }

    private JPanel buildComparatorWorkspaceView() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        Theme.RoundedPanel topControl = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        topControl.setLayout(new BorderLayout(12, 8));
        topControl.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel headerRow = new JPanel(new BorderLayout(12, 0));
        headerRow.setOpaque(false);

        JLabel lblTitle = new JLabel("  Comparador Algorítmico Espejo (Java · Python · C++)");
        lblTitle.setFont(Theme.FONT_SUBTITLE);
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        lblTitle.setIcon(Theme.createVectorIcon(Theme.IconKind.CODE, 18, Theme.ACCENT_CYAN));

        JPanel topicButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topicButtons.setOpaque(false);

        JButton btnTopic1 = Theme.createSecondaryButton("Secuencia #1: Polimorfismo");
        JButton btnTopic2 = Theme.createSecondaryButton("Secuencia #2: Búsqueda Binaria");
        JButton btnTopic3 = Theme.createSecondaryButton("Secuencia #3: Memoria y Listas");
        btnTopic1.setFont(Theme.FONT_SMALL);
        btnTopic2.setFont(Theme.FONT_SMALL);
        btnTopic3.setFont(Theme.FONT_SMALL);

        btnTopic1.addActionListener(e -> loadComparatorTopic(0));
        btnTopic2.addActionListener(e -> loadComparatorTopic(1));
        btnTopic3.addActionListener(e -> loadComparatorTopic(2));

        topicButtons.add(btnTopic1);
        topicButtons.add(btnTopic2);
        topicButtons.add(btnTopic3);

        headerRow.add(lblTitle, BorderLayout.CENTER);
        headerRow.add(topicButtons, BorderLayout.EAST);

        // Barra interactiva de resaltado sincronizado de líneas equivalentes
        JPanel syncBar = new JPanel(new BorderLayout(8, 0));
        syncBar.setOpaque(false);
        lblCompMirrorSource = new JLabel("Sincronizado por orden_secuencia en Lecciones (Oracle)");
        lblCompMirrorSource.setFont(Theme.FONT_SMALL);
        lblCompMirrorSource.setForeground(Theme.TEXT_MUTED);

        JPanel highlightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        highlightBtns.setOpaque(false);
        JLabel lblHl = new JLabel("Resaltar líneas equivalentes:");
        lblHl.setFont(Theme.FONT_SMALL);
        lblHl.setForeground(Theme.TEXT_SECONDARY);
        highlightBtns.add(lblHl);

        JButton btnHl1 = Theme.createGhostButton("1. Declaración / Firma", Theme.LANG_JAVA);
        JButton btnHl2 = Theme.createGhostButton("2. Lógica / Condición", Theme.LANG_PYTHON);
        JButton btnHl3 = Theme.createGhostButton("3. Retorno / Cierre", Theme.LANG_CPP);
        btnHl1.setFont(Theme.FONT_SMALL);
        btnHl2.setFont(Theme.FONT_SMALL);
        btnHl3.setFont(Theme.FONT_SMALL);

        btnHl1.addActionListener(e -> highlightEquivalentBlock(0));
        btnHl2.addActionListener(e -> highlightEquivalentBlock(1));
        btnHl3.addActionListener(e -> highlightEquivalentBlock(2));

        highlightBtns.add(btnHl1);
        highlightBtns.add(btnHl2);
        highlightBtns.add(btnHl3);

        syncBar.add(lblCompMirrorSource, BorderLayout.WEST);
        syncBar.add(highlightBtns, BorderLayout.EAST);

        topControl.add(headerRow, BorderLayout.NORTH);
        topControl.add(syncBar, BorderLayout.SOUTH);

        // 3 columnas simultáneas con los colores de marca de cada lenguaje
        JPanel columnsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        columnsGrid.setOpaque(false);

        txtCompJava = createReadOnlyCodeArea();
        txtCompPython = createReadOnlyCodeArea();
        txtCompCpp = createReadOnlyCodeArea();

        columnsGrid.add(wrapCodeColumn("JAVA 21 (JVM · #E76F00)", Theme.LANG_JAVA, txtCompJava));
        columnsGrid.add(wrapCodeColumn("PYTHON 3.12 (ABC · #3776AB)", Theme.LANG_PYTHON, txtCompPython));
        columnsGrid.add(wrapCodeColumn("C++ 20 (Nativo · #00599C)", Theme.LANG_CPP, txtCompCpp));

        Theme.RoundedPanel takeawayBar = new Theme.RoundedPanel(10, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_CYAN);
        takeawayBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        lblCompTakeaway = new JLabel();
        lblCompTakeaway.setFont(Theme.FONT_REGULAR);
        lblCompTakeaway.setForeground(Theme.TEXT_PRIMARY);
        takeawayBar.add(lblCompTakeaway, BorderLayout.CENTER);

        panel.add(topControl, BorderLayout.NORTH);
        panel.add(columnsGrid, BorderLayout.CENTER);
        panel.add(takeawayBar, BorderLayout.SOUTH);
        return panel;
    }

    private JTextArea createReadOnlyCodeArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(Theme.FONT_CODE);
        area.setBackground(Theme.EDITOR_BG);
        area.setForeground(Theme.TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    private JPanel wrapCodeColumn(String headerTitle, Color accent, JTextArea area) {
        Theme.RoundedPanel col = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, accent);
        col.setLayout(new BorderLayout(0, 8));
        col.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        JLabel lblH = new JLabel(headerTitle);
        lblH.setFont(Theme.FONT_SECTION);
        lblH.setForeground(accent);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        scroll.getViewport().setBackground(Theme.EDITOR_BG);

        col.add(lblH, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        return col;
    }

    private void loadComparatorTopic(int topicIndex) {
        this.currentComparatorTopic = topicIndex;
        int seq = topicIndex + 1;
        String jTitle = findLessonTitleBySequence(javaLessons, seq, "Herencia y Despacho Dinámico");
        String pTitle = findLessonTitleBySequence(pythonLessons, seq, "Protocolos Abstractos (abc.ABC)");
        String cTitle = findLessonTitleBySequence(cppLessons, seq, "Funciones Virtuales Puras (= 0)");
        lblCompMirrorSource.setText("Lecciones espejo (orden_secuencia=" + seq + "): "
                + jTitle + "  |  " + pTitle + "  |  " + cTitle);

        if (topicIndex == 0) {
            txtCompJava.setText(
                    "abstract class Figura {\n"
                    + "    abstract double area();\n"
                    + "}\n\n"
                    + "class Circulo extends Figura {\n"
                    + "    @Override\n"
                    + "    double area() {\n"
                    + "        return Math.PI * r * r;\n"
                    + "    }\n"
                    + "}"
            );
            txtCompPython.setText(
                    "from abc import ABC, abstractmethod\n\n"
                    + "class Figura(ABC):\n"
                    + "    @abstractmethod\n"
                    + "    def area(self) -> float: ...\n\n"
                    + "class Circulo(Figura):\n"
                    + "    def area(self) -> float:\n"
                    + "        return 3.14159 * (self.r ** 2)"
            );
            txtCompCpp.setText(
                    "class Figura {\n"
                    + "public:\n"
                    + "    virtual ~Figura() = default;\n"
                    + "    virtual double area() const = 0;\n"
                    + "};\n\n"
                    + "class Circulo : public Figura {\n"
                    + "public:\n"
                    + "    double area() const override {\n"
                    + "        return 3.14159 * r * r;\n"
                    + "    }\n"
                    + "};"
            );
            lblCompTakeaway.setText("Análisis Comparativo: Los tres lenguajes expresan la relación ES-UN mediante contratos abstractos; C++ requiere destructor virtual para evitar fugas de memoria.");
        } else if (topicIndex == 1) {
            txtCompJava.setText(
                    "static int search(int[] a, int target) {\n"
                    + "    int low = 0, high = a.length - 1;\n"
                    + "    while (low <= high) {\n"
                    + "        int mid = low + (high - low) / 2;\n"
                    + "        if (a[mid] == target) return mid;\n"
                    + "        if (a[mid] < target) low = mid + 1;\n"
                    + "        else high = mid - 1;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}"
            );
            txtCompPython.setText(
                    "def search(a: list[int], target: int) -> int:\n"
                    + "    low, high = 0, len(a) - 1\n"
                    + "    while low <= high:\n"
                    + "        mid = (low + high) // 2\n"
                    + "        if a[mid] == target: return mid\n"
                    + "        elif a[mid] < target: low = mid + 1\n"
                    + "        else: high = mid - 1\n"
                    + "    return -1"
            );
            txtCompCpp.setText(
                    "int search(const std::vector<int>& a, int x) {\n"
                    + "    int low = 0, high = (int)a.size() - 1;\n"
                    + "    while (low <= high) {\n"
                    + "        int mid = low + (high - low) / 2;\n"
                    + "        if (a[mid] == x) return mid;\n"
                    + "        if (a[mid] < x) low = mid + 1;\n"
                    + "        else high = mid - 1;\n"
                    + "    }\n"
                    + "    return -1;\n"
                    + "}"
            );
            lblCompTakeaway.setText("Análisis Comparativo: Java y C++ calculan mid = low + (high - low) / 2 para prevenir overflow de 32 bits; Python usa división entera // con enteros de precisión arbitraria.");
        } else {
            txtCompJava.setText(
                    "import java.util.ArrayList;\n"
                    + "import java.util.List;\n\n"
                    + "List<Integer> nums = new ArrayList<>();\n"
                    + "nums.add(10);\n"
                    + "nums.add(20);\n\n"
                    + "for (int n : nums) {\n"
                    + "    System.out.println(n);\n"
                    + "}"
            );
            txtCompPython.setText(
                    "# Lista dinámica nativa\n"
                    + "nums: list[int] = [10, 20]\n"
                    + "nums.append(30)\n\n"
                    + "for n in nums:\n"
                    + "    print(n)"
            );
            txtCompCpp.setText(
                    "#include <vector>\n"
                    + "#include <iostream>\n\n"
                    + "std::vector<int> nums = {10, 20};\n"
                    + "nums.push_back(30);\n\n"
                    + "for (int n : nums) {\n"
                    + "    std::cout << n << \"\\n\";\n"
                    + "}"
            );
            lblCompTakeaway.setText("Análisis Comparativo: ArrayList (Java), list (Python) y std::vector (C++) redimensionan en O(1) amortizado; std::vector libera su bloque de forma determinista con RAII.");
        }
        highlightEquivalentBlock(0);
    }

    private String findLessonTitleBySequence(List<Lesson> lessons, int seq, String fallback) {
        for (Lesson l : lessons) {
            if (l.getOrderIndex() == seq) {
                return l.getTitle();
            }
        }
        return fallback;
    }

    private void highlightEquivalentBlock(int blockIndex) {
        Color hlJava = new Color(255, 237, 213);
        Color hlPy = new Color(219, 234, 254);
        Color hlCpp = new Color(224, 242, 254);

        if (blockIndex == 0) {
            highlightLines(txtCompJava, 0, 2, hlJava);
            highlightLines(txtCompPython, 0, 3, hlPy);
            highlightLines(txtCompCpp, 0, 3, hlCpp);
        } else if (blockIndex == 1) {
            highlightLines(txtCompJava, 3, 6, hlJava);
            highlightLines(txtCompPython, 3, 6, hlPy);
            highlightLines(txtCompCpp, 3, 6, hlCpp);
        } else {
            highlightLines(txtCompJava, 6, 9, hlJava);
            highlightLines(txtCompPython, 6, 8, hlPy);
            highlightLines(txtCompCpp, 6, 10, hlCpp);
        }
    }

    private void highlightLines(JTextArea area, int startLine, int endLine, Color color) {
        Highlighter hl = area.getHighlighter();
        hl.removeAllHighlights();
        String text = area.getText();
        String[] lines = text.split("\n");
        int charStart = 0;
        int from = Math.max(0, Math.min(startLine, lines.length - 1));
        int to = Math.max(from, Math.min(endLine, lines.length - 1));
        for (int i = 0; i < from; i++) {
            charStart += lines[i].length() + 1;
        }
        int charEnd = charStart;
        for (int i = from; i <= to; i++) {
            charEnd += lines[i].length() + 1;
        }
        try {
            hl.addHighlight(
                    Math.min(charStart, text.length()),
                    Math.min(charEnd, text.length()),
                    new DefaultHighlighter.DefaultHighlightPainter(color)
            );
        } catch (Exception ignored) {
        }
    }

    private JPanel buildAchievementsWorkspaceView() {
        JPanel view = new JPanel(new BorderLayout(0, 14));
        view.setOpaque(false);

        view.add(Theme.createSectionHeader(
                "Sistema de Logros e Insignias Desbloqueables (Logros · Usuario_Logro)",
                "Se desbloquean automáticamente según tu racha, XP y retos aprobados"), BorderLayout.NORTH);

        achievementsGridContainer = new JPanel(new GridLayout(2, 3, 14, 14));
        achievementsGridContainer.setOpaque(false);

        view.add(achievementsGridContainer, BorderLayout.CENTER);
        refreshAchievementsGrid();
        return view;
    }

    private void refreshAchievementsGrid() {
        if (achievementsGridContainer == null) {
            return;
        }
        achievementsGridContainer.removeAll();

        UserStats stats = new UserStats(
                currentUser.getCurrentStreak(),
                Math.max(currentUser.getMaxStreak(), currentUser.getCurrentStreak()),
                currentUser.getTotalExp(),
                solvedChallengesCount
        );
        if (oracleConnected && currentUser.getId() > 0) {
            try {
                Optional<UserStats> dbStats = AppContext.getInstance()
                        .getOracleAchievementRepository().findStatsByUserId(currentUser.getId());
                if (dbStats.isPresent()) {
                    stats = dbStats.get();
                }
            } catch (Exception ignored) {
            }
        }

        for (Achievement ach : achievementsCatalog) {
            boolean unlocked = ach.isUnlockedBy(stats);
            Color accent = unlocked ? Theme.ACCENT_GREEN : Theme.TEXT_MUTED;

            Theme.RoundedPanel card = new Theme.RoundedPanel(
                    12,
                    unlocked ? Theme.SURFACE : Theme.SURFACE_ELEVATED,
                    unlocked ? Theme.ACCENT_GREEN : Theme.BORDER_SUBTLE,
                    accent
            );
            card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 16));

            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.setOpaque(false);

            JLabel lblName = new JLabel("  " + ach.getName());
            lblName.setFont(Theme.FONT_SUBTITLE);
            lblName.setForeground(unlocked ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            lblName.setIcon(Theme.createVectorIcon(
                    unlocked ? Theme.IconKind.TROPHY : Theme.IconKind.SHIELD, 18, accent));

            top.add(lblName, BorderLayout.CENTER);
            top.add(Theme.createPillBadge(unlocked ? "DESBLOQUEADO" : "BLOQUEADO", accent), BorderLayout.EAST);

            JLabel lblDesc = new JLabel("<html><body style='width:200px;color:#334155;'>"
                    + ach.getDescription() + "</body></html>");
            lblDesc.setFont(Theme.FONT_REGULAR);

            JLabel lblTarget = new JLabel(
                    "Criterio: " + ach.getAchievementType() + " · Meta: " + ach.getTargetValue());
            lblTarget.setFont(Theme.FONT_SMALL);
            lblTarget.setForeground(unlocked ? Theme.ACCENT_GREEN : Theme.TEXT_MUTED);

            card.add(top, BorderLayout.NORTH);
            card.add(lblDesc, BorderLayout.CENTER);
            card.add(lblTarget, BorderLayout.SOUTH);

            achievementsGridContainer.add(card);
        }

        achievementsGridContainer.revalidate();
        achievementsGridContainer.repaint();
    }

    private JPanel buildAdminCrudWorkspaceView() {
        JPanel view = new JPanel(new BorderLayout(0, 14));
        view.setOpaque(false);

        view.add(Theme.createSectionHeader(
                "Panel de Administración CRUD · Creación de Retos y Casos_Prueba en Oracle",
                "Persiste en Retos, Reto_Codigo y Casos_Prueba y evalúa al instante con SimulatedCodeRunner"), BorderLayout.NORTH);

        Theme.RoundedPanel formCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_GREEN);
        formCard.setLayout(new BorderLayout(16, 14));
        formCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel fieldsGrid = new JPanel(new GridLayout(3, 2, 16, 12));
        fieldsGrid.setOpaque(false);

        cmbAdminLesson = new JComboBox<>();
        cmbAdminLesson.setFont(Theme.FONT_REGULAR);
        for (Lesson l : allLessonsForAdmin) {
            String langTag = (l.getModule() != null && l.getModule().getLanguage() != null)
                    ? l.getModule().getLanguage().getName()
                    : "Curso";
            cmbAdminLesson.addItem("[" + langTag + "] Lección #" + l.getId() + " - " + l.getTitle());
        }

        txtAdminTitle = Theme.createStyledTextField("Título del nuevo reto");
        txtAdminTitle.setText("Clasificador de Nivel de Alerta");

        txtAdminStatement = Theme.createStyledTextField("Enunciado del reto");
        txtAdminStatement.setText("Retorna \"CRITICO\" cuando reciba \"Rojo\" y \"ESTABLE\" cuando reciba \"Verde\".");

        txtAdminReward = Theme.createStyledTextField("80");
        txtAdminReward.setText("80");

        txtAdminCase1In = Theme.createStyledTextField("Entrada Caso #1");
        txtAdminCase1In.setText("Rojo");
        txtAdminCase1Out = Theme.createStyledTextField("Salida Esperada #1");
        txtAdminCase1Out.setText("CRITICO");

        txtAdminCase2In = Theme.createStyledTextField("Entrada Caso #2");
        txtAdminCase2In.setText("Verde");
        txtAdminCase2Out = Theme.createStyledTextField("Salida Esperada #2");
        txtAdminCase2Out.setText("ESTABLE");

        fieldsGrid.add(wrapLabeledField("LECCIÓN ASOCIADA (TABLA LECCIONES)", cmbAdminLesson));
        fieldsGrid.add(wrapLabeledField("TÍTULO DEL RETO (TITULO_RETO)", txtAdminTitle));
        fieldsGrid.add(wrapLabeledField("ENUNCIADO DEL PROBLEMA (ENUNCIADO)", txtAdminStatement));
        fieldsGrid.add(wrapLabeledField("RECOMPENSA DE EXPERIENCIA (EXP_RECOMPENSA)", txtAdminReward));

        JPanel case1Row = new JPanel(new GridLayout(1, 2, 8, 0));
        case1Row.setOpaque(false);
        case1Row.add(txtAdminCase1In);
        case1Row.add(txtAdminCase1Out);
        fieldsGrid.add(wrapLabeledField("CASO DE PRUEBA #1: ENTRADA → SALIDA_ESPERADA", case1Row));

        JPanel case2Row = new JPanel(new GridLayout(1, 2, 8, 0));
        case2Row.setOpaque(false);
        case2Row.add(txtAdminCase2In);
        case2Row.add(txtAdminCase2Out);
        fieldsGrid.add(wrapLabeledField("CASO DE PRUEBA #2: ENTRADA → SALIDA_ESPERADA", case2Row));

        txtAdminTemplate = new JTextArea(
                "public class AlertSolver {\n"
                + "    public static String clasificar(String color) {\n"
                + "        if (\"Rojo\".equalsIgnoreCase(color)) {\n"
                + "            return \"CRITICO\";\n"
                + "        }\n"
                + "        // Completa el caso para Verde\n"
                + "        return \"\";\n"
                + "    }\n"
                + "}"
        );
        txtAdminTemplate.setFont(Theme.FONT_CODE);
        txtAdminTemplate.setBackground(Theme.EDITOR_BG);
        txtAdminTemplate.setForeground(Theme.TEXT_PRIMARY);
        txtAdminTemplate.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scrollTemplate = new JScrollPane(txtAdminTemplate);
        scrollTemplate.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));

        JPanel centerBox = new JPanel(new BorderLayout(0, 6));
        centerBox.setOpaque(false);
        JLabel lblTpl = new JLabel("PLANTILLA DE CÓDIGO INICIAL (RETO_CODIGO.CODIGO_INICIAL)");
        lblTpl.setFont(Theme.FONT_SECTION);
        lblTpl.setForeground(Theme.TEXT_SECONDARY);
        centerBox.add(lblTpl, BorderLayout.NORTH);
        centerBox.add(scrollTemplate, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(12, 0));
        bottomBar.setOpaque(false);
        lblAdminStatus = new JLabel("Completa los datos para insertar el reto y sus casos de prueba en Oracle.");
        lblAdminStatus.setFont(Theme.FONT_REGULAR);
        lblAdminStatus.setForeground(Theme.TEXT_SECONDARY);

        JButton btnCreateAndTest = Theme.createPrimaryButton(
                "Guardar en Oracle y Probar en el Evaluador Dinámico", Theme.ACCENT_GREEN);
        btnCreateAndTest.setIcon(Theme.createVectorIcon(Theme.IconKind.PLAY, 14, Color.WHITE));
        btnCreateAndTest.addActionListener(e -> handleCreateAdminChallenge());

        bottomBar.add(lblAdminStatus, BorderLayout.CENTER);
        bottomBar.add(btnCreateAndTest, BorderLayout.EAST);

        formCard.add(fieldsGrid, BorderLayout.NORTH);
        formCard.add(centerBox, BorderLayout.CENTER);
        formCard.add(bottomBar, BorderLayout.SOUTH);

        view.add(formCard, BorderLayout.CENTER);
        return view;
    }

    private JPanel wrapLabeledField(String caption, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(caption);
        lbl.setFont(Theme.FONT_SECTION);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        return p;
    }

    private void handleCreateAdminChallenge() {
        int lessonIdx = cmbAdminLesson.getSelectedIndex();
        Lesson targetLesson = (lessonIdx >= 0 && lessonIdx < allLessonsForAdmin.size())
                ? allLessonsForAdmin.get(lessonIdx)
                : (!javaLessons.isEmpty() ? javaLessons.get(0) : null);

        String title = txtAdminTitle.getText() != null ? txtAdminTitle.getText().trim() : "";
        String statement = txtAdminStatement.getText() != null ? txtAdminStatement.getText().trim() : "";
        String template = txtAdminTemplate.getText() != null ? txtAdminTemplate.getText() : "";
        int reward = 80;
        try {
            reward = Integer.parseInt(txtAdminReward.getText().trim());
        } catch (Exception ignored) {
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

        if (oracleConnected && targetLesson != null && targetLesson.getId() > 0) {
            try {
                User adminActor = new User();
                adminActor.setId(currentUser.getId() > 0 ? currentUser.getId() : 2);
                adminActor.setUsername("Admin");
                adminActor.setRole(new Role(2, "Administrador", "Gestión CRUD"));

                Challenge saved = AppContext.getInstance().getContentService().createChallenge(adminActor, newCh);
                targetLesson.addChallenge(saved);
                lblAdminStatus.setForeground(Theme.ACCENT_GREEN);
                lblAdminStatus.setText("¡Reto #" + saved.getId() + " guardado en Retos, Reto_Codigo y Casos_Prueba en Oracle!");
                populateLessonsForLanguage(activeLanguageKey);
                new ChallengeFrame(currentUser, saved, this::onChallengeOrLessonCompleted).setVisible(true);
                return;
            } catch (Exception ex) {
                lblAdminStatus.setForeground(Theme.ACCENT_ROSE);
                lblAdminStatus.setText("Aviso: " + ex.getMessage());
            }
        }

        if (targetLesson != null) {
            targetLesson.addChallenge(newCh);
        }
        lblAdminStatus.setForeground(Theme.ACCENT_GREEN);
        lblAdminStatus.setText("Reto creado y abierto en el evaluador dinámico.");
        new ChallengeFrame(currentUser, newCh, this::onChallengeOrLessonCompleted).setVisible(true);
    }

    private void switchWorkspaceView(String cardKey) {
        workspaceCardLayout.show(workspaceContainer, cardKey);

        navCoursesBtn.setPalette(
                "COURSES".equals(cardKey) ? Theme.ACCENT_INDIGO : Theme.SURFACE,
                "COURSES".equals(cardKey) ? Theme.ACCENT_INDIGO_HOVER : Theme.SURFACE_ELEVATED,
                "COURSES".equals(cardKey) ? Color.WHITE : Theme.TEXT_SECONDARY,
                "COURSES".equals(cardKey) ? null : Theme.BORDER_SUBTLE
        );
        navComparatorBtn.setPalette(
                "COMPARATOR".equals(cardKey) ? Theme.ACCENT_INDIGO : Theme.SURFACE,
                "COMPARATOR".equals(cardKey) ? Theme.ACCENT_INDIGO_HOVER : Theme.SURFACE_ELEVATED,
                "COMPARATOR".equals(cardKey) ? Color.WHITE : Theme.TEXT_SECONDARY,
                "COMPARATOR".equals(cardKey) ? null : Theme.BORDER_SUBTLE
        );
        navAchievementsBtn.setPalette(
                "ACHIEVEMENTS".equals(cardKey) ? Theme.ACCENT_INDIGO : Theme.SURFACE,
                "ACHIEVEMENTS".equals(cardKey) ? Theme.ACCENT_INDIGO_HOVER : Theme.SURFACE_ELEVATED,
                "ACHIEVEMENTS".equals(cardKey) ? Color.WHITE : Theme.TEXT_SECONDARY,
                "ACHIEVEMENTS".equals(cardKey) ? null : Theme.BORDER_SUBTLE
        );
        navAdminBtn.setPalette(
                "ADMIN".equals(cardKey) ? Theme.ACCENT_INDIGO : Theme.SURFACE,
                "ADMIN".equals(cardKey) ? Theme.ACCENT_INDIGO_HOVER : Theme.SURFACE_ELEVATED,
                "ADMIN".equals(cardKey) ? Color.WHITE : Theme.TEXT_SECONDARY,
                "ADMIN".equals(cardKey) ? null : Theme.BORDER_SUBTLE
        );
    }

    private void onChallengeOrLessonCompleted() {
        solvedChallengesCount++;
        loadActivityHistoryFromOracle();
        rebuildHeatmapCells();
        refreshGamificationTelemetry();
        populateLessonsForLanguage(activeLanguageKey);
        refreshAchievementsGrid();
    }

    private void refreshGamificationTelemetry() {
        int exp = currentUser.getTotalExp();
        int computedLevel = Math.max(currentUser.getLevel(), 1 + exp / 300);
        currentUser.setLevel(computedLevel);

        lblExpValue.setText("  " + exp + " XP");
        lblLevelValue.setText("Nivel " + computedLevel);
        lblStreakValue.setText("  " + Math.max(1, currentUser.getCurrentStreak()) + " Días");
        lblLivesValue.setText("  " + availableLives + " / 5");

        int xpInCurrentTier = exp % 300;
        levelProgressBar.setProgress(xpInCurrentTier == 0 ? 150 : xpInCurrentTier, 300);
    }

    private static String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "LU";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    /**
     * Nodo gráfico del árbol de habilidades (Skill-Tree) con conector vertical.
     */
    private static final class SkillTreeNodeGraphic extends JComponent {

        private static final long serialVersionUID = 1L;
        private final int stepNumber;
        private final ProgressStatus status;
        private final Color accent;
        private final boolean drawBottomConnector;

        private SkillTreeNodeGraphic(int stepNumber, ProgressStatus status, Color accent, boolean drawBottomConnector) {
            this.stepNumber = stepNumber;
            this.status = status;
            this.accent = accent;
            this.drawBottomConnector = drawBottomConnector;
            setPreferredSize(new Dimension(42, 76));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.enableAntialiasing(g2);

            int cx = getWidth() / 2;
            int cy = 28;
            int r = 15;

            if (drawBottomConnector) {
                g2.setColor(Theme.withAlpha(accent, 95));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(cx, cy + r, cx, getHeight());
            }

            g2.setColor(Theme.withAlpha(accent, 32));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            String symbol = status == ProgressStatus.COMPLETED
                    ? "✓"
                    : (status == ProgressStatus.IN_PROGRESS ? "▶" : String.valueOf(stepNumber));
            g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx - fm.stringWidth(symbol) / 2;
            int ty = cy - fm.getHeight() / 2 + fm.getAscent();
            g2.drawString(symbol, tx, ty);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardFrame(createFallbackUser()).setVisible(true));
    }
}
