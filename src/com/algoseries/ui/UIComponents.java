package com.algoseries.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Reusable custom Swing components used throughout AlgoSeries-Pro.
 * All components read colours from ThemeManager and repaint on theme change.
 */
public class UIComponents {

    /* ─────────────────────────────────────────────────────────────
     *  StyledButton  –  rounded, gradient, hover + press effects
     * ───────────────────────────────────────────────────────────── */
    public static class StyledButton extends JButton {

        public enum Style { PRIMARY, SECONDARY, DANGER, SUCCESS, GHOST }

        private final Style style;
        private boolean hovered = false;
        private boolean pressed = false;
        private float alpha = 1f;

        public StyledButton(String text, Style style) {
            super(text);
            this.style = style;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 13));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hovered = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        public StyledButton(String text) { this(text, Style.PRIMARY); }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            float arc = 10f;
            RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);

            Color base, hover, press;
            Color textColor = Color.WHITE;

            switch (style) {
                case PRIMARY:
                    base  = tm.ACCENT;
                    hover = tm.ACCENT_DARK;
                    press = new Color(50, 52, 160);
                    break;
                case DANGER:
                    base  = tm.ERROR;
                    hover = new Color(200, 30, 30);
                    press = new Color(170, 20, 20);
                    break;
                case SUCCESS:
                    base  = tm.SUCCESS;
                    hover = new Color(16, 170, 110);
                    press = new Color(10, 145, 90);
                    break;
                case SECONDARY:
                    base  = tm.BG_HOVER;
                    hover = tm.BORDER_LIGHT;
                    press = tm.BORDER;
                    textColor = tm.TEXT_PRIMARY;
                    break;
                default: // GHOST
                    base  = new Color(0, 0, 0, 0);
                    hover = tm.ACCENT_DIM;
                    press = tm.BORDER;
                    textColor = tm.ACCENT;
                    break;
            }

            Color fill = pressed ? press : hovered ? hover : base;
            g2.setColor(fill);
            g2.fill(shape);

            // Subtle top highlight
            if (style != Style.GHOST && style != Style.SECONDARY) {
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(255,255,255,40), 0, h/2, new Color(0,0,0,0));
                g2.setPaint(gp);
                g2.fill(shape);
            }

            g2.dispose();

            setForeground(textColor);
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 28, Math.max(d.height, 36));
        }
    }

    /* ─────────────────────────────────────────────────────────────
     *  CardPanel  –  rounded-corner card with optional title
     * ───────────────────────────────────────────────────────────── */
    public static class CardPanel extends JPanel {
        private String title;
        private boolean hasShadow;
        private int arc = 14;

        public CardPanel() { this(null, false); }
        public CardPanel(String title) { this(title, false); }
        public CardPanel(String title, boolean shadow) {
            this.title = title;
            this.hasShadow = shadow;
            setOpaque(false);
            int pad = title != null ? 40 : 16;
            setBorder(BorderFactory.createEmptyBorder(pad, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            if (hasShadow) {
                g2.setColor(new Color(0,0,0, tm.isDarkMode() ? 60 : 25));
                g2.fill(new RoundRectangle2D.Float(3, 4, w-4, h-4, arc, arc));
            }

            g2.setColor(tm.BG_CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, w-1, h-1, arc, arc));
            g2.setColor(tm.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w-1, h-1, arc, arc));

            if (title != null && !title.isEmpty()) {
                g2.setColor(tm.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString(title, 16, 26);
                g2.setColor(tm.BORDER);
                g2.drawLine(16, 34, w - 16, 34);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ─────────────────────────────────────────────────────────────
     *  Badge  –  small coloured pill label
     * ───────────────────────────────────────────────────────────── */
    public static class Badge extends JLabel {
        private Color bg, fg;

        public Badge(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.dispose();
            setForeground(fg);
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 14, d.height + 6);
        }
    }

    /* ─────────────────────────────────────────────────────────────
     *  RoundBorder  –  simple rounded-rect border
     * ───────────────────────────────────────────────────────────── */
    public static class RoundBorder extends AbstractBorder {
        private final int arc;
        private final Color color;

        public RoundBorder(Color color, int arc) {
            this.color = color; this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(x, y, w-1, h-1, arc, arc));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(4,8,4,8); }
    }

    /* ─────────────────────────────────────────────────────────────
     *  SectionLabel  –  section header with accent underline
     * ───────────────────────────────────────────────────────────── */
    public static class SectionLabel extends JLabel {
        public SectionLabel(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            setForeground(tm.TEXT_PRIMARY);
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(tm.ACCENT);
            g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, d.height + 6);
        }
    }

    /* ─────────────────────────────────────────────────────────────
     *  StyledTextField  –  rounded text field
     * ───────────────────────────────────────────────────────────── */
    public static class StyledTextField extends JTextField {
        public StyledTextField(int cols) {
            super(cols);
            setOpaque(false);
            ThemeManager tm = ThemeManager.getInstance();
            setBackground(tm.INPUT_BG);
            setForeground(tm.INPUT_TEXT);
            setCaretColor(tm.ACCENT);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(tm.INPUT_BORDER, 8),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        }
    }

    /* ─────────────────────────────────────────────────────────────
     *  StatCard  –  dashboard KPI tile
     * ───────────────────────────────────────────────────────────── */
    public static class StatCard extends JPanel {
        private final String title;
        private String value;
        private final String icon;
        private final Color accentColor;

        public StatCard(String icon, String title, String value, Color accent) {
            this.icon = icon; this.title = title;
            this.value = value; this.accentColor = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(190, 90));
        }

        public void setValue(String v) { this.value = v; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth(), h = getHeight();
            // Card background
            g2.setColor(tm.BG_CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, w-1, h-1, 14, 14));
            // Left accent stripe
            g2.setColor(accentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, 4, h, 4, 4));
            // Border
            g2.setColor(tm.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w-1, h-1, 14, 14));

            // Icon
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            g2.setColor(accentColor);
            g2.drawString(icon, 14, h/2 + 4);

            // Value
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(tm.TEXT_PRIMARY);
            g2.drawString(value, 52, h/2 + 6);

            // Title
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(tm.TEXT_SECONDARY);
            g2.drawString(title, 52, h/2 + 22);

            g2.dispose();
        }
    }
}
