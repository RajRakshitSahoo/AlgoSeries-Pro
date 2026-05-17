package com.algoseries.ui;

import com.algoseries.algorithms.PascalGenerator;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.ArrayList;

/**
 * Patterns panel – visual rendering of number patterns.
 * Tabs: Pascal Triangle (visual), Number Pyramid, Star Patterns.
 */
public class PatternsPanel extends JPanel {

    public PatternsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        header.add(new UIComponents.SectionLabel("Pattern Generator"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabs.setOpaque(false);

        tabs.addTab("Pascal Triangle", buildPascalTab());
        tabs.addTab("Number Pyramid",  buildPyramidTab());
        tabs.addTab("Star Pattern",    buildStarTab());
        tabs.addTab("Multiplication",  buildMultTab());

        add(tabs, BorderLayout.CENTER);
    }

    /* ── Pascal Triangle (graphical) ─────────────────────────────── */
    private JPanel buildPascalTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        // Controls
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        ctrl.setOpaque(false);
        JLabel lbl = new JLabel("Rows:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        JSpinner rowSpin = new JSpinner(new SpinnerNumberModel(8, 1, 16, 1));
        rowSpin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UIComponents.StyledButton genBtn = new UIComponents.StyledButton("Generate", UIComponents.StyledButton.Style.PRIMARY);
        ctrl.add(lbl); ctrl.add(rowSpin); ctrl.add(genBtn);
        outer.add(ctrl, BorderLayout.NORTH);

        // Visual canvas
        PascalCanvas canvas = new PascalCanvas();
        UIComponents.CardPanel card = new UIComponents.CardPanel();
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(canvas) {{
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
        }}, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER);

        genBtn.addActionListener(e -> {
            int rows = (int) rowSpin.getValue();
            PascalGenerator g = new PascalGenerator();
            SeriesResult r = g.generateIterative(rows);
            canvas.setData(r, rows);
        });

        // Default render
        PascalGenerator g = new PascalGenerator();
        canvas.setData(g.generateIterative(8), 8);

        return outer;
    }

    /* ── Number Pyramid ──────────────────────────────────────────── */
    private JPanel buildPyramidTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        ctrl.setOpaque(false);
        JLabel lbl = new JLabel("Height:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        JSpinner hSpin = new JSpinner(new SpinnerNumberModel(7, 1, 15, 1));
        hSpin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UIComponents.StyledButton genBtn = new UIComponents.StyledButton("Generate", UIComponents.StyledButton.Style.PRIMARY);
        ctrl.add(lbl); ctrl.add(hSpin); ctrl.add(genBtn);
        outer.add(ctrl, BorderLayout.NORTH);

        JTextArea output = buildOutputArea();
        JScrollPane scroll = styledScroll(output);
        UIComponents.CardPanel card = new UIComponents.CardPanel("Output");
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER);

        Runnable generate = () -> {
            int h = (int) hSpin.getValue();
            StringBuilder sb = new StringBuilder();
            int maxW = h * 3;
            for (int row = 1; row <= h; row++) {
                int pad = (maxW - row * 3) / 2;
                sb.append(" ".repeat(Math.max(0, pad)));
                for (int col = 1; col <= row; col++) sb.append(String.format("%-3d", col));
                sb.append("\n");
            }
            output.setText(sb.toString());
        };
        genBtn.addActionListener(e -> generate.run());
        generate.run();
        return outer;
    }

    /* ── Star Pattern ────────────────────────────────────────────── */
    private JPanel buildStarTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        ctrl.setOpaque(false);
        JLabel rowLbl = new JLabel("Rows:");
        rowLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rowLbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        JSpinner rSpin = new JSpinner(new SpinnerNumberModel(8, 1, 20, 1));
        rSpin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JComboBox<String> patternBox = new JComboBox<>(new String[]{
            "Right Triangle", "Pyramid", "Diamond", "Hollow Square", "Hourglass"});
        patternBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UIComponents.StyledButton genBtn = new UIComponents.StyledButton("Generate", UIComponents.StyledButton.Style.PRIMARY);
        ctrl.add(rowLbl); ctrl.add(rSpin); ctrl.add(patternBox); ctrl.add(genBtn);
        outer.add(ctrl, BorderLayout.NORTH);

        JTextArea output = buildOutputArea();
        JScrollPane scroll = styledScroll(output);
        UIComponents.CardPanel card = new UIComponents.CardPanel("Output");
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER);

        genBtn.addActionListener(e -> {
            int n = (int) rSpin.getValue();
            String pattern = (String) patternBox.getSelectedItem();
            output.setText(generateStarPattern(n, pattern));
        });

        output.setText(generateStarPattern(8, "Right Triangle"));
        return outer;
    }

    /* ── Multiplication Table ────────────────────────────────────── */
    private JPanel buildMultTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 4, 4, 4));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        ctrl.setOpaque(false);
        JLabel sizeLbl = new JLabel("Size (n×n):");
        sizeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sizeLbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        JSpinner nSpin = new JSpinner(new SpinnerNumberModel(10, 2, 20, 1));
        nSpin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UIComponents.StyledButton genBtn = new UIComponents.StyledButton("Generate", UIComponents.StyledButton.Style.PRIMARY);
        ctrl.add(sizeLbl); ctrl.add(nSpin); ctrl.add(genBtn);
        outer.add(ctrl, BorderLayout.NORTH);

        JTextArea output = buildOutputArea();
        output.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        JScrollPane scroll = styledScroll(output);
        UIComponents.CardPanel card = new UIComponents.CardPanel("Output");
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);
        outer.add(card, BorderLayout.CENTER);

        Runnable generate = () -> {
            int n = (int) nSpin.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append("    ");
            for (int i = 1; i <= n; i++) sb.append(String.format("%5d", i));
            sb.append("\n    ").append("─".repeat(n * 5 + 1)).append("\n");
            for (int i = 1; i <= n; i++) {
                sb.append(String.format("%3d │", i));
                for (int j = 1; j <= n; j++) sb.append(String.format("%5d", i * j));
                sb.append("\n");
            }
            output.setText(sb.toString());
        };
        genBtn.addActionListener(e -> generate.run());
        generate.run();
        return outer;
    }

    /* ── Star pattern generator ──────────────────────────────────── */
    private String generateStarPattern(int n, String type) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case "Right Triangle":
                for (int i = 1; i <= n; i++) { sb.append("* ".repeat(i).trim()); sb.append("\n"); }
                break;
            case "Pyramid":
                for (int i = 1; i <= n; i++) {
                    sb.append(" ".repeat(n - i));
                    sb.append("* ".repeat(i).trim());
                    sb.append("\n");
                }
                break;
            case "Diamond":
                for (int i = 1; i <= n; i++) {
                    sb.append(" ".repeat(n - i)).append("* ".repeat(i).trim()).append("\n");
                }
                for (int i = n - 1; i >= 1; i--) {
                    sb.append(" ".repeat(n - i)).append("* ".repeat(i).trim()).append("\n");
                }
                break;
            case "Hollow Square":
                for (int i = 1; i <= n; i++) {
                    for (int j = 1; j <= n; j++)
                        sb.append(i==1||i==n||j==1||j==n ? "* " : "  ");
                    sb.append("\n");
                }
                break;
            case "Hourglass":
                for (int i = n; i >= 1; i--) {
                    sb.append(" ".repeat(n - i)).append("* ".repeat(i).trim()).append("\n");
                }
                for (int i = 2; i <= n; i++) {
                    sb.append(" ".repeat(n - i)).append("* ".repeat(i).trim()).append("\n");
                }
                break;
        }
        return sb.toString();
    }

    /* ── Helpers ─────────────────────────────────────────────────── */
    private JTextArea buildOutputArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        ta.setBackground(ThemeManager.getInstance().INPUT_BG);
        ta.setForeground(ThemeManager.getInstance().INPUT_TEXT);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return ta;
    }

    private JScrollPane styledScroll(JTextArea ta) {
        JScrollPane sp = new JScrollPane(ta);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));
        return sp;
    }

    /* ── Pascal canvas ───────────────────────────────────────────── */
    static class PascalCanvas extends JPanel {
        private List<List<Long>> triangle = new ArrayList<>();

        PascalCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(600, 400));
        }

        void setData(SeriesResult r, int rows) {
            triangle.clear();
            List<Double> flat = r.getValues();
            int idx = 0;
            for (int row = 0; row < rows; row++) {
                List<Long> rowData = new ArrayList<>();
                for (int col = 0; col <= row && idx < flat.size(); col++, idx++)
                    rowData.add(flat.get(idx).longValue());
                triangle.add(rowData);
            }
            int needed = rows * 52 + 40;
            setPreferredSize(new Dimension(Math.max(600, rows * 70), needed));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int cellW = 60, cellH = 46;
            Color[] palette = ThemeManager.CHART_SERIES_COLORS;

            for (int row = 0; row < triangle.size(); row++) {
                List<Long> rowData = triangle.get(row);
                int startX = (w - rowData.size() * cellW) / 2;
                int y = 20 + row * cellH;
                for (int col = 0; col < rowData.size(); col++) {
                    long val = rowData.get(col);
                    int x = startX + col * cellW;
                    Color accent = palette[row % palette.length];
                    Color fill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                            tm.isDarkMode() ? 50 : 30);
                    g2.setColor(fill);
                    g2.fill(new RoundRectangle2D.Float(x + 3, y, cellW - 6, cellH - 6, 8, 8));
                    g2.setColor(accent);
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(x + 3, y, cellW - 6, cellH - 6, 8, 8));
                    g2.setColor(tm.TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, val > 999 ? 9 : val > 99 ? 11 : 13));
                    FontMetrics fm = g2.getFontMetrics();
                    String str = String.valueOf(val);
                    g2.drawString(str, x + (cellW - fm.stringWidth(str)) / 2, y + cellH / 2 + fm.getAscent() / 2 - 2);
                }
            }
            g2.dispose();
        }
    }
}
