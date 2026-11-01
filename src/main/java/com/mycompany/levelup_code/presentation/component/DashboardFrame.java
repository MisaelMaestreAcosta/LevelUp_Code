package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Achievement;
import com.mycompany.levelup_code.domain.model.ChallengeCountAchievement;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.ExperienceAchievement;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.StreakAchievement;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.domain.model.UserStats;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Panel principal interactivo del estudiante y administrador con barra lateral de navegación,
 * telemetría de gamificación en tiempo real, comparador multi-lenguaje y sistema de logros.
 */
public class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final User currentUser;
    private final List<Language> catalogLanguages = new ArrayList<>();
    private final List<Lesson> javaLessons = new ArrayList<>();
    private final List<Lesson> pythonLessons = new ArrayList<>();
    private final List<Lesson> cppLessons = new ArrayList<>();
    private final List<Achievement> achievementsCatalog = new ArrayList<>();

    private int solvedChallengesCount = 4;
    private int availableLives = 5;

    // Navegación principal
    private CardLayout workspaceCardLayout;
    private JPanel workspaceContainer;
    private Theme.ModernButton navCoursesBtn;
    private Theme.ModernButton navComparatorBtn;
    private Theme.ModernButton navAchievementsBtn;

    // Etiquetas de telemetría actualizables en tiempo real
    private JLabel lblExpValue;
    private JLabel lblLevelValue;
    private JLabel lblStreakValue;
    private JLabel lblLivesValue;
    private Theme.ModernProgressBar levelProgressBar;

    // Contenedores dinámicos
    private JPanel lessonsListContainer;
    private JLabel lblSelectedLanguageHeader;
    private JPanel achievementsGridContainer;

    // Comparador de 3 columnas
    private JTextArea txtCompJava;
    private JTextArea txtCompPython;
    private JTextArea txtCompCpp;
    private JLabel lblCompTakeaway;

    public DashboardFrame(User user) {
        this.currentUser = user != null ? user : createFallbackUser();
        seedDomainCurriculum();
        seedAchievementsCatalog();
        initComponents();
        refreshGamificationTelemetry();
        populateLessonsForLanguage("Java");
        loadComparatorTopic(0);
    }

    private static User createFallbackUser() {
        User u = new User();
        u.setId(1);
        u.setUsername("Misael Maestre");
        u.setEmail("misael@unicesar.edu.co");
        u.setTotalExp(1450);
        u.setLevel(5);
        u.setCurrentStreak(7);
        u.setMaxStreak(12);
        return u;
    }

    private void seedDomainCurriculum() {
        Language javaLang = new Language(1, "Java", "POO, Principios SOLID y Colecciones Tipadas", "java-icon", true);
        Language pyLang = new Language(2, "Python", "Estructuras Dinámicas, Comprensiones y Algoritmos", "python-icon", true);
        Language cppLang = new Language(3, "C++", "Gestión de Memoria, Punteros y STL de Alto Rendimiento", "cpp-icon", true);
        catalogLanguages.addAll(Arrays.asList(javaLang, pyLang, cppLang));

        CourseModule javaMod = new CourseModule(1, "Arquitectura Orientada a Objetos", "Herencia y Polimorfismo", 1, javaLang);
        Lesson jLesson1 = new Lesson(1, "Herencia, Clases Abstractas y Despacho Dinámico",
                "1. JERARQUÍAS ES-UN Y CONTRATOS\n"
                + "Las clases abstractas permiten factorizar estado y comportamiento común mientras delegan operaciones específicas a las subclases mediante métodos abstractos.\n\n"
                + "2. POLIMORFISMO EN JAVA VS C++ VS PYTHON\n"
                + "• En Java, todo método no final ni static se enlaza dinámicamente en tiempo de ejecución.\n"
                + "• En C++, el despacho dinámico exige declarar el método como 'virtual' en la clase base.\n"
                + "• En Python, el protocolo se resuelve en tiempo de ejecución mediante Duck Typing o 'abc.ABC'.",
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
                + "        // Agrega el caso para Gato aquí\n"
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

        Lesson jLesson2 = new Lesson(2, "Principios SOLID y Desacoplamiento por Capas",
                "1. INVERSIÓN DE DEPENDENCIAS (DIP)\n"
                + "Los módulos de alto nivel (Servicios de Negocio) no deben depender de implementaciones concretas de datos (Oracle JDBC), sino de abstracciones (Interfaces Repository/DAO).\n\n"
                + "2. RESPONSABILIDAD ÚNICA (SRP)\n"
                + "Cada paquete cumple un único rol: 'domain' modela entidades puras, 'business' coordina reglas de evaluación y 'presentation' gestiona la interacción con el usuario.",
                2, 75, javaMod);
        jLesson2.addChallenge(jQuiz);

        javaLessons.add(jLesson1);
        javaLessons.add(jLesson2);

        CourseModule pyMod = new CourseModule(2, "Estructuras Algorítmicas en Python", "Listas, Diccionarios y Complejidad", 1, pyLang);
        Lesson pyLesson1 = new Lesson(3, "Complejidad Temporal y Búsqueda Binaria O(log n)",
                "En Python, las listas son arreglos dinámicos de referencias en memoria contigua. "
                + "El acceso por índice list[i] toma tiempo constante O(1), lo que permite aplicar búsqueda binaria sobre secuencias ordenadas en O(log n).",
                1, 65, pyMod);
        pyLesson1.addChallenge(jCode);
        pythonLessons.add(pyLesson1);

        CourseModule cppMod = new CourseModule(3, "Memoria, Referencias y Contenedores STL", "RAII y std::vector", 1, cppLang);
        Lesson cppLesson1 = new Lesson(4, "Paso por Valor vs Referencia Constante (const T&)",
                "En C++, pasar un std::vector por valor copia todos sus elementos en memoria O(n). "
                + "Usar 'const std::vector<int>&' evita la copia y protege la colección contra modificaciones accidentales.",
                1, 80, cppMod);
        cppLesson1.addChallenge(jCode);
        cppLessons.add(cppLesson1);
    }

    private void seedAchievementsCatalog() {
        achievementsCatalog.add(new StreakAchievement(1, "Constancia de Acero", "Mantén una racha activa de 3 días seguidos practicando.", "FLAME", 3));
        achievementsCatalog.add(new StreakAchievement(2, "Hábito Algorítmico", "Alcanza una racha continua de 7 días de estudio.", "FLAME", 7));
        achievementsCatalog.add(new ExperienceAchievement(3, "Explorador Políglota", "Acumula tus primeros 1,000 puntos de experiencia (XP).", "BOLT", 1000));
        achievementsCatalog.add(new ExperienceAchievement(4, "Arquitecto Senior", "Supera la barrera de los 1,500 XP resolviendo lecciones y retos.", "TROPHY", 1500));
        achievementsCatalog.add(new ChallengeCountAchievement(5, "Domador de Casos de Prueba", "Resuelve exitosamente 5 retos de código o quiz.", "CODE", 5));
        achievementsCatalog.add(new StreakAchievement(6, "Leyenda del Semestre", "Alcanza una racha histórica de 14 días consecutivos.", "SHIELD", 14));
    }

    private void initComponents() {
        setTitle("LevelUp Code — Espacio de Trabajo (" + currentUser.getUsername() + ")");
        setSize(1200, 740);
        setMinimumSize(new Dimension(1040, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);

        root.add(buildLeftSidebar(), BorderLayout.WEST);

        JPanel centerShell = new JPanel(new BorderLayout(0, 18));
        centerShell.setBackground(Theme.BG_DARK);
        centerShell.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        centerShell.add(buildTopTelemetryBar(), BorderLayout.NORTH);

        workspaceCardLayout = new CardLayout();
        workspaceContainer = new JPanel(workspaceCardLayout);
        workspaceContainer.setOpaque(false);

        workspaceContainer.add(buildCoursesWorkspaceView(), "COURSES");
        workspaceContainer.add(buildComparatorWorkspaceView(), "COMPARATOR");
        workspaceContainer.add(buildAchievementsWorkspaceView(), "ACHIEVEMENTS");

        centerShell.add(workspaceContainer, BorderLayout.CENTER);
        root.add(centerShell, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(22, 16, 20, 16)
        ));

        // Marca y navegación
        JPanel navColumn = new JPanel();
        navColumn.setLayout(new BoxLayout(navColumn, BoxLayout.Y_AXIS));
        navColumn.setOpaque(false);

        JLabel lblBrand = new JLabel("  LevelUp Code");
        lblBrand.setIcon(Theme.createVectorIcon(Theme.IconKind.TERMINAL, 22, Theme.ACCENT_INDIGO));
        lblBrand.setFont(Theme.FONT_TITLE);
        lblBrand.setForeground(Theme.TEXT_PRIMARY);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("  Aprendizaje Comparativo");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNavSection = new JLabel("  NAVEGACIÓN PRINCIPAL");
        lblNavSection.setFont(Theme.FONT_SECTION);
        lblNavSection.setForeground(Theme.TEXT_MUTED);
        lblNavSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        navCoursesBtn = new Theme.ModernButton(
                "Ruta y Lenguajes", Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
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
        btnQuickLab.addActionListener(e -> {
            Lesson l = javaLessons.get(0);
            new ChallengeFrame(currentUser, l.getChallenges().get(0), this::onChallengeOrLessonCompleted).setVisible(true);
        });

        navColumn.add(lblBrand);
        navColumn.add(Box.createVerticalStrut(4));
        navColumn.add(lblSub);
        navColumn.add(Box.createVerticalStrut(26));
        navColumn.add(lblNavSection);
        navColumn.add(Box.createVerticalStrut(10));
        navColumn.add(navCoursesBtn);
        navColumn.add(Box.createVerticalStrut(8));
        navColumn.add(navComparatorBtn);
        navColumn.add(Box.createVerticalStrut(8));
        navColumn.add(navAchievementsBtn);
        navColumn.add(Box.createVerticalStrut(18));
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

        // 3. Tarjeta de Racha Activa
        Theme.RoundedPanel cardStreak = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE, Theme.ACCENT_AMBER);
        cardStreak.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 14));
        JLabel lblStreakCaption = new JLabel("RACHA DIARIA");
        lblStreakCaption.setFont(Theme.FONT_SECTION);
        lblStreakCaption.setForeground(Theme.TEXT_MUTED);
        lblStreakValue = new JLabel();
        lblStreakValue.setFont(Theme.FONT_METRIC);
        lblStreakValue.setForeground(Theme.TEXT_PRIMARY);
        lblStreakValue.setIcon(Theme.createVectorIcon(Theme.IconKind.FLAME, 20, Theme.ACCENT_AMBER));
        JLabel lblStreakSub = new JLabel("L · M · M · J · V · S · D  (Activa)");
        lblStreakSub.setFont(Theme.FONT_SMALL);
        lblStreakSub.setForeground(Theme.ACCENT_AMBER);
        cardStreak.add(lblStreakCaption, BorderLayout.NORTH);
        cardStreak.add(lblStreakValue, BorderLayout.CENTER);
        cardStreak.add(lblStreakSub, BorderLayout.SOUTH);

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

    private JPanel buildCoursesWorkspaceView() {
        JPanel view = new JPanel(new BorderLayout(0, 18));
        view.setOpaque(false);

        // Parte superior: Tarjetas de los 3 lenguajes
        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);
        topSection.add(Theme.createSectionHeader(
                "Lenguajes de Estudio Inscritos",
                "Selecciona un lenguaje para filtrar sus lecciones o abre un reto práctico"), BorderLayout.NORTH);

        JPanel langGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        langGrid.setOpaque(false);

        langGrid.add(createInteractiveLanguageCard(
                "Java", "Java 21 LTS", "POO, Jerarquías ES-UN y Principios SOLID",
                75, Theme.ACCENT_INDIGO, javaLessons));
        langGrid.add(createInteractiveLanguageCard(
                "Python", "Python 3.12", "Estructuras de Datos y Búsqueda Algorítmica",
                45, Theme.ACCENT_GREEN, pythonLessons));
        langGrid.add(createInteractiveLanguageCard(
                "C++", "ISO C++ 20", "Punteros, Referencias Constantes y Memoria",
                25, Theme.ACCENT_VIOLET, cppLessons));

        topSection.add(langGrid, BorderLayout.CENTER);
        view.add(topSection, BorderLayout.NORTH);

        // Parte inferior: Módulos y Lecciones interactivas del lenguaje activo
        Theme.RoundedPanel lessonsCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        lessonsCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel lessonsHeader = new JPanel(new BorderLayout(12, 0));
        lessonsHeader.setOpaque(false);
        lessonsHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)
        ));

        lblSelectedLanguageHeader = new JLabel("RUTA DE LECCIONES SECUENCIALES · JAVA");
        lblSelectedLanguageHeader.setFont(Theme.FONT_SECTION);
        lblSelectedLanguageHeader.setForeground(Theme.TEXT_MUTED);
        lessonsHeader.add(lblSelectedLanguageHeader, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("Completa lecciones y retos para sumar XP en tiempo real");
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

        Theme.RoundedPanel card = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel headerRow = new JPanel(new BorderLayout(8, 0));
        headerRow.setOpaque(false);

        JLabel lblName = new JLabel(langKey);
        lblName.setFont(Theme.FONT_TITLE);
        lblName.setForeground(Theme.TEXT_PRIMARY);
        headerRow.add(lblName, BorderLayout.CENTER);
        headerRow.add(Theme.createPillBadge(versionTag, accent), BorderLayout.EAST);

        JLabel lblDesc = new JLabel("<html><body style='width:210px;color:#334155;'>" + description + "</body></html>");
        lblDesc.setFont(Theme.FONT_REGULAR);

        JPanel progressRow = new JPanel(new BorderLayout(0, 6));
        progressRow.setOpaque(false);
        JLabel lblPct = new JLabel("Avance del módulo: " + progressPct + "%");
        lblPct.setFont(Theme.FONT_SMALL);
        lblPct.setForeground(Theme.TEXT_MUTED);
        Theme.ModernProgressBar bar = Theme.createProgressBar(progressPct, 100, accent);
        progressRow.add(lblPct, BorderLayout.NORTH);
        progressRow.add(bar, BorderLayout.SOUTH);

        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 8, 0));
        actionsRow.setOpaque(false);

        JButton btnFilter = Theme.createSecondaryButton("Ver Módulos");
        btnFilter.setFont(Theme.FONT_SMALL);
        btnFilter.addActionListener(e -> populateLessonsForLanguage(langKey));

        JButton btnStudy = Theme.createPrimaryButton("Continuar", accent);
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
        body.add(Box.createVerticalStrut(12));
        body.add(progressRow);
        body.add(Box.createVerticalStrut(14));
        body.add(actionsRow);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void populateLessonsForLanguage(String langKey) {
        lblSelectedLanguageHeader.setText("RUTA DE LECCIONES SECUENCIALES · " + langKey.toUpperCase());
        lessonsListContainer.removeAll();

        List<Lesson> targetLessons = "Python".equalsIgnoreCase(langKey)
                ? pythonLessons
                : ("C++".equalsIgnoreCase(langKey) ? cppLessons : javaLessons);

        for (Lesson lesson : targetLessons) {
            Theme.RoundedPanel row = new Theme.RoundedPanel(10, Theme.EDITOR_BG, Theme.BORDER_SUBTLE);
            row.setLayout(new BorderLayout(16, 0));
            row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

            JPanel leftInfo = new JPanel(new BorderLayout(0, 4));
            leftInfo.setOpaque(false);

            JLabel lblLessonTitle = new JLabel(
                    "Lección " + lesson.getOrderIndex() + " — " + lesson.getTitle());
            lblLessonTitle.setFont(Theme.FONT_SUBTITLE);
            lblLessonTitle.setForeground(Theme.TEXT_PRIMARY);
            lblLessonTitle.setIcon(Theme.createVectorIcon(Theme.IconKind.BOOK, 16, Theme.ACCENT_INDIGO));

            int chCount = lesson.getChallenges() != null ? lesson.getChallenges().size() : 0;
            JLabel lblMeta = new JLabel(
                    "Recompensa de lectura: +" + lesson.getExpReward() + " XP  ·  "
                    + chCount + " reto(s) práctico(s) disponible(s)");
            lblMeta.setFont(Theme.FONT_SMALL);
            lblMeta.setForeground(Theme.TEXT_MUTED);

            leftInfo.add(lblLessonTitle, BorderLayout.NORTH);
            leftInfo.add(lblMeta, BorderLayout.SOUTH);

            JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightButtons.setOpaque(false);

            JButton btnRead = Theme.createSecondaryButton("Estudiar Teoría");
            btnRead.setFont(Theme.FONT_SMALL);
            btnRead.addActionListener(e ->
                    new LessonFrame(currentUser, lesson, this::onChallengeOrLessonCompleted).setVisible(true));

            JButton btnChallenge = Theme.createPrimaryButton("Resolver Reto", Theme.ACCENT_GREEN);
            btnChallenge.setFont(Theme.FONT_SMALL);
            btnChallenge.addActionListener(e -> {
                if (lesson.getChallenges() != null && !lesson.getChallenges().isEmpty()) {
                    new ChallengeFrame(currentUser, lesson.getChallenges().get(0), this::onChallengeOrLessonCompleted)
                            .setVisible(true);
                } else {
                    new ChallengeFrame().setVisible(true);
                }
            });

            rightButtons.add(btnRead);
            rightButtons.add(btnChallenge);

            row.add(leftInfo, BorderLayout.CENTER);
            row.add(rightButtons, BorderLayout.EAST);

            lessonsListContainer.add(row);
            lessonsListContainer.add(Box.createVerticalStrut(10));
        }

        lessonsListContainer.revalidate();
        lessonsListContainer.repaint();
    }

    private JPanel buildComparatorWorkspaceView() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        Theme.RoundedPanel topControl = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        topControl.setLayout(new BorderLayout(12, 0));
        topControl.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("  Comparador Algorítmico Simultáneo (Java · Python · C++)");
        lblTitle.setFont(Theme.FONT_SUBTITLE);
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        lblTitle.setIcon(Theme.createVectorIcon(Theme.IconKind.CODE, 18, Theme.ACCENT_CYAN));

        JPanel topicButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topicButtons.setOpaque(false);

        JButton btnTopic1 = Theme.createSecondaryButton("1. Colecciones y Listas");
        JButton btnTopic2 = Theme.createSecondaryButton("2. Herencia y Polimorfismo");
        JButton btnTopic3 = Theme.createSecondaryButton("3. Gestión de Memoria");
        btnTopic1.setFont(Theme.FONT_SMALL);
        btnTopic2.setFont(Theme.FONT_SMALL);
        btnTopic3.setFont(Theme.FONT_SMALL);

        btnTopic1.addActionListener(e -> loadComparatorTopic(0));
        btnTopic2.addActionListener(e -> loadComparatorTopic(1));
        btnTopic3.addActionListener(e -> loadComparatorTopic(2));

        topicButtons.add(btnTopic1);
        topicButtons.add(btnTopic2);
        topicButtons.add(btnTopic3);

        topControl.add(lblTitle, BorderLayout.CENTER);
        topControl.add(topicButtons, BorderLayout.EAST);

        // 3 columnas simultáneas
        JPanel columnsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        columnsGrid.setOpaque(false);

        txtCompJava = createReadOnlyCodeArea();
        txtCompPython = createReadOnlyCodeArea();
        txtCompCpp = createReadOnlyCodeArea();

        columnsGrid.add(wrapCodeColumn("JAVA 21 (JVM · Tipado Estático)", Theme.ACCENT_INDIGO, txtCompJava));
        columnsGrid.add(wrapCodeColumn("PYTHON 3.12 (Tipado Dinámico)", Theme.ACCENT_GREEN, txtCompPython));
        columnsGrid.add(wrapCodeColumn("C++ 20 (Compilado Nativo · RAII)", Theme.ACCENT_VIOLET, txtCompCpp));

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
        if (topicIndex == 0) {
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
            lblCompTakeaway.setText("Análisis: ArrayList (Java), list (Python) y std::vector (C++) redimensionan en O(1) amortizado; std::vector almacena valores contiguos sin boxing.");
        } else if (topicIndex == 1) {
            txtCompJava.setText(
                    "abstract class Figura {\n"
                    + "    abstract double area();\n"
                    + "}\n\n"
                    + "class Circulo extends Figura {\n"
                    + "    double r;\n"
                    + "    @Override\n"
                    + "    double area() {\n"
                    + "        return Math.PI * r * r;\n"
                    + "    }\n"
                    + "}"
            );
            txtCompPython.setText(
                    "from abc import ABC, abstractmethod\n"
                    + "import math\n\n"
                    + "class Figura(ABC):\n"
                    + "    @abstractmethod\n"
                    + "    def area(self) -> float: ...\n\n"
                    + "class Circulo(Figura):\n"
                    + "    def area(self) -> float:\n"
                    + "        return math.pi * (self.r ** 2)"
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
            lblCompTakeaway.setText("Análisis: Los tres lenguajes expresan la relación ES-UN mediante clases abstractas; C++ requiere destructor virtual para evitar fugas de memoria.");
        } else {
            txtCompJava.setText(
                    "// Heap + Garbage Collector (G1GC)\n"
                    + "public void procesar() {\n"
                    + "    User u = new User();\n"
                    + "    u.setUsername(\"Ana\");\n"
                    + "    // Liberado automáticamente\n"
                    + "    // cuando queda sin referencias\n"
                    + "}"
            );
            txtCompPython.setText(
                    "# Conteo de referencias + GC cíclico\n"
                    + "def procesar() -> None:\n"
                    + "    u = User(username=\"Ana\")\n"
                    + "    # __del__ cuando refcount == 0"
            );
            txtCompCpp.setText(
                    "// Determinismo RAII y Smart Pointers\n"
                    + "#include <memory>\n\n"
                    + "void procesar() {\n"
                    + "    auto u = std::make_unique<User>();\n"
                    + "    // Se destruye exactamente al\n"
                    + "    // salir del bloque (llave final)\n"
                    + "}"
            );
            lblCompTakeaway.setText("Análisis: Java y Python delegan la liberación al recolector de basura; C++ garantiza destrucción determinista al cerrar el ámbito (RAII).");
        }
    }

    private JPanel buildAchievementsWorkspaceView() {
        JPanel view = new JPanel(new BorderLayout(0, 14));
        view.setOpaque(false);

        view.add(Theme.createSectionHeader(
                "Sistema de Logros e Insignias Desbloqueables",
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

    private void switchWorkspaceView(String cardKey) {
        workspaceCardLayout.show(workspaceContainer, cardKey);

        if ("COURSES".equals(cardKey)) {
            navCoursesBtn.setPalette(Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
            navComparatorBtn.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            navAchievementsBtn.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        } else if ("COMPARATOR".equals(cardKey)) {
            navComparatorBtn.setPalette(Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
            navCoursesBtn.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            navAchievementsBtn.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        } else {
            navAchievementsBtn.setPalette(Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
            navCoursesBtn.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            navComparatorBtn.setPalette(Theme.SURFACE, Theme.SURFACE_ELEVATED, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        }
    }

    private void onChallengeOrLessonCompleted() {
        solvedChallengesCount++;
        refreshGamificationTelemetry();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardFrame(createFallbackUser()).setVisible(true));
    }
}
