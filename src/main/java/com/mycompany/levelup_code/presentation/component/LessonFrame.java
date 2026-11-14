package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.enums.ProgressStatus;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.LessonProgress;
import com.mycompany.levelup_code.domain.model.User;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Lesson reader using the shared palette and background progress operations. */
public class LessonFrame extends Stage {

    private static final Logger LOGGER = Logger.getLogger(LessonFrame.class.getName());
    // Serialize writes across lesson windows so a late start cannot undo a
    // completion. This monitor is only acquired by background workers.
    private static final Object PROGRESS_LOCK = new Object();
    private static final Map<User, Set<Object>> LOCAL_COMPLETIONS = new WeakHashMap<>();

    private final User currentUser;
    private final Lesson currentLesson;
    private final Runnable onProgressCallback;
    private boolean lessonCompleted;
    private boolean localProgress;
    private boolean closed;
    private boolean completing;
    private boolean failed;
    private Task<ProgressResult> progressTask;

    private TextArea txtComparativeCode;
    private Label lblComparisonCaption;
    private ToggleButton btnTabJava;
    private ToggleButton btnTabPython;
    private ToggleButton btnTabCpp;
    private Label lblStatusInfo;
    private Label lblNextAction;
    private Button btnMarkComplete;
    private Button btnRetry;
    private Button btnBack;
    private ProgressBar progressBar;
    private ScrollPane theoryScroll;
    private TabPane compactTabs;
    private Tab theoryTab;
    private Tab codeTab;

    private record ProgressResult(boolean completed, boolean local, boolean newlyCompleted) { }

    public LessonFrame(Lesson lesson) {
        this(null, lesson, null);
    }

    public LessonFrame(User user, Lesson lesson, Runnable onProgressCallback) {
        this.currentUser = user;
        this.currentLesson = lesson;
        this.onProgressCallback = onProgressCallback;
        initComponents();
        SessionWindowRegistry.track(this);
        switchComparativeSnippet(resolveInitialLanguageKey());
        runProgressOperation(false);
        addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            closed = true;
            // Do not cancel JDBC writes: cancellation cannot undo a commit.
            // The result still refreshes the dashboard, never a hidden reader.
        });
        addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
            boolean reopening = closed;
            closed = false;
            if (reopening && progressTask == null) {
                runProgressOperation(false);
            } else {
                renderProgress();
            }
        });
    }

    private String resolveInitialLanguageKey() {
        if (currentLesson != null && currentLesson.getModule() != null
                && currentLesson.getModule().getLanguage() != null
                && currentLesson.getModule().getLanguage().getName() != null) {
            String lang = currentLesson.getModule().getLanguage().getName().toLowerCase(Locale.ROOT);
            if (lang.contains("python")) {
                return "PYTHON";
            }
            if (lang.contains("c++") || lang.contains("cpp")) {
                return "CPP";
            }
        }
        return "JAVA";
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
            toFront();
        } else {
            close();
        }
    }

    private int lessonReward() {
        return currentLesson == null ? 50 : Math.max(0, currentLesson.getExpReward());
    }

    /** Session-only progress for the dashboard's demo-mode status lookup. */
    static boolean isLocallyCompleted(User user, Lesson lesson) {
        synchronized (LOCAL_COMPLETIONS) {
            Set<Object> completed = LOCAL_COMPLETIONS.get(user);
            return completed != null && completed.contains(localLessonKey(lesson));
        }
    }

    private static Object localLessonKey(Lesson lesson) {
        return lesson == null ? "preview" : lesson.getId() > 0 ? lesson.getId() : lesson;
    }

    private void runProgressOperation(boolean complete) {
        if (closed || progressTask != null || (complete && lessonCompleted)) {
            return;
        }
        completing = complete;
        failed = false;
        Task<ProgressResult> task = new Task<>() {
            @Override
            protected ProgressResult call() {
                synchronized (PROGRESS_LOCK) {
                    // Context initialization can connect to the database too.
                    AppContext ctx = AppContext.getInstance();
                    boolean persistent = ctx.isOracleConnected()
                            && currentUser != null && currentUser.getId() > 0
                            && currentLesson != null && currentLesson.getId() > 0;
                    if (!persistent) {
                        // A separate, short-lived cache lock allows dashboard
                        // reads without waiting for a database operation.
                        synchronized (LOCAL_COMPLETIONS) {
                            Object key = localLessonKey(currentLesson);
                            Set<Object> completed = LOCAL_COMPLETIONS.computeIfAbsent(
                                    currentUser, ignored -> new HashSet<>());
                            boolean newlyCompleted = complete && completed.add(key);
                            return new ProgressResult(completed.contains(key), true, newlyCompleted);
                        }
                    }
                    // Read before writing, including after an uncertain save failure.
                    LessonProgress progress = ctx.getProgressRepository()
                            .findByUserAndLesson(currentUser.getId(), currentLesson.getId()).orElse(null);
                    if (progress != null && progress.isCompleted()) {
                        return new ProgressResult(true, false, false);
                    }
                    if (progress == null || progress.getStatus() != ProgressStatus.IN_PROGRESS) {
                        progress = ctx.getProgressService().startLesson(currentUser, currentLesson.getId());
                    }
                    if (complete && !progress.isCompleted()) {
                        progress = ctx.getProgressService().completeLesson(currentUser, currentLesson.getId());
                    }
                    return new ProgressResult(progress.isCompleted(), false, complete);
                }
            }
        };
        progressTask = task;
        task.setOnSucceeded(event -> {
            progressTask = null;
            ProgressResult result = task.getValue();
            lessonCompleted = result.completed();
            localProgress = result.local();
            if (result.local() && result.newlyCompleted() && currentUser != null) {
                currentUser.addExperience(lessonReward());
                currentUser.setLevel(Math.max(1, 1 + currentUser.getTotalExp() / 300));
            }
            if (!closed) {
                renderProgress();
            }
            notifyDashboard();
        });
        task.setOnFailed(event -> {
            progressTask = null;
            failed = true;
            LOGGER.log(Level.WARNING, "Could not " + (complete ? "complete" : "start")
                    + " lesson " + (currentLesson == null ? "preview" : currentLesson.getId()), task.getException());
            if (!closed) {
                renderProgress();
            }
            // A failed response can follow a committed write.
            if (complete) {
                notifyDashboard();
            }
        });
        renderProgress();
        Thread worker = new Thread(task, "lesson-progress-" + (currentLesson == null ? 0 : currentLesson.getId()));
        worker.setDaemon(true);
        worker.start();
    }

    private void notifyDashboard() {
        if (onProgressCallback != null) {
            try {
                onProgressCallback.run(); // Task handlers run on the FX thread.
            } catch (RuntimeException ex) {
                // A refresh failure must never turn a successful save into a retry.
                LOGGER.log(Level.WARNING, "Could not refresh the lesson dashboard", ex);
            }
        }
    }

    private void renderProgress() {
        boolean busy = progressTask != null;
        btnMarkComplete.setDisable(busy || lessonCompleted || failed);
        btnRetry.setVisible(failed && !busy);
        btnRetry.setManaged(failed && !busy);
        btnRetry.setDisable(busy);
        btnMarkComplete.setText(lessonCompleted ? "Lectura completada"
                : busy && completing ? "Guardando…" : "_Completar lectura (+" + lessonReward() + " XP)");
        progressBar.setProgress(busy ? ProgressBar.INDETERMINATE_PROGRESS : lessonCompleted ? 1 : 0);
        String message;
        String style;
        if (busy) {
            message = completing ? "Guardando tu lectura… Puedes seguir leyendo o cerrar esta ventana."
                    : "Consultando tu progreso… Puedes empezar a leer.";
            style = "body-text";
        } else if (failed) {
            message = completing
                    ? "No pudimos confirmar el guardado. Revisa tu conexión y pulsa Reintentar; comprobaremos si ya se guardó."
                    : "No pudimos consultar tu progreso. Revisa tu conexión y pulsa Reintentar. Puedes seguir leyendo.";
            style = "text-danger";
        } else if (lessonCompleted) {
            message = localProgress ? "Lectura completada en esta sesión de práctica. El avance no se guarda en tu cuenta."
                    : "Lectura completada y guardada. Puedes repasarla cuando quieras; no se repiten los XP.";
            style = "text-success";
        } else {
            message = localProgress ? "Modo práctica: el avance y los XP solo se conservan durante esta sesión."
                    : "Lectura en curso. Marca la lección como completada cuando termines.";
            style = "body-text";
        }
        lblStatusInfo.getStyleClass().setAll(style);
        lblStatusInfo.setText(message);
        progressBar.setAccessibleText(busy ? "Progreso: operación en curso"
                : lessonCompleted ? "Lectura completada" : "Lectura pendiente de completar");
        lblNextAction.setText(lessonCompleted
                ? hasChallenges() ? "Siguiente paso: elige un reto para practicar." : "Siguiente paso: vuelve al módulo para continuar."
                : "Lee la explicación, compara el ejemplo y completa la lectura. Los retos se resuelven por separado.");
    }

    private void initComponents() {
        String lessonTitle = currentLesson != null && currentLesson.getTitle() != null
                ? currentLesson.getTitle() : "Herencia, Clases Abstractas y Polimorfismo";
        setTitle("LevelUp Code — Lección: " + lessonTitle);
        setMinWidth(560);
        setMinHeight(620);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");
        root.setPadding(new Insets(16));

        VBox headerCard = new VBox(8);
        headerCard.getStyleClass().add("surface-card");
        String moduleCrumb = currentLesson != null && currentLesson.getModule() != null
                && currentLesson.getModule().getTitle() != null
                ? currentLesson.getModule().getTitle() : "Módulo de práctica";
        Label breadcrumb = wrappedLabel(moduleCrumb + "  ›  Lección "
                + (currentLesson == null ? 1 : currentLesson.getOrderIndex()), "body-text");
        Label title = wrappedLabel(lessonTitle, "page-title");
        title.setGraphic(Theme.createIcon(Theme.IconKind.BOOK, 20, Theme.HEX_INDIGO));
        title.setGraphicTextGap(10);
        FlowPane badges = new FlowPane(8, 6);
        badges.getChildren().addAll(Theme.createPillBadge("Lectura + práctica", "badge-indigo"),
                Theme.createPillBadge("Lectura: +" + lessonReward() + " XP", "badge-green"));
        headerCard.getChildren().addAll(breadcrumb, title, badges);
        root.setTop(headerCard);
        BorderPane.setMargin(headerCard, new Insets(0, 0, 12, 0));

        VBox theoryPane = buildTheoryPane();
        VBox codePane = buildComparativeCodePane();
        SplitPane split = new SplitPane(theoryPane, codePane);
        split.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        split.setDividerPositions(0.52);
        split.setAccessibleText("Explicación y ejemplo. Arrastra el divisor para ajustar el ancho.");
        compactTabs = new TabPane();
        compactTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        theoryTab = new Tab("1. Leer");
        codeTab = new Tab("2. Comparar");
        compactTabs.getTabs().addAll(theoryTab, codeTab);
        root.setCenter(split);

        VBox bottomBar = buildBottomBar();
        root.setBottom(bottomBar);
        BorderPane.setMargin(bottomBar, new Insets(12, 0, 0, 0));
        Scene scene = new Scene(root, 1100, 800);
        scene.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() < 880 && root.getCenter() != compactTabs) {
                split.getItems().clear();
                theoryTab.setContent(theoryPane);
                codeTab.setContent(codePane);
                root.setCenter(compactTabs);
            } else if (newWidth.doubleValue() >= 880 && root.getCenter() != split) {
                theoryTab.setContent(null);
                codeTab.setContent(null);
                split.getItems().setAll(theoryPane, codePane);
                root.setCenter(split);
                split.setDividerPositions(0.52);
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN), () -> focusPane(false));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN), () -> focusPane(true));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), () -> {
            if (btnRetry.isVisible()) {
                btnRetry.fire();
            } else {
                btnMarkComplete.fire();
            }
        });
        Theme.applyCss(scene);
        setScene(scene);
    }

    private static Label wrappedLabel(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    private VBox buildTheoryPane() {
        VBox card = new VBox(10);
        card.getStyleClass().add("surface-card");
        card.setMinSize(0, 0);
        VBox reading = new VBox(12);
        reading.setPadding(new Insets(4, 14, 14, 4));
        reading.setMinWidth(0);
        String defaultTheory = "1. UNA IDEA COMÚN, DISTINTAS RESPUESTAS\n"
                + "Una clase base describe lo que comparten varios objetos. Las clases derivadas pueden adaptar su comportamiento.\n\n"
                + "2. COMPARA EL EJEMPLO\n"
                + "Observa cómo cada lenguaje expresa la misma idea. Identifica la clase base y el método que cambia en las subclases.\n\n"
                + "3. PONLO EN PRÁCTICA\n"
                + "Explica con tus palabras qué método se ejecutará. Después, elige un reto para comprobarlo.";
        String theory = currentLesson != null && currentLesson.getTheory() != null
                && !currentLesson.getTheory().isBlank() ? currentLesson.getTheory()
                : currentLesson == null ? defaultTheory : "Esta lección aún no tiene una explicación. Puedes revisar el ejemplo y los retos disponibles.";
        // Preserve authored paragraphs and bullets, with separate section titles.
        for (String block : theory.split("\\R\\s*\\R")) {
            String[] lines = block.strip().split("\\R", 2);
            boolean headingLine = lines[0].matches("\\d+[.)]\\s+.+");
            if (headingLine) {
                reading.getChildren().add(wrappedLabel(lines[0], "card-title"));
            }
            String body = headingLine ? lines.length > 1 ? lines[1] : "" : block.strip();
            if (!body.isBlank()) {
                Label paragraph = wrappedLabel(body, "body-text");
                paragraph.setStyle("-fx-font-size: 15px; -fx-line-spacing: 5px;");
                reading.getChildren().add(paragraph);
            }
        }
        theoryScroll = new ScrollPane(reading);
        theoryScroll.setFitToWidth(true);
        theoryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        theoryScroll.getStyleClass().add("scroll-pane-clean");
        theoryScroll.setMinSize(0, 0);
        theoryScroll.setFocusTraversable(true);
        theoryScroll.setAccessibleText("Explicación de la lección");
        theoryScroll.setAccessibleHelp("Usa las flechas o Re Pág y Av Pág para leer. Control más 2 abre el ejemplo.");
        VBox.setVgrow(theoryScroll, Priority.ALWAYS);
        Button compare = new Button("Ver _ejemplo →");
        compare.setMnemonicParsing(true);
        compare.getStyleClass().add("btn-secondary");
        compare.setOnAction(event -> focusPane(true));
        card.getChildren().addAll(wrappedLabel("1. Comprende la idea", "card-title"), theoryScroll, compare);
        return card;
    }

    private VBox buildComparativeCodePane() {
        VBox card = new VBox(10);
        card.getStyleClass().add("surface-card");
        card.setMinSize(0, 0);
        ToggleGroup languages = new ToggleGroup();
        btnTabJava = languageButton("Java", "JAVA", languages);
        btnTabPython = languageButton("Python", "PYTHON", languages);
        btnTabCpp = languageButton("C++", "CPP", languages);
        languages.selectedToggleProperty().addListener((observable, previous, selected) -> {
            // Arrow-key selection in a ToggleGroup does not fire an action.
            if (selected != null) {
                switchComparativeSnippet((String) selected.getUserData());
            }
        });
        FlowPane languageBar = new FlowPane(8, 6, btnTabJava, btnTabPython, btnTabCpp);
        txtComparativeCode = new TextArea();
        txtComparativeCode.setEditable(false);
        txtComparativeCode.setWrapText(true);
        txtComparativeCode.setMinSize(0, 50);
        txtComparativeCode.getStyleClass().add("code-editor");
        txtComparativeCode.setAccessibleText("Ejemplo de código de la lección");
        txtComparativeCode.setAccessibleHelp("Puedes seleccionar y copiar el código. Control más 1 vuelve a la explicación.");
        // A read-only TextArea otherwise consumes Tab, trapping keyboard readers.
        txtComparativeCode.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB && !event.isControlDown() && !event.isAltDown()) {
                if (event.isShiftDown()) {
                    btnTabCpp.requestFocus();
                } else if (btnRetry.isVisible()) {
                    btnRetry.requestFocus();
                } else if (!btnMarkComplete.isDisabled()) {
                    btnMarkComplete.requestFocus();
                } else {
                    btnBack.requestFocus();
                }
                event.consume();
            }
        });
        VBox.setVgrow(txtComparativeCode, Priority.ALWAYS);
        lblComparisonCaption = wrappedLabel("", "body-text");
        card.getChildren().addAll(wrappedLabel("2. Compara el ejemplo", "card-title"),
                wrappedLabel("Cambia de lenguaje y observa cómo se expresa la misma idea.", "muted-text"),
                languageBar, txtComparativeCode, lblComparisonCaption);
        return card;
    }

    private ToggleButton languageButton(String name, String key, ToggleGroup group) {
        ToggleButton button = new ToggleButton(name);
        button.setToggleGroup(group);
        button.setUserData(key);
        button.setAccessibleText("Ver ejemplo en " + name);
        button.setTooltip(new Tooltip("Comparar esta lección en " + name));
        button.setOnAction(event -> switchComparativeSnippet(key));
        return button;
    }

    private boolean hasChallenges() {
        return currentLesson != null && currentLesson.getChallenges() != null
                && currentLesson.getChallenges().stream().anyMatch(challenge -> challenge != null);
    }

    private VBox buildBottomBar() {
        VBox bar = new VBox(8);
        bar.getStyleClass().add("surface-card");
        lblStatusInfo = wrappedLabel("Consultando tu progreso…", "body-text");
        lblStatusInfo.setStyle("-fx-font-size: 13px;");
        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("progress-bar-indigo");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        lblNextAction = wrappedLabel("", "body-text");
        btnMarkComplete = new Button("_Completar lectura");
        btnMarkComplete.setMnemonicParsing(true);
        btnMarkComplete.getStyleClass().add("btn-primary");
        btnMarkComplete.setTooltip(new Tooltip("Completar la lectura · Ctrl+Enter"));
        btnMarkComplete.setOnAction(event -> runProgressOperation(true));
        btnRetry = new Button("_Reintentar");
        btnRetry.setMnemonicParsing(true);
        btnRetry.getStyleClass().add("btn-secondary");
        btnRetry.setOnAction(event -> runProgressOperation(completing));
        btnBack = new Button("_Volver al módulo");
        btnBack.setMnemonicParsing(true);
        btnBack.setCancelButton(true);
        btnBack.getStyleClass().add("btn-ghost");
        btnBack.setTooltip(new Tooltip("Volver al módulo · Esc"));
        btnBack.setOnAction(event -> close());
        FlowPane actions = new FlowPane(8, 8, btnMarkComplete, btnRetry, btnBack);
        bar.getChildren().addAll(lblStatusInfo, progressBar, actions, lblNextAction);
        if (hasChallenges()) {
            VBox challengeList = new VBox(6);
            int index = 0;
            for (Challenge challenge : currentLesson.getChallenges()) {
                if (challenge == null) {
                    continue;
                }
                boolean quiz = challenge instanceof com.mycompany.levelup_code.domain.model.QuizChallenge;
                String title = challenge.getTitle() == null || challenge.getTitle().isBlank()
                        ? quiz ? "Preguntas de repaso" : "Ejercicio de código" : challenge.getTitle();
                Button launch = new Button(++index + ". " + challenge.getType().label() + ": " + title
                        + " (+" + challenge.getExpReward() + " XP)");
                launch.setMnemonicParsing(false);
                launch.setWrapText(true);
                launch.setMaxWidth(Double.MAX_VALUE);
                launch.setMinHeight(Region.USE_PREF_SIZE);
                launch.getStyleClass().add("btn-secondary");
                launch.setAccessibleText("Abrir " + launch.getText());
                launch.setOnAction(event -> ChallengeFrame.open(currentUser, challenge, onProgressCallback));
                challengeList.getChildren().add(launch);
            }
            ScrollPane challengesScroll = new ScrollPane(challengeList);
            challengesScroll.setFitToWidth(true);
            challengesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            challengesScroll.getStyleClass().add("scroll-pane-clean");
            challengesScroll.setPrefViewportHeight(Math.min(80, index * 40));
            challengesScroll.setMaxHeight(84);
            challengesScroll.setAccessibleText("Retos de esta lección");
            bar.getChildren().add(challengesScroll);
        }
        return bar;
    }

    private void focusPane(boolean code) {
        compactTabs.getSelectionModel().select(code ? codeTab : theoryTab);
        if (code) {
            txtComparativeCode.requestFocus();
        } else {
            theoryScroll.requestFocus();
        }
    }

    private void switchComparativeSnippet(String lang) {
        String[] snippet = com.mycompany.levelup_code.presentation.controller.DemoSeedCatalog
                .resolveComparativeSnippet(currentLesson, lang);
        updateLanguageButton(btnTabJava, "JAVA".equals(lang), "btn-java");
        updateLanguageButton(btnTabPython, "PYTHON".equals(lang), "btn-python");
        updateLanguageButton(btnTabCpp, "CPP".equals(lang), "btn-cpp");
        txtComparativeCode.setText(snippet[0]);
        txtComparativeCode.positionCaret(0);
        txtComparativeCode.setScrollTop(0);
        txtComparativeCode.setAccessibleText("Ejemplo de código en " + ("CPP".equals(lang) ? "C++" : lang));
        lblComparisonCaption.setText(snippet[1]);
    }

    private void updateLanguageButton(ToggleButton button, boolean selected, String style) {
        button.setSelected(selected);
        // Keep the base control class and its keyboard focus treatment.
        button.getStyleClass().removeAll("btn-java", "btn-python", "btn-cpp", "btn-secondary");
        button.getStyleClass().add(selected ? style : "btn-secondary");
    }
}
