package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.business.evaluator.SimulatedCodeRunner;
import com.mycompany.levelup_code.business.service.ChallengeService;
import com.mycompany.levelup_code.data.local.ChallengeDraftStore;
import com.mycompany.levelup_code.domain.enums.AttemptResult;
import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.model.Attempt;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.CourseModule;
import com.mycompany.levelup_code.domain.model.Language;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Entorno interactivo de resolución de retos (Código y Quiz) en JavaFX + CSS ({@code /styles/levelup.css})
 * con numeración de líneas reactiva, casos de prueba visibles/ocultos y motor de evaluación
 * dinámica conectado a {@code Casos_Prueba} e {@code Intentos} en Oracle Database.
 */
public class ChallengeFrame extends Stage {

    private static final Logger LOGGER = Logger.getLogger(ChallengeFrame.class.getName());
    private static final DateTimeFormatter ATTEMPT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern JAVA_CLASS_PATTERN = Pattern.compile("(?:public\\s+)?class\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern JAVA_METHOD_PATTERN = Pattern.compile(
            "public\\s+static\\s+[A-Za-z0-9_<>\\[\\]]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^\\)]*)\\)");

    private final User currentUser;
    private final List<Challenge> availableChallenges;
    private final Runnable onSolvedCallback;
    private final SimulatedCodeRunner codeRunner = AppContext.getInstance().getCodeRunner();
    private final ChallengeDraftStore draftStore = new ChallengeDraftStore();
    private final PauseTransition draftSaveDelay = new PauseTransition(Duration.millis(900));

    private Challenge activeChallenge;
    private boolean xpAwardedForCurrent = false;
    private int selectedQuizIndex = -1;
    // Session-only drafts: challenge identity also keeps unsaved/demo IDs separate.
    private final Map<Challenge, Draft> drafts = new IdentityHashMap<>();
    private final Map<Challenge, Integer> hintStages = new IdentityHashMap<>();
    private final Map<Challenge, Long> draftRevisions = new IdentityHashMap<>();
    private final Object draftWriteLock = new Object();
    private long nextDraftRevision;
    private boolean viewLoaded;
    private boolean busy;
    private boolean saveUncertain;
    private String pendingRequestId;
    private String pendingPayloadHash;
    private int editorFontSize = 14;
    private ProgressIndicator busyIndicator;
    private Label lblBusy;
    private Label lblDraftStatus;
    private TitledPane consolePane;

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
    private Button btnHint;

    // Editor de código y numeración de líneas
    private TextArea txtEditor;
    private TextArea txtLineGutter;

    // Opciones de Quiz
    private VBox quizOptionsContainer;
    private final List<RadioButton> quizOptionCards = new ArrayList<>();

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
        draftSaveDelay.setOnFinished(event -> rememberDraft());
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
        SessionWindowRegistry.track(this);
        addEventHandler(WindowEvent.WINDOW_HIDING, event -> {
            draftSaveDelay.stop();
            rememberDraft();
        });
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
            toFront();
        } else {
            close();
        }
    }

    @Override
    public void close() {
        if (allowClose()) {
            super.close();
        }
    }

    private boolean allowClose() {
        if (!busy) {
            draftSaveDelay.stop();
            rememberDraft();
            return true;
        }
        showFeedback("Espera a que termine el envío antes de cerrar. El guardado sigue en curso.", "text-warning");
        return false;
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
        setMinWidth(560);
        setMinHeight(420);
        addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            if (!allowClose()) {
                event.consume();
            }
        });

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");
        root.setPadding(new Insets(18, 22, 18, 22));

        VBox topHeader = buildTopHeaderBar();
        SplitPane workspaceSplit = buildWorkspaceSplit();
        VBox content = new VBox(14, topHeader, workspaceSplit);
        ScrollPane contentScroll = new ScrollPane(content);
        contentScroll.setFitToWidth(true);
        contentScroll.getStyleClass().add("scroll-pane-clean");
        root.setCenter(contentScroll);

        VBox bottomBar = buildBottomActionBar();
        BorderPane.setMargin(bottomBar, new Insets(14, 0, 0, 0));
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 1140, 720);
        Runnable resizeWorkspace = () -> {
            boolean narrow = scene.getWidth() < 900;
            workspaceSplit.setOrientation(narrow ? Orientation.VERTICAL : Orientation.HORIZONTAL);
            workspaceSplit.setMinHeight(narrow ? 880 : 440);
            workspaceSplit.setPrefHeight(Math.max(narrow ? 880 : 440,
                    scene.getHeight() - topHeader.getHeight() - bottomBar.getHeight() - 70));
        };
        scene.widthProperty().addListener((obs, before, after) -> resizeWorkspace.run());
        scene.heightProperty().addListener((obs, before, after) -> resizeWorkspace.run());
        topHeader.heightProperty().addListener((obs, before, after) -> resizeWorkspace.run());
        resizeWorkspace.run();
        KeyCombination submitShortcut = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (submitShortcut.match(event)) {
                evaluateSubmission();
                event.consume();
            }
        });
        Theme.applyCss(scene);
        setScene(scene);
    }

    private VBox buildTopHeaderBar() {
        VBox headerCard = new VBox(8);
        headerCard.getStyleClass().add("surface-card");

        FlowPane topRow = new FlowPane(10, 8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        lblChallengeTitle = new Label();
        lblChallengeTitle.getStyleClass().add("page-title");
        lblChallengeTitle.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 20, Theme.HEX_INDIGO));
        lblChallengeTitle.setGraphicTextGap(8);
        lblChallengeTitle.setWrapText(true);
        lblChallengeTitle.setMaxWidth(Double.MAX_VALUE);

        badgeType = Theme.createPillBadge("CÓDIGO", "badge-indigo");
        badgeDifficulty = Theme.createDifficultyBadge(60);
        badgeExecutionMode = Theme.createPillBadge("EJECUCIÓN LOCAL JVM", "badge-green");
        badgeReward = Theme.createPillBadge("+60 XP", "badge-amber");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(badgeType, badgeDifficulty, badgeExecutionMode, badgeReward);
        if (currentUser != null && currentUser.getId() > 0
                && AppContext.getInstance().isOracleConnected()) {
            Button history = new Button("Ver historial");
            history.getStyleClass().add("btn-secondary");
            history.setOnAction(event -> showAttemptHistory());
            topRow.getChildren().add(history);
        }
        headerCard.getChildren().addAll(lblChallengeTitle, topRow);

        switcherButtons.clear();
        if (availableChallenges.size() > 1) {
            FlowPane switcher = new FlowPane(8, 8);
            switcher.setAlignment(Pos.CENTER_LEFT);
            Label lblSwitch = new Label("Retos de la lección:");
            lblSwitch.getStyleClass().add("muted-text");
            switcher.getChildren().add(lblSwitch);

            for (int i = 0; i < availableChallenges.size(); i++) {
                final Challenge ch = availableChallenges.get(i);
                String btnLabel = (ch.getType() == ChallengeType.QUIZ ? "Quiz #" : "Código #") + (i + 1);
                Button btnCh = new Button(btnLabel);
                btnCh.getStyleClass().add("btn-secondary");
                btnCh.setOnAction(e -> loadChallengeIntoView(ch));
                btnCh.setTooltip(new Tooltip(ch.getTitle()));
                switcherButtons.add(btnCh);
                switcher.getChildren().add(btnCh);
            }
            headerCard.getChildren().add(switcher);
        }

        lblStatement = new Label();
        lblStatement.getStyleClass().add("body-text");
        lblStatement.setWrapText(true);

        headerCard.getChildren().add(lblStatement);
        return headerCard;
    }

    private void showAttemptHistory() {
        if (activeChallenge == null || activeChallenge.getId() <= 0) return;
        int challengeId = activeChallenge.getId();
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.initOwner(this);
        dialog.setTitle("Historial de intentos");
        dialog.setHeaderText("Tus intentos en " + activeChallenge.getTitle());
        dialog.getButtonTypes().setAll(ButtonType.CLOSE);
        dialog.setResizable(true);

        Label status = new Label("Consultando intentos guardados…");
        status.getStyleClass().add("muted-text");
        VBox entries = new VBox(10);
        ScrollPane scroll = new ScrollPane(entries);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportWidth(540);
        scroll.setPrefViewportHeight(350);
        scroll.getStyleClass().add("scroll-pane-clean");
        Button refresh = new Button("Actualizar historial");
        refresh.getStyleClass().add("btn-secondary");
        VBox content = new VBox(12, status, scroll, refresh);
        dialog.getDialogPane().setContent(content);
        refresh.setOnAction(event -> loadAttemptHistory(challengeId, status, entries, refresh));
        dialog.show();
        Theme.applyCss(dialog.getDialogPane().getScene());
        loadAttemptHistory(challengeId, status, entries, refresh);
    }

    private void loadAttemptHistory(int challengeId, Label status, VBox entries, Button refresh) {
        refresh.setDisable(true);
        status.setText("Consultando intentos guardados…");
        Task<List<Attempt>> query = new Task<>() {
            @Override protected List<Attempt> call() {
                return AppContext.getInstance().getChallengeService()
                        .getAttemptHistory(currentUser.getId(), challengeId);
            }
        };
        query.setOnSucceeded(event -> {
            refresh.setDisable(false);
            entries.getChildren().clear();
            List<Attempt> attempts = new ArrayList<>(query.getValue());
            status.setText(attempts.isEmpty() ? "Todavía no hay intentos guardados para este reto."
                    : attempts.size() + " intento(s) reciente(s), del más reciente al más antiguo."
                    + (attempts.size() == ChallengeService.ATTEMPT_HISTORY_LIMIT
                            ? " Se muestran como máximo los últimos 50." : ""));
            for (Attempt attempt : attempts) {
                VBox card = new VBox(6);
                card.getStyleClass().add("surface-subtle");
                String date = attempt.getAttemptDate() == null ? "Fecha desconocida"
                        : ATTEMPT_DATE.format(attempt.getAttemptDate());
                Label summary = new Label(date + " · " + attemptResultLabel(attempt.getResult())
                        + " · +" + attempt.getEarnedExperience() + " XP"
                        + " · " + attempt.getExecutionTimeMs() + " ms");
                summary.setWrapText(true);
                summary.getStyleClass().add(attempt.isCorrect() ? "text-success" : "text-warning");
                TextArea submission = new TextArea(attempt.getSubmittedContent() == null
                        ? "" : attempt.getSubmittedContent());
                submission.setEditable(false);
                submission.setWrapText(true);
                submission.setPrefRowCount(4);
                submission.setAccessibleText("Respuesta enviada el " + date);
                card.getChildren().addAll(summary, submission);
                entries.getChildren().add(card);
            }
        });
        query.setOnFailed(event -> {
            LOGGER.log(Level.WARNING, "No se pudo cargar el historial de intentos", query.getException());
            refresh.setDisable(false);
            status.setText("No pudimos consultar el historial. Comprueba la conexión y pulsa Actualizar historial.");
        });
        Thread worker = new Thread(query, "challenge-attempt-history");
        worker.setDaemon(true);
        worker.start();
    }

    private static String attemptResultLabel(AttemptResult result) {
        if (result == null) return "Sin resultado";
        return switch (result) {
            case CORRECT -> "Correcto";
            case INCORRECT -> "Por corregir";
            case ERROR -> "Error de ejecución";
            case TIMEOUT -> "Tiempo agotado";
        };
    }

    private SplitPane buildWorkspaceSplit() {
        SplitPane split = new SplitPane();

        centerCardPane = new StackPane();
        codeEditorCard = buildCodeEditorCard();
        quizCard = buildQuizCard();
        centerCardPane.getChildren().setAll(codeEditorCard);

        VBox rightValidationPane = buildRightValidationPane();

        centerCardPane.setMinWidth(0);
        codeEditorCard.setMinWidth(0);
        quizCard.setMinWidth(0);
        centerCardPane.setPrefWidth(640);
        rightValidationPane.setPrefWidth(420);
        rightValidationPane.setMinWidth(0);

        split.getItems().addAll(centerCardPane, rightValidationPane);
        split.setDividerPositions(0.60);
        return split;
    }

    private VBox buildCodeEditorCard() {
        VBox editorCard = new VBox(10);
        editorCard.getStyleClass().add("surface-card");

        FlowPane toolbar = new FlowPane(10, 8);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label lblFileTab = new Label("Editor de Solución · UTF-8");
        lblFileTab.getStyleClass().add("section-caption");
        lblFileTab.setGraphic(Theme.createIcon(Theme.IconKind.TERMINAL, 15, Theme.HEX_INDIGO));
        lblFileTab.setGraphicTextGap(6);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnHint = new Button("Ver pista 1 de 3");
        btnHint.getStyleClass().add("btn-ghost-green");
        btnHint.setOnAction(e -> showPedagogicalHint());

        Button btnResetCode = new Button("Restaurar Plantilla");
        btnResetCode.getStyleClass().add("btn-secondary");
        btnResetCode.setOnAction(e -> {
            if (!busy && activeChallenge instanceof CodeChallenge && confirmCodeReplacement("Restaurar la plantilla")) {
                txtEditor.setText(((CodeChallenge) activeChallenge).getInitialTemplate());
            }
        });

        Button btnFontDown = new Button("A−");
        btnFontDown.getStyleClass().add("btn-ghost");
        btnFontDown.setTooltip(new Tooltip("Reducir tamaño del código"));
        btnFontDown.setOnAction(event -> adjustEditorFontSize(-1));

        Button btnFontUp = new Button("A+");
        btnFontUp.getStyleClass().add("btn-ghost");
        btnFontUp.setTooltip(new Tooltip("Aumentar tamaño del código"));
        btnFontUp.setOnAction(event -> adjustEditorFontSize(1));

        toolbar.getChildren().addAll(lblFileTab, btnHint, btnResetCode, btnFontDown, btnFontUp);

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
        txtEditor.setAccessibleText("Código de la solución");
        txtEditor.setAccessibleHelp("Ctrl+Enter para enviar. El borrador se conserva al cerrar y volver a abrir.");
        txtEditor.setMinWidth(0);
        txtEditor.setPrefRowCount(12);
        txtEditor.getStyleClass().add("code-editor");
        txtEditor.setStyle("-fx-font-size: " + editorFontSize + "px;");
        HBox.setHgrow(txtEditor, Priority.ALWAYS);

        txtEditor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && !event.isControlDown() && !event.isAltDown()) {
                txtEditor.insertText(txtEditor.getCaretPosition(), "    ");
                event.consume();
            }
        });

        txtEditor.textProperty().addListener((obs, oldVal, newVal) -> {
            updateLineGutter(newVal);
            scheduleDraftSave();
        });
        txtEditor.scrollTopProperty().addListener((obs, oldVal, newVal) -> txtLineGutter.setScrollTop(newVal.doubleValue()));

        editorContainer.getChildren().addAll(txtLineGutter, txtEditor);
        editorCard.getChildren().addAll(toolbar, editorContainer);
        return editorCard;
    }

    private void adjustEditorFontSize(int delta) {
        editorFontSize = Math.max(11, Math.min(24, editorFontSize + delta));
        txtEditor.setStyle("-fx-font-size: " + editorFontSize + "px;");
        txtEditor.setAccessibleHelp("Editor a " + editorFontSize
                + " píxeles. Tab inserta cuatro espacios; Ctrl+Enter envía.");
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

        FlowPane header = new FlowPane(10, 8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblQuizTitle = new Label("SELECCIONA LA RESPUESTA CORRECTA");
        lblQuizTitle.getStyleClass().add("section-caption");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnQuizHint = new Button("Ver Pista Conceptual");
        btnQuizHint.getStyleClass().add("btn-ghost-green");
        btnQuizHint.setOnAction(e -> showPedagogicalHint());
        Label badgeSingle = Theme.createPillBadge("Evaluación de selección única", "badge-violet");
        header.getChildren().addAll(lblQuizTitle, btnQuizHint, badgeSingle);

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
        scrollCases.setPrefViewportHeight(140);
        scrollCases.setMinHeight(80);
        VBox.setVgrow(scrollCases, Priority.ALWAYS);

        VBox inspectorCard = new VBox(4);
        inspectorCard.getStyleClass().add("surface-subtle");
        lblCaseInspectorTitle = new Label("Inspector de Caso de Prueba");
        lblCaseInspectorTitle.getStyleClass().add("card-title");
        lblCaseInspectorDetail = new Label("Haz clic en cualquier caso de prueba superior para ver su entrada, salida esperada y diagnóstico.");
        lblCaseInspectorDetail.getStyleClass().add("muted-text");
        lblCaseInspectorDetail.setWrapText(true);
        inspectorCard.getChildren().addAll(lblCaseInspectorTitle, lblCaseInspectorDetail);

        ScrollPane inspectorScroll = new ScrollPane(inspectorCard);
        inspectorScroll.setFitToWidth(true);
        inspectorScroll.setPrefViewportHeight(100);
        inspectorScroll.setMinHeight(70);
        inspectorScroll.getStyleClass().add("scroll-pane-clean");
        casesCard.getChildren().addAll(casesHeader, scrollCases, inspectorScroll);

        // Tarjeta inferior: Consola de salida y métricas de tiempo
        VBox consoleCard = new VBox(8);
        consoleCard.getStyleClass().add("surface-card");
        consoleCard.setPrefHeight(205);

        FlowPane consoleHeader = new FlowPane(8, 6);
        consoleHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblConsoleTitle = new Label("SALIDA DE CONSOLA / VEREDICTO");
        lblConsoleTitle.getStyleClass().add("section-caption");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblExecutionMetrics = new Label("Tiempo: -- ms");
        lblExecutionMetrics.getStyleClass().add("muted-text");

        Button copyConsole = new Button("Copiar detalles");
        copyConsole.getStyleClass().add("btn-secondary");
        copyConsole.setOnAction(event -> {
            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString(txtConsoleOutput.getText());
            Clipboard.getSystemClipboard().setContent(clipboard);
        });
        consoleHeader.getChildren().addAll(lblExecutionMetrics, copyConsole);

        txtConsoleOutput = new TextArea();
        txtConsoleOutput.setEditable(false);
        txtConsoleOutput.setWrapText(true);
        txtConsoleOutput.setAccessibleText("Detalles de evaluación y guardado; texto seleccionable");
        txtConsoleOutput.setPrefRowCount(6);
        txtConsoleOutput.setMinHeight(70);
        txtConsoleOutput.getStyleClass().add("console-output");
        txtConsoleOutput.setText("> Listo para validar solución.\n> Presiona 'Ejecutar y Validar Solución' para correr las pruebas.");
        VBox.setVgrow(txtConsoleOutput, Priority.ALWAYS);

        consoleCard.getChildren().addAll(consoleHeader, txtConsoleOutput);

        consolePane = new TitledPane("Consola y detalles", consoleCard);
        consolePane.setExpanded(false);
        consolePane.setAnimated(false);
        rightColumn.getChildren().addAll(casesCard, consolePane);
        return rightColumn;
    }

    private VBox buildBottomActionBar() {
        VBox bottomBar = new VBox(8);
        bottomBar.getStyleClass().add("surface-card");
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        lblResultBanner = new Label("Completa el reto para ganar experiencia y mantener tu racha activa.");
        lblResultBanner.getStyleClass().add("body-text");
        lblResultBanner.setGraphic(Theme.createIcon(Theme.IconKind.BOLT, 16, Theme.HEX_AMBER));
        lblResultBanner.setGraphicTextGap(8);
        lblResultBanner.setWrapText(true);
        lblResultBanner.setMaxWidth(Double.MAX_VALUE);

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

        btnSubmit = new Button("Enviar solución");
        btnSubmit.setTooltip(new Tooltip("Ejecutar y guardar · Ctrl+Enter"));
        btnSubmit.getStyleClass().add("btn-success");
        btnSubmit.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_WHITE));
        btnSubmit.setGraphicTextGap(6);
        btnSubmit.setOnAction(e -> evaluateSubmission());

        busyIndicator = new ProgressIndicator();
        busyIndicator.setPrefSize(20, 20);
        busyIndicator.setMaxSize(20, 20);
        lblBusy = new Label();
        lblBusy.getStyleClass().add("muted-text");
        busyIndicator.setVisible(false);
        busyIndicator.setManaged(false);
        lblBusy.setVisible(false);
        lblBusy.setManaged(false);
        lblDraftStatus = new Label("Guardado automático activo");
        lblDraftStatus.getStyleClass().add("muted-text");
        lblDraftStatus.setAccessibleText("Estado del borrador local");
        FlowPane actions = new FlowPane(10, 8, btnClose, btnNextChallenge, btnSubmit,
                new Label("Ctrl+Enter"), busyIndicator, lblBusy, lblDraftStatus);
        bottomBar.getChildren().addAll(lblResultBanner, actions);
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
        if (busy || (viewLoaded && activeChallenge == challenge)) {
            return;
        }
        draftSaveDelay.stop();
        rememberDraft();
        viewLoaded = false;
        this.activeChallenge = challenge;
        Draft draft = drafts.get(challenge);
        boolean outdatedDraftSkipped = false;
        if (draft == null && canPersistDraft(challenge)) {
            try {
                ChallengeDraftStore.DraftData saved = draftStore
                        .load(draftMode(), currentUser.getEmail(), challenge.getId())
                        .orElse(null);
                if (saved != null && saved.challengeFingerprint() != null
                        && !saved.challengeFingerprint().equals(challengeFingerprint(challenge))
                        && !confirmOutdatedDraftRecovery()) {
                    saved = null;
                    outdatedDraftSkipped = true;
                }
                if (saved != null) {
                    draft = new Draft(saved.code(), saved.quizIndex(), saved.caret(),
                            saved.scrollTop(), false, saved.saveUncertain(),
                            saved.pendingRequestId(), saved.pendingPayloadHash());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "No se pudo leer el borrador del reto", ex);
            }
        }
        this.saveUncertain = draft != null && draft.saveUncertain();
        this.pendingRequestId = draft != null ? draft.pendingRequestId() : null;
        this.pendingPayloadHash = draft != null ? draft.pendingPayloadHash() : null;
        this.xpAwardedForCurrent = draft != null && draft.xpAwarded();
        this.selectedQuizIndex = draft != null ? draft.quizIndex() : -1;
        int revealedHints = hintStages.getOrDefault(challenge, 0);
        btnHint.setText(revealedHints >= 3 ? "Pistas completas"
                : "Ver pista " + (revealedHints + 1) + " de 3");
        if (btnSubmit != null) {
            btnSubmit.setText(isPracticeOnly()
                    ? "Practicar sin XP" : "Enviar solución");
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
                    ? "EJECUCIÓN LOCAL JVM"
                    : "MODO DEMO SIMULADO (" + langName.toUpperCase(Locale.ROOT) + ")");
            badgeExecutionMode.getStyleClass().setAll(realJvm ? "badge-green" : "badge-amber");
            badgeExecutionMode.setVisible(true);
            badgeExecutionMode.setManaged(true);

            centerCardPane.getChildren().setAll(codeEditorCard);
            String initial = codeChallenge.getInitialTemplate() != null ? codeChallenge.getInitialTemplate() : "";
            txtEditor.setText(draft != null ? draft.code() : initial);
            if (draft != null) {
                txtEditor.positionCaret(Math.min(draft.caret(), txtEditor.getLength()));
                txtEditor.setScrollTop(draft.scrollTop());
            }

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
            if (selectedQuizIndex >= 0 && selectedQuizIndex < quizOptionCards.size()) {
                selectQuizOption(selectedQuizIndex);
            }

            VBox infoBox = new VBox(6);
            infoBox.getStyleClass().add("surface-subtle");
            Label lblQuizInfo = new Label("Elige una opción con Tab y Espacio o las flechas. Envía con Ctrl+Enter.");
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
                        ? "> Motor activo: Compilación JavaCompiler + subproceso JVM local (-Xmx64m)."
                        : "> Aviso: Este lenguaje se evalúa en modo simulación académica de demostración (sin compilador nativo externo).")
                : "> Motor activo: Verificación conceptual de selección única.";
        txtConsoleOutput.setText("> Reto cargado: " + challenge.getTitle() + "\n" + modeNote + "\n> Esperando envío del estudiante...");
        lblExecutionMetrics.setText("Tiempo: -- ms");
        consolePane.setExpanded(false);
        showFeedback(isPracticeOnly()
                ? practiceModeMessage()
                : outdatedDraftSkipped ? "El reto cambió. Se cargó su contenido actualizado y el borrador anterior se conservó en este equipo."
                : draft != null ? "Borrador recuperado. Puedes continuar y enviar tu respuesta."
                : "Resuelve el reto y envía tu respuesta. Tu borrador se guarda en este equipo.", "body-text");
        if (saveUncertain && !isPracticeOnly()) {
            btnSubmit.setText("Volver a enviar");
            showFeedback("El guardado anterior sigue sin confirmar. Puedes reintentar con seguridad: Oracle no duplicará el mismo envío.", "text-warning");
        }
        lblDraftStatus.setText(draft != null ? "Borrador recuperado" : "Guardado automático activo");
        viewLoaded = true;
    }

    private void rememberDraft() {
        if (viewLoaded) {
            Draft draft = new Draft(txtEditor.getText(), selectedQuizIndex,
                    txtEditor.getCaretPosition(), txtEditor.getScrollTop(), xpAwardedForCurrent,
                    saveUncertain, pendingRequestId, pendingPayloadHash);
            drafts.put(activeChallenge, draft);
            if (canPersistDraft(activeChallenge)) {
                Challenge challengeToSave = activeChallenge;
                String mode = draftMode();
                String email = currentUser.getEmail();
                int challengeId = challengeToSave.getId();
                long revision = ++nextDraftRevision;
                draftRevisions.put(challengeToSave, revision);
                ChallengeDraftStore.DraftData data = new ChallengeDraftStore.DraftData(
                        draft.code(), draft.quizIndex(), draft.caret(), draft.scrollTop(),
                        draft.saveUncertain(), draft.pendingRequestId(), draft.pendingPayloadHash(),
                        challengeFingerprint(challengeToSave));
                lblDraftStatus.setText("Guardando borrador…");
                Thread writer = new Thread(() -> saveDraftInBackground(
                        challengeToSave, revision, mode, email, challengeId, data),
                        "challenge-draft-save-" + challengeId);
                // Keep the short write alive during application shutdown so the
                // latest answer is not lost after the window disappears.
                writer.setDaemon(false);
                writer.start();
            }
        }
    }

    private void saveDraftInBackground(Challenge challenge, long revision, String mode,
            String email, int challengeId, ChallengeDraftStore.DraftData data) {
        try {
            synchronized (draftWriteLock) {
                Long latest = draftRevisions.get(challenge);
                if (latest == null || latest != revision) return;
                draftStore.save(mode, email, challengeId, data);
            }
            Platform.runLater(() -> {
                if (activeChallenge == challenge && revision == draftRevisions.getOrDefault(challenge, -1L)) {
                    lblDraftStatus.setText("Borrador guardado en este equipo");
                }
            });
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "No se pudo guardar el borrador del reto", ex);
            Platform.runLater(() -> {
                if (activeChallenge == challenge) {
                    lblDraftStatus.setText("No se pudo guardar el borrador");
                    lblDraftStatus.getStyleClass().setAll("text-warning");
                }
            });
        }
    }

    private boolean canPersistDraft(Challenge challenge) {
        return currentUser != null && currentUser.getEmail() != null
                && !currentUser.getEmail().isBlank() && challenge != null && challenge.getId() > 0;
    }

    private String draftMode() {
        return AppContext.getInstance().isOracleConnected() ? "oracle" : "demo";
    }

    private boolean confirmOutdatedDraftRecovery() {
        ButtonType recover = new ButtonType("Recuperar mi respuesta", ButtonBar.ButtonData.OK_DONE);
        ButtonType useCurrent = new ButtonType("Usar contenido actualizado", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "El contenido de este reto cambió desde que guardaste tu borrador. "
                + "Puedes recuperar tu respuesta para copiarla o empezar con la versión actual.",
                recover, useCurrent);
        alert.initOwner(this);
        alert.setTitle("El reto fue actualizado");
        alert.setHeaderText("Tu borrador pertenece a una versión anterior");
        return alert.showAndWait().orElse(useCurrent) == recover;
    }

    private String challengeFingerprint(Challenge challenge) {
        StringBuilder content = new StringBuilder();
        appendFingerprintField(content, challenge.getType());
        appendFingerprintField(content, challenge.getTitle());
        appendFingerprintField(content, challenge.getDescription());
        if (challenge instanceof CodeChallenge code) {
            appendFingerprintField(content, code.getInitialTemplate());
            appendFingerprintField(content, code.getTimeLimitSeconds());
            appendFingerprintField(content, resolveChallengeLanguageName(code));
            for (TestCase testCase : code.getTestCases()) {
                appendFingerprintField(content, testCase.getInput());
                appendFingerprintField(content, testCase.getExpectedOutput());
                appendFingerprintField(content, testCase.isHidden());
            }
        } else if (challenge instanceof QuizChallenge quiz) {
            for (String option : quiz.getOptions()) {
                appendFingerprintField(content, option);
            }
            appendFingerprintField(content, quiz.getCorrectAnswer());
        }
        return hashPayload(content.toString());
    }

    private static void appendFingerprintField(StringBuilder target, Object value) {
        String text = value == null ? "<null>" : value.toString();
        target.append(text.length()).append(':').append(text).append(';');
    }

    private void scheduleDraftSave() {
        if (viewLoaded && !busy) {
            lblDraftStatus.getStyleClass().setAll("muted-text");
            lblDraftStatus.setText("Cambios pendientes de guardar…");
            draftSaveDelay.playFromStart();
        }
    }

    private record Draft(String code, int quizIndex, int caret, double scrollTop,
            boolean xpAwarded, boolean saveUncertain, String pendingRequestId,
            String pendingPayloadHash) { }

    private void showFeedback(String message, String style) {
        lblResultBanner.setGraphic(null);
        lblResultBanner.getStyleClass().setAll(style);
        lblResultBanner.setText(message);
    }

    private boolean confirmCodeReplacement(String action) {
        if (busy) {
            return false;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(this);
        confirmation.setTitle(action);
        confirmation.setHeaderText("Se reemplazará el código de este reto");
        confirmation.setContentText("Puedes cancelar para conservar tu borrador. Esta acción reemplazará todo el contenido del editor.");
        ButtonType replace = new ButtonType("Reemplazar código");
        confirmation.getButtonTypes().setAll(ButtonType.CANCEL, replace);
        Theme.applyCss(confirmation.getDialogPane().getScene());
        return confirmation.showAndWait().filter(replace::equals).isPresent();
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
        lblCase.setWrapText(true);
        lblCase.setMinWidth(0);
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
        lblDetail.setWrapText(true);
        lblDetail.getStyleClass().add("muted-text");

        row.getChildren().addAll(top, lblDetail);
        row.setOnMouseClicked(e -> inspectTestCase(zeroBasedIndex));
        row.setFocusTraversable(true);
        row.setAccessibleText(titleText);
        row.setAccessibleHelp("Enter o Espacio para inspeccionar este caso");
        row.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                inspectTestCase(zeroBasedIndex);
                event.consume();
            }
        });
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
        if (busy) {
            return;
        }
        String title = activeChallenge != null && activeChallenge.getTitle() != null
                ? activeChallenge.getTitle().toLowerCase(Locale.ROOT)
                : "";
        int stage = Math.min(3, hintStages.getOrDefault(activeChallenge, 0) + 1);
        hintStages.put(activeChallenge, stage);
        btnHint.setText(stage >= 3 ? "Pistas completas" : "Ver pista " + (stage + 1) + " de 3");
        String hint;
        if (activeChallenge instanceof QuizChallenge) {
            String lessonTheory = (activeChallenge.getLesson() != null && activeChallenge.getLesson().getTheory() != null)
                    ? activeChallenge.getLesson().getTheory()
                    : "Revisa el concepto clave de la lección antes de elegir una opción.";
            hint = stage == 1
                    ? "> [PISTA 1 · CONCEPTO]\n> Identifica la definición principal que pregunta el enunciado antes de mirar las opciones."
                    : stage == 2
                            ? "> [PISTA 2 · DESCARTE]\n> Compara cada opción con el contrato explicado en la lección y descarta las que usan afirmaciones absolutas o conceptos distintos."
                            : "> [PISTA 3 · REPASO]\n" + lessonTheory;
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
        if (!(activeChallenge instanceof QuizChallenge)) {
            String[] lines = hint.split("\n");
            int visibleLines = Math.min(lines.length, stage + 1);
            hint = String.join("\n", Arrays.copyOf(lines, visibleLines));
        }
        txtConsoleOutput.setText(hint);
        consolePane.setExpanded(true);
        Theme.playPulse(txtConsoleOutput);
    }

    private void populateQuizOptions(QuizChallenge quiz) {
        quizOptionsContainer.getChildren().clear();
        quizOptionCards.clear();

        List<String> opts = quiz.getOptions();
        ToggleGroup optionsGroup = new ToggleGroup();
        char letter = 'A';
        for (int i = 0; i < opts.size(); i++) {
            final int optionIdx = i;
            String text = opts.get(i);

            RadioButton optionCard = new RadioButton(letter + ")   " + text);
            optionCard.setToggleGroup(optionsGroup);
            optionCard.setWrapText(true);
            optionCard.setMaxWidth(Double.MAX_VALUE);
            optionCard.setMinWidth(0);
            optionCard.setAccessibleHelp("Espacio para seleccionar; flechas para cambiar de opción; Ctrl+Enter para enviar");
            optionCard.setAlignment(Pos.CENTER_LEFT);
            optionCard.getStyleClass().add("quiz-option-card");

            optionCard.setOnAction(e -> selectQuizOption(optionIdx));
            optionCard.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.LEFT
                        || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.RIGHT) {
                    int direction = event.getCode() == KeyCode.UP || event.getCode() == KeyCode.LEFT ? -1 : 1;
                    int next = Math.floorMod(optionIdx + direction, quizOptionCards.size());
                    selectQuizOption(next);
                    quizOptionCards.get(next).requestFocus();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER && !event.isControlDown()) {
                    selectQuizOption(optionIdx);
                    event.consume();
                }
            });

            quizOptionCards.add(optionCard);
            quizOptionsContainer.getChildren().add(optionCard);
            letter++;
        }
        optionsGroup.selectedToggleProperty().addListener((obs, previous, selected) -> {
            int index = quizOptionCards.indexOf(selected);
            if (index >= 0 && index != selectedQuizIndex) {
                selectQuizOption(index);
            }
        });
    }

    private void selectQuizOption(int index) {
        if (busy || index < 0 || index >= quizOptionCards.size()) {
            return;
        }
        this.selectedQuizIndex = index;
        for (int i = 0; i < quizOptionCards.size(); i++) {
            RadioButton card = quizOptionCards.get(i);
            card.setSelected(i == index);
            card.getStyleClass().setAll("radio-button", i == index ? "quiz-option-card-selected" : "quiz-option-card");
        }
        showFeedback("Respuesta seleccionada. Envía con Ctrl+Enter o con el botón.", "body-text");
        scheduleDraftSave();
    }

    /**
     * Construye dinámicamente una solución de referencia a partir de la plantilla inicial
     * y los casos de prueba registrados en la base de datos para este reto.
     */
    private void fillReferenceSolution() {
        if (!(activeChallenge instanceof CodeChallenge) || !confirmCodeReplacement("Insertar solución modelo")) {
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
        if (busy) {
            return;
        }
        if (saveUncertain && !isPracticeOnly()) {
            Alert retry = new Alert(Alert.AlertType.CONFIRMATION);
            retry.initOwner(this);
            retry.setTitle("Confirmar nuevo envío");
            retry.setHeaderText("El guardado anterior no pudo confirmarse");
            retry.setContentText("Si mantienes la misma respuesta, Oracle comprobará la clave del envío y devolverá el intento existente sin duplicarlo.");
            ButtonType resend = new ButtonType("Comprobar y reintentar");
            retry.getButtonTypes().setAll(ButtonType.CANCEL, resend);
            Theme.applyCss(retry.getDialogPane().getScene());
            if (retry.showAndWait().filter(resend::equals).isEmpty()) {
                return;
            }
        }
        if (activeChallenge instanceof QuizChallenge quiz
                && (selectedQuizIndex < 0 || selectedQuizIndex >= quiz.getOptions().size())) {
            showFeedback("Selecciona una opción antes de enviar.", "text-warning");
            if (!quizOptionCards.isEmpty()) {
                quizOptionCards.get(0).requestFocus();
            }
            return;
        }
        if (activeChallenge instanceof CodeChallenge && txtEditor.getText().isBlank()) {
            showFeedback("Escribe tu solución antes de enviar.", "text-warning");
            txtEditor.requestFocus();
            return;
        }

        // Capture all submission data on FX. The worker never reads controls, the
        // active challenge, or the shared session user, even while Oracle saves.
        final Submission submission = captureSubmission();
        rememberDraft();
        Task<EvaluationOutcome> task = new Task<>() {
            @Override
            protected EvaluationOutcome call() {
                updateMessage(submission.saveToOracle()
                        ? "Evaluando y guardando en Oracle…" : "Evaluando respuesta…");
                if (submission.saveToOracle()) {
                    try {
                        ChallengeService.SubmissionResult saved = AppContext.getInstance()
                                .getChallengeService().submitAttemptDetailed(
                                        submission.user().toUser(), submission.challengeId(),
                                        submission.payload(), submission.requestId());
                        SimulatedCodeRunner.ExecutionSummary detail = saved.evaluation().getExecutionSummary();
                        return new EvaluationOutcome(detail, saved.attempt().isCorrect(),
                                PersistenceOutcome.saved(saved.attempt()));
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error al evaluar o guardar el reto #" + submission.challengeId(), ex);
                        SimulatedCodeRunner.ExecutionSummary local = submission.code() == null ? null
                                : codeRunner.runAndValidate(submission.code().toChallenge(), submission.payload());
                        boolean correct = local != null ? local.isAllPassed()
                                : submission.quizIndex() == submission.correctQuizIndex()
                                || (submission.correctAnswer() != null
                                && submission.correctAnswer().trim().equalsIgnoreCase(submission.payload().trim()));
                        return new EvaluationOutcome(local, correct,
                                PersistenceOutcome.failed(exceptionDetails(ex)));
                    }
                }
                SimulatedCodeRunner.ExecutionSummary summary = submission.code() == null ? null
                        : codeRunner.runAndValidate(submission.code().toChallenge(), submission.payload());
                boolean correct = summary != null ? summary.isAllPassed()
                        : submission.quizIndex() == submission.correctQuizIndex()
                        || (submission.correctAnswer() != null
                        && submission.correctAnswer().trim().equalsIgnoreCase(submission.payload().trim()));
                updateMessage("Preparando resultado…");
                return new EvaluationOutcome(summary, correct, PersistenceOutcome.localOnly());
            }
        };
        setBusy(true);
        lblBusy.textProperty().bind(task.messageProperty());
        showFeedback(submission.practiceOnly()
                ? "Práctica en curso: no se guardará ni otorgará XP."
                : "Envío en curso. Espera el resultado antes de cambiar de reto o cerrar.", "body-text");
        task.setOnSucceeded(event -> {
            try {
                renderEvaluation(submission, task.getValue());
                rememberDraft();
            } finally {
                finishSubmission();
            }
        });
        task.setOnFailed(event -> {
            if (submission.saveToOracle()) saveUncertain = true;
            LOGGER.log(Level.SEVERE, "Error al evaluar el reto #" + submission.challengeId(), task.getException());
            txtConsoleOutput.setText(exceptionDetails(task.getException()));
            consolePane.setExpanded(true);
            showFeedback(saveUncertain ? "Envío sin confirmar. Puedes comprobarlo y reintentarlo sin duplicar el intento."
                    : "No se pudo evaluar. Conservamos tu borrador; revisa los detalles.", "text-warning");
            rememberDraft();
            finishSubmission();
        });
        // Do not cancel/interrupt JDBC on close or abandon a save on JVM shutdown.
        // The close button, window decoration and setVisible(false) share the guard.
        Thread worker = new Thread(task, "challenge-submit-" + submission.challengeId());
        worker.setDaemon(false);
        try {
            worker.start();
        } catch (RuntimeException ex) {
            finishSubmission();
            txtConsoleOutput.setText(exceptionDetails(ex));
            consolePane.setExpanded(true);
            showFeedback("No se pudo iniciar el envío. Inténtalo de nuevo.", "text-warning");
        }
    }

    private void setBusy(boolean value) {
        busy = value;
        btnSubmit.setDisable(value);
        btnNextChallenge.setDisable(value);
        switcherButtons.forEach(button -> button.setDisable(value));
        codeEditorCard.setDisable(value);
        quizCard.setDisable(value);
        busyIndicator.setVisible(value);
        busyIndicator.setManaged(value);
        lblBusy.setVisible(value);
        lblBusy.setManaged(value);
    }

    private void finishSubmission() {
        lblBusy.textProperty().unbind();
        lblBusy.setText("");
        setBusy(false);
    }

    private Submission captureSubmission() {
        CodeSnapshot code = null;
        String payload;
        String correctAnswer = null;
        int correctQuizIndex = -1;
        if (activeChallenge instanceof CodeChallenge challenge) {
            List<CaseSnapshot> cases = challenge.getTestCases().stream()
                    .map(tc -> new CaseSnapshot(tc.getId(), tc.getInput(), tc.getExpectedOutput(), tc.isHidden()))
                    .toList();
            code = new CodeSnapshot(challenge.getId(), challenge.getTitle(), challenge.getDescription(),
                    challenge.getExpReward(), challenge.getInitialTemplate(), challenge.getTimeLimitSeconds(),
                    resolveChallengeLanguageName(challenge), cases);
            payload = txtEditor.getText();
        } else if (activeChallenge instanceof QuizChallenge quiz) {
            payload = quiz.getOptions().get(selectedQuizIndex);
            correctAnswer = quiz.getCorrectAnswer();
            correctQuizIndex = quiz.getCorrectOptionIndex();
        } else {
            throw new IllegalStateException("Tipo de reto no compatible.");
        }
        UserSnapshot user = currentUser == null ? null : new UserSnapshot(currentUser.getId(),
                currentUser.getExperience(), currentUser.getLevel(), currentUser.getLives(),
                currentUser.getCurrentStreak(), currentUser.getMaxStreak());
        boolean practiceOnly = isPracticeOnly();
        boolean saveToOracle = !practiceOnly && AppContext.getInstance().isOracleConnected()
                && user != null && user.id() > 0 && activeChallenge.getId() > 0;
        String requestId = null;
        if (saveToOracle) {
            String payloadHash = hashPayload(payload);
            boolean samePendingSubmission = saveUncertain && pendingRequestId != null
                    && payloadHash.equals(pendingPayloadHash);
            requestId = samePendingSubmission ? pendingRequestId : UUID.randomUUID().toString();
            pendingRequestId = requestId;
            pendingPayloadHash = payloadHash;
            // Persist this state before the network operation. If the app closes
            // before receiving Oracle's response, the same key is reused later.
            saveUncertain = true;
        }
        return new Submission(activeChallenge.getId(), payload, code, selectedQuizIndex,
                correctQuizIndex, correctAnswer, user, saveToOracle, practiceOnly, requestId);
    }

    private record Submission(int challengeId, String payload, CodeSnapshot code, int quizIndex,
            int correctQuizIndex, String correctAnswer, UserSnapshot user,
            boolean saveToOracle, boolean practiceOnly, String requestId) { }

    private static String hashPayload(String payload) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible.", ex);
        }
    }

    private boolean isPracticeOnly() {
        AppContext context = AppContext.getInstance();
        return (currentUser != null && currentUser.getLives() <= 0)
                || (context.isOracleConnected() && !context.isOracleAvailable())
                || isSimulatedCodeEvaluation();
    }

    private boolean isSimulatedCodeEvaluation() {
        return activeChallenge instanceof CodeChallenge
                && !codeRunner.supportsRealIsolatedExecution(
                        resolveChallengeLanguageName(activeChallenge));
    }

    private String practiceModeMessage() {
        if (isSimulatedCodeEvaluation()) {
            return "Práctica simulada: puedes probar tu idea, pero este resultado no modificará XP ni vidas hasta contar con ejecución real para este lenguaje.";
        }
        if (currentUser != null && currentUser.getLives() <= 0) {
            return "Modo práctica: no tienes vidas disponibles. Puedes seguir aprendiendo sin guardar ni recibir XP.";
        }
        return "Modo práctica: Oracle no está disponible. Puedes continuar sin guardar ni recibir XP.";
    }

    private String practiceSuccessMessage() {
        if (isSimulatedCodeEvaluation()) {
            return "¡Práctica simulada completada! El resultado no modificó XP ni vidas.";
        }
        if (currentUser != null && currentUser.getLives() <= 0) {
            return "¡Práctica aprobada! El intento no se guardó ni dio XP porque no había vidas disponibles.";
        }
        return "¡Práctica aprobada! El intento no se guardó ni dio XP porque Oracle no estaba disponible.";
    }

    private record CaseSnapshot(int id, String input, String expected, boolean hidden) { }

    private record CodeSnapshot(int id, String title, String description, int reward, String template,
            int timeout, String language, List<CaseSnapshot> cases) {
        private CodeSnapshot {
            cases = List.copyOf(cases);
        }

        CodeChallenge toChallenge() {
            Language detachedLanguage = new Language();
            detachedLanguage.setName(language);
            CourseModule module = new CourseModule();
            module.setLanguage(detachedLanguage);
            Lesson lesson = new Lesson();
            lesson.setModule(module);
            CodeChallenge challenge = new CodeChallenge(id, title, description, lesson, reward, template, timeout);
            for (CaseSnapshot tc : cases) {
                challenge.addTestCase(new TestCase(tc.id(), challenge, tc.input(), tc.expected(), tc.hidden()));
            }
            return challenge;
        }
    }

    private record UserSnapshot(int id, int experience, int level, int lives, int streak, int maxStreak) {
        User toUser() {
            User user = new User();
            user.setId(id);
            user.setExperience(experience);
            user.setLevel(level);
            user.setLives(lives);
            user.setCurrentStreak(streak);
            user.setMaxStreak(maxStreak);
            return user;
        }
    }

    private record EvaluationOutcome(SimulatedCodeRunner.ExecutionSummary summary, boolean correct,
            PersistenceOutcome persistence) { }

    private void renderEvaluation(Submission submission, EvaluationOutcome result) {
        if (result.persistence().savedToOracle && currentUser != null) {
            User savedUser = result.persistence().attempt.getUser();
            currentUser.setExperience(savedUser.getExperience());
            currentUser.setLevel(savedUser.getLevel());
            currentUser.setLives(savedUser.getLives());
            currentUser.setCurrentStreak(savedUser.getCurrentStreak());
            currentUser.setMaxStreak(savedUser.getMaxStreak());
        } else if (!submission.practiceOnly() && !result.persistence().attemptedOracleSave
                && !result.correct() && currentUser != null) {
            currentUser.loseLife();
        }
        if (submission.code() != null) {

            SimulatedCodeRunner.ExecutionSummary summary = result.summary();
            if (summary == null) {
                lblExecutionMetrics.setText("Evaluación sin detalle de casos");
                handleEvaluationOutcome(result.correct(), false,
                        "> No se recibió el detalle de los casos de prueba.\n", result.persistence(),
                        submission.practiceOnly());
                return;
            }
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

            handleEvaluationOutcome(summary.isAllPassed(), summary.isSandboxedExecution(), log.toString(),
                    result.persistence(), submission.practiceOnly());

        } else {
            boolean correct = result.persistence().savedToOracle
                    ? result.persistence().attempt.isCorrect() : result.correct();
            String chosenOption = submission.payload();

            if (submission.quizIndex() < quizOptionCards.size()) {
                RadioButton chosenCard = quizOptionCards.get(submission.quizIndex());
                chosenCard.getStyleClass().setAll("radio-button", correct ? "quiz-option-card-correct" : "quiz-option-card-wrong");
                Theme.playPulse(chosenCard);
            }

            lblExecutionMetrics.setText("Respuesta evaluada");
            String msg = correct
                    ? "> Respuesta verificada: CORRECTA ✓\n> Opción elegida: \"" + chosenOption + "\"\n> Concepto dominado con éxito."
                    : "> Respuesta incorrecta ✗\n> Opción elegida: \"" + chosenOption + "\"\n> Usa 'Ver Pista Conceptual' o revisa la teoría de la lección.";

            lblCaseInspectorTitle.setText(correct ? "Veredicto Conceptual: CORRECTO ✓" : "Veredicto Conceptual: INCORRECTO ✗");
            lblCaseInspectorDetail.setText(correct
                    ? "Has identificado correctamente el fundamento técnico evaluado en este reto."
                    : "La opción seleccionada no corresponde al contrato esperado. Revisa la pista conceptual e inténtalo de nuevo.");

            handleEvaluationOutcome(correct, true, msg + "\n", result.persistence(), submission.practiceOnly());
        }
    }

    private static String exceptionDetails(Throwable error) {
        StringWriter details = new StringWriter();
        error.printStackTrace(new PrintWriter(details));
        return details.toString();
    }

    private void handleEvaluationOutcome(
            boolean localSuccess,
            boolean sandboxedValidation,
            String consoleMessage,
            PersistenceOutcome persistence,
            boolean practiceOnly) {

        StringBuilder fullConsole = new StringBuilder(consoleMessage);
        if (!practiceOnly) saveUncertain = persistence.attemptedOracleSave && !persistence.savedToOracle;

        if (persistence.savedToOracle) {
            pendingRequestId = null;
            pendingPayloadHash = null;
        }

        if (saveUncertain && !practiceOnly) {
            fullConsole.append("\n[ERROR DE GUARDADO EN ORACLE] ").append(persistence.errorMessage).append("\n")
                    .append("> No se pudo confirmar el guardado. Reintentar la misma respuesta reutiliza su clave y no crea un duplicado.");
            txtConsoleOutput.setText(fullConsole.toString());
            consolePane.setExpanded(true);
            btnSubmit.setText("Volver a enviar");
            showFeedback((localSuccess ? "Aprobado localmente. " : "Evaluación terminada. ")
                    + "Guardado sin confirmar: puedes comprobarlo y reintentarlo con seguridad.", "text-warning");
            return;
        }

        btnSubmit.setText(isPracticeOnly()
                ? "Practicar sin XP" : "Enviar solución");
        boolean effectiveSuccess = persistence.savedToOracle && persistence.attempt != null
                ? persistence.attempt.isCorrect()
                : localSuccess;

        if (persistence.savedToOracle) {
            fullConsole.append("> Persistencia: Intento #")
                    .append(persistence.attempt != null ? persistence.attempt.getId() : 0)
                    .append(" guardado transaccionalmente en Oracle (Intentos).\n");
        } else if (practiceOnly) {
            fullConsole.append("> Modo práctica: resultado local sin guardado ni XP.\n");
        } else {
            fullConsole.append("> Modo local: este intento no se envió a Oracle.\n");
        }
        txtConsoleOutput.setText(fullConsole.toString());
        consolePane.setExpanded(!effectiveSuccess);

        if (effectiveSuccess) {
            if (btnNextChallenge != null && availableChallenges.size() > 1) {
                btnNextChallenge.getStyleClass().setAll("btn-primary");
                Theme.playPulse(btnNextChallenge);
            }
            int reward = practiceOnly ? 0 : persistence.savedToOracle && persistence.attempt != null
                    ? persistence.attempt.getEarnedExperience()
                    : activeChallenge.getExpReward();

            boolean awardLocal = !persistence.savedToOracle && !xpAwardedForCurrent && currentUser != null && reward > 0;
            if (awardLocal) {
                currentUser.addExperience(reward);
                currentUser.setLevel(Math.max(1, 1 + currentUser.getTotalExp() / 300));
            }
            if (!practiceOnly) xpAwardedForCurrent = true;
            String rewardMessage = currentUser != null && reward > 0 && (persistence.savedToOracle || awardLocal)
                    ? " +" + reward + " XP." : "";
            showFeedback((persistence.savedToOracle ? "¡Aprobado y guardado!"
                    : practiceOnly ? practiceSuccessMessage()
                    : "¡Aprobado en modo local!")
                    + (sandboxedValidation ? "" : " (Simulación)") + rewardMessage
                    + (availableChallenges.size() > 1 ? " Continúa con el siguiente reto." : ""), "text-success");
            Theme.playPulse(lblResultBanner);
        } else {
            showFeedback((persistence.savedToOracle ? "Intento guardado. "
                    : practiceOnly ? "Práctica sin XP. " : "Evaluado en modo local. ")
                    + (activeChallenge instanceof QuizChallenge
                    ? "Revisa la pista y elige otra respuesta." : "Revisa los casos fallidos y corrige tu código."), "text-danger");
            Theme.playPulse(lblResultBanner);
        }
        if (onSolvedCallback != null) {
            try {
                onSolvedCallback.run();
            } catch (RuntimeException ex) {
                // A panel refresh failure must never turn a confirmed save into a retry.
                LOGGER.log(Level.WARNING, "No se pudo actualizar el panel tras evaluar el reto", ex);
                txtConsoleOutput.appendText("\nEl resultado anterior se mantiene; falló la actualización del panel.\n" + exceptionDetails(ex));
                consolePane.setExpanded(true);
            }
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
