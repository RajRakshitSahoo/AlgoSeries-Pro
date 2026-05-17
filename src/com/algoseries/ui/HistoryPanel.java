package com.algoseries.ui;

import com.algoseries.model.SeriesResult;
import com.algoseries.utils.HistoryManager;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * History panel – shows all generated series in a searchable table.
 */
public class HistoryPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel countLabel;

    private static final String[] COLS = {
        "#", "Series", "Algorithm", "Terms", "Time", "Memory", "Parameters", "Timestamp"
    };

    public HistoryPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout(12, 0));
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        headerRow.add(new UIComponents.SectionLabel("Generation History"), BorderLayout.WEST);

        // Search
        UIComponents.StyledTextField searchField = new UIComponents.StyledTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search by series name…");
        headerRow.add(searchField, BorderLayout.CENTER);
        add(headerRow, BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        UIComponents.StyledButton clearBtn  = new UIComponents.StyledButton("🗑 Clear History", UIComponents.StyledButton.Style.DANGER);
        UIComponents.StyledButton exportBtn = new UIComponents.StyledButton("📋 Copy Selected", UIComponents.StyledButton.Style.SECONDARY);
        countLabel = new JLabel("0 records");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        toolbar.add(clearBtn); toolbar.add(exportBtn); toolbar.add(countLabel);
        add(toolbar, BorderLayout.NORTH); // overrides, combine

        // Table
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));

        UIComponents.CardPanel card = new UIComponents.CardPanel("History");
        card.setLayout(new BorderLayout(0, 8));
        card.add(toolbar, BorderLayout.NORTH);
        card.add(scroll,  BorderLayout.CENTER);
        add(headerRow, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);

        // Detail pane
        JTextArea detail = new JTextArea(5, 40);
        detail.setEditable(false);
        detail.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        detail.setBackground(ThemeManager.getInstance().INPUT_BG);
        detail.setForeground(ThemeManager.getInstance().INPUT_TEXT);
        detail.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane detScroll = new JScrollPane(detail);
        detScroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));
        UIComponents.CardPanel detCard = new UIComponents.CardPanel("Selected Series Values");
        detCard.setLayout(new BorderLayout());
        detCard.add(detScroll, BorderLayout.CENTER);
        detCard.setPreferredSize(new Dimension(0, 140));
        add(detCard, BorderLayout.SOUTH);

        // Wire up
        HistoryManager.getInstance().addChangeListener(this::refresh);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    List<SeriesResult> hist = HistoryManager.getInstance().getHistory();
                    if (row < hist.size()) {
                        SeriesResult r = hist.get(hist.size() - 1 - row);
                        detail.setText(r.getType().getDisplayName() + " (" + r.getAlgorithm() + ")\n"
                                + r.getParameters() + "\n\n" + r.getValuesAsString());
                        detail.setCaretPosition(0);
                    }
                }
            }
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Clear all history?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) HistoryManager.getInstance().clear();
        });

        exportBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                List<SeriesResult> hist = HistoryManager.getInstance().getHistory();
                if (row < hist.size()) {
                    SeriesResult r = hist.get(hist.size() - 1 - row);
                    StringSelection sel = new StringSelection(r.getValuesAsString());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
                    JOptionPane.showMessageDialog(this, "Copied to clipboard!", "Done", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(searchField.getText()); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(searchField.getText()); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(searchField.getText()); }
        });

        refresh();
    }

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            List<SeriesResult> hist = HistoryManager.getInstance().getHistory();
            for (int i = 0; i < hist.size(); i++) {
                SeriesResult r = hist.get(i);
                model.addRow(new Object[]{
                    hist.size() - i,
                    r.getType().getDisplayName(),
                    r.getAlgorithm(),
                    r.getCount(),
                    r.getFormattedTime(),
                    r.getFormattedMemory(),
                    r.getParameters(),
                    r.getFormattedTimestamp()
                });
            }
            countLabel.setText(hist.size() + " records");
        });
    }

    private void filterTable(String query) {
        model.setRowCount(0);
        String q = query.toLowerCase().trim();
        List<SeriesResult> hist = HistoryManager.getInstance().getHistory();
        int num = hist.size();
        for (int i = 0; i < num; i++) {
            SeriesResult r = hist.get(i);
            if (q.isEmpty() || r.getType().getDisplayName().toLowerCase().contains(q)
                    || r.getAlgorithm().toLowerCase().contains(q)) {
                model.addRow(new Object[]{
                    num - i, r.getType().getDisplayName(), r.getAlgorithm(),
                    r.getCount(), r.getFormattedTime(), r.getFormattedMemory(),
                    r.getParameters(), r.getFormattedTimestamp()
                });
            }
        }
    }

    private void styleTable(JTable t) {
        ThemeManager tm = ThemeManager.getInstance();
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(28);
        t.setBackground(tm.BG_CARD);
        t.setForeground(tm.TEXT_PRIMARY);
        t.setGridColor(tm.BORDER);
        t.setSelectionBackground(tm.ACCENT_DIM);
        t.setSelectionForeground(tm.TEXT_PRIMARY);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(tm.BG_SECONDARY);
        t.getTableHeader().setForeground(tm.TEXT_SECONDARY);
        int[] widths = {40, 140, 170, 55, 80, 80, 200, 160};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
}
