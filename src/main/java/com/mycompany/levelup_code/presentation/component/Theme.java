package com.mycompany.levelup_code.presentation.component;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Sistema de diseño visual claro (Daylight Paper) y componentes personalizados para LevelUp Code.
 */
public final class Theme {

    // Paleta de superficies claras (Blanco limpio y gris papel suave)
    public static final Color BG_DARK = new Color(243, 245, 249);
    public static final Color BG_SIDEBAR = new Color(255, 255, 255);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color SURFACE_ELEVATED = new Color(241, 245, 249);
    public static final Color SURFACE_LIGHT = new Color(226, 232, 240);
    public static final Color EDITOR_BG = new Color(248, 250, 252);
    public static final Color EDITOR_GUTTER = new Color(238, 242, 246);
    public static final Color BORDER_SUBTLE = new Color(216, 222, 233);
    public static final Color BORDER_STRONG = new Color(180, 190, 206);

    // Paleta de acentos de alto contraste sobre fondo blanco
    public static final Color ACCENT_INDIGO = new Color(37, 99, 235);
    public static final Color ACCENT_INDIGO_HOVER = new Color(29, 78, 216);
    public static final Color ACCENT_CYAN = new Color(2, 132, 199);
    public static final Color ACCENT_GREEN = new Color(22, 163, 74);
    public static final Color ACCENT_GREEN_HOVER = new Color(21, 128, 61);
    public static final Color ACCENT_AMBER = new Color(217, 119, 6);
    public static final Color ACCENT_ROSE = new Color(225, 29, 72);
    public static final Color ACCENT_VIOLET = new Color(124, 58, 237);

    // Identidad cromática oficial por lenguaje de programación
    public static final Color LANG_JAVA = new Color(231, 111, 0);     // #E76F00 Java Orange
    public static final Color LANG_PYTHON = new Color(55, 118, 171);  // #3776AB Python Blue
    public static final Color LANG_CPP = new Color(0, 89, 156);       // #00599C C++ Steel Blue

    // Jerarquía tipográfica oscura sobre fondo claro
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(51, 65, 85);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);

    // Escala tipográfica
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_CODE = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_CODE_BOLD = new Font("Consolas", Font.BOLD, 13);

    private Theme() {
    }

    public static Color colorForLanguage(String langName) {
        if (langName == null) {
            return ACCENT_INDIGO;
        }
        String lower = langName.trim().toLowerCase();
        if (lower.contains("java")) {
            return LANG_JAVA;
        }
        if (lower.contains("python")) {
            return LANG_PYTHON;
        }
        if (lower.contains("c++") || lower.contains("cpp")) {
            return LANG_CPP;
        }
        return ACCENT_INDIGO;
    }

    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static JButton createPrimaryButton(String text, Color baseColor) {
        Color hoverColor = darken(baseColor, 0.12f);
        return new ModernButton(text, baseColor, hoverColor, Color.WHITE, null);
    }

    public static JButton createSecondaryButton(String text) {
        return new ModernButton(text, SURFACE_ELEVATED, SURFACE_LIGHT, TEXT_PRIMARY, BORDER_SUBTLE);
    }

    public static JButton createGhostButton(String text, Color textColor) {
        return new ModernButton(text, SURFACE, SURFACE_ELEVATED, textColor, BORDER_SUBTLE);
    }

    public static JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        styleInputComponent(field, placeholder);
        return field;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        styleInputComponent(field, null);
        return field;
    }

    private static void styleInputComponent(JTextField field, String toolTip) {
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_INDIGO);
        field.setBackground(SURFACE);
        if (toolTip != null && !toolTip.isEmpty()) {
            field.setToolTipText(toolTip);
        }
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_STRONG, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_INDIGO, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_STRONG, 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    public static JComponent createPillBadge(String text, Color accent) {
        return new PillBadge(text, accent);
    }

    public static ModernProgressBar createProgressBar(int value, int max, Color accent) {
        return new ModernProgressBar(value, max, accent);
    }

    public static Color brighten(Color color, float fraction) {
        int r = Math.min(255, Math.round(color.getRed() + (255 - color.getRed()) * fraction));
        int g = Math.min(255, Math.round(color.getGreen() + (255 - color.getGreen()) * fraction));
        int b = Math.min(255, Math.round(color.getBlue() + (255 - color.getBlue()) * fraction));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Color darken(Color color, float fraction) {
        int r = Math.max(0, Math.round(color.getRed() * (1f - fraction)));
        int g = Math.max(0, Math.round(color.getGreen() * (1f - fraction)));
        int b = Math.max(0, Math.round(color.getBlue() * (1f - fraction)));
        return new Color(r, g, b, color.getAlpha());
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    public static class RoundedPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private Color fillColor;
        private Color borderColor;
        private Color accentStripe;
        private final int radius;

        public RoundedPanel(int radius, Color fillColor, Color borderColor) {
            this(radius, fillColor, borderColor, null);
        }

        public RoundedPanel(int radius, Color fillColor, Color borderColor, Color accentStripe) {
            super(new BorderLayout());
            this.radius = radius;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            this.accentStripe = accentStripe;
            setOpaque(false);
        }

        public void setFillColor(Color fillColor) {
            this.fillColor = fillColor;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        public void setAccentStripe(Color accentStripe) {
            this.accentStripe = accentStripe;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);

            int w = getWidth();
            int h = getHeight();

            if (fillColor != null) {
                g2.setColor(fillColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, radius, radius));
            }

            if (accentStripe != null) {
                g2.setColor(accentStripe);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, h - 1, radius, radius));
                g2.fillRect(3, 0, 2, h - 1);
            }

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, radius, radius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ModernButton extends JButton {

        private static final long serialVersionUID = 1L;
        private Color baseColor;
        private Color hoverColor;
        private Color borderColor;
        private boolean hovered;
        private boolean pressed;

        public ModernButton(String text, Color baseColor, Color hoverColor, Color fgColor, Color borderColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.borderColor = borderColor;

            setFont(FONT_SUBTITLE);
            setForeground(fgColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        public void setPalette(Color baseColor, Color hoverColor, Color fgColor, Color borderColor) {
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.borderColor = borderColor;
            setForeground(fgColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);

            int w = getWidth();
            int h = getHeight();

            Color activeFill = !isEnabled()
                    ? SURFACE_LIGHT
                    : (pressed ? darken(hoverColor, 0.08f) : (hovered ? hoverColor : baseColor));

            if (activeFill.getAlpha() > 0) {
                g2.setColor(activeFill);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));
            }

            if (borderColor != null) {
                g2.setColor(hovered ? darken(borderColor, 0.15f) : borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 8, 8));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class PillBadge extends JLabel {

        private static final long serialVersionUID = 1L;
        private Color accent;

        public PillBadge(String text, Color accent) {
            super(text);
            this.accent = accent;
            setFont(FONT_SECTION);
            setForeground(darken(accent, 0.15f));
            setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
            setOpaque(false);
        }

        public void setAccent(Color accent) {
            this.accent = accent;
            setForeground(darken(accent, 0.15f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(withAlpha(accent, 26));
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h));
            g2.setColor(withAlpha(accent, 85));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, h, h));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ModernProgressBar extends JComponent {

        private static final long serialVersionUID = 1L;
        private int value;
        private int maximum;
        private Color accent;

        public ModernProgressBar(int value, int maximum, Color accent) {
            this.value = value;
            this.maximum = Math.max(1, maximum);
            this.accent = accent;
            setPreferredSize(new Dimension(120, 8));
            setMinimumSize(new Dimension(60, 8));
        }

        public void setProgress(int value, int maximum) {
            this.value = Math.max(0, value);
            this.maximum = Math.max(1, maximum);
            repaint();
        }

        public void setAccent(Color accent) {
            this.accent = accent;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(SURFACE_LIGHT);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));

            float ratio = Math.min(1f, Math.max(0f, (float) value / (float) maximum));
            int fillW = Math.round(w * ratio);
            if (fillW > 0) {
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, Math.max(h, fillW), h, h, h));
            }

            g2.dispose();
        }
    }

    public enum IconKind {
        TERMINAL,
        FLAME,
        BOLT,
        HEART,
        TROPHY,
        CODE,
        CHECK,
        PLAY,
        SHIELD,
        BOOK
    }

    public static Icon createVectorIcon(IconKind kind, int size, Color color) {
        return new VectorIcon(kind, size, color);
    }

    private static final class VectorIcon implements Icon {

        private final IconKind kind;
        private final int size;
        private final Color color;

        private VectorIcon(IconKind kind, int size, Color color) {
            this.kind = kind;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int s = size;
            switch (kind) {
                case TERMINAL: {
                    g2.drawRoundRect(1, 2, s - 2, s - 4, 4, 4);
                    g2.drawLine(4, s / 2 - 3, 8, s / 2);
                    g2.drawLine(8, s / 2, 4, s / 2 + 3);
                    g2.drawLine(10, s / 2 + 3, s - 4, s / 2 + 3);
                    break;
                }
                case FLAME: {
                    Path2D flame = new Path2D.Float();
                    flame.moveTo(s * 0.52, s * 0.08);
                    flame.curveTo(s * 0.82, s * 0.34, s * 0.90, s * 0.62, s * 0.72, s * 0.84);
                    flame.curveTo(s * 0.60, s * 0.95, s * 0.40, s * 0.95, s * 0.28, s * 0.84);
                    flame.curveTo(s * 0.12, s * 0.65, s * 0.22, s * 0.38, s * 0.52, s * 0.08);
                    flame.closePath();
                    g2.fill(flame);
                    break;
                }
                case BOLT: {
                    Polygon bolt = new Polygon(
                            new int[]{s / 2 + 2, s / 4, s / 2, s / 2 - 2, s * 3 / 4, s / 2},
                            new int[]{1, s / 2, s / 2, s - 1, s / 2 - 1, s / 2 - 1},
                            6
                    );
                    g2.fillPolygon(bolt);
                    break;
                }
                case HEART: {
                    Path2D heart = new Path2D.Float();
                    heart.moveTo(s * 0.5, s * 0.86);
                    heart.curveTo(s * 0.12, s * 0.60, s * 0.08, s * 0.26, s * 0.28, s * 0.16);
                    heart.curveTo(s * 0.42, s * 0.10, s * 0.5, s * 0.24, s * 0.5, s * 0.30);
                    heart.curveTo(s * 0.5, s * 0.24, s * 0.58, s * 0.10, s * 0.72, s * 0.16);
                    heart.curveTo(s * 0.92, s * 0.26, s * 0.88, s * 0.60, s * 0.5, s * 0.86);
                    heart.closePath();
                    g2.fill(heart);
                    break;
                }
                case TROPHY: {
                    g2.drawRoundRect(s / 4, 2, s / 2, s / 2 - 1, 4, 4);
                    g2.drawArc(2, 3, s / 4, s / 3, 90, 180);
                    g2.drawArc(s - s / 4 - 2, 3, s / 4, s / 3, 270, 180);
                    g2.drawLine(s / 2, s / 2 + 1, s / 2, s - 4);
                    g2.drawLine(s / 4 + 1, s - 3, s * 3 / 4 - 1, s - 3);
                    break;
                }
                case CODE: {
                    g2.drawLine(s / 3, s / 4, 2, s / 2);
                    g2.drawLine(2, s / 2, s / 3, s * 3 / 4);
                    g2.drawLine(s * 2 / 3, s / 4, s - 2, s / 2);
                    g2.drawLine(s - 2, s / 2, s * 2 / 3, s * 3 / 4);
                    break;
                }
                case CHECK: {
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(3, s / 2 + 1, s / 2 - 1, s - 4);
                    g2.drawLine(s / 2 - 1, s - 4, s - 3, 4);
                    break;
                }
                case PLAY: {
                    Polygon play = new Polygon(
                            new int[]{s / 4 + 1, s / 4 + 1, s * 3 / 4 + 1},
                            new int[]{3, s - 3, s / 2},
                            3
                    );
                    g2.fillPolygon(play);
                    break;
                }
                case SHIELD: {
                    Polygon shield = new Polygon(
                            new int[]{s / 2, s - 2, s - 4, s / 2, 4, 2},
                            new int[]{2, 5, s * 2 / 3, s - 2, s * 2 / 3, 5},
                            6
                    );
                    g2.drawPolygon(shield);
                    break;
                }
                case BOOK: {
                    g2.drawRoundRect(3, 2, s - 6, s - 4, 3, 3);
                    g2.drawLine(6, 2, 6, s - 2);
                    g2.drawLine(9, 6, s - 6, 6);
                    g2.drawLine(9, 10, s - 6, 10);
                    break;
                }
                default:
                    break;
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    public static JPanel createSectionHeader(String title, String rightNote) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(TEXT_MUTED);
        panel.add(lblTitle, BorderLayout.CENTER);

        if (rightNote != null && !rightNote.isEmpty()) {
            JLabel lblRight = new JLabel(rightNote);
            lblRight.setFont(FONT_SMALL);
            lblRight.setForeground(TEXT_SECONDARY);
            panel.add(lblRight, BorderLayout.EAST);
        }

        return panel;
    }

    public static JComponent createAvatarBadge(String initials, Color accent, int diameter) {
        return new AvatarBadge(initials, accent, diameter);
    }

    public static class AvatarBadge extends JComponent {

        private static final long serialVersionUID = 1L;
        private final String initials;
        private final Color accent;
        private final int diameter;

        public AvatarBadge(String initials, Color accent, int diameter) {
            this.initials = initials;
            this.accent = accent;
            this.diameter = diameter;
            setPreferredSize(new Dimension(diameter, diameter));
            setMinimumSize(new Dimension(diameter, diameter));
            setMaximumSize(new Dimension(diameter, diameter));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            g2.setColor(withAlpha(accent, 28));
            g2.fillOval(0, 0, diameter - 1, diameter - 1);
            g2.setColor(withAlpha(accent, 110));
            g2.drawOval(0, 0, diameter - 1, diameter - 1);

            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(11, diameter / 3)));
            g2.setColor(darken(accent, 0.15f));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (diameter - fm.stringWidth(initials)) / 2;
            int ty = (diameter - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, tx, ty);
            g2.dispose();
        }
    }
}
