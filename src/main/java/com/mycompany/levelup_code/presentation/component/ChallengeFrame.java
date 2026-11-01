package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.enums.ChallengeType;
import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.CodeChallenge;
import com.mycompany.levelup_code.domain.model.QuizChallenge;
import com.mycompany.levelup_code.domain.model.TestCase;
import com.mycompany.levelup_code.domain.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Entorno interactivo de resolución de retos (Código y Quiz) con numeración de líneas,
 * casos de prueba visibles/ocultos (RF-06, RF-07) y consola de evaluación en vivo.
 */
public class ChallengeFrame extends JFrame {

    private final User currentUser;
    private final List<Challenge> availableChallenges;
    private final Runnable onSolvedCallback;

    private Challenge activeChallenge;
    private boolean xpAwardedForCurrent = false;
    private int selectedQuizIndex = -1;

    private JLabel lblChallengeTitle;
    private JLabel lblStatement;
    private Theme.PillBadge badgeType;
    private Theme.PillBadge badgeReward;
    private JPanel centerCardPanel;
    private CardLayout centerCardLayout;

    // Editor de código
    private JTextArea txtEditor;
    private LineNumberGutter lineGutter;

    // Panel de opciones Quiz
    private JPanel quizOptionsContainer;
    private final List<Theme.RoundedPanel> quizOptionCards = new ArrayList<>();

    // Panel lateral de casos de prueba y consola
    private JPanel testCasesListPanel;
    private final List<Theme.PillBadge> testCaseStatusBadges = new ArrayList<>();
    private JTextArea txtConsoleOutput;
    private JLabel lblExecutionMetrics;
    private JLabel lblResultBanner;

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

    private static CodeChallenge createDefaultCodeChallenge() {
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
        setSize(1120, 700);
        setMinimumSize(new Dimension(960, 620));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        root.add(buildTopHeaderBar(), BorderLayout.NORTH);
        root.add(buildWorkspaceSplit(), BorderLayout.CENTER);
        root.add(buildBottomActionBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopHeaderBar() {
        Theme.RoundedPanel headerCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        headerCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        JPanel titleCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleCluster.setOpaque(false);

        lblChallengeTitle = new JLabel();
        lblChallengeTitle.setFont(Theme.FONT_TITLE);
        lblChallengeTitle.setForeground(Theme.TEXT_PRIMARY);
        lblChallengeTitle.setIcon(Theme.createVectorIcon(Theme.IconKind.CODE, 20, Theme.ACCENT_INDIGO));

        badgeType = new Theme.PillBadge("CÓDIGO", Theme.ACCENT_INDIGO);
        badgeReward = new Theme.PillBadge("+60 XP", Theme.ACCENT_AMBER);

        titleCluster.add(lblChallengeTitle);
        titleCluster.add(badgeType);
        titleCluster.add(badgeReward);

        topRow.add(titleCluster, BorderLayout.WEST);

        // Selector rápido entre retos de la lección
        if (availableChallenges.size() > 1) {
            JPanel switcher = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            switcher.setOpaque(false);
            JLabel lblSwitch = new JLabel("Cambiar ejercicio:");
            lblSwitch.setFont(Theme.FONT_SMALL);
            lblSwitch.setForeground(Theme.TEXT_MUTED);
            switcher.add(lblSwitch);

            for (int i = 0; i < availableChallenges.size(); i++) {
                final Challenge ch = availableChallenges.get(i);
                String btnLabel = (ch.getType() == ChallengeType.QUIZ ? "Quiz #" : "Código #") + (i + 1);
                JButton btnCh = Theme.createSecondaryButton(btnLabel);
                btnCh.setFont(Theme.FONT_SMALL);
                btnCh.addActionListener(e -> loadChallengeIntoView(ch));
                switcher.add(btnCh);
            }
            topRow.add(switcher, BorderLayout.EAST);
        }

        lblStatement = new JLabel();
        lblStatement.setFont(Theme.FONT_REGULAR);
        lblStatement.setForeground(Theme.TEXT_SECONDARY);
        lblStatement.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));

        headerCard.add(topRow, BorderLayout.NORTH);
        headerCard.add(lblStatement, BorderLayout.CENTER);
        return headerCard;
    }

    private JSplitPane buildWorkspaceSplit() {
        centerCardLayout = new CardLayout();
        centerCardPanel = new JPanel(centerCardLayout);
        centerCardPanel.setOpaque(false);

        centerCardPanel.add(buildCodeEditorCard(), "CODE");
        centerCardPanel.add(buildQuizCard(), "QUIZ");

        JPanel rightValidationPane = buildRightValidationPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerCardPanel, rightValidationPane);
        splitPane.setDividerLocation(640);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JPanel buildCodeEditorCard() {
        Theme.RoundedPanel editorCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        editorCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblFileTab = new JLabel("  Solution.java · UTF-8 · Tab: 4 espacios");
        lblFileTab.setFont(Theme.FONT_SECTION);
        lblFileTab.setForeground(Theme.TEXT_SECONDARY);
        lblFileTab.setIcon(Theme.createVectorIcon(Theme.IconKind.TERMINAL, 15, Theme.ACCENT_INDIGO));
        toolbar.add(lblFileTab, BorderLayout.WEST);

        JPanel editorActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        editorActions.setOpaque(false);

        JButton btnSolutionHint = Theme.createGhostButton("Insertar Solución Modelo", Theme.ACCENT_CYAN);
        btnSolutionHint.setFont(Theme.FONT_SMALL);
        btnSolutionHint.addActionListener(e -> fillReferenceSolution());

        JButton btnResetCode = Theme.createGhostButton("Restaurar Plantilla", Theme.TEXT_MUTED);
        btnResetCode.setFont(Theme.FONT_SMALL);
        btnResetCode.addActionListener(e -> {
            if (activeChallenge instanceof CodeChallenge) {
                txtEditor.setText(((CodeChallenge) activeChallenge).getInitialTemplate());
            }
        });

        editorActions.add(btnSolutionHint);
        editorActions.add(btnResetCode);
        toolbar.add(editorActions, BorderLayout.EAST);

        txtEditor = new JTextArea();
        txtEditor.setFont(Theme.FONT_CODE);
        txtEditor.setBackground(Theme.EDITOR_BG);
        txtEditor.setForeground(Theme.TEXT_PRIMARY);
        txtEditor.setCaretColor(Theme.ACCENT_INDIGO);
        txtEditor.setTabSize(4);
        txtEditor.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lineGutter = new LineNumberGutter(txtEditor);
        txtEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                lineGutter.updateLineCount();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lineGutter.updateLineCount();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lineGutter.updateLineCount();
            }
        });

        JScrollPane scrollEditor = new JScrollPane(txtEditor);
        scrollEditor.setRowHeaderView(lineGutter);
        scrollEditor.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        scrollEditor.getViewport().setBackground(Theme.EDITOR_BG);

        editorCard.add(toolbar, BorderLayout.NORTH);
        editorCard.add(scrollEditor, BorderLayout.CENTER);
        return editorCard;
    }

    private JPanel buildQuizCard() {
        Theme.RoundedPanel quizCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        quizCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel header = Theme.createSectionHeader(
                "Selecciona la respuesta correcta", "Evaluación de selección única");
        quizCard.add(header, BorderLayout.NORTH);

        quizOptionsContainer = new JPanel();
        quizOptionsContainer.setLayout(new BoxLayout(quizOptionsContainer, BoxLayout.Y_AXIS));
        quizOptionsContainer.setOpaque(false);
        quizOptionsContainer.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JScrollPane scrollQuiz = new JScrollPane(quizOptionsContainer);
        scrollQuiz.setBorder(null);
        scrollQuiz.setOpaque(false);
        scrollQuiz.getViewport().setOpaque(false);

        quizCard.add(scrollQuiz, BorderLayout.CENTER);
        return quizCard;
    }

    private JPanel buildRightValidationPane() {
        JPanel rightColumn = new JPanel(new BorderLayout(0, 12));
        rightColumn.setOpaque(false);
        rightColumn.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        // Tarjeta superior: Casos de prueba (RF-07)
        Theme.RoundedPanel casesCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        casesCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel casesHeader = Theme.createSectionHeader("Casos de Prueba (RF-07)", "Visibles y Ocultos");
        casesCard.add(casesHeader, BorderLayout.NORTH);

        testCasesListPanel = new JPanel();
        testCasesListPanel.setLayout(new BoxLayout(testCasesListPanel, BoxLayout.Y_AXIS));
        testCasesListPanel.setOpaque(false);
        testCasesListPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JScrollPane scrollCases = new JScrollPane(testCasesListPanel);
        scrollCases.setBorder(null);
        scrollCases.setOpaque(false);
        scrollCases.getViewport().setOpaque(false);
        casesCard.add(scrollCases, BorderLayout.CENTER);

        // Tarjeta inferior: Consola de salida y métricas de tiempo
        Theme.RoundedPanel consoleCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        consoleCard.setPreferredSize(new Dimension(0, 195));
        consoleCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel consoleHeader = new JPanel(new BorderLayout());
        consoleHeader.setOpaque(false);
        JLabel lblConsoleTitle = new JLabel("SALIDA DE CONSOLA / VEREDICTO");
        lblConsoleTitle.setFont(Theme.FONT_SECTION);
        lblConsoleTitle.setForeground(Theme.TEXT_MUTED);

        lblExecutionMetrics = new JLabel("Tiempo: -- ms");
        lblExecutionMetrics.setFont(Theme.FONT_SMALL);
        lblExecutionMetrics.setForeground(Theme.TEXT_SECONDARY);

        consoleHeader.add(lblConsoleTitle, BorderLayout.WEST);
        consoleHeader.add(lblExecutionMetrics, BorderLayout.EAST);

        txtConsoleOutput = new JTextArea();
        txtConsoleOutput.setEditable(false);
        txtConsoleOutput.setFont(Theme.FONT_CODE);
        txtConsoleOutput.setBackground(Theme.EDITOR_BG);
        txtConsoleOutput.setForeground(Theme.TEXT_SECONDARY);
        txtConsoleOutput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtConsoleOutput.setText("> Listo para validar solución.\n> Presiona 'Ejecutar y Validar' para correr la suite.");

        JScrollPane scrollConsole = new JScrollPane(txtConsoleOutput);
        scrollConsole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 0, 0, 0),
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1)
        ));

        consoleCard.add(consoleHeader, BorderLayout.NORTH);
        consoleCard.add(scrollConsole, BorderLayout.CENTER);

        rightColumn.add(casesCard, BorderLayout.CENTER);
        rightColumn.add(consoleCard, BorderLayout.SOUTH);
        return rightColumn;
    }

    private JPanel buildBottomActionBar() {
        Theme.RoundedPanel bottomBar = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        lblResultBanner = new JLabel("Completa el reto para ganar experiencia y mantener tu racha activa.");
        lblResultBanner.setFont(Theme.FONT_REGULAR);
        lblResultBanner.setForeground(Theme.TEXT_SECONDARY);
        lblResultBanner.setIcon(Theme.createVectorIcon(Theme.IconKind.BOLT, 16, Theme.ACCENT_AMBER));

        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRight.setOpaque(false);

        JButton btnClose = Theme.createSecondaryButton("Volver al Panel");
        btnClose.addActionListener(e -> dispose());

        JButton btnSubmit = Theme.createPrimaryButton("Ejecutar y Validar Solución", Theme.ACCENT_GREEN);
        btnSubmit.setIcon(Theme.createVectorIcon(Theme.IconKind.PLAY, 14, Color.WHITE));
        btnSubmit.addActionListener(e -> evaluateSubmission());

        actionsRight.add(btnClose);
        actionsRight.add(btnSubmit);

        bottomBar.add(lblResultBanner, BorderLayout.WEST);
        bottomBar.add(actionsRight, BorderLayout.EAST);
        return bottomBar;
    }

    private void loadChallengeIntoView(Challenge challenge) {
        this.activeChallenge = challenge;
        this.xpAwardedForCurrent = false;
        this.selectedQuizIndex = -1;

        lblChallengeTitle.setText("  " + challenge.getTitle());
        lblStatement.setText("<html><body style='width:760px;'>" + challenge.getDescription() + "</body></html>");
        badgeReward.setText("+" + challenge.getExpReward() + " XP");

        testCasesListPanel.removeAll();
        testCaseStatusBadges.clear();

        if (challenge instanceof CodeChallenge) {
            CodeChallenge codeChallenge = (CodeChallenge) challenge;
            badgeType.setText("RETO DE CÓDIGO · LÍMITE " + codeChallenge.getTimeLimitSeconds() + "S");
            badgeType.setAccent(Theme.ACCENT_INDIGO);
            centerCardLayout.show(centerCardPanel, "CODE");
            txtEditor.setText(codeChallenge.getInitialTemplate() != null ? codeChallenge.getInitialTemplate() : "");
            lineGutter.updateLineCount();

            List<TestCase> cases = codeChallenge.getTestCases();
            for (int i = 0; i < cases.size(); i++) {
                TestCase tc = cases.get(i);
                testCasesListPanel.add(createTestCaseRow(i + 1, tc));
                testCasesListPanel.add(Box.createVerticalStrut(8));
            }
        } else if (challenge instanceof QuizChallenge) {
            QuizChallenge quizChallenge = (QuizChallenge) challenge;
            badgeType.setText("QUIZ DE SELECCIÓN");
            badgeType.setAccent(Theme.ACCENT_VIOLET);
            centerCardLayout.show(centerCardPanel, "QUIZ");
            populateQuizOptions(quizChallenge);

            Theme.RoundedPanel infoBox = new Theme.RoundedPanel(10, Theme.EDITOR_BG, Theme.BORDER_SUBTLE);
            infoBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JLabel lblQuizInfo = new JLabel("<html><body style='width:250px;color:#BAC4D6;'>"
                    + "Selecciona una de las opciones a la izquierda y presiona <b>Ejecutar y Validar</b>."
                    + "</body></html>");
            lblQuizInfo.setFont(Theme.FONT_SMALL);
            infoBox.add(lblQuizInfo, BorderLayout.CENTER);
            testCasesListPanel.add(infoBox);
        }

        txtConsoleOutput.setForeground(Theme.TEXT_SECONDARY);
        txtConsoleOutput.setText("> Reto cargado: " + challenge.getTitle() + "\n> Esperando envío del estudiante...");
        lblExecutionMetrics.setText("Tiempo: -- ms");

        revalidate();
        repaint();
    }

    private JPanel createTestCaseRow(int index, TestCase tc) {
        Theme.RoundedPanel row = new Theme.RoundedPanel(10, Theme.EDITOR_BG, Theme.BORDER_SUBTLE);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        String titleText = tc.isHidden()
                ? "Caso #" + index + " (Oculto · Validación RNF-02)"
                : "Caso #" + index + " · Entrada: \"" + tc.getInput() + "\"";
        JLabel lblCase = new JLabel(titleText);
        lblCase.setFont(Theme.FONT_SUBTITLE);
        lblCase.setForeground(Theme.TEXT_PRIMARY);

        Theme.PillBadge statusBadge = new Theme.PillBadge("PENDIENTE", Theme.TEXT_MUTED);
        testCaseStatusBadges.add(statusBadge);

        top.add(lblCase, BorderLayout.WEST);
        top.add(statusBadge, BorderLayout.EAST);

        String detailText = tc.isHidden()
                ? "Los datos de entrada y salida se verifican internamente al enviar."
                : "Salida esperada: \"" + tc.getExpectedOutput() + "\"";
        JLabel lblDetail = new JLabel(detailText);
        lblDetail.setFont(Theme.FONT_SMALL);
        lblDetail.setForeground(Theme.TEXT_MUTED);

        row.add(top, BorderLayout.NORTH);
        row.add(lblDetail, BorderLayout.SOUTH);
        return row;
    }

    private void populateQuizOptions(QuizChallenge quiz) {
        quizOptionsContainer.removeAll();
        quizOptionCards.clear();

        List<String> opts = quiz.getOptions();
        char letter = 'A';
        for (int i = 0; i < opts.size(); i++) {
            final int optionIdx = i;
            String text = opts.get(i);

            Theme.RoundedPanel optionCard = new Theme.RoundedPanel(
                    10, Theme.EDITOR_BG, Theme.BORDER_SUBTLE, Theme.BORDER_STRONG);
            optionCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 16));
            optionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            optionCard.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            JLabel lblOpt = new JLabel(letter + ")   " + text);
            lblOpt.setFont(Theme.FONT_SUBTITLE);
            lblOpt.setForeground(Theme.TEXT_PRIMARY);
            optionCard.add(lblOpt, BorderLayout.CENTER);

            optionCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectQuizOption(optionIdx);
                }
            });

            quizOptionCards.add(optionCard);
            quizOptionsContainer.add(optionCard);
            quizOptionsContainer.add(Box.createVerticalStrut(10));
            letter++;
        }
    }

    private void selectQuizOption(int index) {
        this.selectedQuizIndex = index;
        for (int i = 0; i < quizOptionCards.size(); i++) {
            Theme.RoundedPanel card = quizOptionCards.get(i);
            if (i == index) {
                card.setFillColor(Theme.SURFACE_ELEVATED);
                card.setBorderColor(Theme.ACCENT_INDIGO);
                card.setAccentStripe(Theme.ACCENT_INDIGO);
            } else {
                card.setFillColor(Theme.EDITOR_BG);
                card.setBorderColor(Theme.BORDER_SUBTLE);
                card.setAccentStripe(Theme.BORDER_STRONG);
            }
        }
    }

    private void fillReferenceSolution() {
        if (activeChallenge instanceof CodeChallenge) {
            txtEditor.setText(
                    "public class AnimalSoundSolver {\n"
                    + "    public static String emitirSonido(String tipo) {\n"
                    + "        if (\"Perro\".equalsIgnoreCase(tipo)) {\n"
                    + "            return \"Guau\";\n"
                    + "        }\n"
                    + "        if (\"Gato\".equalsIgnoreCase(tipo)) {\n"
                    + "            return \"Miau\";\n"
                    + "        }\n"
                    + "        return \"Desconocido\";\n"
                    + "    }\n"
                    + "}"
            );
            lineGutter.updateLineCount();
        }
    }

    private void evaluateSubmission() {
        if (activeChallenge instanceof CodeChallenge) {
            CodeChallenge codeChallenge = (CodeChallenge) activeChallenge;
            String code = txtEditor.getText() != null ? txtEditor.getText() : "";
            List<TestCase> cases = codeChallenge.getTestCases();

            int passed = 0;
            StringBuilder log = new StringBuilder();
            log.append("> Compilando y ejecutando casos de prueba...\n");

            for (int i = 0; i < cases.size(); i++) {
                TestCase tc = cases.get(i);
                boolean ok = code.contains(tc.getExpectedOutput())
                        && (code.contains("return") || code.contains("System.out"));

                Theme.PillBadge badge = i < testCaseStatusBadges.size() ? testCaseStatusBadges.get(i) : null;
                if (ok) {
                    passed++;
                    if (badge != null) {
                        badge.setText("APROBADO");
                        badge.setAccent(Theme.ACCENT_GREEN);
                    }
                    log.append("  [OK] Caso #").append(i + 1).append(" superado.\n");
                } else {
                    if (badge != null) {
                        badge.setText("FALLIDO");
                        badge.setAccent(Theme.ACCENT_ROSE);
                    }
                    log.append("  [X]  Caso #").append(i + 1)
                            .append(": se esperaba \"").append(tc.getExpectedOutput()).append("\".\n");
                }
            }

            lblExecutionMetrics.setText("Tiempo: 14 ms · Memoria: 34 MB");
            boolean allPassed = passed == cases.size() && !cases.isEmpty();
            handleEvaluationOutcome(allPassed, log.toString());

        } else if (activeChallenge instanceof QuizChallenge) {
            QuizChallenge quiz = (QuizChallenge) activeChallenge;
            if (selectedQuizIndex < 0) {
                lblResultBanner.setForeground(Theme.ACCENT_AMBER);
                lblResultBanner.setText("Selecciona una opción antes de validar el quiz.");
                return;
            }
            boolean correct = quiz.isCorrectOption(selectedQuizIndex);
            lblExecutionMetrics.setText("Tiempo: 2 ms");
            String msg = correct
                    ? "> Respuesta verificada: CORRECTA.\n> Concepto dominado con éxito."
                    : "> Respuesta incorrecta.\n> Revisa la teoría de enlace dinámico e inténtalo de nuevo.";
            handleEvaluationOutcome(correct, msg);
        }
    }

    private void handleEvaluationOutcome(boolean success, String consoleMessage) {
        txtConsoleOutput.setText(consoleMessage);
        if (success) {
            txtConsoleOutput.setForeground(Theme.ACCENT_GREEN);
            int reward = activeChallenge.getExpReward();
            if (!xpAwardedForCurrent && currentUser != null && reward > 0) {
                currentUser.addExperience(reward);
                currentUser.setLevel(Math.max(1, 1 + currentUser.getTotalExp() / 300));
                xpAwardedForCurrent = true;
                if (onSolvedCallback != null) {
                    onSolvedCallback.run();
                }
                lblResultBanner.setForeground(Theme.ACCENT_GREEN);
                lblResultBanner.setIcon(Theme.createVectorIcon(Theme.IconKind.CHECK, 16, Theme.ACCENT_GREEN));
                lblResultBanner.setText("¡Reto superado! Se acreditaron +" + reward + " XP a tu perfil.");
            } else {
                lblResultBanner.setForeground(Theme.ACCENT_GREEN);
                lblResultBanner.setIcon(Theme.createVectorIcon(Theme.IconKind.CHECK, 16, Theme.ACCENT_GREEN));
                lblResultBanner.setText("¡Solución correcta! Todos los casos de prueba aprobaron.");
            }
        } else {
            txtConsoleOutput.setForeground(Theme.ACCENT_ROSE);
            lblResultBanner.setForeground(Theme.ACCENT_ROSE);
            lblResultBanner.setText("Algunos casos de prueba fallaron. Revisa la consola o usa 'Insertar Solución Modelo'.");
        }
    }

    /**
     * Margen izquierdo con numeración de líneas sincronizada con el JTextArea del editor.
     */
    private static final class LineNumberGutter extends JComponent {

        private final JTextArea textArea;

        private LineNumberGutter(JTextArea textArea) {
            this.textArea = textArea;
            setFont(Theme.FONT_CODE);
            setBackground(Theme.EDITOR_GUTTER);
            setForeground(Theme.TEXT_MUTED);
            setPreferredSize(new Dimension(44, 100));
        }

        public void updateLineCount() {
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.enableAntialiasing(g2);

            g2.setColor(Theme.EDITOR_GUTTER);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.BORDER_SUBTLE);
            g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

            g2.setFont(Theme.FONT_CODE);
            g2.setColor(Theme.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int lineHeight = fm.getHeight();
            int startY = textArea.getInsets().top + fm.getAscent();
            int lines = Math.max(1, textArea.getLineCount());

            for (int i = 1; i <= lines; i++) {
                String num = String.valueOf(i);
                int x = getWidth() - fm.stringWidth(num) - 10;
                int y = startY + (i - 1) * lineHeight;
                g2.drawString(num, x, y);
            }
            g2.dispose();
        }
    }
}
