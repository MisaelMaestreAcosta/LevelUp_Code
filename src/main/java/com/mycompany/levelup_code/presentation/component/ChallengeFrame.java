package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.model.Attempt;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Entorno interactivo de resolución de retos (Código y Quiz) en JavaFX + CSS ({@code /styles/levelup.css})
 * con numeración de líneas reactiva, casos de prueba visibles/ocultos y motor de evaluación
 * dinámica conectado a {@code Casos_Prueba} e {@code Intentos} en Oracle Database.
 */
public class ChallengeFrame extends Stage {

    private static final Logger LOGGER = Logger.getLogger(ChallengeFrame.class.getName());
    private static final Pattern JAVA_CLASS_PATTERN = Pattern.compile("(?:public\\s+)?class\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern JAVA_METHOD_PATTERN = Pattern.compile(
            "public\\s+static\\s+[A-Za-z0-9_<>\\[\\]]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^\\)]*)\\)");

    private final User currentUser;
    private final List<Challenge> availableChallenges;
    private final Runnable onSolvedCallback;
    private final SimulatedCodeRunner codeRunner = AppContext.getInstance().getCodeRunner();

    private Challenge activeChallenge;
    private boolean xpAwardedForCurrent = false;
    private int selectedQuizIndex = -1;

    private Label lblChallengeTitle;
    private Label lblStatement;
    private Label badgeType;
    private Label badgeDifficulty;
    private Label badgeExecutionMode;
    private Label badgeReward;
    private final List<Button> switcherButtons = new ArrayList<>();
    private StackPane centerCardPane;
    private VBox codeEditorCard;
    private VBox quizCard;
    private Button btnNextChallenge;
    private Button btnSubmit;

    // Editor de código y numeración de líneas
    private TextArea txtEditor;
    private TextArea txtLineGutter;

    // Opciones de Quiz
    private VBox quizOptionsContainer;
    private final List<HBox> quizOptionCards = new ArrayList<>();

    // Panel lateral de casos de prueba, inspector interactivo y consola
    private VBox testCasesListBox;
    private final List<VBox> testCaseRowCards = new ArrayList<>();
    private final List<Label> testCaseStatusBadges = new ArrayList<>();
    private final List<SimulatedCodeRunner.CaseOutcome> latestCaseOutcomes = new ArrayList<>();
    private Label lblCaseInspectorTitle;
    private Label lblCaseInspectorDetail;
    private TextArea txtConsoleOutput;
    private Label lblExecutionMetrics;
    private Label lblResultBanner;

    public ChallengeFrame() {
        this(null, createDefaultCodeChallenge(), null);
    }

    public ChallengeFrame(User user, Challenge challenge, Runnable onSolvedCallback) {
        this.currentUser = user;
        this.onSolvedCallback = onSolvedCallback;
        this.availableChallenges = new ArrayList<>();
        if (challenge != null) {
            if (challenge.getLesson() != null && challenge.getLesson().getChallenges() != null
                    && !challenge.getLesson().getChallenges().isEmpty()) {
                this.availableChallenges.addAll(challenge.getLesson().getChallenges());
            } else {
                this.availableChallenges.add(challenge);
                this.availableChallenges.add(createDefaultQuizChallenge());
            }
            this.activeChallenge = challenge;
        } else {
            CodeChallenge defCode = createDefaultCodeChallenge();
            QuizChallenge defQuiz = createDefaultQuizChallenge();
            this.availableChallenges.add(defCode);
            this.availableChallenges.add(defQuiz);
            this.activeChallenge = defCode;
        }

        initComponents();
        loadChallengeIntoView(this.activeChallenge);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
            toFront();
        } else {
            close();
        }
    }

    private static CodeChallenge createDefaultCodeChallenge() {
        if (AppContext.getInstance().isOracleConnected()) {
            Optional<Challenge> fromDb = AppContext.getInstance()
                    .getChallengeRepository().findById(101);
            if (fromDb.isPresent() && fromDb.get() instanceof CodeChallenge) {
                return (CodeChallenge) fromDb.get();
            }
        }
        CodeChallenge code = new CodeChallenge(
                101,
                "Polimorfismo: Despacho Dinámico de Sonidos",
                "Implementa el método emitirSonido(String tipo) para que retorne \"Guau\" cuando reciba \"Perro\" y \"Miau\" cuando reciba \"Gato\".",
                null,
                60,
                "public class AnimalSoundSolver {\n"
                + "    public static String emitirSonido(String tipo) {\n"
                + "        // Completa la lógica polimórfica aquí\n"
                + "        if (\"Perro\".equalsIgnoreCase(tipo)) {\n"
                + "            return \"Guau\";\n"
                + "        }\n"
                + "        return \"\";\n"
                + "    }\n"
                + "}",
                2
        );
        code.addTestCase(new TestCase(1, code, "Perro", "Guau", false));
        code.addTestCase(new TestCase(2, code, "Gato", "Miau", false));
        code.addTestCase(new TestCase(3, code, "PERRO", "Guau", true));
        return code;
    }

    private static QuizChallenge createDefaultQuizChallenge() {
        if (AppContext.getInstance().isOracleConnected()) {
            Optional<Challenge> fromDb = AppContext.getInstance()
                    .getChallengeRepository().findById(102);
            if (fromDb.isPresent() && fromDb.get() instanceof QuizChallenge) {
                return (QuizChallenge) fromDb.get();
            }
        }
        List<String> options = Arrays.asList(
                "Sobrecarga estática en tiempo de compilación (Overloading)",
                "Enlace dinámico en tiempo de ejecución mediante tabla virtual (vtable)",
                "Conversión implícita de tipos primitivos en la pila",
                "Duplicación de constructores en la clase base"
        );
        return new QuizChallenge(
                102,
                "Quiz Conceptual: Enlace Dinámico en POO",
                "¿Qué mecanismo permite que una referencia de tipo superclase invoque la implementación sobrescrita (@Override) de la subclase en Java y C++?",
                null,
                40,
                options,
                options.get(1)
        );
    }

    private void initComponents() {
        setTitle("LevelUp Code — Laboratorio de Evaluación de Retos");
        setMinWidth(980);
        setMinHeight(640);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");
        root.setPadding(new Insets(18, 22, 18, 22));

        VBox topHeader = buildTopHeaderBar();
        BorderPane.setMargin(topHeader, new Insets(0, 0, 14, 0));
        root.setTop(topHeader);

        HBox workspaceSplit = buildWorkspaceSplit();
        root.setCenter(workspaceSplit);

        HBox bottomBar = buildBottomActionBar();
        BorderPane.setMargin(bottomBar, new Insets(14, 0, 0, 0));
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 1140, 720);
        Theme.applyCss(scene);
        setScene(scene);
    }

    private VBox buildTopHeaderBar() {
        VBox headerCard = new VBox(8);
        headerCard.getStyleClass().add("surface-card");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        lblChallengeTitle = new Label();
        lblChallengeTitle.getStyleClass().add("page-title");
        lblChallengeTitle.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 20, Theme.HEX_INDIGO));
        lblChallengeTitle.setGraphicTextGap(8);

        badgeType = Theme.createPillBadge("CÓDIGO", "badge-indigo");
        badgeDifficulty = Theme.createDifficultyBadge(60);
        badgeExecutionMode = Theme.createPillBadge("EJECUCIÓN AISLADA JVM", "badge-green");
        badgeReward = Theme.createPillBadge("+60 XP", "badge-amber");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(lblChallengeTitle, badgeType, badgeDifficulty, badgeExecutionMode, badgeReward, spacer);

        switcherButtons.clear();
        if (availableChallenges.size() > 1) {
            HBox switcher = new HBox(8);
            switcher.setAlignment(Pos.CENTER_RIGHT);
            Label lblSwitch = new Label("Retos de la lección:");
            lblSwitch.getStyleClass().add("muted-text");
            switcher.getChildren().add(lblSwitch);

            for (int i = 0; i < availableChallenges.size(); i++) {
                final Challenge ch = availableChallenges.get(i);
                String btnLabel = (ch.getType() == ChallengeType.QUIZ ? "Quiz #" : "Código #") + (i + 1);
                Button btnCh = new Button(btnLabel);
                btnCh.getStyleClass().add("btn-secondary");
                btnCh.setOnAction(e -> loadChallengeIntoView(ch));
                switcherButtons.add(btnCh);
                switcher.getChildren().add(btnCh);
            }
            topRow.getChildren().add(switcher);
        }

        lblStatement = new Label();
        lblStatement.getStyleClass().add("body-text");
        lblStatement.setWrapText(true);

        headerCard.getChildren().addAll(topRow, lblStatement);
        return headerCard;
    }

    private HBox buildWorkspaceSplit() {
        HBox split = new HBox(16);

        centerCardPane = new StackPane();
        codeEditorCard = buildCodeEditorCard();
        quizCard = buildQuizCard();
        centerCardPane.getChildren().setAll(codeEditorCard);

        VBox rightValidationPane = buildRightValidationPane();

        HBox.setHgrow(centerCardPane, Priority.ALWAYS);
        centerCardPane.setPrefWidth(640);
        rightValidationPane.setPrefWidth(420);
        rightValidationPane.setMinWidth(360);

        split.getChildren().addAll(centerCardPane, rightValidationPane);
        return split;
    }

    private VBox buildCodeEditorCard() {
        VBox editorCard = new VBox(10);
        editorCard.getStyleClass().add("surface-card");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label lblFileTab = new Label("Editor de Solución · UTF-8");
        lblFileTab.getStyleClass().add("section-caption");
        lblFileTab.setGraphic(Theme.createIcon(Theme.IconKind.TERMINAL, 15, Theme.HEX_INDIGO));
        lblFileTab.setGraphicTextGap(6);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnHint = new Button("Ver Pista Didáctica");
        btnHint.getStyleClass().add("btn-ghost-green");
        btnHint.setOnAction(e -> showPedagogicalHint());

        Button btnSolutionHint = new Button("Insertar Solución Modelo");
        btnSolutionHint.getStyleClass().add("btn-ghost");
        btnSolutionHint.setOnAction(e -> fillReferenceSolution());

        Button btnResetCode = new Button("Restaurar Plantilla");
        btnResetCode.getStyleClass().add("btn-secondary");
        btnResetCode.setOnAction(e -> {
            if (activeChallenge instanceof CodeChallenge) {
                txtEditor.setText(((CodeChallenge) activeChallenge).getInitialTemplate());
            }
        });

        toolbar.getChildren().addAll(lblFileTab, spacer, btnHint, btnSolutionHint, btnResetCode);

        // Contenedor con numeración de líneas (gutter) y área de edición
        HBox editorContainer = new HBox(0);
        VBox.setVgrow(editorContainer, Priority.ALWAYS);

        txtLineGutter = new TextArea("1");
        txtLineGutter.setEditable(false);
        txtLineGutter.setFocusTraversable(false);
        txtLineGutter.setPrefWidth(48);
        txtLineGutter.setMinWidth(48);
        txtLineGutter.setMaxWidth(48);
        txtLineGutter.getStyleClass().add("line-gutter");

        txtEditor = new TextArea();
        txtEditor.getStyleClass().add("code-editor");
        HBox.setHgrow(txtEditor, Priority.ALWAYS);

        txtEditor.textProperty().addListener((obs, oldVal, newVal) -> updateLineGutter(newVal));
        txtEditor.scrollTopProperty().addListener((obs, oldVal, newVal) -> txtLineGutter.setScrollTop(newVal.doubleValue()));

        editorContainer.getChildren().addAll(txtLineGutter, txtEditor);
        editorCard.getChildren().addAll(toolbar, editorContainer);
        return editorCard;
    }

    private void updateLineGutter(String text) {
        int lines = 1;
        if (text != null && !text.isEmpty()) {
            lines = text.split("\n", -1).length;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            if (i > 1) {
                sb.append("\n");
            }
            sb.append(i);
        }
        txtLineGutter.setText(sb.toString());
    }

    private VBox buildQuizCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("surface-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblQuizTitle = new Label("SELECCIONA LA RESPUESTA CORRECTA");
        lblQuizTitle.getStyleClass().add("section-caption");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnQuizHint = new Button("Ver Pista Conceptual");
        btnQuizHint.getStyleClass().add("btn-ghost-green");
        btnQuizHint.setOnAction(e -> showPedagogicalHint());
        Label badgeSingle = Theme.createPillBadge("Evaluación de selección única", "badge-violet");
        header.getChildren().addAll(lblQuizTitle, sp, btnQuizHint, badgeSingle);

        quizOptionsContainer = new VBox(10);
        ScrollPane scrollQuiz = new ScrollPane(quizOptionsContainer);
        scrollQuiz.setFitToWidth(true);
        scrollQuiz.getStyleClass().add("scroll-pane-clean");
        VBox.setVgrow(scrollQuiz, Priority.ALWAYS);

        card.getChildren().addAll(header, scrollQuiz);
        return card;
    }

    private VBox buildRightValidationPane() {
        VBox rightColumn = new VBox(14);

        // Tarjeta superior: Casos de prueba e inspector interactivo
        VBox casesCard = new VBox(10);
        casesCard.getStyleClass().add("surface-card");
        VBox.setVgrow(casesCard, Priority.ALWAYS);

        HBox casesHeader = Theme.createSectionHeader("Casos de Prueba", "Clic para inspeccionar", "badge-indigo");

        testCasesListBox = new VBox(8);
        ScrollPane scrollCases = new ScrollPane(testCasesListBox);
        scrollCases.setFitToWidth(true);
        scrollCases.getStyleClass().add("scroll-pane-clean");
        VBox.setVgrow(scrollCases, Priority.ALWAYS);

        VBox inspectorCard = new VBox(4);
        inspectorCard.getStyleClass().add("surface-subtle");
        lblCaseInspectorTitle = new Label("Inspector de Caso de Prueba");
        lblCaseInspectorTitle.getStyleClass().add("card-title");
        lblCaseInspectorDetail = new Label("Haz clic en cualquier caso de prueba superior para ver su entrada, salida esperada y diagnóstico.");
        lblCaseInspectorDetail.getStyleClass().add("muted-text");
        lblCaseInspectorDetail.setWrapText(true);
        inspectorCard.getChildren().addAll(lblCaseInspectorTitle, lblCaseInspectorDetail);

        casesCard.getChildren().addAll(casesHeader, scrollCases, inspectorCard);

        // Tarjeta inferior: Consola de salida y métricas de tiempo
        VBox consoleCard = new VBox(8);
        consoleCard.getStyleClass().add("surface-card");
        consoleCard.setPrefHeight(205);

        HBox consoleHeader = new HBox(8);
        consoleHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblConsoleTitle = new Label("SALIDA DE CONSOLA / VEREDICTO");
        lblConsoleTitle.getStyleClass().add("section-caption");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblExecutionMetrics = new Label("Tiempo: -- ms");
        lblExecutionMetrics.getStyleClass().add("muted-text");

        consoleHeader.getChildren().addAll(lblConsoleTitle, spacer, lblExecutionMetrics);

        txtConsoleOutput = new TextArea();
        txtConsoleOutput.setEditable(false);
        txtConsoleOutput.setWrapText(true);
        txtConsoleOutput.getStyleClass().add("console-output");
        txtConsoleOutput.setText("> Listo para validar solución.\n> Presiona 'Ejecutar y Validar Solución' para correr las pruebas.");
        VBox.setVgrow(txtConsoleOutput, Priority.ALWAYS);

        consoleCard.getChildren().addAll(consoleHeader, txtConsoleOutput);

        rightColumn.getChildren().addAll(casesCard, consoleCard);
        return rightColumn;
    }

    private HBox buildBottomActionBar() {
        HBox bottomBar = new HBox(14);
        bottomBar.getStyleClass().add("surface-card");
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        lblResultBanner = new Label("Completa el reto para ganar experiencia y mantener tu racha activa.");
        lblResultBanner.getStyleClass().add("body-text");
        lblResultBanner.setGraphic(Theme.createIcon(Theme.IconKind.BOLT, 16, Theme.HEX_AMBER));
        lblResultBanner.setGraphicTextGap(8);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("Volver al Panel");
        btnClose.getStyleClass().add("btn-secondary");
        btnClose.setOnAction(e -> close());

        btnNextChallenge = new Button("Siguiente Reto →");
        btnNextChallenge.getStyleClass().add("btn-ghost");
        btnNextChallenge.setVisible(availableChallenges.size() > 1);
        btnNextChallenge.setManaged(availableChallenges.size() > 1);
        btnNextChallenge.setOnAction(e -> advanceToNextChallenge());

        btnSubmit = new Button("Ejecutar y Validar Solución");
        btnSubmit.getStyleClass().add("btn-success");
        btnSubmit.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_WHITE));
        btnSubmit.setGraphicTextGap(6);
        btnSubmit.setOnAction(e -> evaluateSubmission());

        bottomBar.getChildren().addAll(lblResultBanner, spacer, btnClose, btnNextChallenge, btnSubmit);
        return bottomBar;
    }

    private void advanceToNextChallenge() {
        if (availableChallenges.size() <= 1) {
            return;
        }
        int currentIdx = availableChallenges.indexOf(activeChallenge);
        int nextIdx = (currentIdx + 1) % availableChallenges.size();
        loadChallengeIntoView(availableChallenges.get(nextIdx));
    }

    private String resolveChallengeLanguageName(Challenge challenge) {
        if (challenge != null && challenge.getLesson() != null
                && challenge.getLesson().getModule() != null
                && challenge.getLesson().getModule().getLanguage() != null
                && challenge.getLesson().getModule().getLanguage().getName() != null) {
            return challenge.getLesson().getModule().getLanguage().getName();
        }
        return "Java";
    }

    private void loadChallengeIntoView(Challenge challenge) {
        this.activeChallenge = challenge;
        this.xpAwardedForCurrent = false;
        this.selectedQuizIndex = -1;
        if (btnSubmit != null) {
            btnSubmit.setText("Ejecutar y Validar Solución");
        }
        if (btnNextChallenge != null) {
            btnNextChallenge.getStyleClass().setAll("btn-ghost");
        }

        for (int i = 0; i < switcherButtons.size() && i < availableChallenges.size(); i++) {
            boolean isActive = availableChallenges.get(i) == challenge;
            switcherButtons.get(i).getStyleClass().setAll(isActive ? "btn-primary" : "btn-secondary");
        }

        lblChallengeTitle.setText(challenge.getTitle());
        lblStatement.setText(challenge.getDescription());
        badgeReward.setText("+" + challenge.getExpReward() + " XP");
        updateDifficultyBadge(challenge.getExpReward());

        testCasesListBox.getChildren().clear();
        testCaseRowCards.clear();
        testCaseStatusBadges.clear();
        latestCaseOutcomes.clear();

        if (challenge instanceof CodeChallenge) {
            CodeChallenge codeChallenge = (CodeChallenge) challenge;
            badgeType.setText("RETO DE CÓDIGO · LÍMITE " + codeChallenge.getTimeLimitSeconds() + "S");
            badgeType.getStyleClass().setAll("badge-indigo");

            String langName = resolveChallengeLanguageName(codeChallenge);
            boolean realJvm = codeRunner.supportsRealIsolatedExecution(langName);
            badgeExecutionMode.setText(realJvm
                    ? "EJECUCIÓN REAL AISLADA JVM"
                    : "MODO DEMO SIMULADO (" + langName.toUpperCase(Locale.ROOT) + ")");
            badgeExecutionMode.getStyleClass().setAll(realJvm ? "badge-green" : "badge-amber");
            badgeExecutionMode.setVisible(true);
            badgeExecutionMode.setManaged(true);

            centerCardPane.getChildren().setAll(codeEditorCard);
            String initial = codeChallenge.getInitialTemplate() != null ? codeChallenge.getInitialTemplate() : "";
            txtEditor.setText(initial);
            updateLineGutter(initial);

            List<TestCase> cases = codeChallenge.getTestCases();
            for (int i = 0; i < cases.size(); i++) {
                TestCase tc = cases.get(i);
                testCasesListBox.getChildren().add(createTestCaseRow(i, tc));
            }
            if (!cases.isEmpty()) {
                inspectTestCase(0);
            } else {
                lblCaseInspectorTitle.setText("Inspector de Caso de Prueba");
                lblCaseInspectorDetail.setText("Este reto no registra casos de prueba individuales.");
            }
        } else if (challenge instanceof QuizChallenge) {
            QuizChallenge quizChallenge = (QuizChallenge) challenge;
            badgeType.setText("QUIZ DE SELECCIÓN");
            badgeType.getStyleClass().setAll("badge-violet");
            badgeExecutionMode.setText("VALIDACIÓN EXACTA");
            badgeExecutionMode.getStyleClass().setAll("badge-cyan");
            badgeExecutionMode.setVisible(true);
            badgeExecutionMode.setManaged(true);
            centerCardPane.getChildren().setAll(quizCard);
            populateQuizOptions(quizChallenge);

            VBox infoBox = new VBox(6);
            infoBox.getStyleClass().add("surface-subtle");
            Label lblQuizInfo = new Label("Selecciona una de las 4 opciones a la izquierda y presiona 'Ejecutar y Validar Solución'.");
            lblQuizInfo.getStyleClass().add("body-text");
            lblQuizInfo.setWrapText(true);
            infoBox.getChildren().add(lblQuizInfo);
            testCasesListBox.getChildren().add(infoBox);

            lblCaseInspectorTitle.setText("Verificación Conceptual");
            lblCaseInspectorDetail.setText("Al enviar tu respuesta se validará inmediatamente la opción elegida y se resaltará en verde o rojo.");
        }

        Theme.playFadeIn(centerCardPane);
        txtConsoleOutput.getStyleClass().setAll("console-output");
        String modeNote = (challenge instanceof CodeChallenge)
                ? (codeRunner.supportsRealIsolatedExecution(resolveChallengeLanguageName(challenge))
                        ? "> Motor activo: Compilación real JavaCompiler + subproceso JVM aislado (-Xmx64m)."
                        : "> Aviso: Este lenguaje se evalúa en modo simulación académica de demostración (sin compilador nativo externo).")
                : "> Motor activo: Verificación conceptual de selección única.";
        txtConsoleOutput.setText("> Reto cargado: " + challenge.getTitle() + "\n" + modeNote + "\n> Esperando envío del estudiante...");
        lblExecutionMetrics.setText("Tiempo: -- ms");
    }

    private void updateDifficultyBadge(int expReward) {
        if (badgeDifficulty == null) {
            return;
        }
        if (expReward <= 65) {
            badgeDifficulty.setText("NIVEL FÁCIL");
            badgeDifficulty.getStyleClass().setAll("badge-green");
        } else if (expReward <= 85) {
            badgeDifficulty.setText("NIVEL MEDIO");
            badgeDifficulty.getStyleClass().setAll("badge-amber");
        } else {
            badgeDifficulty.setText("NIVEL DIFÍCIL");
            badgeDifficulty.getStyleClass().setAll("badge-rose");
        }
    }

    private VBox createTestCaseRow(final int zeroBasedIndex, final TestCase tc) {
        int displayNum = zeroBasedIndex + 1;
        VBox row = new VBox(6);
        row.getStyleClass().add("test-case-card");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        String titleText = tc.isHidden()
                ? "Caso #" + displayNum + " (Oculto)"
                : "Caso #" + displayNum + " · Entrada: \"" + tc.getInput() + "\"";
        Label lblCase = new Label(titleText);
        lblCase.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = Theme.createPillBadge("PENDIENTE", "badge-neutral");
        testCaseStatusBadges.add(statusBadge);

        top.getChildren().addAll(lblCase, spacer, statusBadge);

        String detailText = tc.isHidden()
                ? "Los datos de entrada y salida se verifican internamente al enviar."
                : "Salida esperada: \"" + tc.getExpectedOutput() + "\"";
        Label lblDetail = new Label(detailText);
        lblDetail.getStyleClass().add("muted-text");

        row.getChildren().addAll(top, lblDetail);
        row.setOnMouseClicked(e -> inspectTestCase(zeroBasedIndex));
        testCaseRowCards.add(row);
        return row;
    }

    private void inspectTestCase(int index) {
        if (!(activeChallenge instanceof CodeChallenge)) {
            return;
        }
        List<TestCase> cases = ((CodeChallenge) activeChallenge).getTestCases();
        if (index < 0 || index >= cases.size()) {
            return;
        }
        TestCase tc = cases.get(index);
        for (int i = 0; i < testCaseRowCards.size(); i++) {
            VBox card = testCaseRowCards.get(i);
            if (i < latestCaseOutcomes.size()) {
                boolean passed = latestCaseOutcomes.get(i).isPassed();
                card.getStyleClass().setAll(i == index
                        ? "test-case-card-selected"
                        : (passed ? "test-case-card-passed" : "test-case-card-failed"));
            } else {
                card.getStyleClass().setAll(i == index ? "test-case-card-selected" : "test-case-card");
            }
        }

        lblCaseInspectorTitle.setText("Inspector · Caso #" + (index + 1) + (tc.isHidden() ? " (Oculto)" : " (Visible)"));
        String inputDesc = tc.isHidden() ? "[Entrada reservada para validación anti-hardcoding]" : tc.getInput();
        String expectedDesc = tc.isHidden() ? "[Salida verificada internamente]" : tc.getExpectedOutput();
        String verdictDesc = "Pendiente de ejecución.";
        if (index < latestCaseOutcomes.size()) {
            SimulatedCodeRunner.CaseOutcome outcome = latestCaseOutcomes.get(index);
            verdictDesc = (outcome.isPassed() ? "APROBADO — " : "FALLIDO — ") + outcome.getMessage();
        }
        lblCaseInspectorDetail.setText("• Entrada: " + inputDesc
                + "\n• Salida esperada: " + expectedDesc
                + "\n• Estado: " + verdictDesc);
    }

    private void showPedagogicalHint() {
        String title = activeChallenge != null && activeChallenge.getTitle() != null
                ? activeChallenge.getTitle().toLowerCase(Locale.ROOT)
                : "";
        String hint;
        if (activeChallenge instanceof QuizChallenge) {
            String lessonTheory = (activeChallenge.getLesson() != null && activeChallenge.getLesson().getTheory() != null)
                    ? activeChallenge.getLesson().getTheory()
                    : "Revisa el concepto clave de la lección antes de elegir una opción.";
            hint = "> [PISTA CONCEPTUAL]\n> Repasa el fundamento de la lección:\n" + lessonTheory;
        } else if (title.contains("binaria")) {
            hint = "> [PISTA DIDÁCTICA — BÚSQUEDA BINARIA]\n"
                    + "> 1. Mantén dos límites: low = 0 y high = n - 1 mientras low <= high.\n"
                    + "> 2. Calcula el punto medio evitando overflow: mid = low + (high - low) / 2 (o // 2 en Python).\n"
                    + "> 3. Si arr[mid] == target retorna mid; si es menor incrementa low = mid + 1; si no, high = mid - 1.";
        } else if (title.contains("palíndromo") || title.contains("palindromo") || title.contains("punteros") || title.contains("slicing")) {
            hint = "> [PISTA DIDÁCTICA — PALÍNDROMOS Y SECUENCIAS]\n"
                    + "> 1. Normaliza el texto a minúsculas antes de comparar.\n"
                    + "> 2. Compara extremos simétricos (left y right) avanzando hacia el centro, o invierte la secuencia.";
        } else if (title.contains("pares")) {
            hint = "> [PISTA DIDÁCTICA — FILTRADO Y ACUMULACIÓN]\n"
                    + "> 1. Inicializa un acumulador total = 0.\n"
                    + "> 2. Recorre cada número n y verifica la condición de paridad (n % 2 == 0) antes de sumarlo.";
        } else if (title.contains("duplicado") || title.contains("hash")) {
            hint = "> [PISTA DIDÁCTICA — CONJUNTOS HASH EN O(n)]\n"
                    + "> 1. Crea un conjunto hash vacío (HashSet / set / std::unordered_set).\n"
                    + "> 2. Al recorrer la secuencia, si el elemento ya está en el conjunto, retórnalo inmediatamente; si termina el ciclo, retorna -1.";
        } else {
            hint = "> [PISTA DIDÁCTICA — POLIMORFISMO Y CONTRATOS]\n"
                    + "> 1. Normaliza la cadena de entrada ignorando diferencias entre mayúsculas y minúsculas.\n"
                    + "> 2. Retorna exactamente la cadena esperada por cada caso de prueba visible y oculto.";
        }
        txtConsoleOutput.setText(hint);
        Theme.playPulse(txtConsoleOutput);
    }

    private void populateQuizOptions(QuizChallenge quiz) {
        quizOptionsContainer.getChildren().clear();
        quizOptionCards.clear();

        List<String> opts = quiz.getOptions();
        char letter = 'A';
        for (int i = 0; i < opts.size(); i++) {
            final int optionIdx = i;
            String text = opts.get(i);

            HBox optionCard = new HBox(12);
            optionCard.setAlignment(Pos.CENTER_LEFT);
            optionCard.getStyleClass().add("quiz-option-card");

            Label lblOpt = new Label(letter + ")   " + text);
            lblOpt.getStyleClass().add("card-title");
            lblOpt.setWrapText(true);
            optionCard.getChildren().add(lblOpt);

            optionCard.setOnMouseClicked(e -> selectQuizOption(optionIdx));

            quizOptionCards.add(optionCard);
            quizOptionsContainer.getChildren().add(optionCard);
            letter++;
        }
    }

    private void selectQuizOption(int index) {
        this.selectedQuizIndex = index;
        for (int i = 0; i < quizOptionCards.size(); i++) {
            HBox card = quizOptionCards.get(i);
            card.getStyleClass().setAll(i == index ? "quiz-option-card-selected" : "quiz-option-card");
        }
    }

    /**
     * Construye dinámicamente una solución de referencia a partir de la plantilla inicial
     * y los casos de prueba registrados en la base de datos para este reto.
     */
    private void fillReferenceSolution() {
        if (!(activeChallenge instanceof CodeChallenge)) {
            return;
        }
        CodeChallenge codeChallenge = (CodeChallenge) activeChallenge;
        String template = codeChallenge.getInitialTemplate() != null ? codeChallenge.getInitialTemplate() : "";
        String title = codeChallenge.getTitle() != null ? codeChallenge.getTitle().toLowerCase(Locale.ROOT) : "";
        String lang = resolveChallengeLanguageName(codeChallenge).toLowerCase(Locale.ROOT);
        List<TestCase> cases = codeChallenge.getTestCases();

        boolean isPython = lang.contains("python") || template.contains("def ");
        boolean isCpp = lang.contains("c++") || lang.contains("cpp") || template.contains("#include");
        boolean isBinarySearch = !cases.isEmpty() && cases.get(0).getInput().contains("target");
        boolean isPalindrome = title.contains("palíndromo") || title.contains("palindromo") || template.toLowerCase(Locale.ROOT).contains("palindrom");
        boolean isEvenSum = title.contains("pares") || template.toLowerCase(Locale.ROOT).contains("pares");
        boolean isFirstDuplicate = title.contains("duplicado") || template.toLowerCase(Locale.ROOT).contains("duplicado");

        if (isBinarySearch) {
            if (isPython) {
                txtEditor.setText(
                        "def search(a: list, target: int) -> int:\n"
                        + "    low, high = 0, len(a) - 1\n"
                        + "    while low <= high:\n"
                        + "        mid = (low + high) // 2\n"
                        + "        if a[mid] == target:\n"
                        + "            return mid\n"
                        + "        elif a[mid] < target:\n"
                        + "            low = mid + 1\n"
                        + "        else:\n"
                        + "            high = mid - 1\n"
                        + "    return -1\n"
                );
            } else if (isCpp) {
                txtEditor.setText(
                        "#include <vector>\n\n"
                        + "int search(const std::vector<int>& a, int target) {\n"
                        + "    int low = 0;\n"
                        + "    int high = static_cast<int>(a.size()) - 1;\n"
                        + "    while (low <= high) {\n"
                        + "        int mid = low + (high - low) / 2;\n"
                        + "        if (a[mid] == target) return mid;\n"
                        + "        if (a[mid] < target) low = mid + 1;\n"
                        + "        else high = mid - 1;\n"
                        + "    }\n"
                        + "    return -1;\n"
                        + "}"
                );
            } else {
                String className = extractJavaClassName(template, "BinarySearchSolver");
                String methodName = extractJavaMethodName(template, "search");
                txtEditor.setText(
                        "public class " + className + " {\n"
                        + "    public static int " + methodName + "(int[] a, int target) {\n"
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
            }
            return;
        }

        if (isPalindrome) {
            if (isPython) {
                txtEditor.setText(
                        "def es_palindromo(texto: str) -> bool:\n"
                        + "    limpio = texto.lower()\n"
                        + "    return limpio == limpio[::-1]\n"
                );
            } else if (isCpp) {
                txtEditor.setText(
                        "#include <string>\n#include <cctype>\n\n"
                        + "bool esPalindromo(const std::string& texto) {\n"
                        + "    int left = 0, right = static_cast<int>(texto.size()) - 1;\n"
                        + "    while (left < right) {\n"
                        + "        if (std::tolower(texto[left]) != std::tolower(texto[right])) return false;\n"
                        + "        left++; right--;\n"
                        + "    }\n"
                        + "    return true;\n"
                        + "}"
                );
            } else {
                String className = extractJavaClassName(template, "PalindromeSolver");
                String methodName = extractJavaMethodName(template, "esPalindromo");
                txtEditor.setText(
                        "public class " + className + " {\n"
                        + "    public static boolean " + methodName + "(String texto) {\n"
                        + "        String s = texto.toLowerCase();\n"
                        + "        int left = 0, right = s.length() - 1;\n"
                        + "        while (left < right) {\n"
                        + "            if (s.charAt(left) != s.charAt(right)) return false;\n"
                        + "            left++;\n"
                        + "            right--;\n"
                        + "        }\n"
                        + "        return true;\n"
                        + "    }\n"
                        + "}"
                );
            }
            return;
        }

        if (isEvenSum) {
            if (isPython) {
                txtEditor.setText(
                        "def sumar_pares(nums: list) -> int:\n"
                        + "    total = 0\n"
                        + "    for n in nums:\n"
                        + "        if n % 2 == 0:\n"
                        + "            total += n\n"
                        + "    return total\n"
                );
            } else if (isCpp) {
                txtEditor.setText(
                        "#include <vector>\n\n"
                        + "int sumarPares(const std::vector<int>& nums) {\n"
                        + "    int total = 0;\n"
                        + "    for (int n : nums) {\n"
                        + "        if (n % 2 == 0) total += n;\n"
                        + "    }\n"
                        + "    return total;\n"
                        + "}"
                );
            } else {
                String className = extractJavaClassName(template, "EvenSumSolver");
                String methodName = extractJavaMethodName(template, "sumarPares");
                txtEditor.setText(
                        "public class " + className + " {\n"
                        + "    public static int " + methodName + "(int[] nums) {\n"
                        + "        int total = 0;\n"
                        + "        for (int n : nums) {\n"
                        + "            if (n % 2 == 0) total += n;\n"
                        + "        }\n"
                        + "        return total;\n"
                        + "    }\n"
                        + "}"
                );
            }
            return;
        }

        if (isFirstDuplicate) {
            if (isPython) {
                txtEditor.setText(
                        "def primer_duplicado(nums: list) -> int:\n"
                        + "    vistos = set()\n"
                        + "    for n in nums:\n"
                        + "        if n in vistos:\n"
                        + "            return n\n"
                        + "        vistos.add(n)\n"
                        + "    return -1\n"
                );
            } else if (isCpp) {
                txtEditor.setText(
                        "#include <vector>\n#include <unordered_set>\n\n"
                        + "int primerDuplicado(const std::vector<int>& nums) {\n"
                        + "    std::unordered_set<int> vistos;\n"
                        + "    for (int n : nums) {\n"
                        + "        if (vistos.count(n) > 0) return n;\n"
                        + "        vistos.insert(n);\n"
                        + "    }\n"
                        + "    return -1;\n"
                        + "}"
                );
            } else {
                String className = extractJavaClassName(template, "DuplicateFinderSolver");
                String methodName = extractJavaMethodName(template, "primerDuplicado");
                txtEditor.setText(
                        "import java.util.HashSet;\nimport java.util.Set;\n\n"
                        + "public class " + className + " {\n"
                        + "    public static int " + methodName + "(int[] nums) {\n"
                        + "        Set<Integer> vistos = new HashSet<>();\n"
                        + "        for (int n : nums) {\n"
                        + "            if (!vistos.add(n)) return n;\n"
                        + "        }\n"
                        + "        return -1;\n"
                        + "    }\n"
                        + "}"
                );
            }
            return;
        }

        if (isPython) {
            StringBuilder py = new StringBuilder();
            py.append("def emitir_sonido(tipo: str) -> str:\n");
            py.append("    clave = tipo.strip().lower()\n");
            for (TestCase tc : cases) {
                if (!tc.isHidden()) {
                    py.append("    if clave == \"")
                            .append(tc.getInput().trim().toLowerCase(Locale.ROOT))
                            .append("\":\n        return \"")
                            .append(tc.getExpectedOutput())
                            .append("\"\n");
                }
            }
            py.append("    return \"\"\n");
            txtEditor.setText(py.toString());
            return;
        }

        if (isCpp) {
            StringBuilder cpp = new StringBuilder();
            cpp.append("#include <string>\n\n");
            cpp.append("std::string emitirSonido(const std::string& input) {\n");
            for (TestCase tc : cases) {
                if (!tc.isHidden()) {
                    cpp.append("    if (input == \"")
                            .append(tc.getInput().trim())
                            .append("\") return \"")
                            .append(tc.getExpectedOutput())
                            .append("\";\n");
                }
            }
            cpp.append("    return \"\";\n}\n");
            txtEditor.setText(cpp.toString());
            return;
        }

        String className = extractJavaClassName(template, "Solution");
        String methodName = extractJavaMethodName(template, "solve");
        StringBuilder javaSol = new StringBuilder();
        javaSol.append("public class ").append(className).append(" {\n");
        javaSol.append("    public static String ").append(methodName).append("(String input) {\n");
        for (TestCase tc : cases) {
            if (!tc.isHidden()) {
                javaSol.append("        if (\"")
                        .append(tc.getInput().trim())
                        .append("\".equalsIgnoreCase(input)) {\n")
                        .append("            return \"")
                        .append(tc.getExpectedOutput())
                        .append("\";\n")
                        .append("        }\n");
            }
        }
        javaSol.append("        return \"\";\n");
        javaSol.append("    }\n");
        javaSol.append("}");
        txtEditor.setText(javaSol.toString());
    }

    private static String extractJavaClassName(String template, String fallback) {
        Matcher m = JAVA_CLASS_PATTERN.matcher(template);
        return m.find() ? m.group(1) : fallback;
    }

    private static String extractJavaMethodName(String template, String fallback) {
        Matcher m = JAVA_METHOD_PATTERN.matcher(template);
        while (m.find()) {
            String candidate = m.group(1);
            if (!"main".equals(candidate)) {
                return candidate;
            }
        }
        return fallback;
    }

    private void evaluateSubmission() {
        if (activeChallenge instanceof CodeChallenge) {
            CodeChallenge codeChallenge = (CodeChallenge) activeChallenge;
            String code = txtEditor.getText() != null ? txtEditor.getText() : "";

            SimulatedCodeRunner.ExecutionSummary summary = codeRunner.runAndValidate(codeChallenge, code);
            List<SimulatedCodeRunner.CaseOutcome> outcomes = summary.getCaseOutcomes();
            latestCaseOutcomes.clear();
            latestCaseOutcomes.addAll(outcomes);

            StringBuilder log = new StringBuilder();
            log.append("> [").append(summary.getExecutionModeDescription()).append("]\n");
            log.append("> Validando contra ")
                    .append(outcomes.size())
                    .append(" casos de prueba (Casos_Prueba)...\n");

            int firstFailedIdx = -1;
            for (int i = 0; i < outcomes.size(); i++) {
                SimulatedCodeRunner.CaseOutcome outcome = outcomes.get(i);
                Label badge = i < testCaseStatusBadges.size() ? testCaseStatusBadges.get(i) : null;
                VBox rowCard = i < testCaseRowCards.size() ? testCaseRowCards.get(i) : null;
                if (outcome.isPassed()) {
                    if (badge != null) {
                        badge.setText("APROBADO ✓");
                        badge.getStyleClass().setAll("badge-green");
                    }
                    if (rowCard != null) {
                        rowCard.getStyleClass().setAll("test-case-card-passed");
                    }
                    log.append("  [OK] ").append(outcome.getMessage()).append("\n");
                } else {
                    if (firstFailedIdx < 0) {
                        firstFailedIdx = i;
                    }
                    if (badge != null) {
                        badge.setText("FALLIDO ✗");
                        badge.getStyleClass().setAll("badge-rose");
                    }
                    if (rowCard != null) {
                        rowCard.getStyleClass().setAll("test-case-card-failed");
                    }
                    log.append("  [X]  ").append(outcome.getMessage()).append("\n");
                }
            }

            if (!outcomes.isEmpty()) {
                inspectTestCase(firstFailedIdx >= 0 ? firstFailedIdx : 0);
            }

            lblExecutionMetrics.setText(String.format("Tiempo: %.2f ms · Casos: %d/%d",
                    summary.getExecutionTimeSeconds() * 1000.0,
                    summary.getPassedCount(),
                    summary.getTotalCount()));

            PersistenceOutcome persistence = persistAttemptIfConnected(code, summary.isAllPassed());
            handleEvaluationOutcome(summary.isAllPassed(), summary.isSandboxedExecution(), log.toString(), persistence);

        } else if (activeChallenge instanceof QuizChallenge) {
            QuizChallenge quiz = (QuizChallenge) activeChallenge;
            if (selectedQuizIndex < 0) {
                lblResultBanner.getStyleClass().setAll("text-warning");
                lblResultBanner.setText("Selecciona una opción antes de validar el quiz.");
                return;
            }
            boolean correct = quiz.isCorrectOption(selectedQuizIndex);
            String chosenOption = quiz.getOptions().get(selectedQuizIndex);

            if (selectedQuizIndex < quizOptionCards.size()) {
                HBox chosenCard = quizOptionCards.get(selectedQuizIndex);
                chosenCard.getStyleClass().setAll(correct ? "quiz-option-card-correct" : "quiz-option-card-wrong");
                Theme.playPulse(chosenCard);
            }

            lblExecutionMetrics.setText("Tiempo: 2.00 ms");
            String msg = correct
                    ? "> Respuesta verificada: CORRECTA ✓\n> Opción elegida: \"" + chosenOption + "\"\n> Concepto dominado con éxito."
                    : "> Respuesta incorrecta ✗\n> Opción elegida: \"" + chosenOption + "\"\n> Usa 'Ver Pista Conceptual' o revisa la teoría de la lección.";

            lblCaseInspectorTitle.setText(correct ? "Veredicto Conceptual: CORRECTO ✓" : "Veredicto Conceptual: INCORRECTO ✗");
            lblCaseInspectorDetail.setText(correct
                    ? "Has identificado correctamente el fundamento técnico evaluado en este reto."
                    : "La opción seleccionada no corresponde al contrato esperado. Revisa la pista conceptual e inténtalo de nuevo.");

            PersistenceOutcome persistence = persistAttemptIfConnected(chosenOption, correct);
            handleEvaluationOutcome(correct, true, msg, persistence);
        }
    }

    private PersistenceOutcome persistAttemptIfConnected(String submittedPayload, boolean localSuccess) {
        AppContext ctx = AppContext.getInstance();
        boolean shouldSaveToOracle = ctx.isOracleConnected()
                && currentUser != null && currentUser.getId() > 0
                && activeChallenge != null && activeChallenge.getId() > 0;

        if (!shouldSaveToOracle) {
            if (!localSuccess && currentUser != null) {
                currentUser.loseLife();
            }
            return PersistenceOutcome.localOnly();
        }

        try {
            Attempt saved = ctx.getChallengeService().submitAttempt(currentUser, activeChallenge.getId(), submittedPayload);
            return PersistenceOutcome.saved(saved);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al persistir intento del reto #" + activeChallenge.getId() + " en Oracle", ex);
            String causeMsg = ex.getMessage() != null && !ex.getMessage().isBlank()
                    ? ex.getMessage()
                    : ex.getClass().getSimpleName();
            return PersistenceOutcome.failed(causeMsg);
        }
    }

    private void handleEvaluationOutcome(
            boolean localSuccess,
            boolean sandboxedValidation,
            String consoleMessage,
            PersistenceOutcome persistence) {

        StringBuilder fullConsole = new StringBuilder(consoleMessage);

        if (persistence.attemptedOracleSave && !persistence.savedToOracle) {
            fullConsole.append("\n[ERROR DE GUARDADO EN ORACLE] ").append(persistence.errorMessage).append("\n")
                    .append("> El intento NO fue persistido en la base de datos. Puedes volver a presionar el botón para reintentar.");
            txtConsoleOutput.setText(fullConsole.toString());
            btnSubmit.setText("Reintentar Envío y Guardado");

            if (localSuccess) {
                lblResultBanner.getStyleClass().setAll("text-warning");
                lblResultBanner.setGraphic(Theme.createIcon(Theme.IconKind.BOLT, 16, Theme.HEX_AMBER));
                lblResultBanner.setText("Aprobado localmente, pero NO guardado en Oracle: " + persistence.errorMessage);
            } else {
                lblResultBanner.getStyleClass().setAll("text-danger");
                lblResultBanner.setText("Intento fallido y error al registrar en Oracle: " + persistence.errorMessage);
            }
            return;
        }

        btnSubmit.setText("Ejecutar y Validar Solución");
        boolean effectiveSuccess = persistence.savedToOracle && persistence.attempt != null
                ? persistence.attempt.isCorrect()
                : localSuccess;

        if (persistence.savedToOracle) {
            fullConsole.append("> Persistencia: Intento #")
                    .append(persistence.attempt != null ? persistence.attempt.getId() : 0)
                    .append(" guardado transaccionalmente en Oracle (Intentos).\n");
        } else {
            fullConsole.append("> Persistencia: Evaluado en modo local sin conexión a Oracle.\n");
        }
        txtConsoleOutput.setText(fullConsole.toString());

        if (effectiveSuccess) {
            if (btnNextChallenge != null && availableChallenges.size() > 1) {
                btnNextChallenge.getStyleClass().setAll("btn-primary");
                Theme.playPulse(btnNextChallenge);
            }
            int reward = persistence.savedToOracle && persistence.attempt != null
                    ? persistence.attempt.getEarnedExperience()
                    : activeChallenge.getExpReward();

            String modeQualifier = sandboxedValidation
                    ? ""
                    : " (validación simulada de demostración)";

            if (!xpAwardedForCurrent && currentUser != null && reward > 0) {
                if (!persistence.savedToOracle) {
                    currentUser.addExperience(reward);
                    currentUser.setLevel(Math.max(1, 1 + currentUser.getTotalExp() / 300));
                }
                xpAwardedForCurrent = true;
                if (onSolvedCallback != null) {
                    onSolvedCallback.run();
                }
                lblResultBanner.getStyleClass().setAll("text-success");
                lblResultBanner.setGraphic(Theme.createIcon(Theme.IconKind.CHECK, 16, Theme.HEX_GREEN));
                String statusPrefix = persistence.savedToOracle
                        ? "¡Aprobado y guardado en Oracle!"
                        : "¡Aprobado localmente (modo demo)!";
                lblResultBanner.setText(statusPrefix + modeQualifier + " Se acreditaron +" + reward + " XP.");
            } else {
                if (onSolvedCallback != null) {
                    onSolvedCallback.run();
                }
                lblResultBanner.getStyleClass().setAll("text-success");
                lblResultBanner.setGraphic(Theme.createIcon(Theme.IconKind.CHECK, 16, Theme.HEX_GREEN));
                String statusPrefix = persistence.savedToOracle
                        ? "¡Solución aprobada y guardada en Oracle!"
                        : "¡Solución aprobada localmente!";
                lblResultBanner.setText(statusPrefix + modeQualifier);
            }
            Theme.playPulse(lblResultBanner);
        } else {
            if (onSolvedCallback != null) {
                onSolvedCallback.run();
            }
            lblResultBanner.getStyleClass().setAll("text-danger");
            String saveSuffix = persistence.savedToOracle
                    ? " (intento registrado en Oracle; se descontó 1 vida)."
                    : " (modo local; revisa la consola e inténtalo de nuevo).";
            lblResultBanner.setText("Algunos casos fallaron" + saveSuffix);
            Theme.playPulse(lblResultBanner);
        }
    }

    private static final class PersistenceOutcome {

        final boolean attemptedOracleSave;
        final boolean savedToOracle;
        final Attempt attempt;
        final String errorMessage;

        private PersistenceOutcome(boolean attemptedOracleSave, boolean savedToOracle, Attempt attempt, String errorMessage) {
            this.attemptedOracleSave = attemptedOracleSave;
            this.savedToOracle = savedToOracle;
            this.attempt = attempt;
            this.errorMessage = errorMessage;
        }

        static PersistenceOutcome localOnly() {
            return new PersistenceOutcome(false, false, null, null);
        }

        static PersistenceOutcome saved(Attempt attempt) {
            return new PersistenceOutcome(true, true, attempt, null);
        }

        static PersistenceOutcome failed(String errorMessage) {
            return new PersistenceOutcome(true, false, null, errorMessage);
        }
    }
}
