package com.algoseries.ui;

import com.algoseries.model.SeriesResult;
import com.algoseries.utils.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Export panel – exports the current session history to TXT or CSV.
 * Also provides a live preview of what will be exported.
 */
public class ExportPanel extends JPanel {

    private final JTextArea preview;
    private final JLabel statusLabel;

    public ExportPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerRow.add(new UIComponents.SectionLabel("Export & Save"), BorderLayout.WEST);
        add(headerRow, BorderLayout.NORTH);

        // Options card
        UIComponents.CardPanel optCard = new UIComponents.CardPanel("Export Options");
        optCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 10));

        JRadioButton allRbtn  = new JRadioButton("All History", true);
        JRadioButton lastRbtn = new JRadioButton("Last Result Only");
        styleRadio(allRbtn); styleRadio(lastRbtn);
        ButtonGroup bg = new ButtonGroup(); bg.add(allRbtn); bg.add(lastRbtn);

        UIComponents.StyledButton txtBtn = new UIComponents.StyledButton("📄 Export TXT",  UIComponents.StyledButton.Style.PRIMARY);
        UIComponents.StyledButton csvBtn = new UIComponents.StyledButton("📊 Export CSV",  UIComponents.StyledButton.Style.SECONDARY);
        UIComponents.StyledButton rptBtn = new UIComponents.StyledButton("📋 Export Report", UIComponents.StyledButton.Style.SECONDARY);
        UIComponents.StyledButton prevBtn= new UIComponents.StyledButton("🔄 Refresh Preview", UIComponents.StyledButton.Style.GHOST);

        optCard.add(allRbtn); optCard.add(lastRbtn);
        optCard.add(new JSeparator(JSeparator.VERTICAL));
        optCard.add(txtBtn); optCard.add(csvBtn); optCard.add(rptBtn);
        optCard.add(prevBtn);

        statusLabel = new JLabel("Ready to export");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        optCard.add(statusLabel);

        add(optCard, BorderLayout.NORTH);

        // Preview area
        preview = new JTextArea();
        preview.setEditable(false);
        preview.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        preview.setBackground(ThemeManager.getInstance().INPUT_BG);
        preview.setForeground(ThemeManager.getInstance().INPUT_TEXT);
        preview.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(preview);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));

        UIComponents.CardPanel previewCard = new UIComponents.CardPanel("Export Preview");
        previewCard.setLayout(new BorderLayout());
        previewCard.add(scroll, BorderLayout.CENTER);
        add(previewCard, BorderLayout.CENTER);

        // Wire up
        prevBtn.addActionListener(e -> refreshPreview(allRbtn.isSelected()));
        allRbtn.addActionListener(e -> refreshPreview(true));
        lastRbtn.addActionListener(e -> refreshPreview(false));

        txtBtn.addActionListener(e -> exportTXT(allRbtn.isSelected()));
        csvBtn.addActionListener(e -> exportCSV(allRbtn.isSelected()));
        rptBtn.addActionListener(e -> exportReport(allRbtn.isSelected()));

        HistoryManager.getInstance().addChangeListener(() -> refreshPreview(allRbtn.isSelected()));
        refreshPreview(true);
    }

    private void refreshPreview(boolean all) {
        List<SeriesResult> history = HistoryManager.getInstance().getHistory();
        List<SeriesResult> data = all ? history
                : (history.isEmpty() ? history : List.of(history.get(0)));
        preview.setText(buildTXT(data));
        preview.setCaretPosition(0);
    }

    /* ── Export methods ──────────────────────────────────────────── */

    private void exportTXT(boolean all) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("algoseries_export.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<SeriesResult> data = getExportData(all);
            Files.writeString(fc.getSelectedFile().toPath(), buildTXT(data));
            showSuccess("TXT exported successfully!");
        } catch (IOException ex) { showError(ex.getMessage()); }
    }

    private void exportCSV(boolean all) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("algoseries_export.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<SeriesResult> data = getExportData(all);
            Files.writeString(fc.getSelectedFile().toPath(), buildCSV(data));
            showSuccess("CSV exported successfully!");
        } catch (IOException ex) { showError(ex.getMessage()); }
    }

    private void exportReport(boolean all) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("algoseries_report.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<SeriesResult> data = getExportData(all);
            Files.writeString(fc.getSelectedFile().toPath(), buildReport(data));
            showSuccess("Report exported successfully!");
        } catch (IOException ex) { showError(ex.getMessage()); }
    }

    /* ── Builders ────────────────────────────────────────────────── */

    private String buildTXT(List<SeriesResult> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════╗\n");
        sb.append("║         AlgoSeries-Pro  –  Export Report        ║\n");
        sb.append("╚══════════════════════════════════════════════════╝\n\n");
        sb.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("Total records: ").append(data.size()).append("\n\n");

        for (int i = 0; i < data.size(); i++) {
            SeriesResult r = data.get(i);
            sb.append("─".repeat(60)).append("\n");
            sb.append("Entry #").append(i + 1).append("  |  ").append(r.getFormattedTimestamp()).append("\n");
            sb.append("Series     : ").append(r.getType().getDisplayName()).append("\n");
            sb.append("Algorithm  : ").append(r.getAlgorithm()).append("\n");
            sb.append("Parameters : ").append(r.getParameters()).append("\n");
            sb.append("Terms      : ").append(r.getCount()).append("\n");
            sb.append("Time       : ").append(r.getFormattedTime()).append("\n");
            sb.append("Memory     : ").append(r.getFormattedMemory()).append("\n");
            sb.append("Values     : ").append(r.getValuesAsString()).append("\n\n");
        }
        return sb.toString();
    }

    private String buildCSV(List<SeriesResult> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Index,Series,Algorithm,Parameters,Terms,Time_ns,Memory_bytes,Timestamp,Values\n");
        for (int i = 0; i < data.size(); i++) {
            SeriesResult r = data.get(i);
            sb.append(i + 1).append(",")
              .append(csvEscape(r.getType().getDisplayName())).append(",")
              .append(csvEscape(r.getAlgorithm())).append(",")
              .append(csvEscape(r.getParameters())).append(",")
              .append(r.getCount()).append(",")
              .append(r.getExecutionTimeNs()).append(",")
              .append(r.getMemoryUsedBytes()).append(",")
              .append(r.getFormattedTimestamp()).append(",")
              .append(csvEscape(r.getValuesAsString())).append("\n");
        }
        return sb.toString();
    }

    private String buildReport(List<SeriesResult> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALGOSERIES-PRO — SESSION REPORT\n");
        sb.append("=".repeat(70)).append("\n\n");
        sb.append("Date       : ").append(java.time.LocalDate.now()).append("\n");
        sb.append("Records    : ").append(data.size()).append("\n\n");

        // Summary table
        sb.append(String.format("%-25s %-22s %8s %12s%n", "Series", "Algorithm", "Terms", "Time"));
        sb.append("-".repeat(70)).append("\n");
        for (SeriesResult r : data) {
            sb.append(String.format("%-25s %-22s %8d %12s%n",
                r.getType().getDisplayName(), r.getAlgorithm(),
                r.getCount(), r.getFormattedTime()));
        }
        sb.append("\n");

        // Fastest
        data.stream().min((a, b) -> Long.compare(a.getExecutionTimeNs(), b.getExecutionTimeNs()))
            .ifPresent(r -> sb.append("Fastest: ").append(r.getType().getDisplayName())
                .append(" (").append(r.getAlgorithm()).append(") — ").append(r.getFormattedTime()).append("\n"));

        // Slowest
        data.stream().max((a, b) -> Long.compare(a.getExecutionTimeNs(), b.getExecutionTimeNs()))
            .ifPresent(r -> sb.append("Slowest: ").append(r.getType().getDisplayName())
                .append(" (").append(r.getAlgorithm()).append(") — ").append(r.getFormattedTime()).append("\n"));

        return sb.toString();
    }

    private List<SeriesResult> getExportData(boolean all) {
        List<SeriesResult> h = HistoryManager.getInstance().getHistory();
        return all ? h : (h.isEmpty() ? h : List.of(h.get(0)));
    }

    private String csvEscape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private void showSuccess(String msg) {
        statusLabel.setText("✔ " + msg);
        statusLabel.setForeground(ThemeManager.getInstance().SUCCESS);
    }

    private void showError(String msg) {
        statusLabel.setText("✘ " + msg);
        statusLabel.setForeground(ThemeManager.getInstance().ERROR);
    }

    private void styleRadio(JRadioButton r) {
        r.setOpaque(false);
        r.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        r.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
    }
}
