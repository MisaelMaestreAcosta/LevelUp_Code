package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Challenge;
import com.mycompany.levelup_code.domain.model.Lesson;
import com.mycompany.levelup_code.domain.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * Visor interactivo de lección con teoría estructurada, comparador sintáctico
 * entre lenguajes (Java, Python, C++) y acceso directo al laboratorio de retos.
 */
public class LessonFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final User currentUser;
    private final Lesson currentLesson;
    private final Runnable onProgressCallback;
    private boolean lessonCompleted = false;

    private JTextArea txtComparativeCode;
    private JLabel lblComparisonCaption;
    private Theme.ModernButton btnTabJava;
    private Theme.ModernButton btnTabPython;
    private Theme.ModernButton btnTabCpp;
    private JLabel lblStatusInfo;
    private JButton btnMarkComplete;

    public LessonFrame(Lesson lesson) {
        this(null, lesson, null);
    }

    public LessonFrame(User user, Lesson lesson, Runnable onProgressCallback) {
        this.currentUser = user;
        this.currentLesson = lesson;
        this.onProgressCallback = onProgressCallback;
        initComponents();
        switchComparativeSnippet("JAVA");
    }

    private void initComponents() {
        String lessonTitle = currentLesson != null && currentLesson.getTitle() != null
                ? currentLesson.getTitle()
                : "Herencia, Clases Abstractas y Polimorfismo";
        int reward = currentLesson != null && currentLesson.getExpReward() > 0
                ? currentLesson.getExpReward()
                : 50;

        setTitle("LevelUp Code — Lección: " + lessonTitle);
        setSize(1040, 640);
        setMinimumSize(new Dimension(900, 560));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        // Encabezado de la lección
        Theme.RoundedPanel headerCard = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        headerCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("  " + lessonTitle);
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        lblTitle.setIcon(Theme.createVectorIcon(Theme.IconKind.BOOK, 20, Theme.ACCENT_INDIGO));

        leftHeader.add(lblTitle);
        leftHeader.add(Theme.createPillBadge("LECCIÓN #" + (currentLesson != null ? currentLesson.getOrderIndex() : 1), Theme.ACCENT_INDIGO));
        leftHeader.add(Theme.createPillBadge("+" + reward + " XP", Theme.ACCENT_GREEN));

        headerCard.add(leftHeader, BorderLayout.WEST);
        root.add(headerCard, BorderLayout.NORTH);

        // Panel dividido: Teoría Conceptual (Izquierda) vs Código Comparativo (Derecha)
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, buildTheoryPane(), buildComparativeCodePane());
        splitPane.setDividerLocation(510);
        splitPane.setResizeWeight(0.50);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        root.add(splitPane, BorderLayout.CENTER);

        // Barra inferior de progreso y salto al reto
        root.add(buildBottomBar(reward), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTheoryPane() {
        Theme.RoundedPanel card = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        JPanel top = Theme.createSectionHeader("Fundamento Teórico y Diseño", "Lectura interactiva");
        card.add(top, BorderLayout.NORTH);

        JTextArea txtTheory = new JTextArea();
        txtTheory.setEditable(false);
        txtTheory.setLineWrap(true);
        txtTheory.setWrapStyleWord(true);
        txtTheory.setFont(Theme.FONT_REGULAR);
        txtTheory.setForeground(Theme.TEXT_PRIMARY);
        txtTheory.setBackground(Theme.EDITOR_BG);
        txtTheory.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

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

        JScrollPane scroll = new JScrollPane(txtTheory);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0),
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1)
        ));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildComparativeCodePane() {
        Theme.RoundedPanel card = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblSection = new JLabel("TRADUCCIÓN ENTRE PARADIGMAS");
        lblSection.setFont(Theme.FONT_SECTION);
        lblSection.setForeground(Theme.TEXT_MUTED);
        header.add(lblSection, BorderLayout.WEST);

        JPanel langSwitch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        langSwitch.setOpaque(false);

        btnTabJava = new Theme.ModernButton("Java", Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
        btnTabPython = new Theme.ModernButton("Python", Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
        btnTabCpp = new Theme.ModernButton("C++", Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);

        Insets compact = new Insets(4, 10, 4, 10);
        btnTabJava.setMargin(compact);
        btnTabPython.setMargin(compact);
        btnTabCpp.setMargin(compact);
        btnTabJava.setFont(Theme.FONT_SMALL);
        btnTabPython.setFont(Theme.FONT_SMALL);
        btnTabCpp.setFont(Theme.FONT_SMALL);

        btnTabJava.addActionListener(e -> switchComparativeSnippet("JAVA"));
        btnTabPython.addActionListener(e -> switchComparativeSnippet("PYTHON"));
        btnTabCpp.addActionListener(e -> switchComparativeSnippet("CPP"));

        langSwitch.add(btnTabJava);
        langSwitch.add(btnTabPython);
        langSwitch.add(btnTabCpp);
        header.add(langSwitch, BorderLayout.EAST);

        txtComparativeCode = new JTextArea();
        txtComparativeCode.setEditable(false);
        txtComparativeCode.setFont(Theme.FONT_CODE);
        txtComparativeCode.setBackground(Theme.EDITOR_BG);
        txtComparativeCode.setForeground(Theme.TEXT_PRIMARY);
        txtComparativeCode.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        lblComparisonCaption = new JLabel();
        lblComparisonCaption.setFont(Theme.FONT_SMALL);
        lblComparisonCaption.setForeground(Theme.ACCENT_CYAN);
        lblComparisonCaption.setIcon(Theme.createVectorIcon(Theme.IconKind.CODE, 14, Theme.ACCENT_CYAN));
        lblComparisonCaption.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 2));

        JScrollPane scrollCode = new JScrollPane(txtComparativeCode);
        scrollCode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0),
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1)
        ));

        card.add(header, BorderLayout.NORTH);
        card.add(scrollCode, BorderLayout.CENTER);
        card.add(lblComparisonCaption, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildBottomBar(final int reward) {
        Theme.RoundedPanel bar = new Theme.RoundedPanel(12, Theme.SURFACE, Theme.BORDER_SUBTLE);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        lblStatusInfo = new JLabel("Estado de la lección: EN CURSO · Completa la lectura o pasa al reto práctico.");
        lblStatusInfo.setFont(Theme.FONT_REGULAR);
        lblStatusInfo.setForeground(Theme.TEXT_SECONDARY);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        btnMarkComplete = Theme.createSecondaryButton("Marcar Lección Completada (+" + reward + " XP)");
        btnMarkComplete.setIcon(Theme.createVectorIcon(Theme.IconKind.CHECK, 14, Theme.ACCENT_GREEN));
        btnMarkComplete.addActionListener(e -> {
            if (!lessonCompleted) {
                lessonCompleted = true;
                btnMarkComplete.setEnabled(false);
                btnMarkComplete.setText("Lección Completada");
                if (currentUser != null && reward > 0) {
                    currentUser.addExperience(reward);
                    currentUser.setLevel(Math.max(1, 1 + currentUser.getTotalExp() / 300));
                }
                if (onProgressCallback != null) {
                    onProgressCallback.run();
                }
                lblStatusInfo.setForeground(Theme.ACCENT_GREEN);
                lblStatusInfo.setText("¡Lección aprobada! Se sumaron +" + reward + " XP a tu progreso.");
            }
        });

        JButton btnOpenChallenge = Theme.createPrimaryButton("Abrir Laboratorio de Retos", Theme.ACCENT_INDIGO);
        btnOpenChallenge.setIcon(Theme.createVectorIcon(Theme.IconKind.PLAY, 14, Color.WHITE));
        btnOpenChallenge.addActionListener(e -> {
            Challenge firstChallenge = (currentLesson != null && currentLesson.getChallenges() != null
                    && !currentLesson.getChallenges().isEmpty())
                    ? currentLesson.getChallenges().get(0)
                    : null;
            ChallengeFrame challengeFrame = new ChallengeFrame(currentUser, firstChallenge, onProgressCallback);
            challengeFrame.setVisible(true);
        });

        buttons.add(btnMarkComplete);
        buttons.add(btnOpenChallenge);

        bar.add(lblStatusInfo, BorderLayout.WEST);
        bar.add(buttons, BorderLayout.EAST);
        return bar;
    }

    private void switchComparativeSnippet(String lang) {
        if ("JAVA".equals(lang)) {
            btnTabJava.setPalette(Theme.ACCENT_INDIGO, Theme.ACCENT_INDIGO_HOVER, Color.WHITE, null);
            btnTabPython.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            btnTabCpp.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
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
            lblComparisonCaption.setText("Java usa 'extends' y '@Override'; el despacho dinámico es automático.");
        } else if ("PYTHON".equals(lang)) {
            btnTabPython.setPalette(Theme.ACCENT_GREEN, Theme.ACCENT_GREEN_HOVER, Color.WHITE, null);
            btnTabJava.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            btnTabCpp.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            txtComparativeCode.setText(
                    "# Python 3.12: Módulo abc (Abstract Base Classes)\n"
                    + "from abc import ABC, abstractmethod\n\n"
                    + "class Animal(ABC):\n"
                    + "    @abstractmethod\n"
                    + "    def emitir_sonido(self) -> str:\n"
                    + "        pass\n\n"
                    + "class Perro(Animal):\n"
                    + "    def emitir_sonido(self) -> str:\n"
                    + "        return \"Guau\""
            );
            lblComparisonCaption.setText("Python emplea decoradores '@abstractmethod' y resolución dinámica de nombres.");
        } else {
            btnTabCpp.setPalette(Theme.ACCENT_VIOLET, Theme.brighten(Theme.ACCENT_VIOLET, 0.15f), Color.WHITE, null);
            btnTabJava.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
            btnTabPython.setPalette(Theme.SURFACE_ELEVATED, Theme.SURFACE_LIGHT, Theme.TEXT_SECONDARY, Theme.BORDER_SUBTLE);
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
            lblComparisonCaption.setText("C++ requiere 'virtual' explícito y destructor virtual para liberar memoria sin fugas.");
        }
    }
}
