package com.algoseries.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Left-side navigation sidebar.
 * Provides icon + label buttons for each app section.
 */
public class Sidebar extends JPanel {

    /* ── Navigation item ─────────────────────────────────────────── */
    private static class NavItem {
        final String icon, label, key;
        boolean selected = false;
        boolean hovered  = false;
        final Rectangle bounds = new Rectangle();

        NavItem(String icon, String label, String key) {
            this.icon = icon; this.label = label; this.key = key;
        }
    }

    /* ── Fields ──────────────────────────────────────────────────── */
    private final List<NavItem>  items    = new ArrayList<>();
    private final List<Runnable> onChange = new ArrayList<>();
    private String selectedKey;

    /* ── Constructor ─────────────────────────────────────────────── */
    public Sidebar() {
        setPreferredSize(new Dimension(210, 0));
        setOpaque(false);
        setCursor(Cursor.getDefaultCursor());

        // Define navigation sections
        items.add(new NavItem("🏠", "Dashboard",     "dashboard"));
        items.add(new NavItem("📊", "Series Generator","series"));
        items.add(new NavItem("📈", "Graph Viewer",  "graph"));
        items.add(new NavItem("🔄", "Compare Series","compare"));
        items.add(new NavItem("🔷", "Patterns",      "patterns"));
        items.add(new NavItem("⚡", "Performance",   "performance"));
        items.add(new NavItem("👣", "Step-by-Step",  "stepbystep"));
        items.add(new NavItem("🔢", "Matrix Series", "matrix"));
        items.add(new NavItem("🔀", "Sort Visualizer","sorting"));
        items.add(new NavItem("📐", "Complexity",    "complexity"));
        items.add(new NavItem("📋", "History",       "history"));
        items.add(new NavItem("💾", "Export",        "export"));

        selectedKey = "dashboard";
        items.get(0).selected = true;

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean changed = false;
                for (NavItem item : items) {
                    boolean was = item.hovered;
                    item.hovered = item.bounds.contains(e.getPoint());
                    if (was != item.hovered) changed = true;
                }
                if (changed) repaint();
                setCursor(isOverItem(e.getPoint())
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (NavItem item : items) {
                    if (item.bounds.contains(e.getPoint())) {
                        selectItem(item.key);
                        break;
                    }
                }
            }
        });
    }

    /* ── Public API ─────────────────────────────────────────────── */
    public void addSelectionListener(Runnable r) { onChange.add(r); }
    public String getSelectedKey() { return selectedKey; }

    public void selectItem(String key) {
        selectedKey = key;
        items.forEach(i -> i.selected = i.key.equals(key));
        repaint();
        onChange.forEach(Runnable::run);
    }

    /* ── Paint ───────────────────────────────────────────────────── */
    @Override
    protected void paintComponent(Graphics g) {
        ThemeManager tm = ThemeManager.getInstance();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(tm.SIDEBAR_BG);
        g2.fillRect(0, 0, w, h);

        // Logo area
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(tm.ACCENT);
        g2.drawString("Algo", 18, 40);
        g2.setColor(Color.WHITE);
        g2.drawString("Series", 66, 40);
        g2.setColor(tm.ACCENT);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("PRO", 136, 40);

        // Version badge
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(255,255,255,60));
        g2.drawString("v1.0.0", 18, 56);

        // Separator
        g2.setColor(new Color(255,255,255,25));
        g2.fillRect(12, 66, w - 24, 1);

        // Nav label
        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g2.setColor(new Color(255,255,255,55));
        g2.drawString("NAVIGATION", 18, 88);

        // Nav items
        int y = 98;
        for (NavItem item : items) {
            drawNavItem(g2, item, y, w, tm);
            item.bounds.setBounds(0, y, w, 44);
            y += 46;
        }

        // Bottom info
        g2.setColor(new Color(255,255,255,25));
        g2.fillRect(12, h - 46, w - 24, 1);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(255,255,255,55));
        g2.drawString("© 2025 AlgoSeries-Pro", 18, h - 22);
        g2.drawString("Final Year Project", 18, h - 10);

        g2.dispose();
    }

    private void drawNavItem(Graphics2D g2, NavItem item, int y, int w, ThemeManager tm) {
        boolean isDark = tm.isDarkMode();

        // Selected / hover highlight
        if (item.selected) {
            g2.setColor(tm.SIDEBAR_SELECTED);
            g2.fill(new RoundRectangle2D.Float(8, y + 2, w - 16, 40, 10, 10));
            // Left accent bar
            g2.setColor(tm.ACCENT);
            g2.fill(new RoundRectangle2D.Float(8, y + 2, 3, 40, 3, 3));
        } else if (item.hovered) {
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fill(new RoundRectangle2D.Float(8, y + 2, w - 16, 40, 10, 10));
        }

        // Icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        g2.setColor(item.selected ? tm.ACCENT : new Color(200, 210, 240, 180));
        g2.drawString(item.icon, 20, y + 26);

        // Label
        g2.setFont(new Font("Segoe UI", item.selected ? Font.BOLD : Font.PLAIN, 13));
        g2.setColor(item.selected ? Color.WHITE : new Color(200, 210, 240, 180));
        g2.drawString(item.label, 48, y + 26);
    }

    private boolean isOverItem(Point p) {
        return items.stream().anyMatch(i -> i.bounds.contains(p));
    }
}
