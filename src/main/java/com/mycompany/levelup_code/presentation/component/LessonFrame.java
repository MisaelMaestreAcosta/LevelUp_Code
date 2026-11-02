package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.app.AppContext;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Visor interactivo de lección en JavaFX + CSS ({@code /styles/levelup.css})
 * con teoría estructurada, comparador sintáctico entre lenguajes (Java, Python, C++)
 * y persistencia en {@code Progreso_Leccion}.
 */
public class LessonFrame extends Stage {

    private final User currentUser;
    private final Lesson currentLesson;
    private final Runnable onProgressCallback;
    private boolean lessonCompleted = false;

    private TextArea txtComparativeCode;
    private Label lblComparisonCaption;
    private Button btnTabJava;
    private Button btnTabPython;
    private Button btnTabCpp;
    private Label lblStatusInfo;
    private Button btnMarkComplete;

    public LessonFrame(Lesson lesson) {
        this(null, lesson, null);
    }

    public LessonFrame(User user, Lesson lesson, Runnable onProgressCallback) {
        this.currentUser = user;
        this.currentLesson = lesson;
        this.onProgressCallback = onProgressCallback;
        markLessonStartedInOracle();
        initComponents();
        switchComparativeSnippet("JAVA");
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show();
            toFront();
        } else {
            close();
        }
    }

    private void markLessonStartedInOracle() {
        if (currentUser == null || currentUser.getId() <= 0
                || currentLesson == null || currentLesson.getId() <= 0) {
            return;
        }
        AppContext ctx = AppContext.getInstance();
        if (ctx.isOracleConnected()) {
            try {
                ctx.getProgressService().startLesson(currentUser, currentLesson.getId());
            } catch (Exception ignored) {
            }
        }
    }

    private void initComponents() {
        String lessonTitle = currentLesson != null && currentLesson.getTitle() != null
                ? currentLesson.getTitle()
                : "Herencia, Clases Abstractas y Polimorfismo";
        int reward = currentLesson != null && currentLesson.getExpReward() > 0
                ? currentLesson.getExpReward()
                : 50;

        setTitle("LevelUp Code — Lección: " + lessonTitle);
        setMinWidth(940);
        setMinHeight(580);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-shell");
        root.setPadding(new Insets(18, 22, 18, 22));

        // Encabezado de la lección
        HBox headerCard = new HBox(12);
        headerCard.getStyleClass().add("surface-card");
        headerCard.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(lessonTitle);
        lblTitle.getStyleClass().add("page-title");
        lblTitle.setGraphic(Theme.createIcon(Theme.IconKind.BOOK, 20, Theme.HEX_INDIGO));
        lblTitle.setGraphicTextGap(10);

        int orderIdx = currentLesson != null ? currentLesson.getOrderIndex() : 1;
        Label badgeOrder = Theme.createPillBadge("LECCIÓN #" + orderIdx, "badge-indigo");
        Label badgeReward = Theme.createPillBadge("+" + reward + " XP", "badge-green");

        headerCard.getChildren().addAll(lblTitle, badgeOrder, badgeReward);
        BorderPane.setMargin(headerCard, new Insets(0, 0, 14, 0));
        root.setTop(headerCard);

        // Panel central dividido: Teoría Conceptual (Izquierda) vs Código Comparativo (Derecha)
        HBox centerSplit = new HBox(16);
        VBox theoryPane = buildTheoryPane();
        VBox codePane = buildComparativeCodePane();
        HBox.setHgrow(theoryPane, Priority.ALWAYS);
        HBox.setHgrow(codePane, Priority.ALWAYS);
        theoryPane.setPrefWidth(480);
        codePane.setPrefWidth(480);
        centerSplit.getChildren().addAll(theoryPane, codePane);
        root.setCenter(centerSplit);

        // Barra inferior de progreso y salto al reto
        HBox bottomBar = buildBottomBar(reward);
        BorderPane.setMargin(bottomBar, new Insets(14, 0, 0, 0));
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 1060, 660);
        Theme.applyCss(scene);
        setScene(scene);
    }

    private VBox buildTheoryPane() {
        VBox card = new VBox(12);
        card.getStyleClass().add("surface-card");

        HBox top = Theme.createSectionHeader("Fundamento Teórico y Diseño", "Lectura interactiva", "badge-indigo");

        TextArea txtTheory = new TextArea();
        txtTheory.setEditable(false);
        txtTheory.setWrapText(true);
        txtTheory.getStyleClass().add("code-editor");
        VBox.setVgrow(txtTheory, Priority.ALWAYS);

        String defaultTheory = "1. ABSTRACCIÓN Y CONTRATOS DE COMPORTAMIENTO\n"
                + "En el diseño orientado a objetos, una clase base define el contrato común que todas las especializaciones deben cumplir. "
                + "Esto permite que el cliente opere sobre la abstracción sin conocer los detalles internos de cada subtipo.\n\n"
                + "2. DESPACHO DINÁMICO (POLIMORFISMO EN TIEMPO DE EJECUCIÓN)\n"
                + "• En Java, todos los métodos no estáticos ni privados son virtuales por defecto.\n"
                + "• En C++, se requiere la palabra clave 'virtual' y 'override' para habilitar la tabla virtual (vtable).\n"
                + "• En Python, el polimorfismo se resuelve mediante Duck Typing y jerarquías con abc.ABC.\n\n"
                + "3. PRINCIPIO ABIERTO/CERRADO (OCP - SOLID)\n"
                + "Al encapsular las variaciones en subclases (como CodeChallenge y QuizChallenge en LevelUp Code), "
                + "podemos agregar nuevos tipos de evaluación sin alterar el motor central.";

        txtTheory.setText(currentLesson != null && currentLesson.getTheory() != null && !currentLesson.getTheory().isEmpty()
                ? currentLesson.getTheory()
                : defaultTheory);

        card.getChildren().addAll(top, txtTheory);
        return card;
    }

    private VBox buildComparativeCodePane() {
        VBox card = new VBox(12);
        card.getStyleClass().add("surface-card");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblSection = new Label("TRADUCCIÓN ENTRE PARADIGMAS");
        lblSection.getStyleClass().add("section-caption");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnTabJava = new Button("Java");
        btnTabPython = new Button("Python");
        btnTabCpp = new Button("C++");

        btnTabJava.setOnAction(e -> switchComparativeSnippet("JAVA"));
        btnTabPython.setOnAction(e -> switchComparativeSnippet("PYTHON"));
        btnTabCpp.setOnAction(e -> switchComparativeSnippet("CPP"));

        header.getChildren().addAll(lblSection, spacer, btnTabJava, btnTabPython, btnTabCpp);

        txtComparativeCode = new TextArea();
        txtComparativeCode.setEditable(false);
        txtComparativeCode.getStyleClass().add("code-editor");
        VBox.setVgrow(txtComparativeCode, Priority.ALWAYS);

        lblComparisonCaption = new Label();
        lblComparisonCaption.getStyleClass().add("badge-cyan");
        lblComparisonCaption.setGraphic(Theme.createIcon(Theme.IconKind.CODE, 14, Theme.HEX_CYAN));
        lblComparisonCaption.setGraphicTextGap(6);
        lblComparisonCaption.setWrapText(true);

        card.getChildren().addAll(header, txtComparativeCode, lblComparisonCaption);
        return card;
    }

    private HBox buildBottomBar(final int reward) {
        HBox bar = new HBox(14);
        bar.getStyleClass().add("surface-card");
        bar.setAlignment(Pos.CENTER_LEFT);

        lblStatusInfo = new Label("Estado: En curso — Completa la lectura o abre el reto práctico.");
        lblStatusInfo.getStyleClass().add("body-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnMarkComplete = new Button("Completar Lección (+" + reward + " XP)");
        btnMarkComplete.getStyleClass().add("btn-secondary");
        btnMarkComplete.setGraphic(Theme.createIcon(Theme.IconKind.CHECK, 14, Theme.HEX_GREEN));
        btnMarkComplete.setGraphicTextGap(6);
        btnMarkComplete.setOnAction(e -> {
            if (!lessonCompleted) {
                lessonCompleted = true;
                btnMarkComplete.setDisable(true);
                btnMarkComplete.setText("Lección Completada");

                AppContext ctx = AppContext.getInstance();
                boolean persistedInOracle = false;
                if (ctx.isOracleConnected() && currentUser != null && currentUser.getId() > 0
                        && currentLesson != null && currentLesson.getId() > 0) {
                    try {
                        ctx.getProgressService().startLesson(currentUser, currentLesson.getId());
                        ctx.getProgressService().completeLesson(currentUser, currentLesson.getId());
                        persistedInOracle = true;
                    } catch (Exception ignored) {
                    }
                }

                if (!persistedInOracle && currentUser != null && reward > 0) {
                    currentUser.addExperience(reward);
                    currentUser.setLevel(Math.max(1, 1 + currentUser.getTotalExp() / 300));
                }

                if (onProgressCallback != null) {
                    onProgressCallback.run();
                }
                lblStatusInfo.getStyleClass().setAll("text-success");
                lblStatusInfo.setText("¡Lección aprobada! Estado actualizado a COMPLETADA en Progreso_Leccion (+" + reward + " XP).");
            }
        });

        Button btnOpenChallenge = new Button("Ir al Reto Práctico");
        btnOpenChallenge.getStyleClass().add("btn-primary");
        btnOpenChallenge.setGraphic(Theme.createIcon(Theme.IconKind.PLAY, 14, Theme.HEX_WHITE));
        btnOpenChallenge.setGraphicTextGap(6);
        btnOpenChallenge.setOnAction(e -> {
            Challenge firstChallenge = (currentLesson != null && currentLesson.getChallenges() != null
                    && !currentLesson.getChallenges().isEmpty())
                    ? currentLesson.getChallenges().get(0)
                    : null;
            ChallengeFrame challengeFrame = new ChallengeFrame(currentUser, firstChallenge, onProgressCallback);
            challengeFrame.show();
        });

        bar.getChildren().addAll(lblStatusInfo, spacer, btnMarkComplete, btnOpenChallenge);
        return bar;
    }

    private void switchComparativeSnippet(String lang) {
        if ("JAVA".equals(lang)) {
            btnTabJava.getStyleClass().setAll("btn-java");
            btnTabPython.getStyleClass().setAll("btn-secondary");
            btnTabCpp.getStyleClass().setAll("btn-secondary");
            txtComparativeCode.setText(
                    "// Java 21: Clase abstracta y sobrescritura verificada\n"
                    + "public abstract class Animal {\n"
                    + "    public abstract String emitirSonido();\n"
                    + "}\n\n"
                    + "public class Perro extends Animal {\n"
                    + "    @Override\n"
                    + "    public String emitirSonido() {\n"
                    + "        return \"Guau\";\n"
                    + "    }\n"
                    + "}"
            );
            lblComparisonCaption.setText("Java: @Override valida en tiempo de compilación que la firma coincida con la superclase.");
        } else if ("PYTHON".equals(lang)) {
            btnTabJava.getStyleClass().setAll("btn-secondary");
            btnTabPython.getStyleClass().setAll("btn-python");
            btnTabCpp.getStyleClass().setAll("btn-secondary");
            txtComparativeCode.setText(
                    "# Python 3.12: Clases Base Abstractas con el módulo abc\n"
                    + "from abc import ABC, abstractmethod\n\n"
                    + "class Animal(ABC):\n"
                    + "    @abstractmethod\n"
                    + "    def emitir_sonido(self) -> str:\n"
                    + "        pass\n\n"
                    + "class Perro(Animal):\n"
                    + "    def emitir_sonido(self) -> str:\n"
                    + "        return \"Guau\""
            );
            lblComparisonCaption.setText("Python: @abstractmethod impide instanciar Animal si la subclase no implementa emitir_sonido().");
        } else {
            btnTabJava.getStyleClass().setAll("btn-secondary");
            btnTabPython.getStyleClass().setAll("btn-secondary");
            btnTabCpp.getStyleClass().setAll("btn-cpp");
            txtComparativeCode.setText(
                    "// C++ 20: Función virtual pura (= 0) y destructor virtual\n"
                    + "#include <string>\n\n"
                    + "class Animal {\n"
                    + "public:\n"
                    + "    virtual ~Animal() = default;\n"
                    + "    virtual std::string emitirSonido() const = 0;\n"
                    + "};\n\n"
                    + "class Perro : public Animal {\n"
                    + "public:\n"
                    + "    std::string emitirSonido() const override {\n"
                    + "        return \"Guau\";\n"
                    + "    }\n"
                    + "};"
            );
            lblComparisonCaption.setText("C++: '= 0' define una clase abstracta pura y 'override' garantiza despacho vía vtable.");
        }
    }
}
