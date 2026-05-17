package com.algoseries.ui;

import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import com.algoseries.utils.HistoryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Dashboard panel – the home screen.
 * Shows KPI stat cards, quick-launch grid, and a recent-history feed.
 */
public class DashboardPanel extends JPanel {

    private final UIComponents.StatCard totalCard;
    private final UIComponents.StatCard fastestCard;
    private final UIComponents.StatCard todayCard;
    private final JPanel recentList;

    public DashboardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Header ────────────────────────────────────────────────
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── Body ──────────────────────────────────────────────────
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 16, 16);

        // Stat cards row
        ThemeManager tm = ThemeManager.getInstance();
        totalCard   = new UIComponents.StatCard("📊", "Total Generated", "0",     tm.ACCENT);
        fastestCard = new UIComponents.StatCard("⚡", "Fastest Algorithm", "—",    tm.SUCCESS);
        todayCard   = new UIComponents.StatCard("🕐", "Session Series",  "0",     tm.INFO);
        UIComponents.StatCard algoCard = new UIComponents.StatCard("🧮", "Algorithms Available", "12", tm.WARNING);

        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0.25; gbc.weighty=0; body.add(totalCard, gbc);
        gbc.gridx=1;                                                  body.add(fastestCard, gbc);
        gbc.gridx=2;                                                  body.add(todayCard, gbc);
        gbc.gridx=3; gbc.insets=new Insets(0,0,16,0);                body.add(algoCard, gbc);

        // Quick launch
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=4; gbc.weightx=1; gbc.weighty=0;
        gbc.insets=new Insets(0,0,16,0);
        body.add(buildQuickLaunch(), gbc);

        // Recent activity
        recentList = new JPanel();
        recentList.setOpaque(false);
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(recentList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        UIComponents.CardPanel recentCard = new UIComponents.CardPanel("📋  Recent History");
        recentCard.setLayout(new BorderLayout());
        recentCard.add(scroll, BorderLayout.CENTER);

        gbc.gridy=2; gbc.weighty=1; gbc.insets=new Insets(0,0,0,0);
        body.add(recentCard, gbc);

        add(body, BorderLayout.CENTER);

        // Listen for history changes
        HistoryManager.getInstance().addChangeListener(this::refreshData);
        refreshData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Welcome to AlgoSeries-Pro");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);

        JLabel sub = new JLabel("Generate, analyse, and visualise mathematical series");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new GridLayout(2,1,0,4));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(sub);
        p.add(textPanel, BorderLayout.WEST);
        return p;
    }

    private JPanel buildQuickLaunch() {
        UIComponents.CardPanel card = new UIComponents.CardPanel("🚀  Quick Launch");
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        String[][] items = {
            {"Fibonacci","series"},{"Prime Numbers","series"},{"Pascal Triangle","patterns"},
            {"Compare","compare"},{"Performance","performance"},{"Export","export"}
        };

        for (String[] item : items) {
            UIComponents.StyledButton btn = new UIComponents.StyledButton(item[0],
                    UIComponents.StyledButton.Style.SECONDARY);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            // Fire navigation event via property change
            btn.addActionListener(e -> firePropertyChange("navigate", null, item[1]));
            card.add(btn);
        }
        return card;
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            HistoryManager hm = HistoryManager.getInstance();
            int total = hm.size();
            totalCard.setValue(String.valueOf(total));
            todayCard.setValue(String.valueOf(total));

            // Find fastest
            if (!hm.getHistory().isEmpty()) {
                SeriesResult fastest = hm.getHistory().stream()
                        .min((a, b) -> Long.compare(a.getExecutionTimeNs(), b.getExecutionTimeNs()))
                        .orElse(null);
                if (fastest != null)
                    fastestCard.setValue(fastest.getType().getDisplayName().split(" ")[0]);
            }

            // Rebuild recent list
            recentList.removeAll();
            List<SeriesResult> history = hm.getHistory();
            int show = Math.min(8, history.size());
            if (show == 0) {
                JLabel empty = new JLabel("  No series generated yet. Use the Series Generator to start.");
                empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                empty.setForeground(ThemeManager.getInstance().TEXT_MUTED);
                empty.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 0));
                recentList.add(empty);
            }
            for (int i = 0; i < show; i++) {
                recentList.add(buildRecentRow(history.get(i), i % 2 == 0));
            }
            recentList.revalidate();
            recentList.repaint();
        });
    }

    private JPanel buildRecentRow(SeriesResult r, boolean even) {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(even ? new Color(tm.BG_HOVER.getRed(), tm.BG_HOVER.getGreen(),
                        tm.BG_HOVER.getBlue(), 80) : new Color(0,0,0,0));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JLabel name = new JLabel(r.getType().getDisplayName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(tm.TEXT_PRIMARY);

        JLabel detail = new JLabel(r.getCount() + " terms  |  " + r.getAlgorithm()
                + "  |  " + r.getFormattedTime());
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detail.setForeground(tm.TEXT_SECONDARY);

        JLabel time = new JLabel(r.getFormattedTimestamp());
        time.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        time.setForeground(tm.TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2,1,0,2));
        left.setOpaque(false);
        left.add(name); left.add(detail);

        row.add(left, BorderLayout.CENTER);
        row.add(time, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        return row;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Repaint child labels when theme changes
        ThemeManager tm = ThemeManager.getInstance();
        Component[] comps = getComponents();
        updateColors(comps, tm);
        super.paintComponent(g);
    }

    private void updateColors(Component[] comps, ThemeManager tm) {
        for (Component c : comps) {
            if (c instanceof JLabel) {
                ((JLabel) c).setForeground(tm.TEXT_PRIMARY);
            }
            if (c instanceof JPanel) {
                updateColors(((JPanel) c).getComponents(), tm);
            }
        }
    }
}
