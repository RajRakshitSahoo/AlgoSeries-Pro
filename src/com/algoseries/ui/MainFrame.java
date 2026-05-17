package com.algoseries.ui;

import com.algoseries.model.SeriesResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Root application window.
 * Hosts:
 *  • Custom title bar area (logo + theme toggle)
 *  • JMenuBar with File / View / Tools / Help menus
 *  • Left Sidebar (navigation)
 *  • Main content CardLayout area
 *
 * Navigation is driven by Sidebar selection; each panel is lazily
 * kept in a CardLayout so state is preserved between switches.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     contentArea = new JPanel(cardLayout);
    private final Sidebar    sidebar;

    // Panels (lazily used)
    private final DashboardPanel   dashPanel;
    private final SeriesPanel      seriesPanel;
    private final GraphPanel       graphPanel;
    private final ComparePanel     comparePanel;
    private final PatternsPanel    patternsPanel;
    private final PerformancePanel perfPanel;
    private final HistoryPanel     historyPanel;
    private final ExportPanel         exportPanel;
    private final StepByStepPanel     stepByStepPanel;
    private final MatrixSeriesPanel   matrixPanel;
    private final SortingVisualizerPanel sortingPanel;
    private final ComplexityVisualizerPanel complexityPanel;

    public MainFrame() {
        super("AlgoSeries-Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // Set app icon
        try {
            Image icon = createIconImage();
            setIconImage(icon);
        } catch (Exception ignored) {}

        ThemeManager tm = ThemeManager.getInstance();

        // Root panel
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.getInstance().BG_PRIMARY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        // ── Menu bar ──────────────────────────────────────────────
        setJMenuBar(buildMenuBar());

        // ── Sidebar ───────────────────────────────────────────────
        sidebar = new Sidebar();
        root.add(sidebar, BorderLayout.WEST);

        // ── Content area ──────────────────────────────────────────
        contentArea.setOpaque(false);
        root.add(contentArea, BorderLayout.CENTER);

        // Instantiate panels
        dashPanel    = new DashboardPanel();
        seriesPanel  = new SeriesPanel();
        graphPanel   = new GraphPanel();
        comparePanel = new ComparePanel();
        patternsPanel= new PatternsPanel();
        perfPanel    = new PerformancePanel();
        historyPanel = new HistoryPanel();
        exportPanel  = new ExportPanel();
        stepByStepPanel  = new StepByStepPanel();
        matrixPanel      = new MatrixSeriesPanel();
        sortingPanel     = new SortingVisualizerPanel();
        complexityPanel  = new ComplexityVisualizerPanel();

        contentArea.add(dashPanel,      "dashboard");
        contentArea.add(seriesPanel,    "series");
        contentArea.add(graphPanel,     "graph");
        contentArea.add(comparePanel,   "compare");
        contentArea.add(patternsPanel,  "patterns");
        contentArea.add(perfPanel,      "performance");
        contentArea.add(stepByStepPanel,"stepbystep");
        contentArea.add(matrixPanel,    "matrix");
        contentArea.add(sortingPanel,   "sorting");
        contentArea.add(complexityPanel,"complexity");
        contentArea.add(historyPanel,   "history");
        contentArea.add(exportPanel,    "export");

        // Wire sidebar navigation
        sidebar.addSelectionListener(() -> {
            String key = sidebar.getSelectedKey();
            cardLayout.show(contentArea, key);
        });

        // Wire dashboard quick-launch buttons
        dashPanel.addPropertyChangeListener("navigate", e -> {
            String key = (String) e.getNewValue();
            sidebar.selectItem(key);
            cardLayout.show(contentArea, key);
        });

        // Wire series → graph
        seriesPanel.setOnGenerated(this::onSeriesGenerated);

        // ── Right header strip (theme toggle + title) ─────────────
        JPanel topBar = buildTopBar();
        root.add(topBar, BorderLayout.NORTH);

        // Theme change → repaint everything
        tm.addThemeListener(() -> SwingUtilities.invokeLater(() -> {
            tm.applyGlobalDefaults();
            repaintAll(root);
        }));

        // Show dashboard
        cardLayout.show(contentArea, "dashboard");
    }

    /* ── Menu bar ─────────────────────────────────────────────────── */
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setOpaque(true);
        bar.setBackground(ThemeManager.getInstance().BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getInstance().BORDER));

        // File
        JMenu file = menu("File");
        file.add(menuItem("New Session",       "ctrl N", e -> newSession()));
        file.add(menuItem("Export TXT",        "ctrl S", e -> sidebar.selectItem("export")));
        file.add(menuItem("Export CSV",        "ctrl E", e -> sidebar.selectItem("export")));
        file.addSeparator();
        file.add(menuItem("Exit",              "ctrl Q", e -> System.exit(0)));
        bar.add(file);

        // View
        JMenu view = menu("View");
        view.add(menuItem("Dashboard",         "ctrl 1", e -> sidebar.selectItem("dashboard")));
        view.add(menuItem("Series Generator",  "ctrl 2", e -> sidebar.selectItem("series")));
        view.add(menuItem("Graph Viewer",      "ctrl 3", e -> sidebar.selectItem("graph")));
        view.add(menuItem("Compare Series",    "ctrl 4", e -> sidebar.selectItem("compare")));
        view.add(menuItem("Patterns",          "ctrl 5", e -> sidebar.selectItem("patterns")));
        view.addSeparator();
        view.add(menuItem("Toggle Dark/Light", "ctrl T", e -> ThemeManager.getInstance().toggleTheme()));
        bar.add(view);

        // Tools
        JMenu tools = menu("Tools");
        tools.add(menuItem("Performance Benchmark", "ctrl P", e -> sidebar.selectItem("performance")));
        tools.add(menuItem("History",               "ctrl H", e -> sidebar.selectItem("history")));
        tools.addSeparator();
        tools.add(menuItem("Clear History",          null,    e -> {
            int c = JOptionPane.showConfirmDialog(this, "Clear all history?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION)
                com.algoseries.utils.HistoryManager.getInstance().clear();
        }));
        bar.add(tools);

        // Help
        JMenu help = menu("Help");
        help.add(menuItem("About",    null, e -> showAbout()));
        help.add(menuItem("Keyboard Shortcuts", null, e -> showShortcuts()));
        bar.add(help);

        styleMenuBar(bar);
        return bar;
    }

    private JMenu menu(String label) {
        JMenu m = new JMenu(label);
        m.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return m;
    }

    private JMenuItem menuItem(String label, String shortcut, ActionListener action) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);
        item.setBackground(ThemeManager.getInstance().BG_CARD);
        if (shortcut != null)
            item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        item.addActionListener(action);
        return item;
    }

    private void styleMenuBar(JMenuBar bar) {
        for (int i = 0; i < bar.getMenuCount(); i++) {
            JMenu m = bar.getMenu(i);
            for (int j = 0; j < m.getItemCount(); j++) {
                JMenuItem item = m.getItem(j);
                if (item != null) {
                    item.setBackground(ThemeManager.getInstance().BG_CARD);
                    item.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);
                }
            }
        }
    }

    /* ── Top bar ─────────────────────────────────────────────────── */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                ThemeManager tm = ThemeManager.getInstance();
                g.setColor(tm.BG_SECONDARY);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(tm.BORDER);
                g.fillRect(0, getHeight()-1, getWidth(), 1);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 42));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        // Breadcrumb / page title
        JLabel pageLabel = new JLabel("Dashboard");
        pageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pageLabel.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);
        bar.add(pageLabel, BorderLayout.CENTER);

        sidebar.addSelectionListener(() -> {
            String key = sidebar.getSelectedKey();
            String title = capitalize(key);
            pageLabel.setText(title);
        });

        // Right controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);

        JToggleButton themeBtn = new JToggleButton(ThemeManager.getInstance().isDarkMode() ? "☀ Light" : "🌙 Dark");
        themeBtn.setSelected(ThemeManager.getInstance().isDarkMode());
        themeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        themeBtn.setFocusPainted(false);
        themeBtn.setOpaque(false);
        themeBtn.setBorder(BorderFactory.createCompoundBorder(
                new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        themeBtn.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        themeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeBtn.addActionListener(e -> {
            ThemeManager.getInstance().toggleTheme();
            themeBtn.setText(ThemeManager.getInstance().isDarkMode() ? "☀ Light" : "🌙 Dark");
        });

        right.add(themeBtn);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /* ── Callbacks ───────────────────────────────────────────────── */
    private void onSeriesGenerated(SeriesResult result) {
        graphPanel.showResult(result);
    }

    private void newSession() {
        int c = JOptionPane.showConfirmDialog(this,
                "Start a new session? (History will be cleared)", "New Session", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            com.algoseries.utils.HistoryManager.getInstance().clear();
            sidebar.selectItem("dashboard");
        }
    }

    /* ── Dialogs ─────────────────────────────────────────────────── */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "AlgoSeries-Pro v1.0.0\n\n" +
                "An Advanced Mathematical Series Manipulator\n" +
                "Built with Java Swing · Pure Java 2D Charts\n\n" +
                "Features: 12 algorithms · Dark/Light mode\n" +
                "         Iterative & Recursive variants\n" +
                "         Performance benchmarking · Export\n\n" +
                "Final Year Project Portfolio",
                "About AlgoSeries-Pro", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showShortcuts() {
        JOptionPane.showMessageDialog(this,
                "Keyboard Shortcuts\n" +
                "─────────────────────────────\n" +
                "Ctrl+1   Dashboard\n" +
                "Ctrl+2   Series Generator\n" +
                "Ctrl+3   Graph Viewer\n" +
                "Ctrl+4   Compare Series\n" +
                "Ctrl+5   Patterns\n" +
                "Ctrl+T   Toggle Dark/Light\n" +
                "Ctrl+S   Export TXT\n" +
                "Ctrl+E   Export CSV\n" +
                "Ctrl+P   Performance\n" +
                "Ctrl+H   History\n" +
                "Ctrl+N   New Session\n" +
                "Ctrl+Q   Exit",
                "Keyboard Shortcuts", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ── Utility ─────────────────────────────────────────────────── */
    private void repaintAll(Component c) {
        c.repaint();
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents())
                repaintAll(child);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Creates a simple programmatic app icon. */
    private Image createIconImage() {
        int size = 64;
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(13, 17, 30));
        g2.fillRoundRect(0, 0, size, size, 16, 16);
        g2.setColor(new Color(99, 102, 241));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
        g2.drawString("A", 16, 44);
        g2.setColor(new Color(52, 211, 153));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.drawString("S", 38, 50);
        g2.dispose();
        return img;
    }
}
