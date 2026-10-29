package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.Lesson;
import javax.swing.*;
import java.awt.*;

public class LessonFrame extends JFrame {
    private Lesson currentLesson;

    public LessonFrame(Lesson lesson) {
        this.currentLesson = lesson;
        initComponents();
    }

    private void initComponents() {
        setTitle("LevelUp Code — Lección");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(currentLesson != null ? currentLesson.getTitle() : "Herencia y Polimorfismo en Java");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JTextArea txtTheory = new JTextArea();
        txtTheory.setText("Contenido teórico de la lección:\n\n"
                + "1. Relaciones ES-UN en la Programación Orientada a Objetos.\n"
                + "2. Uso de clases abstractas y métodos sobrescritos (@Override).\n"
                + "3. Aplicación de principios SOLID en el diseño de clases.");
        txtTheory.setFont(Theme.FONT_REGULAR);
        txtTheory.setForeground(Theme.TEXT_PRIMARY);
        txtTheory.setBackground(Theme.SURFACE);
        txtTheory.setEditable(false);
        txtTheory.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(txtTheory);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        JButton btnPractice = new JButton("Ir a los Retos 🚀");
        btnPractice.setBackground(Theme.ACCENT_INDIGO);
        btnPractice.setForeground(Color.WHITE);
        btnPractice.setFont(Theme.FONT_SUBTITLE);
        btnPractice.setFocusPainted(false);
        btnPractice.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Abriendo entorno de práctica...");
        });

        bottomPanel.add(btnPractice);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}
