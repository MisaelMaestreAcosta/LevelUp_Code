package com.mycompany.levelup_code.presentation.component;

import com.mycompany.levelup_code.domain.model.User;
import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private User currentUser;

    public DashboardFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("LevelUp Code — Panel del Estudiante");
        setSize(980, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header de Bienvenida y Estadísticas
        JPanel headerPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        headerPanel.setOpaque(false);

        String username = (currentUser != null) ? currentUser.getUsername() : "Rafael Vega";
        int exp = (currentUser != null) ? currentUser.getTotalExp() : 1450;
        int level = (currentUser != null) ? currentUser.getLevel() : 5;
        int streak = (currentUser != null) ? currentUser.getCurrentStreak() : 7;

        headerPanel.add(createMetricCard("Estudiante", username, Theme.ACCENT_INDIGO));
        headerPanel.add(createMetricCard("Experiencia", exp + " XP", Theme.ACCENT_GREEN));
        headerPanel.add(createMetricCard("Nivel Actual", "Nivel " + level, new Color(245, 158, 11)));
        headerPanel.add(createMetricCard("Racha Activa", streak + " Días 🔥", new Color(239, 68, 68)));

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Centro: Cursos / Lenguajes Activos
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.SURFACE_LIGHT),
            " Tus Lenguajes de Estudio ",
            0, 0, Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY
        ));

        contentPanel.add(createCourseCard("Java", "POO y Principios SOLID", "75% Completado", Theme.ACCENT_INDIGO));
        contentPanel.add(createCourseCard("Python", "Estructuras y Algoritmos", "40% Completado", Theme.ACCENT_GREEN));
        contentPanel.add(createCourseCard("C++", "Punteros y Memoria", "15% Completado", new Color(147, 51, 234)));

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createMetricCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_REGULAR);
        lblTitle.setForeground(Theme.TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(Theme.FONT_TITLE);
        lblValue.setForeground(Theme.TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCourseCard(String lang, String desc, String progress, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblLang = new JLabel(lang);
        lblLang.setFont(Theme.FONT_TITLE);
        lblLang.setForeground(color);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(Theme.FONT_REGULAR);
        lblDesc.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblProg = new JLabel(progress);
        lblProg.setFont(Theme.FONT_REGULAR);
        lblProg.setForeground(Theme.TEXT_MUTED);

        JButton btnAction = new JButton("Continuar Lección");
        btnAction.setBackground(color);
        btnAction.setForeground(Color.WHITE);
        btnAction.setFont(Theme.FONT_SUBTITLE);
        btnAction.setFocusPainted(false);
        btnAction.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblLang);
        card.add(Box.createVerticalStrut(10));
        card.add(lblDesc);
        card.add(Box.createVerticalStrut(8));
        card.add(lblProg);
        card.add(Box.createVerticalStrut(20));
        card.add(btnAction);

        return card;
    }
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            User demoUser = new User();
            demoUser.setUsername("Daniela");
            demoUser.setTotalExp(1450);
            demoUser.setLevel(5);
            demoUser.setCurrentStreak(7);

            new DashboardFrame(demoUser).setVisible(true);
        });
    }
}
