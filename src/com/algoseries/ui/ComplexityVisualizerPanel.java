package com.algoseries.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Complexity Visualizer
 * Plots Big-O growth curves side by side: O(1), O(log n), O(n), O(n log n),
 * O(n²), O(2ⁿ) — with interactive legend and highlight on hover.
 */
public class ComplexityVisualizerPanel extends JPanel {

    private ComplexCanvas canvas;
    private JCheckBox[] checkBoxes;
    private JSlider nSlider;
    private JLabel nLabel;

    private static final String[] NAMES = {
        "O(1)       — Constant",
        "O(log n)   — Logarithmic",
        "O(n)       — Linear",
        "O(n log n) — Linearithmic",
        "O(n²)      — Quadratic",
        "O(2ⁿ)      — Exponential",
    };

    private static final Color[] COLORS = {
        new Color(52,  211, 153),   // green
        new Color(56,  189, 248),   // sky
        new Color(99,  102, 241),   // indigo
        new Color(251, 191,  36),   // amber
        new Color(239,  68,  68),   // red
        new Color(236,  72, 153),   // pink
    };

    private static final boolean[] DEFAULT_VISIBLE = { true, true, true, true, true, false };

    public ComplexityVisualizerPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        canvas = new ComplexCanvas();
        UIComponents.CardPanel cc = new UIComponents.CardPanel("Growth Curves");
        cc.setLayout(new BorderLayout());
        cc.add(canvas, BorderLayout.CENTER);
        add(cc, BorderLayout.CENTER);
        add(buildControls(), BorderLayout.EAST);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.add(new UIComponents.SectionLabel("Time Complexity Visualizer"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel lbl = new JLabel("Max n:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        nSlider = new JSlider(10, 200, 50);
        nSlider.setOpaque(false);
        nSlider.setPreferredSize(new Dimension(180, 28));
        nLabel = new JLabel("n = 50");
        nLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nLabel.setForeground(ThemeManager.getInstance().ACCENT);
        nSlider.addChangeListener(e -> {
            nLabel.setText("n = " + nSlider.getValue());
            canvas.repaint();
        });
        right.add(lbl); right.add(nSlider); right.add(nLabel);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildControls() {
        UIComponents.CardPanel card = new UIComponents.CardPanel("Toggle Curves");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(200, 0));

        checkBoxes = new JCheckBox[NAMES.length];
        for (int i = 0; i < NAMES.length; i++) {
            JCheckBox cb = new JCheckBox(NAMES[i], DEFAULT_VISIBLE[i]);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            cb.setOpaque(false);
            cb.setForeground(COLORS[i]);
            cb.setFocusPainted(false);
            cb.addActionListener(e -> canvas.repaint());
            checkBoxes[i] = cb;
            card.add(cb);
            card.add(Box.createVerticalStrut(4));
        }

        card.add(Box.createVerticalStrut(16));

        JLabel note = new JLabel("<html><small>Scroll the chart or<br>use n slider to zoom</small></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        note.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        card.add(note);

        return card;
    }

    /* ── Canvas ─────────────────────────────────────────────────── */
    private class ComplexCanvas extends JPanel {
        private static final int ML=72, MR=20, MT=30, MB=50;

        ComplexCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(600, 400));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ThemeManager tm = ThemeManager.getInstance();

            int w = getWidth(), h = getHeight();
            int cw = w-ML-MR, ch = h-MT-MB;

            // Background
            g2.setColor(tm.CHART_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

            int maxN = nSlider.getValue();

            // Compute maxY from visible curves
            double maxY = computeMaxY(maxN);
            if (maxY <= 0) maxY = 1;

            // Grid
            g2.setColor(tm.CHART_GRID);
            g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{4,4}, 0));
            for (int i = 0; i <= 5; i++) {
                int y = MT + (int)(ch * i / 5.0);
                g2.drawLine(ML, y, ML+cw, y);
            }
            g2.setStroke(new BasicStroke(1f));

            // Axes
            g2.setColor(tm.CHART_AXIS);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(ML, MT, ML, MT+ch);
            g2.drawLine(ML, MT+ch, ML+cw, MT+ch);

            // Y labels
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(tm.TEXT_MUTED);
            for (int i = 0; i <= 5; i++) {
                double val = maxY * (5-i) / 5.0;
                int y = MT + (int)(ch * i / 5.0);
                String s = formatVal(val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s, ML-fm.stringWidth(s)-5, y+4);
            }

            // X labels
            for (int i = 0; i <= 5; i++) {
                int xVal = maxN * i / 5;
                int x = ML + (int)(cw * i / 5.0);
                g2.drawString(String.valueOf(xVal), x-8, MT+ch+18);
            }

            // Axis titles
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString("n (input size)", ML+cw/2-40, h-6);
            // Y title
            Graphics2D gRot = (Graphics2D) g2.create();
            gRot.rotate(-Math.PI/2, 14, MT+ch/2);
            gRot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            gRot.setColor(tm.TEXT_MUTED);
            gRot.drawString("Operations", 14, MT+ch/2);
            gRot.dispose();

            // Curves
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int ci = 0; ci < NAMES.length; ci++) {
                if (!checkBoxes[ci].isSelected()) continue;
                g2.setColor(COLORS[ci]);
                Path2D.Float path = new Path2D.Float();
                boolean first = true;
                for (int xi = 1; xi <= maxN; xi++) {
                    double n2 = xi;
                    double y2 = computeY(ci, n2);
                    if (Double.isNaN(y2) || Double.isInfinite(y2) || y2 > maxY * 1.05) continue;
                    int px = ML + (int)(cw * (n2-1) / (maxN-1));
                    int py = MT + ch - (int)(ch * y2 / maxY);
                    py = Math.max(MT, py);
                    if (first) { path.moveTo(px, py); first=false; }
                    else path.lineTo(px, py);
                }
                g2.draw(path);
            }

            // Legend
            drawLegend(g2, tm, w, MT);
        }

        private double computeY(int ci, double n) {
            return switch(ci) {
                case 0 -> 1;
                case 1 -> Math.log(n) / Math.log(2);
                case 2 -> n;
                case 3 -> n * Math.log(n) / Math.log(2);
                case 4 -> n * n;
                case 5 -> Math.pow(2, n);
                default -> 0;
            };
        }

        private double computeMaxY(int maxN) {
            double max = 1;
            for (int ci = 0; ci < NAMES.length; ci++) {
                if (!checkBoxes[ci].isSelected()) continue;
                double y = computeY(ci, maxN);
                if (!Double.isInfinite(y) && !Double.isNaN(y) && y > max) max = y;
            }
            return max;
        }

        private void drawLegend(Graphics2D g2, ThemeManager tm, int w, int top) {
            int lx = ML, ly = top - 22;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < NAMES.length; i++) {
                if (!checkBoxes[i].isSelected()) continue;
                g2.setColor(COLORS[i]);
                g2.fillRoundRect(lx, ly, 10, 10, 3, 3);
                g2.setColor(tm.TEXT_SECONDARY);
                String label = NAMES[i].split(" ")[0].trim();
                g2.drawString(label, lx+13, ly+10);
                lx += g2.getFontMetrics().stringWidth(label) + 24;
            }
        }

        private String formatVal(double v) {
            if (v >= 1_000_000_000) return String.format("%.1fB", v/1e9);
            if (v >= 1_000_000) return String.format("%.1fM", v/1e6);
            if (v >= 1_000) return String.format("%.1fK", v/1e3);
            return String.valueOf((int)v);
        }
    }
}
