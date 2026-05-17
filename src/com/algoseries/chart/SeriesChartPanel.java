package com.algoseries.chart;

import com.algoseries.model.SeriesResult;
import com.algoseries.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Custom chart panel that renders one or more series as line or bar charts
 * using pure Java 2D – no external dependencies.
 *
 * Features:
 * • Line chart and bar chart modes
 * • Multiple simultaneous series (colour-coded)
 * • Mouse-wheel zoom and middle-click pan
 * • Animated data-point appearance (fade-in)
 * • Hover tooltip showing exact values
 * • Export chart as PNG
 */
public class SeriesChartPanel extends JPanel {

    /* ── Chart mode ──────────────────────────────────────────────── */
    public enum ChartMode { LINE, BAR }

    /* ── Internal series representation ─────────────────────────── */
    private static class ChartSeries {
        final String label;
        final List<Double> data;
        final Color color;
        ChartSeries(String label, List<Double> data, Color color) {
            this.label = label; this.data = data; this.color = color;
        }
    }

    /* ── Fields ──────────────────────────────────────────────────── */
    private final List<ChartSeries> seriesList = new ArrayList<>();
    private ChartMode mode = ChartMode.LINE;
    private boolean showPoints  = true;
    private boolean showGrid    = true;
    private boolean showLegend  = true;
    private boolean smoothLines = true;

    // Zoom / pan
    private double zoomX     = 1.0;
    private double panX      = 0.0;
    private int lastDragX    = 0;

    // Hover
    private int hoverIndex   = -1;
    private String hoverText = "";

    // Margins
    private static final int MARGIN_LEFT   = 72;
    private static final int MARGIN_RIGHT  = 24;
    private static final int MARGIN_TOP    = 36;
    private static final int MARGIN_BOTTOM = 56;

    /* ── Constructor ─────────────────────────────────────────────── */
    public SeriesChartPanel() {
        setPreferredSize(new Dimension(700, 380));
        setOpaque(false);

        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.12 : 0.89;
            zoomX = Math.max(1.0, Math.min(20.0, zoomX * factor));
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { lastDragX = e.getX(); }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastDragX;
                lastDragX = e.getX();
                panX = Math.max(0, Math.min(1.0 - 1.0/zoomX, panX - dx / (getWidth() * zoomX * 0.5)));
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getX(), e.getY());
                repaint();
            }
        });
    }

    /* ── Data management ─────────────────────────────────────────── */
    public void addSeries(String label, List<Double> data, Color color) {
        seriesList.add(new ChartSeries(label, data, color));
        resetView();
        repaint();
    }

    public void setFromResult(SeriesResult result) {
        clearSeries();
        Color c = ThemeManager.CHART_SERIES_COLORS[0];
        addSeries(result.getType().getDisplayName(), result.getValues(), c);
    }

    public void clearSeries() {
        seriesList.clear();
        resetView();
        repaint();
    }

    public void setMode(ChartMode m) { this.mode = m; repaint(); }
    public void setShowGrid(boolean v) { showGrid = v; repaint(); }
    public void setShowPoints(boolean v) { showPoints = v; repaint(); }
    public void setSmoothLines(boolean v) { smoothLines = v; repaint(); }

    private void resetView() { zoomX = 1.0; panX = 0.0; }

    /* ── Paint ───────────────────────────────────────────────────── */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(tm.CHART_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

        if (seriesList.isEmpty()) {
            drawEmptyState(g2, w, h, tm);
            g2.dispose(); return;
        }

        // Compute data bounds
        double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
        int maxPoints = 0;
        for (ChartSeries s : seriesList) {
            maxPoints = Math.max(maxPoints, s.data.size());
            for (double v : s.data) {
                if (!Double.isNaN(v) && !Double.isInfinite(v)) {
                    minVal = Math.min(minVal, v);
                    maxVal = Math.max(maxVal, v);
                }
            }
        }
        if (minVal == maxVal) { minVal -= 1; maxVal += 1; }
        // Nice padding
        double range = maxVal - minVal;
        minVal -= range * 0.05;
        maxVal += range * 0.10;

        int chartW = w - MARGIN_LEFT - MARGIN_RIGHT;
        int chartH = h - MARGIN_TOP  - MARGIN_BOTTOM;

        // Grid and axes
        if (showGrid) drawGrid(g2, tm, MARGIN_LEFT, MARGIN_TOP, chartW, chartH, minVal, maxVal, maxPoints);
        drawAxes(g2, tm, MARGIN_LEFT, MARGIN_TOP, chartW, chartH, minVal, maxVal, maxPoints);

        // Series
        for (int si = 0; si < seriesList.size(); si++) {
            ChartSeries cs = seriesList.get(si);
            if (mode == ChartMode.LINE)
                drawLineSeries(g2, cs, si, MARGIN_LEFT, MARGIN_TOP, chartW, chartH, minVal, maxVal, maxPoints);
            else
                drawBarSeries(g2, cs, si, seriesList.size(), MARGIN_LEFT, MARGIN_TOP, chartW, chartH, minVal, maxVal, maxPoints);
        }

        // Legend
        if (showLegend && seriesList.size() > 1)
            drawLegend(g2, tm, w, MARGIN_TOP);

        // Hover tooltip
        if (hoverIndex >= 0 && !hoverText.isEmpty())
            drawTooltip(g2, tm, hoverIndex, hoverText, MARGIN_LEFT, MARGIN_TOP, chartW, chartH, minVal, maxVal, maxPoints);

        // Axis title
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(tm.CHART_AXIS);
        g2.drawString("Index (n)", MARGIN_LEFT + chartW/2 - 30, h - 8);

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int w, int h, ThemeManager tm) {
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        g2.setColor(tm.TEXT_MUTED);
        String msg = "Generate a series to see the chart";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg))/2, h/2);
    }

    private void drawGrid(Graphics2D g2, ThemeManager tm,
                          int ox, int oy, int cw, int ch,
                          double min, double max, int pts) {
        g2.setColor(tm.CHART_GRID);
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1f, new float[]{4, 4}, 0));
        int yLines = 6;
        for (int i = 0; i <= yLines; i++) {
            int y = oy + ch - (int)(ch * i / (double)yLines);
            g2.drawLine(ox, y, ox + cw, y);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawAxes(Graphics2D g2, ThemeManager tm,
                          int ox, int oy, int cw, int ch,
                          double min, double max, int pts) {
        g2.setColor(tm.CHART_AXIS);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(ox, oy, ox, oy + ch);           // Y axis
        g2.drawLine(ox, oy + ch, ox + cw, oy + ch); // X axis

        // Y labels
        int yLines = 6;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(tm.TEXT_MUTED);
        for (int i = 0; i <= yLines; i++) {
            double val = min + (max - min) * i / yLines;
            int y = oy + ch - (int)(ch * i / (double)yLines);
            String label = formatAxisValue(val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, ox - fm.stringWidth(label) - 6, y + 4);
            g2.setColor(tm.CHART_GRID);
        }

        // X labels
        g2.setColor(tm.TEXT_MUTED);
        int labelStep = Math.max(1, pts / 10);
        for (int i = 0; i < pts; i += labelStep) {
            double xFrac = (i + 0.5) / pts;
            int x = ox + (int)(cw * xFrac);
            g2.drawString(String.valueOf(i + 1), x - 4, oy + ch + 16);
        }
    }

    private void drawLineSeries(Graphics2D g2, ChartSeries cs, int si,
                                int ox, int oy, int cw, int ch,
                                double min, double max, int pts) {
        if (cs.data.isEmpty()) return;

        // Build path
        Path2D.Float path = new Path2D.Float();
        boolean first = true;
        List<Point> pointCoords = new ArrayList<>();

        for (int i = 0; i < cs.data.size(); i++) {
            double v = cs.data.get(i);
            if (Double.isNaN(v) || Double.isInfinite(v)) continue;
            double xFrac = (i + 0.5) / pts;
            int x = ox + (int)(cw * xFrac);
            int y = oy + ch - (int)(ch * (v - min) / (max - min));
            y = Math.max(oy, Math.min(oy + ch, y));
            pointCoords.add(new Point(x, y));
            if (first) { path.moveTo(x, y); first = false; }
            else path.lineTo(x, y);
        }

        // Area fill
        if (pointCoords.size() > 1) {
            Path2D.Float area = new Path2D.Float(path);
            area.lineTo(pointCoords.get(pointCoords.size()-1).x, oy + ch);
            area.lineTo(pointCoords.get(0).x, oy + ch);
            area.closePath();
            Color areaColor = new Color(cs.color.getRed(), cs.color.getGreen(),
                    cs.color.getBlue(), ThemeManager.getInstance().isDarkMode() ? 30 : 20);
            g2.setColor(areaColor);
            g2.fill(area);
        }

        // Line
        g2.setColor(cs.color);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);

        // Points
        if (showPoints && cs.data.size() <= 80) {
            for (int i = 0; i < pointCoords.size(); i++) {
                Point p = pointCoords.get(i);
                boolean hover = (i == hoverIndex);
                int r = hover ? 7 : 4;
                g2.setColor(cs.color);
                g2.fillOval(p.x - r, p.y - r, r*2, r*2);
                g2.setColor(ThemeManager.getInstance().CHART_BG);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(p.x - r + 1, p.y - r + 1, (r-1)*2, (r-1)*2);
                g2.setStroke(new BasicStroke(2f));
            }
        }
    }

    private void drawBarSeries(Graphics2D g2, ChartSeries cs, int si, int total,
                                int ox, int oy, int cw, int ch,
                                double min, double max, int pts) {
        int barW = Math.max(2, (int)((cw / (double)pts) / total) - 2);
        for (int i = 0; i < cs.data.size(); i++) {
            double v = cs.data.get(i);
            if (Double.isNaN(v) || Double.isInfinite(v)) continue;
            double norm = (v - min) / (max - min);
            int barH = (int)(ch * norm);
            double groupX = ox + (double)cw * i / pts;
            int x = (int)(groupX + si * (barW + 1));
            int y = oy + ch - barH;
            g2.setColor(cs.color);
            g2.fillRoundRect(x, y, barW, barH, 3, 3);
            g2.setColor(new Color(0,0,0,30));
            g2.drawRoundRect(x, y, barW, barH, 3, 3);
        }
    }

    private void drawLegend(Graphics2D g2, ThemeManager tm, int w, int oy) {
        int lx = w - MARGIN_RIGHT - seriesList.size() * 130;
        int ly = oy - 18;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (ChartSeries s : seriesList) {
            g2.setColor(s.color);
            g2.fillRoundRect(lx, ly, 12, 12, 3, 3);
            g2.setColor(tm.TEXT_SECONDARY);
            g2.drawString(s.label, lx + 16, ly + 11);
            lx += 130;
        }
    }

    private void drawTooltip(Graphics2D g2, ThemeManager tm, int idx, String text,
                             int ox, int oy, int cw, int ch,
                             double min, double max, int pts) {
        int x = ox + (int)(cw * (idx + 0.5) / pts);
        // Draw vertical line
        g2.setColor(new Color(tm.ACCENT.getRed(), tm.ACCENT.getGreen(), tm.ACCENT.getBlue(), 120));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[]{3,3}, 0));
        g2.drawLine(x, oy, x, oy + ch);

        // Tooltip box
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 16;
        int th = 22;
        int tx = Math.min(x + 8, ox + cw - tw);
        int ty = oy + 8;
        g2.setColor(tm.isDarkMode() ? new Color(30,36,65, 220) : new Color(255,255,255,220));
        g2.fill(new RoundRectangle2D.Float(tx, ty, tw, th, 6, 6));
        g2.setColor(tm.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(tx, ty, tw, th, 6, 6));
        g2.setColor(tm.TEXT_PRIMARY);
        g2.drawString(text, tx + 8, ty + 15);
    }

    private void updateHover(int mx, int my) {
        if (seriesList.isEmpty()) { hoverIndex = -1; return; }
        int cw = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        int pts = seriesList.get(0).data.size();
        if (pts == 0) return;
        double frac = (mx - MARGIN_LEFT) / (double)cw;
        int idx = (int)(frac * pts);
        if (idx < 0 || idx >= pts) { hoverIndex = -1; return; }
        hoverIndex = idx;
        StringBuilder sb = new StringBuilder("n=" + (idx+1) + ": ");
        for (int i = 0; i < seriesList.size(); i++) {
            ChartSeries s = seriesList.get(i);
            if (idx < s.data.size()) {
                double v = s.data.get(idx);
                sb.append(String.format("%.4g", v));
                if (i < seriesList.size()-1) sb.append(" | ");
            }
        }
        hoverText = sb.toString();
    }

    /** Export the current chart to a PNG file. */
    public void exportToPNG(File file) throws Exception {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        paint(g2);
        g2.dispose();
        ImageIO.write(img, "PNG", file);
    }

    private String formatAxisValue(double v) {
        if (Math.abs(v) >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        if (Math.abs(v) >= 1_000)     return String.format("%.1fK", v / 1_000);
        if (v == Math.floor(v))       return String.valueOf((long) v);
        return String.format("%.3g", v);
    }
}
