package com.mycompany.levelup_code.presentation.component;

import javax.swing.*;
import java.awt.*;

public class ChallengeFrame extends JFrame {

    public ChallengeFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("LevelUp Code — Resolución de Reto");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("Reto: Sonidos de Animales (Java)");
        lblHeader.setFont(Theme.FONT_TITLE);
        lblHeader.setForeground(Theme.TEXT_PRIMARY);
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setOpaque(false);

        // Editor de código
        JPanel editorPanel = new JPanel(new BorderLayout(5, 5));
        editorPanel.setOpaque(false);
        editorPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            " Editor de Código ", 0, 0, Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY
        ));

        JTextArea txtEditor = new JTextArea();
        txtEditor.setText("public class Animal {\n    public void sonido() {\n        // Escribe tu solución aquí\n    }\n}");
        txtEditor.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtEditor.setBackground(Theme.SURFACE);
        txtEditor.setForeground(Theme.TEXT_PRIMARY);
        editorPanel.add(new JScrollPane(txtEditor), BorderLayout.CENTER);
        splitPane.setLeftComponent(editorPanel);

        // Panel lateral de casos de prueba
        JPanel sidePanel = new JPanel(new GridLayout(2, 1, 0, 15));
        sidePanel.setOpaque(false);

        JPanel testCasesPanel = new JPanel();
        testCasesPanel.setLayout(new BoxLayout(testCasesPanel, BoxLayout.Y_AXIS));
        testCasesPanel.setBackground(Theme.SURFACE);
        testCasesPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            " Casos de Prueba ", 0, 0, Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY
        ));
        
        JLabel lblTC1 = new JLabel("✔ OK: Perro → Guau");
        lblTC1.setForeground(Theme.ACCENT_GREEN);
        JLabel lblTC2 = new JLabel("✔ OK: Gato → Miau");
        lblTC2.setForeground(Theme.ACCENT_GREEN);

        testCasesPanel.add(lblTC1);
        testCasesPanel.add(Box.createVerticalStrut(5));
        testCasesPanel.add(lblTC2);
        sidePanel.add(testCasesPanel);

        JButton btnSubmit = new JButton("Enviar Solución");
        btnSubmit.setBackground(Theme.ACCENT_GREEN);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(Theme.FONT_SUBTITLE);
        btnSubmit.setFocusPainted(false);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnSubmit, BorderLayout.CENTER);
        sidePanel.add(btnWrapper);

        splitPane.setRightComponent(sidePanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);
    }
}
