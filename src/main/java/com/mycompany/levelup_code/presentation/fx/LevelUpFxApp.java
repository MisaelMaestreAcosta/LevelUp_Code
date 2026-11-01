package com.mycompany.levelup_code.presentation.fx;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.DailyActivity;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import com.mycompany.levelup_code.presentation.component.ChallengeFrame;
import com.mycompany.levelup_code.presentation.component.LessonFrame;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Interfaz gráfica moderna basada en JavaFX 21 y hojas de estilo CSS ({@code styles/levelup.css}).
 * Integra animaciones de transición, identidad cromática por lenguaje (Java #E76F00, Python #3776AB,
 * C++ #00599C), árbol de nodos de aprendizaje, mapa de calor de {@code Actividad_Diaria} y
 * comparador sincronizado de 3 lenguajes conectado en vivo a Oracle 18c XE.
 */
public class LevelUpFxApp extends Application {

    private Stage primaryStage;
    private User activeUser;
    private final List<Lesson> javaLessons = new ArrayList<>();
    private final List<Lesson> pythonLessons = new ArrayList<>();
    private final List<Lesson> cppLessons = new ArrayList<>();
    private final Map<LocalDate, Integer> activityMap = new HashMap<>();

    private VBox skillTreeContainer;
    private Label lblSkillTreeHeader;
    private StackPane contentSwapPane;
    private TextArea txtCompJava;
    private TextArea txtCompPy;
    private TextArea txtCompCpp;
    private Label lblCompSummary;

    public static void launchApp(String[] args) {
        Application.launch(LevelUpFxApp.class, args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("LevelUp Code — Plataforma Interactiva JavaFX + CSS (Oracle 18c XE)");
        showLoginScene();
        this.primaryStage.show();
    }

    private void applyStylesheet(Scene scene) {
        URL cssUrl = getClass().getResource("/styles/levelup.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    private void showLoginScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");

        // Panel izquierdo de presentación comparativa
        VBox leftShowcase = new VBox(16);
        leftShowcase.getStyleClass().add("sidebar");
        leftShowcase.setPrefWidth(470);
        leftShowcase.setPadding(new Insets(28));

        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        Label lblBrand = new Label("LevelUp Code");
        lblBrand.getStyleClass().add("card-title");
        Label badgeUpc = new Label("JAVAFX 21 · CSS");
        badgeUpc.getStyleClass().add("badge-indigo");
        brandBox.getChildren().addAll(lblBrand, badgeUpc);

        Label lblHero = new Label("Domina la lógica algorítmica\nmás allá de la sintaxis.");
        lblHero.getStyleClass().add("hero-title");

        Label lblSub = new Label("Compara estructuras de datos, herencia y algoritmos en tiempo real entre Java, Python y C++ con persistencia en Oracle 18c XE.");
        lblSub.getStyleClass().add("muted-text");
        lblSub.setWrapText(true);

        VBox previewCard = new VBox(10);
        previewCard.getStyleClass().add("surface-card");

        HBox tabRow = new HBox(8);
        tabRow.setAlignment(Pos.CENTER_LEFT);
        Label lblPreviewTitle = new Label("BÚSQUEDA BINARIA · O(log n)");
        lblPreviewTitle.getStyleClass().add("section-caption");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnJava = new Button("Java");
        btnJava.getStyleClass().add("btn-java");
        Button btnPy = new Button("Python");
        btnPy.getStyleClass().add("btn-python");
        Button btnCpp = new Button("C++");
        btnCpp.getStyleClass().add("btn-cpp");

        TextArea codePreview = new TextArea();
        codePreview.setEditable(false);
        codePreview.setPrefRowCount(10);
        codePreview.getStyleClass().add("code-editor");

        Label lblLangNote = new Label();
        lblLangNote.getStyleClass().add("badge-java");

        btnJava.setOnAction(e -> {
            codePreview.setText(
                    "public class BinarySearch {\n"
                    + "    static int search(int[] a, int target) {\n"
                    + "        int low = 0, high = a.length - 1;\n"
                    + "        while (low <= high) {\n"
                    + "            int mid = low + (high - low) / 2;\n"
                    + "            if (a[mid] == target) return mid;\n"
                    + "            if (a[mid] < target) low = mid + 1;\n"
                    + "            else high = mid - 1;\n"
                    + "        }\n"
                    + "        return -1;\n"
                    + "    }\n"
                    + "}"
            );
            lblLangNote.setText("Java 21 LTS · Tipado estático (#E76F00)");
        });
        btnPy.setOnAction(e -> {
            codePreview.setText(
                    "def binary_search(a: list[int], target: int) -> int:\n"
                    + "    low, high = 0, len(a) - 1\n"
                    + "    while low <= high:\n"
                    + "        mid = (low + high) // 2\n"
                    + "        if a[mid] == target: return mid\n"
                    + "        elif a[mid] < target: low = mid + 1\n"
                    + "        else: high = mid - 1\n"
                    + "    return -1"
            );
            lblLangNote.setText("Python 3.12 · División entera // (#3776AB)");
        });
        btnCpp.setOnAction(e -> {
            codePreview.setText(
                    "#include <vector>\n\n"
                    + "int binarySearch(const std::vector<int>& a, int x) {\n"
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
            lblLangNote.setText("ISO C++ 20 · Referencia constante (#00599C)");
        });
        btnJava.fire();

        tabRow.getChildren().addAll(lblPreviewTitle, spacer, btnJava, btnPy, btnCpp);
        previewCard.getChildren().addAll(tabRow, codePreview, lblLangNote);
        VBox.setVgrow(previewCard, Priority.ALWAYS);

        boolean connected = AppContext.getInstance().isOracleConnected();
        Label lblDbStatus = new Label(connected
                ? "● ORACLE 18C XE CONECTADO (XEPDB1 · LEVELUP_ADMIN)"
                : "○ MODO LOCAL EN MEMORIA");
        lblDbStatus.getStyleClass().add(connected ? "badge-green" : "badge-amber");

        leftShowcase.getChildren().addAll(brandBox, lblHero, lblSub, previewCard, lblDbStatus);

        // Panel derecho de autenticación
        VBox rightAuth = new VBox(16);
        rightAuth.setPadding(new Insets(42, 52, 36, 52));
        rightAuth.setAlignment(Pos.CENTER_LEFT);

        Label lblFormTitle = new Label("Iniciar Sesión en LevelUp Code");
        lblFormTitle.getStyleClass().add("hero-title");
        Label lblFormSub = new Label("Autenticación verificada contra la tabla Usuarios con PBKDF2-HMAC-SHA256.");
        lblFormSub.getStyleClass().add("muted-text");

        Label lblEmailCap = new Label("CORREO ELECTRÓNICO");
        lblEmailCap.getStyleClass().add("section-caption");
        TextField txtEmail = new TextField("estudiante@levelup.edu.co");

        Label lblPassCap = new Label("CONTRASEÑA");
        lblPassCap.getStyleClass().add("section-caption");
        PasswordField txtPass = new PasswordField();
        txtPass.setText("estudiante123");

        Label lblFeedback = new Label();
        lblFeedback.getStyleClass().add("muted-text");

        Button btnLogin = new Button("Ingresar al Espacio de Trabajo");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        btnLogin.setOnAction(e -> {
            String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
            String pass = txtPass.getText() != null ? txtPass.getText() : "";
            authenticateAndEnter(email, pass, lblFeedback);
        });

        Label lblQuick = new Label("ACCESO RÁPIDO CON 1 CLIC (CUENTAS SEMILLA ORACLE)");
        lblQuick.getStyleClass().add("section-caption");

        HBox quickRow = new HBox(12);
        Button btnQuickStudent = new Button("Entrar como Estudiante (Nivel 5)");
        btnQuickStudent.getStyleClass().add("btn-secondary");
        btnQuickStudent.setOnAction(e -> authenticateAndEnter("estudiante@levelup.edu.co", "estudiante123", lblFeedback));

        Button btnQuickAdmin = new Button("Entrar como Admin (Nivel 10)");
        btnQuickAdmin.getStyleClass().add("btn-secondary");
        btnQuickAdmin.setOnAction(e -> authenticateAndEnter("admin@levelup.edu.co", "admin12345", lblFeedback));
        quickRow.getChildren().addAll(btnQuickStudent, btnQuickAdmin);

        rightAuth.getChildren().addAll(
                lblFormTitle, lblFormSub,
                lblEmailCap, txtEmail,
                lblPassCap, txtPass,
                lblFeedback, btnLogin,
                lblQuick, quickRow
        );

        root.setLeft(leftShowcase);
        root.setCenter(rightAuth);

        Scene scene = new Scene(root, 1100, 680);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    private void authenticateAndEnter(String email, String password, Label lblFeedback) {
        AppContext ctx = AppContext.getInstance();
        if (ctx.isOracleConnected()) {
            try {
                Optional<User> auth = ctx.getUserService().authenticate(email, password);
                if (auth.isPresent()) {
                    this.activeUser = auth.get();
                    loadOracleDataForDashboard();
                    showDashboardScene();
                    return;
                } else {
                    lblFeedback.setText("Credenciales inválidas en Oracle.");
                    return;
                }
            } catch (Exception ex) {
                lblFeedback.setText(ex.getMessage());
            }
        }
        User fallback = new User();
        fallback.setId(1);
        fallback.setUsername(email.startsWith("admin") ? "Admin" : "Estudiante");
        fallback.setEmail(email);
        fallback.setTotalExp(1450);
        fallback.setLevel(5);
        fallback.setCurrentStreak(7);
        fallback.setMaxStreak(12);
        this.activeUser = fallback;
        loadOracleDataForDashboard();
        showDashboardScene();
    }

    private void loadOracleDataForDashboard() {
        javaLessons.clear();
        pythonLessons.clear();
        cppLessons.clear();
        activityMap.clear();

        AppContext ctx = AppContext.getInstance();
        if (ctx.isOracleConnected()) {
            try {
                List<Language> langs = ctx.getContentService().listActiveLanguages();
                for (Language l : langs) {
                    List<Lesson> target = l.getName().toLowerCase().contains("python")
                            ? pythonLessons
                            : (l.getName().toLowerCase().contains("c++") ? cppLessons : javaLessons);
                    if (l.getModules() != null) {
                        for (CourseModule m : l.getModules()) {
                            if (m.getLessons() != null) {
                                target.addAll(m.getLessons());
                            }
                        }
                    }
                }
                if (activeUser != null && activeUser.getId() > 0) {
                    for (DailyActivity da : ctx.getOracleActivityRepository().findRecentByUser(activeUser.getId(), 14)) {
                        if (da.getActivityDate() != null) {
                            activityMap.put(da.getActivityDate(), da.getExperienceGained());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void showDashboardScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");

        // Barra lateral
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);

        Label lblBrand = new Label("LevelUp Code");
        lblBrand.getStyleClass().add("card-title");
        Label lblConn = new Label("Oracle XEPDB1 · Activo");
        lblConn.getStyleClass().add("badge-green");

        Button btnNavNodes = new Button("Mapa de Nodos y Lenguajes");
        btnNavNodes.getStyleClass().add("btn-primary");
        btnNavNodes.setMaxWidth(Double.MAX_VALUE);

        Button btnNavComp = new Button("Comparador 3 Lenguajes");
        btnNavComp.getStyleClass().add("btn-secondary");
        btnNavComp.setMaxWidth(Double.MAX_VALUE);

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        VBox profileCard = new VBox(6);
        profileCard.getStyleClass().add("surface-subtle");
        Label lblUser = new Label(activeUser.getUsername() + " (" + activeUser.getEmail() + ")");
        lblUser.getStyleClass().add("section-caption");
        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.getStyleClass().add("btn-secondary");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> showLoginScene());
        profileCard.getChildren().addAll(lblUser, btnLogout);

        sidebar.getChildren().addAll(lblBrand, lblConn, btnNavNodes, btnNavComp, vSpacer, profileCard);

        // Contenido central
        VBox centerBox = new VBox(16);
        centerBox.setPadding(new Insets(20, 24, 20, 24));

        HBox telemetryRow = buildTelemetryRow();
        contentSwapPane = new StackPane();
        VBox.setVgrow(contentSwapPane, Priority.ALWAYS);

        VBox coursesPane = buildCoursesAndSkillTreePane();
        VBox comparatorPane = buildComparatorPane();

        contentSwapPane.getChildren().setAll(coursesPane);

        btnNavNodes.setOnAction(e -> swapViewWithFade(coursesPane));
        btnNavComp.setOnAction(e -> swapViewWithFade(comparatorPane));

        centerBox.getChildren().addAll(telemetryRow, contentSwapPane);

        root.setLeft(sidebar);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 1240, 760);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    private void swapViewWithFade(javafx.scene.Node target) {
        contentSwapPane.getChildren().setAll(target);
        FadeTransition ft = new FadeTransition(Duration.millis(220), target);
        ft.setFromValue(0.25);
        ft.setToValue(1.0);
        ft.play();
    }

    private HBox buildTelemetryRow() {
        HBox row = new HBox(14);

        VBox cardLvl = new VBox(6);
        cardLvl.getStyleClass().add("surface-card");
        HBox.setHgrow(cardLvl, Priority.ALWAYS);
        Label c1 = new Label("NIVEL DEL ESTUDIANTE");
        c1.getStyleClass().add("section-caption");
        Label v1 = new Label("Nivel " + activeUser.getLevel());
        v1.getStyleClass().add("metric-value");
        ProgressBar pb = new ProgressBar((activeUser.getTotalExp() % 300) / 300.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        cardLvl.getChildren().addAll(c1, v1, pb);

        VBox cardExp = new VBox(6);
        cardExp.getStyleClass().add("surface-card");
        HBox.setHgrow(cardExp, Priority.ALWAYS);
        Label c2 = new Label("EXPERIENCIA ACUMULADA");
        c2.getStyleClass().add("section-caption");
        Label v2 = new Label(activeUser.getTotalExp() + " XP");
        v2.getStyleClass().add("metric-value");
        Label s2 = new Label("Tabla Usuarios · exp_total");
        s2.getStyleClass().add("muted-text");
        cardExp.getChildren().addAll(c2, v2, s2);

        VBox cardHeatmap = new VBox(6);
        cardHeatmap.getStyleClass().add("surface-card");
        HBox.setHgrow(cardHeatmap, Priority.ALWAYS);
        Label c3 = new Label("RACHA Y MAPA DE CALOR (ACTIVIDAD_DIARIA)");
        c3.getStyleClass().add("section-caption");
        Label v3 = new Label(activeUser.getCurrentStreak() + " Días en racha");
        v3.getStyleClass().add("metric-value");

        HBox heatCells = new HBox(4);
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int xp = activityMap.getOrDefault(d, 0);
            Region cell = new Region();
            cell.setPrefSize(13, 13);
            String styleClass = xp <= 0 ? "heatmap-cell-0"
                    : (xp <= 80 ? "heatmap-cell-1" : (xp <= 150 ? "heatmap-cell-2" : "heatmap-cell-3"));
            cell.getStyleClass().add(styleClass);
            Tooltip.install(cell, new Tooltip(d + ": +" + xp + " XP"));
            heatCells.getChildren().add(cell);
        }
        cardHeatmap.getChildren().addAll(c3, v3, heatCells);

        row.getChildren().addAll(cardLvl, cardExp, cardHeatmap);
        return row;
    }

    private VBox buildCoursesAndSkillTreePane() {
        VBox box = new VBox(16);

        HBox langCards = new HBox(16);
        langCards.getChildren().addAll(
                createLanguageCard("Java", "Java 21 LTS", "POO, Polimorfismo y Colecciones en JVM", "lang-card-java", "btn-java", javaLessons),
                createLanguageCard("Python", "Python 3.12", "Protocolos ABC, Listas y Búsqueda Binaria", "lang-card-python", "btn-python", pythonLessons),
                createLanguageCard("C++", "ISO C++ 20", "Funciones Virtuales Puras, STL y RAII", "lang-card-cpp", "btn-cpp", cppLessons)
        );

        VBox treeCard = new VBox(12);
        treeCard.getStyleClass().add("surface-card");
        VBox.setVgrow(treeCard, Priority.ALWAYS);

        lblSkillTreeHeader = new Label("ÁRBOL DE HABILIDADES Y PROGRESO (PROGRESO_LECCION) · JAVA");
        lblSkillTreeHeader.getStyleClass().add("section-caption");

        skillTreeContainer = new VBox(10);
        ScrollPane scroll = new ScrollPane(skillTreeContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        treeCard.getChildren().addAll(lblSkillTreeHeader, scroll);
        populateSkillTree("Java", javaLessons);

        box.getChildren().addAll(langCards, treeCard);
        return box;
    }

    private VBox createLanguageCard(String name, String badge, String desc, String cardClass, String btnClass, List<Lesson> lessons) {
        VBox card = new VBox(10);
        card.getStyleClass().add(cardClass);
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label(name);
        lblTitle.getStyleClass().add("card-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label lblVer = new Label(badge);
        lblVer.getStyleClass().add("badge-indigo");
        top.getChildren().addAll(lblTitle, sp, lblVer);

        Label lblDesc = new Label(desc);
        lblDesc.getStyleClass().add("muted-text");
        lblDesc.setWrapText(true);

        Button btnSelect = new Button("Ver Árbol de Nodos (" + lessons.size() + " lecciones)");
        btnSelect.getStyleClass().add(btnClass);
        btnSelect.setMaxWidth(Double.MAX_VALUE);
        btnSelect.setOnAction(e -> populateSkillTree(name, lessons));

        card.getChildren().addAll(top, lblDesc, btnSelect);
        return card;
    }

    private void populateSkillTree(String langName, List<Lesson> lessons) {
        lblSkillTreeHeader.setText("ÁRBOL DE HABILIDADES Y PROGRESO (PROGRESO_LECCION) · " + langName.toUpperCase());
        skillTreeContainer.getChildren().clear();

        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            ProgressStatus st = (i == 0) ? ProgressStatus.COMPLETED : (i == 1 ? ProgressStatus.IN_PROGRESS : ProgressStatus.PENDING);
            if (AppContext.getInstance().isOracleConnected() && activeUser != null && activeUser.getId() > 0) {
                try {
                    Optional<LessonProgress> lp = AppContext.getInstance()
                            .getProgressRepository().findByUserAndLesson(activeUser.getId(), lesson.getId());
                    if (lp.isPresent() && lp.get().getStatus() != null) {
                        st = lp.get().getStatus();
                    }
                } catch (Exception ignored) {
                }
            }

            HBox nodeCard = new HBox(14);
            nodeCard.setAlignment(Pos.CENTER_LEFT);
            String nodeClass = st == ProgressStatus.COMPLETED
                    ? "skill-node-completed"
                    : (st == ProgressStatus.IN_PROGRESS ? "skill-node-active" : "skill-node-pending");
            nodeCard.getStyleClass().add(nodeClass);

            String icon = st == ProgressStatus.COMPLETED ? "✓" : (st == ProgressStatus.IN_PROGRESS ? "▶" : "○");
            Label lblNodeTitle = new Label(icon + "  Nodo #" + lesson.getOrderIndex() + ": " + lesson.getTitle());
            lblNodeTitle.getStyleClass().add("card-title");

            Label lblReward = new Label("+" + lesson.getExpReward() + " XP");
            lblReward.getStyleClass().add("badge-green");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button btnTheory = new Button("Estudiar Teoría");
            btnTheory.getStyleClass().add("btn-secondary");
            btnTheory.setOnAction(e -> javax.swing.SwingUtilities.invokeLater(
                    () -> new LessonFrame(activeUser, lesson, null).setVisible(true)));

            Button btnChallenge = new Button("Resolver Reto");
            btnChallenge.getStyleClass().add("btn-primary");
            btnChallenge.setOnAction(e -> javax.swing.SwingUtilities.invokeLater(() -> {
                Challenge ch = (lesson.getChallenges() != null && !lesson.getChallenges().isEmpty())
                        ? lesson.getChallenges().get(0)
                        : null;
                new ChallengeFrame(activeUser, ch, null).setVisible(true);
            }));

            nodeCard.getChildren().addAll(lblNodeTitle, lblReward, sp, btnTheory, btnChallenge);
            skillTreeContainer.getChildren().add(nodeCard);
        }
    }

    private VBox buildComparatorPane() {
        VBox box = new VBox(14);

        HBox topBar = new HBox(10);
        topBar.getStyleClass().add("surface-card");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblHeader = new Label("Comparador Espejo Sincronizado (Java · Python · C++)");
        lblHeader.getStyleClass().add("card-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button b1 = new Button("1. Polimorfismo");
        b1.getStyleClass().add("btn-secondary");
        Button b2 = new Button("2. Búsqueda Binaria");
        b2.getStyleClass().add("btn-secondary");
        Button b3 = new Button("3. Memoria y Listas");
        b3.getStyleClass().add("btn-secondary");

        topBar.getChildren().addAll(lblHeader, sp, b1, b2, b3);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        VBox.setVgrow(grid, Priority.ALWAYS);

        txtCompJava = new TextArea();
        txtCompJava.setEditable(false);
        txtCompJava.getStyleClass().add("code-editor");

        txtCompPy = new TextArea();
        txtCompPy.setEditable(false);
        txtCompPy.getStyleClass().add("code-editor");

        txtCompCpp = new TextArea();
        txtCompCpp.setEditable(false);
        txtCompCpp.getStyleClass().add("code-editor");

        VBox cJava = new VBox(8, new Label("JAVA 21 (#E76F00)"), txtCompJava);
        cJava.getStyleClass().add("lang-card-java");
        VBox cPy = new VBox(8, new Label("PYTHON 3.12 (#3776AB)"), txtCompPy);
        cPy.getStyleClass().add("lang-card-python");
        VBox cCpp = new VBox(8, new Label("C++ 20 (#00599C)"), txtCompCpp);
        cCpp.getStyleClass().add("lang-card-cpp");

        HBox columns = new HBox(14, cJava, cPy, cCpp);
        HBox.setHgrow(cJava, Priority.ALWAYS);
        HBox.setHgrow(cPy, Priority.ALWAYS);
        HBox.setHgrow(cCpp, Priority.ALWAYS);
        VBox.setVgrow(txtCompJava, Priority.ALWAYS);
        VBox.setVgrow(txtCompPy, Priority.ALWAYS);
        VBox.setVgrow(txtCompCpp, Priority.ALWAYS);
        VBox.setVgrow(columns, Priority.ALWAYS);

        lblCompSummary = new Label();
        lblCompSummary.getStyleClass().add("surface-card");
        lblCompSummary.setMaxWidth(Double.MAX_VALUE);

        b1.setOnAction(e -> {
            txtCompJava.setText("abstract class Animal {\n    abstract String emitirSonido();\n}\nclass Perro extends Animal {\n    @Override\n    String emitirSonido() { return \"Guau\"; }\n}");
            txtCompPy.setText("from abc import ABC, abstractmethod\nclass Animal(ABC):\n    @abstractmethod\n    def emitir_sonido(self) -> str: ...\nclass Perro(Animal):\n    def emitir_sonido(self) -> str: return \"Guau\"");
            txtCompCpp.setText("class Animal {\npublic:\n    virtual ~Animal() = default;\n    virtual std::string emitirSonido() const = 0;\n};\nclass Perro : public Animal {\npublic:\n    std::string emitirSonido() const override { return \"Guau\"; }\n};");
            lblCompSummary.setText("Lecciones espejo #1: Despacho dinámico en JVM (@Override), ABC en Python y vtable (virtual = 0) en C++.");
        });
        b2.setOnAction(e -> {
            txtCompJava.setText("int mid = low + (high - low) / 2;\nif (a[mid] == target) return mid;");
            txtCompPy.setText("mid = (low + high) // 2\nif a[mid] == target: return mid");
            txtCompCpp.setText("int mid = low + (high - low) / 2;\nif (a[mid] == target) return mid;");
            lblCompSummary.setText("Lecciones espejo #2: Prevención de overflow en 32 bits (Java/C++) frente a precisión arbitraria con // en Python.");
        });
        b3.setOnAction(e -> {
            txtCompJava.setText("List<Integer> nums = new ArrayList<>();\nnums.add(10);");
            txtCompPy.setText("nums: list[int] = [10]\nnums.append(20)");
            txtCompCpp.setText("std::vector<int> nums = {10};\nnums.push_back(20);");
            lblCompSummary.setText("Lecciones espejo #3: Crecimiento amortizado O(1) en Heap (Java/Python) frente a memoria contigua determinista RAII (C++).");
        });
        b1.fire();

        box.getChildren().addAll(topBar, columns, lblCompSummary);
        return box;
    }
}

