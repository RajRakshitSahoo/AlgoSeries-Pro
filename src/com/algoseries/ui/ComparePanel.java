package com.algoseries.ui;

import com.algoseries.algorithms.*;
import com.algoseries.chart.SeriesChartPanel;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import com.algoseries.utils.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Compare panel – lets the user pick up to 4 series and render them
 * on a single overlay chart for side-by-side comparison.
 */
public class ComparePanel extends JPanel {

    private final SeriesChartPanel chart;
    private final List<SeriesResult> compareList = new ArrayList<>();
    private final JPanel legendPanel;
    private final JLabel statusLabel;

    private static final SeriesType[] QUICK_TYPES = {
        SeriesType.FIBONACCI, SeriesType.LUCAS, SeriesType.TRIBONACCI,
        SeriesType.PRIME, SeriesType.SQUARE, SeriesType.CUBE,
        SeriesType.ARITHMETIC, SeriesType.GEOMETRIC
    };

    public ComparePanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        UIComponents.SectionLabel header = new UIComponents.SectionLabel("Compare Series");
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerRow.add(header, BorderLayout.WEST);

        statusLabel = new JLabel("Add up to 4 series to compare");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        headerRow.add(statusLabel, BorderLayout.EAST);
        add(headerRow, BorderLayout.NORTH);

        // Split: controls left, chart right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setDividerSize(6);
        split.setDividerLocation(260);
        split.setBorder(null);

        // ── Left: series selector ──────────────────────────────
        split.setLeftComponent(buildSelector());

        // ── Right: chart ───────────────────────────────────────
        chart = new SeriesChartPanel();
        UIComponents.CardPanel chartCard = new UIComponents.CardPanel("Overlay Chart");
        chartCard.setLayout(new BorderLayout());
        chartCard.add(chart, BorderLayout.CENTER);

        legendPanel = new JPanel();
        legendPanel.setOpaque(false);
        legendPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legendPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0, ThemeManager.getInstance().BORDER));
        chartCard.add(legendPanel, BorderLayout.SOUTH);

        split.setRightComponent(chartCard);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildSelector() {
        UIComponents.CardPanel panel = new UIComponents.CardPanel("Add Series");
        panel.setLayout(new BorderLayout(0, 10));

        // Quick-add buttons
        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 6));
        grid.setOpaque(false);

        JSpinner nSpin = new JSpinner(new SpinnerNumberModel(20, 2, 200, 1));
        nSpin.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel nRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nRow.setOpaque(false);
        JLabel nLbl = new JLabel("Terms (n):");
        nLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nLbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        nRow.add(nLbl); nRow.add(nSpin);

        for (SeriesType type : QUICK_TYPES) {
            UIComponents.StyledButton btn = new UIComponents.StyledButton(
                    "+ " + type.getDisplayName(), UIComponents.StyledButton.Style.SECONDARY);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.addActionListener(e -> addSeries(type, (int) nSpin.getValue()));
            grid.add(btn);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        UIComponents.StyledButton clearBtn = new UIComponents.StyledButton("Clear All", UIComponents.StyledButton.Style.DANGER);
        clearBtn.addActionListener(e -> clearAll());

        panel.add(nRow,     BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);
        panel.add(clearBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void addSeries(SeriesType type, int n) {
        if (compareList.size() >= 4) {
            statusLabel.setText("Maximum 4 series reached. Clear to add more.");
            statusLabel.setForeground(ThemeManager.getInstance().WARNING);
            return;
        }

        SwingWorker<SeriesResult, Void> worker = new SwingWorker<>() {
            @Override protected SeriesResult doInBackground() {
                return generate(type, n);
            }
            @Override protected void done() {
                try {
                    SeriesResult r = get();
                    compareList.add(r);
                    Color c = ThemeManager.CHART_SERIES_COLORS[compareList.size() - 1];
                    chart.addSeries(r.getType().getDisplayName(), r.getValues(), c);
                    addLegendEntry(r.getType().getDisplayName(), c);
                    statusLabel.setText(compareList.size() + " series on chart");
                    statusLabel.setForeground(ThemeManager.getInstance().TEXT_MUTED);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void addLegendEntry(String label, Color color) {
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        entry.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        entry.add(dot); entry.add(lbl);
        legendPanel.add(entry);
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private void clearAll() {
        compareList.clear();
        chart.clearSeries();
        legendPanel.removeAll();
        legendPanel.revalidate();
        legendPanel.repaint();
        statusLabel.setText("Add up to 4 series to compare");
        statusLabel.setForeground(ThemeManager.getInstance().TEXT_MUTED);
    }

    private SeriesResult generate(SeriesType type, int n) {
        switch (type) {
            case FIBONACCI:  return new FibonacciGenerator().generateIterative(n);
            case TRIBONACCI: return new TribonacciGenerator().generateIterative(n);
            case LUCAS:      return new LucasGenerator().generateIterative(n);
            case PRIME:      return new PrimeGenerator().generateIterative(n);
            case ARITHMETIC: return new APGenerator().generateIterative(n, 1, 2);
            case GEOMETRIC:  return new GPGenerator().generateIterative(n, 1, 2);
            case SQUARE:     return new SquareGenerator().generateIterative(n);
            case CUBE:       return new CubeGenerator().generateIterative(n);
            default:         return new FibonacciGenerator().generateIterative(n);
        }
    }
}
