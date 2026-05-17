package com.algoseries.ui;

import com.algoseries.chart.SeriesChartPanel;
import com.algoseries.model.SeriesResult;
import com.algoseries.utils.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Graph Viewer panel – displays the last generated series as a line/bar chart.
 * Supports chart mode toggle, grid toggle, and PNG export.
 */
public class GraphPanel extends JPanel {

    private final SeriesChartPanel chart;
    private SeriesResult currentResult;

    public GraphPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        UIComponents.SectionLabel title = new UIComponents.SectionLabel("Graph Viewer");
        header.add(title, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        UIComponents.StyledButton lineBtn = new UIComponents.StyledButton("Line", UIComponents.StyledButton.Style.PRIMARY);
        UIComponents.StyledButton barBtn  = new UIComponents.StyledButton("Bar",  UIComponents.StyledButton.Style.SECONDARY);
        UIComponents.StyledButton gridBtn = new UIComponents.StyledButton("Grid ✓", UIComponents.StyledButton.Style.SECONDARY);
        UIComponents.StyledButton ptBtn   = new UIComponents.StyledButton("Points ✓", UIComponents.StyledButton.Style.SECONDARY);
        UIComponents.StyledButton exportBtn = new UIComponents.StyledButton("📷 Export PNG", UIComponents.StyledButton.Style.SECONDARY);

        toolbar.add(lineBtn); toolbar.add(barBtn);
        toolbar.add(new JSeparator(JSeparator.VERTICAL));
        toolbar.add(gridBtn); toolbar.add(ptBtn);
        toolbar.add(new JSeparator(JSeparator.VERTICAL));
        toolbar.add(exportBtn);
        header.add(toolbar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Chart area
        chart = new SeriesChartPanel();

        UIComponents.CardPanel chartCard = new UIComponents.CardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.add(chart, BorderLayout.CENTER);

        // Zoom hint
        JLabel hint = new JLabel("  🖱  Scroll to zoom · Drag to pan · Hover for values");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        add(chartCard, BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);

        // Info strip
        JPanel infoStrip = buildInfoStrip();
        chartCard.add(infoStrip, BorderLayout.SOUTH);

        // Wire toolbar
        final boolean[] gridOn = {true};
        final boolean[] ptsOn  = {true};

        lineBtn.addActionListener(e -> {
            chart.setMode(SeriesChartPanel.ChartMode.LINE);
            lineBtn.putClientProperty("style", UIComponents.StyledButton.Style.PRIMARY);
        });
        barBtn.addActionListener(e -> chart.setMode(SeriesChartPanel.ChartMode.BAR));

        gridBtn.addActionListener(e -> {
            gridOn[0] = !gridOn[0];
            chart.setShowGrid(gridOn[0]);
            gridBtn.setText(gridOn[0] ? "Grid ✓" : "Grid");
        });
        ptBtn.addActionListener(e -> {
            ptsOn[0] = !ptsOn[0];
            chart.setShowPoints(ptsOn[0]);
            ptBtn.setText(ptsOn[0] ? "Points ✓" : "Points");
        });

        exportBtn.addActionListener(e -> exportChart());

        // Auto-update when new series generated
        HistoryManager.getInstance().addChangeListener(this::loadLatestFromHistory);
    }

    /** Called by SeriesPanel when a result is freshly generated. */
    public void showResult(SeriesResult result) {
        currentResult = result;
        chart.setFromResult(result);
    }

    private void loadLatestFromHistory() {
        List<SeriesResult> history = HistoryManager.getInstance().getHistory();
        if (!history.isEmpty() && currentResult == null) {
            showResult(history.get(0));
        }
    }

    private JPanel buildInfoStrip() {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        strip.setOpaque(false);
        strip.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getInstance().BORDER));

        JLabel seriesLbl = new JLabel("Series: —");
        JLabel termsLbl  = new JLabel("Terms: —");
        JLabel timeLbl   = new JLabel("Time: —");

        for (JLabel l : new JLabel[]{seriesLbl, termsLbl, timeLbl}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            l.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
            strip.add(l);
        }

        HistoryManager.getInstance().addChangeListener(() -> {
            SwingUtilities.invokeLater(() -> {
                List<SeriesResult> h = HistoryManager.getInstance().getHistory();
                if (!h.isEmpty()) {
                    SeriesResult r = h.get(0);
                    seriesLbl.setText("Series: " + r.getType().getDisplayName());
                    termsLbl.setText("Terms: " + r.getCount());
                    timeLbl.setText("Time: " + r.getFormattedTime());
                }
            });
        });

        return strip;
    }

    private void exportChart() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("chart_export.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                chart.exportToPNG(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Chart exported successfully!", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
