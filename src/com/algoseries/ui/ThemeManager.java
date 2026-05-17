package com.algoseries.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Singleton theme manager.
 * Provides the active colour palette and handles dark ↔ light switching.
 * All UI components should read colours from here rather than hardcoding.
 */
public class ThemeManager {

    /* ── Singleton ──────────────────────────────────────────────── */
    private static ThemeManager instance;
    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    /* ── State ──────────────────────────────────────────────────── */
    private boolean darkMode;
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private final List<Runnable> listeners = new ArrayList<>();

    /* ── Colours (public – read by all panels) ───────────────────── */
    public Color BG_PRIMARY, BG_SECONDARY, BG_CARD, BG_HOVER;
    public Color TEXT_PRIMARY, TEXT_SECONDARY, TEXT_MUTED;
    public Color ACCENT, ACCENT_DARK, ACCENT_DIM;
    public Color BORDER, BORDER_LIGHT;
    public Color SUCCESS, WARNING, ERROR, INFO;
    public Color SIDEBAR_BG, SIDEBAR_TEXT, SIDEBAR_SELECTED;
    public Color CHART_BG, CHART_GRID, CHART_AXIS;
    public Color INPUT_BG, INPUT_BORDER, INPUT_TEXT;

    // Series chart colours
    public static final Color[] CHART_SERIES_COLORS = {
        new Color(99, 102, 241),   // indigo
        new Color(52, 211, 153),   // emerald
        new Color(251, 191, 36),   // amber
        new Color(239, 68, 68),    // red
        new Color(139, 92, 246),   // violet
        new Color(20, 184, 166),   // teal
        new Color(249, 115, 22),   // orange
        new Color(236, 72, 153),   // pink
    };

    /* ── Constructor ────────────────────────────────────────────── */
    private ThemeManager() {
        darkMode = prefs.getBoolean("darkMode", true);
        refresh();
    }

    /* ── Public API ─────────────────────────────────────────────── */
    public boolean isDarkMode() { return darkMode; }

    public void toggleTheme() {
        darkMode = !darkMode;
        prefs.putBoolean("darkMode", darkMode);
        refresh();
        listeners.forEach(Runnable::run);
    }

    public void addThemeListener(Runnable r) { listeners.add(r); }

    /** Apply sensible Swing-wide defaults so stock components pick up theme. */
    public void applyGlobalDefaults() {
        refresh();
        UIManager.put("Panel.background",         BG_PRIMARY);
        UIManager.put("OptionPane.background",     BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Label.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.background",      INPUT_BG);
        UIManager.put("TextField.foreground",      INPUT_TEXT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextArea.background",       INPUT_BG);
        UIManager.put("TextArea.foreground",       INPUT_TEXT);
        UIManager.put("ScrollPane.background",     BG_PRIMARY);
        UIManager.put("Viewport.background",       BG_PRIMARY);
        UIManager.put("Table.background",          BG_CARD);
        UIManager.put("Table.foreground",          TEXT_PRIMARY);
        UIManager.put("Table.gridColor",           BORDER);
        UIManager.put("TableHeader.background",    BG_SECONDARY);
        UIManager.put("TableHeader.foreground",    TEXT_SECONDARY);
        UIManager.put("ComboBox.background",       INPUT_BG);
        UIManager.put("ComboBox.foreground",       INPUT_TEXT);
        UIManager.put("Spinner.background",        INPUT_BG);
        UIManager.put("Spinner.foreground",        INPUT_TEXT);
        UIManager.put("TabbedPane.background",     BG_SECONDARY);
        UIManager.put("TabbedPane.foreground",     TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",       BG_CARD);
        UIManager.put("SplitPane.background",      BG_PRIMARY);
        UIManager.put("ScrollBar.background",      BG_SECONDARY);
        UIManager.put("ScrollBar.thumb",           new Color(BORDER.getRed(), BORDER.getGreen(), BORDER.getBlue()));
    }

    /* ── Private ────────────────────────────────────────────────── */
    private void refresh() {
        if (darkMode) applyDark(); else applyLight();
    }

    private void applyDark() {
        BG_PRIMARY      = new Color(13,  17,  30);
        BG_SECONDARY    = new Color(20,  26,  46);
        BG_CARD         = new Color(26,  33,  58);
        BG_HOVER        = new Color(36,  44,  74);
        TEXT_PRIMARY    = new Color(232, 237, 255);
        TEXT_SECONDARY  = new Color(148, 163, 200);
        TEXT_MUTED      = new Color(80,  95,  140);
        ACCENT          = new Color(99,  102, 241);
        ACCENT_DARK     = new Color(67,  70,  180);
        ACCENT_DIM      = new Color(28,  30,  70);
        BORDER          = new Color(42,  50,  84);
        BORDER_LIGHT    = new Color(55,  65, 105);
        SUCCESS         = new Color(52,  211, 153);
        WARNING         = new Color(251, 191,  36);
        ERROR           = new Color(239,  68,  68);
        INFO            = new Color(56,  189, 248);
        SIDEBAR_BG      = new Color(10,  13,  26);
        SIDEBAR_TEXT    = new Color(180, 190, 220);
        SIDEBAR_SELECTED= new Color(30,  36,  65);
        CHART_BG        = new Color(16,  20,  38);
        CHART_GRID      = new Color(32,  40,  70);
        CHART_AXIS      = new Color(60,  72, 110);
        INPUT_BG        = new Color(22,  28,  50);
        INPUT_BORDER    = new Color(50,  60,  95);
        INPUT_TEXT      = new Color(220, 225, 250);
    }

    private void applyLight() {
        BG_PRIMARY      = new Color(245, 247, 252);
        BG_SECONDARY    = new Color(255, 255, 255);
        BG_CARD         = new Color(255, 255, 255);
        BG_HOVER        = new Color(238, 240, 252);
        TEXT_PRIMARY    = new Color(15,  20,  50);
        TEXT_SECONDARY  = new Color(80,  95, 140);
        TEXT_MUTED      = new Color(150, 162, 200);
        ACCENT          = new Color(79,  83, 220);
        ACCENT_DARK     = new Color(55,  58, 180);
        ACCENT_DIM      = new Color(230, 231, 255);
        BORDER          = new Color(218, 224, 242);
        BORDER_LIGHT    = new Color(235, 239, 252);
        SUCCESS         = new Color(16,  185, 129);
        WARNING         = new Color(217, 119,   6);
        ERROR           = new Color(220,  38,  38);
        INFO            = new Color(14,  165, 233);
        SIDEBAR_BG      = new Color(30,  36,  74);
        SIDEBAR_TEXT    = new Color(200, 210, 240);
        SIDEBAR_SELECTED= new Color(50,  58, 110);
        CHART_BG        = new Color(252, 253, 255);
        CHART_GRID      = new Color(220, 226, 242);
        CHART_AXIS      = new Color(160, 172, 210);
        INPUT_BG        = new Color(252, 253, 255);
        INPUT_BORDER    = new Color(210, 216, 238);
        INPUT_TEXT      = new Color(20,  26,  60);
    }
}
