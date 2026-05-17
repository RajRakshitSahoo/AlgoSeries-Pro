package com.algoseries.ui;

import com.algoseries.algorithms.*;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Performance panel – benchmarks iterative vs recursive for every algorithm
 * at a user-specified n, showing time, memory, and a bar chart.
 */
public class PerformancePanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JProgressBar progressBar;
    private final PerformanceBarChart barChart;
    private final JLabel statusLabel;

    // 9 columns: 8 visible + 1 hidden (_data at index 8 stores the SeriesResult object)
    private static final String[] COLUMNS = {
        "Series", "Category", "Algorithm", "Time", "Memory", "Terms",
        "Time Complexity", "Space Complexity", "_data"
    };

    public PerformancePanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerRow.add(new UIComponents.SectionLabel("Performance Analysis"), BorderLayout.WEST);
        add(headerRow, BorderLayout.NORTH);

        // Controls
        JPanel controls = buildControls();
        add(controls, BorderLayout.NORTH);

        // Body: table + chart
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOpaque(false);
        split.setDividerSize(6);
        split.setDividerLocation(260);
        split.setBorder(null);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));

        UIComponents.CardPanel tableCard = new UIComponents.CardPanel("Benchmark Results");
        tableCard.setLayout(new BorderLayout());
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Progress + status
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        statusLabel = new JLabel("Click 'Run Benchmark' to start");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        JPanel statusRow = new JPanel(new BorderLayout(8, 0));
        statusRow.setOpaque(false);
        statusRow.add(statusLabel, BorderLayout.WEST);
        statusRow.add(progressBar, BorderLayout.CENTER);
        tableCard.add(statusRow, BorderLayout.SOUTH);

        split.setTopComponent(tableCard);

        // Bar chart
        barChart = new PerformanceBarChart();
        UIComponents.CardPanel chartCard = new UIComponents.CardPanel("Execution Time Comparison (µs)");
        chartCard.setLayout(new BorderLayout());
        chartCard.add(barChart, BorderLayout.CENTER);
        split.setBottomComponent(chartCard);

        add(split, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        UIComponents.CardPanel card = new UIComponents.CardPanel();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JLabel nLbl = new JLabel("Terms (n):");
        nLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nLbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        JSpinner nSpin = new JSpinner(new SpinnerNumberModel(30, 5, 200, 5));
        nSpin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nSpin.setPreferredSize(new Dimension(80, 28));

        JCheckBox iterChk = new JCheckBox("Iterative", true);
        JCheckBox recChk  = new JCheckBox("Recursive", true);
        styleCheck(iterChk); styleCheck(recChk);

        UIComponents.StyledButton runBtn = new UIComponents.StyledButton("▶ Run Benchmark", UIComponents.StyledButton.Style.PRIMARY);
        UIComponents.StyledButton clearBtn = new UIComponents.StyledButton("Clear", UIComponents.StyledButton.Style.SECONDARY);

        card.add(nLbl); card.add(nSpin);
        card.add(iterChk); card.add(recChk);
        card.add(runBtn); card.add(clearBtn);

        runBtn.addActionListener(e -> runBenchmark((int) nSpin.getValue(), iterChk.isSelected(), recChk.isSelected(), runBtn));
        clearBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            barChart.clear();
            statusLabel.setText("Cleared.");
        });

        return card;
    }

    private void runBenchmark(int n, boolean doIter, boolean doRec, JButton runBtn) {
        if (!doIter && !doRec) {
            JOptionPane.showMessageDialog(this, "Select at least one algorithm type.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        barChart.clear();
        runBtn.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);

        List<SeriesType> types = Arrays.asList(
            SeriesType.FIBONACCI, SeriesType.TRIBONACCI, SeriesType.LUCAS,
            SeriesType.PRIME, SeriesType.ARITHMETIC, SeriesType.GEOMETRIC,
            SeriesType.SQUARE, SeriesType.CUBE, SeriesType.FACTORIAL,
            SeriesType.HARMONIC
        );

        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int total = types.size() * ((doIter ? 1 : 0) + (doRec ? 1 : 0));
                int done = 0;
                for (SeriesType type : types) {
                    try {
                        if (doIter) {
                            SeriesResult r = runOne(type, n, false);
                            publish(toRow(r));
                            done++;
                            setProgress((int)(done * 100.0 / total));
                        }
                        if (doRec) {
                            SeriesResult r = runOne(type, n, true);
                            publish(toRow(r));
                            done++;
                            setProgress((int)(done * 100.0 / total));
                        }
                    } catch (Exception ex) { /* skip */ }
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] row : chunks) {
                    tableModel.addRow(row);
                    barChart.addBar((String)row[0] + " (" + row[2] + ")",
                            ((SeriesResult) row[8]).getExecutionTimeNs() / 1000.0);
                }
                progressBar.setValue(getProgress());
                statusLabel.setText("Running… " + tableModel.getRowCount() + " benchmarks");
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                runBtn.setEnabled(true);
                statusLabel.setText("Benchmark complete – " + tableModel.getRowCount() + " results");
            }
        };
        worker.execute();
    }

    private SeriesResult runOne(SeriesType type, int n, boolean recursive) {
        switch (type) {
            case FIBONACCI:  { var g = new FibonacciGenerator();  return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case TRIBONACCI: { var g = new TribonacciGenerator(); return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case LUCAS:      { var g = new LucasGenerator();      return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case PRIME:      { var g = new PrimeGenerator();      return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case ARITHMETIC: { var g = new APGenerator();         return recursive ? g.generateRecursive(n,1,2) : g.generateIterative(n,1,2); }
            case GEOMETRIC:  { var g = new GPGenerator();         return recursive ? g.generateRecursive(n,1,2) : g.generateIterative(n,1,2); }
            case SQUARE:     { var g = new SquareGenerator();     return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case CUBE:       { var g = new CubeGenerator();       return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case FACTORIAL:  { var g = new FactorialGenerator();  return recursive ? g.generateRecursive(n) : g.generateIterative(n); }
            case HARMONIC:   { var g = new HarmonicGenerator();   return recursive ? g.generateRecursive(n,1,1) : g.generateIterative(n,1,1); }
            default: throw new IllegalArgumentException("Unknown: " + type);
        }
    }

    /** Returns an Object[] row PLUS the SeriesResult in index 8 (hidden column). */
    private Object[] toRow(SeriesResult r) {
        return new Object[]{
            r.getType().getDisplayName(),
            r.getType().getCategory(),
            r.getAlgorithm(),
            r.getFormattedTime(),
            r.getFormattedMemory(),
            r.getCount(),
            r.getType().getTimeComplexity(),
            r.getType().getSpaceComplexity(),
            r  // hidden – used by bar chart
        };
    }

    private void styleTable(JTable table) {
        ThemeManager tm = ThemeManager.getInstance();
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setBackground(tm.BG_CARD);
        table.setForeground(tm.TEXT_PRIMARY);
        table.setGridColor(tm.BORDER);
        table.setSelectionBackground(tm.ACCENT_DIM);
        table.setSelectionForeground(tm.TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(tm.BG_SECONDARY);
        table.getTableHeader().setForeground(tm.TEXT_SECONDARY);
        // Hide the hidden result column
        table.getColumnModel().getColumn(8).setMinWidth(0);
        table.getColumnModel().getColumn(8).setMaxWidth(0);
        table.getColumnModel().getColumn(8).setWidth(0);
        // Column widths
        int[] widths = {140,110,170,90,90,60,120,120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private void styleCheck(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
    }

    /* ── Simple horizontal bar chart ─────────────────────────────── */
    static class PerformanceBarChart extends JPanel {
        private final List<String> labels = new ArrayList<>();
        private final List<Double> values = new ArrayList<>();

        PerformanceBarChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(600, 200));
        }

        void addBar(String label, double microseconds) {
            labels.add(label); values.add(microseconds);
            repaint();
        }

        void clear() { labels.clear(); values.clear(); repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            ThemeManager tm = ThemeManager.getInstance();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int pad = 40, top = 20, bottom = 30;

            if (labels.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(tm.TEXT_MUTED);
                String msg = "Run benchmark to see chart";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose(); return;
            }

            double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            int chartW = w - pad * 2;
            int chartH = h - top - bottom;
            int barW = Math.max(4, chartW / labels.size() - 4);

            for (int i = 0; i < labels.size(); i++) {
                double val = values.get(i);
                int bh = (int)(chartH * val / max);
                int x = pad + i * (chartW / labels.size()) + (chartW / labels.size() - barW) / 2;
                int y = top + chartH - bh;
                Color c = ThemeManager.CHART_SERIES_COLORS[i % ThemeManager.CHART_SERIES_COLORS.length];
                g2.setColor(c);
                g2.fillRoundRect(x, y, barW, bh, 4, 4);

                // Value on top
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(tm.TEXT_SECONDARY);
                String valStr = String.format("%.1f", val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, x + (barW - fm.stringWidth(valStr)) / 2, y - 3);
            }

            // X axis
            g2.setColor(tm.CHART_AXIS);
            g2.drawLine(pad, top + chartH, w - pad, top + chartH);
            g2.dispose();
        }
    }
}
