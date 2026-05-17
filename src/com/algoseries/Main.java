package com.algoseries;

import com.algoseries.ui.MainFrame;
import com.algoseries.ui.ThemeManager;
import javax.swing.*;
import java.awt.*;

/**
 * ╔═══════════════════════════════════════════════╗
 * ║         AlgoSeries-Pro v1.0.0                ║
 * ║   Advanced Mathematical Series Manipulator   ║
 * ║         Entry Point / Bootstrap              ║
 * ╚═══════════════════════════════════════════════╝
 *
 * Application entry point. Initialises the Swing
 * event dispatch thread and launches the main window.
 */
public class Main {

    public static void main(String[] args) {
        // Enable hardware acceleration
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Try Nimbus first for better default styling
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Fall back silently
            }

            ThemeManager.getInstance().applyGlobalDefaults();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
