package com.algoseries.ui;

import com.algoseries.algorithms.*;
import com.algoseries.model.SeriesResult;
import com.algoseries.model.SeriesType;
import com.algoseries.utils.HistoryManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.*;

/**
 * Main series generation panel.
 * Tabbed interface – one tab per algorithm family.
 */
public class SeriesPanel extends JPanel {

    /* ── Callbacks so other panels can react ────────────────────── */
    private Consumer<SeriesResult> onGenerated;
    public void setOnGenerated(Consumer<SeriesResult> c) { onGenerated = c; }

    /* ── Fields ──────────────────────────────────────────────────── */
    private final JTabbedPane tabs;

    public SeriesPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Section header
        UIComponents.SectionLabel header = new UIComponents.SectionLabel("Series Generator");
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        headerPanel.add(header, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabs.setOpaque(false);

        addTab("Fibonacci",    SeriesType.FIBONACCI,  buildFibonacciTab());
        addTab("Tribonacci",   SeriesType.TRIBONACCI, buildTribonacciTab());
        addTab("Lucas",        SeriesType.LUCAS,      buildLucasTab());
        addTab("Prime",        SeriesType.PRIME,      buildPrimeTab());
        addTab("AP",           SeriesType.ARITHMETIC, buildAPTab());
        addTab("GP",           SeriesType.GEOMETRIC,  buildGPTab());
        addTab("Squares",      SeriesType.SQUARE,     buildSquareTab());
        addTab("Cubes",        SeriesType.CUBE,       buildCubeTab());
        addTab("Factorial",    SeriesType.FACTORIAL,  buildFactorialTab());
        addTab("Harmonic",     SeriesType.HARMONIC,   buildHarmonicTab());
        addTab("Pascal",       SeriesType.PASCAL,     buildPascalTab());
        addTab("Custom",       SeriesType.CUSTOM,     buildCustomTab());

        add(tabs, BorderLayout.CENTER);
    }

    private void addTab(String label, SeriesType type, JPanel content) {
        tabs.addTab(label, content);
    }

    /* ─────────────────────────────────────────────────────────────
     *  Generic tab builder helper
     * ───────────────────────────────────────────────────────────── */
    private interface Generator {
        SeriesResult run(boolean recursive);
    }

    /**
     * Builds a standard tab with input fields, algorithm toggle,
     * generate button, output area and performance display.
     */
    private JPanel buildTab(String title, String description,
                            java.util.function.Supplier<JPanel> inputBuilder,
                            Generator generator) {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 4, 4, 4));

        // Info header
        JPanel info = new JPanel(new GridLayout(2,1,0,2));
        info.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);
        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        info.add(lbl); info.add(desc);
        outer.add(info, BorderLayout.NORTH);

        // Centre: inputs + output
        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setOpaque(false);

        // Input card
        UIComponents.CardPanel inputCard = new UIComponents.CardPanel("Parameters");
        inputCard.setLayout(new BorderLayout(0, 10));

        JPanel inputRow = inputBuilder.get();
        inputRow.setOpaque(false);
        inputCard.add(inputRow, BorderLayout.CENTER);

        // Algo toggle
        JPanel algoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        algoRow.setOpaque(false);
        JLabel algoLbl = new JLabel("Algorithm:");
        algoLbl.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        algoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JRadioButton iterBtn = new JRadioButton("Iterative", true);
        JRadioButton recBtn  = new JRadioButton("Recursive (memoised)");
        styleRadio(iterBtn); styleRadio(recBtn);
        ButtonGroup bg = new ButtonGroup();
        bg.add(iterBtn); bg.add(recBtn);
        algoRow.add(algoLbl); algoRow.add(iterBtn); algoRow.add(recBtn);
        inputCard.add(algoRow, BorderLayout.SOUTH);
        centre.add(inputCard, BorderLayout.NORTH);

        // Output area
        JTextArea output = new JTextArea(6, 40);
        output.setEditable(false);
        output.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        output.setLineWrap(true);
        output.setWrapStyleWord(false);
        output.setBackground(ThemeManager.getInstance().INPUT_BG);
        output.setForeground(ThemeManager.getInstance().INPUT_TEXT);
        output.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane outScroll = new JScrollPane(output);
        outScroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));
        outScroll.setOpaque(false);
        outScroll.getViewport().setOpaque(false);

        // Performance display
        JPanel perfRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        perfRow.setOpaque(false);
        JLabel timeLabel = makePerfLabel("⏱ Time: —");
        JLabel memLabel  = makePerfLabel("💾 Memory: —");
        JLabel cntLabel  = makePerfLabel("📊 Terms: —");
        perfRow.add(timeLabel); perfRow.add(memLabel); perfRow.add(cntLabel);

        // Generate button
        UIComponents.StyledButton genBtn = new UIComponents.StyledButton("▶  Generate", UIComponents.StyledButton.Style.PRIMARY);

        // Copy / clear buttons
        UIComponents.StyledButton copyBtn  = new UIComponents.StyledButton("Copy", UIComponents.StyledButton.Style.SECONDARY);
        UIComponents.StyledButton clearBtn = new UIComponents.StyledButton("Clear", UIComponents.StyledButton.Style.GHOST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(genBtn); btnRow.add(copyBtn); btnRow.add(clearBtn);

        JPanel outputPanel = new JPanel(new BorderLayout(0, 6));
        outputPanel.setOpaque(false);
        outputPanel.add(outScroll, BorderLayout.CENTER);
        outputPanel.add(perfRow, BorderLayout.SOUTH);

        UIComponents.CardPanel outputCard = new UIComponents.CardPanel("Output");
        outputCard.setLayout(new BorderLayout(0, 10));
        outputCard.add(btnRow, BorderLayout.NORTH);
        outputCard.add(outputPanel, BorderLayout.CENTER);
        centre.add(outputCard, BorderLayout.CENTER);

        outer.add(centre, BorderLayout.CENTER);

        // Wire up
        genBtn.addActionListener(e -> {
            genBtn.setEnabled(false);
            genBtn.setText("Generating…");
            boolean recursive = recBtn.isSelected();

            SwingWorker<SeriesResult, Void> worker = new SwingWorker<>() {
                @Override protected SeriesResult doInBackground() {
                    return generator.run(recursive);
                }
                @Override protected void done() {
                    try {
                        SeriesResult result = get();
                        output.setText(formatOutput(result));
                        output.setCaretPosition(0);
                        timeLabel.setText("⏱ " + result.getFormattedTime());
                        memLabel.setText("💾 " + result.getFormattedMemory());
                        cntLabel.setText("📊 " + result.getCount() + " terms");
                        HistoryManager.getInstance().add(result);
                        if (onGenerated != null) onGenerated.accept(result);
                    } catch (Exception ex) {
                        output.setText("Error: " + ex.getCause().getMessage());
                    }
                    genBtn.setEnabled(true);
                    genBtn.setText("▶  Generate");
                }
            };
            worker.execute();
        });

        copyBtn.addActionListener(e -> {
            output.selectAll();
            output.copy();
            output.select(0, 0);
        });

        clearBtn.addActionListener(e -> {
            output.setText("");
            timeLabel.setText("⏱ Time: —");
            memLabel.setText("💾 Memory: —");
            cntLabel.setText("📊 Terms: —");
        });

        return outer;
    }

    private JLabel makePerfLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        return l;
    }

    private void styleRadio(JRadioButton btn) {
        btn.setOpaque(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
    }

    private String formatOutput(SeriesResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Series  : ").append(result.getType().getDisplayName()).append("\n");
        sb.append("Params  : ").append(result.getParameters()).append("\n");
        sb.append("Algo    : ").append(result.getAlgorithm()).append("\n");
        sb.append("Time    : ").append(result.getFormattedTime()).append("\n");
        sb.append("Memory  : ").append(result.getFormattedMemory()).append("\n");
        sb.append("─".repeat(60)).append("\n");

        if (result.getType() == SeriesType.PASCAL && !result.getExtraInfo().isEmpty()) {
            sb.append(result.getExtraInfo());
        } else {
            List<Double> values = result.getValues();
            for (int i = 0; i < values.size(); i++) {
                double v = values.get(i);
                String vs = (v == Math.floor(v) && !Double.isInfinite(v))
                        ? String.valueOf((long) v)
                        : String.format("%.6g", v);
                sb.append(String.format("a(%d) = %s\n", i + 1, vs));
            }
        }
        return sb.toString();
    }

    /* ─────────────────────────────────────────────────────────────
     *  Individual tab builders
     * ───────────────────────────────────────────────────────────── */
    private JPanel buildFibonacciTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(20, 1, 200, 1);
        inputs.add(labelFor("Terms (n):")); inputs.add(nSpin);
        return buildTab("Fibonacci Series",
                "F(n) = F(n-1) + F(n-2),  F(0)=0, F(1)=1",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    FibonacciGenerator g = new FibonacciGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildTribonacciTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(15, 1, 150, 1);
        inputs.add(labelFor("Terms (n):")); inputs.add(nSpin);
        return buildTab("Tribonacci Series",
                "T(n) = T(n-1) + T(n-2) + T(n-3),  seeds: 0,1,1",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    TribonacciGenerator g = new TribonacciGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildLucasTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(20, 1, 200, 1);
        inputs.add(labelFor("Terms (n):")); inputs.add(nSpin);
        return buildTab("Lucas Sequence",
                "L(n) = L(n-1) + L(n-2),  L(0)=2, L(1)=1",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    LucasGenerator g = new LucasGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildPrimeTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(20, 1, 1000, 1);
        inputs.add(labelFor("Count (n):")); inputs.add(nSpin);
        return buildTab("Prime Numbers",
                "First n primes via Sieve of Eratosthenes / Trial Division",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    PrimeGenerator g = new PrimeGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildAPTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner aSpin = makeSpinner(1, -10000, 10000, 1);
        JSpinner dSpin = makeSpinner(2, -10000, 10000, 1);
        JSpinner nSpin = makeSpinner(15, 1, 500, 1);
        inputs.add(labelFor("First term (a):")); inputs.add(aSpin);
        inputs.add(labelFor("Common diff (d):")); inputs.add(dSpin);
        inputs.add(labelFor("Terms (n):")); inputs.add(nSpin);
        return buildTab("Arithmetic Progression",
                "a, a+d, a+2d, a+3d, …",
                () -> inputs,
                rec -> {
                    double a = ((Number) aSpin.getValue()).doubleValue();
                    double d = ((Number) dSpin.getValue()).doubleValue();
                    int n = (int) nSpin.getValue();
                    APGenerator g = new APGenerator();
                    return rec ? g.generateRecursive(n, a, d) : g.generateIterative(n, a, d);
                });
    }

    private JPanel buildGPTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner aSpin = makeSpinner(1, -10000, 10000, 1);
        JSpinner rSpin = makeDecimalSpinner(2.0, 0.001, 1000.0, 0.5);
        JSpinner nSpin = makeSpinner(12, 1, 200, 1);
        inputs.add(labelFor("First term (a):")); inputs.add(aSpin);
        inputs.add(labelFor("Common ratio (r):")); inputs.add(rSpin);
        inputs.add(labelFor("Terms (n):")); inputs.add(nSpin);
        return buildTab("Geometric Progression",
                "a, ar, ar², ar³, …",
                () -> inputs,
                rec -> {
                    double a = ((Number) aSpin.getValue()).doubleValue();
                    double r = ((Number) rSpin.getValue()).doubleValue();
                    int n = (int) nSpin.getValue();
                    GPGenerator g = new GPGenerator();
                    return rec ? g.generateRecursive(n, a, r) : g.generateIterative(n, a, r);
                });
    }

    private JPanel buildSquareTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(20, 1, 1000, 1);
        inputs.add(labelFor("Count (n):")); inputs.add(nSpin);
        return buildTab("Square Numbers",
                "1², 2², 3², 4², …",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    SquareGenerator g = new SquareGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildCubeTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(15, 1, 500, 1);
        inputs.add(labelFor("Count (n):")); inputs.add(nSpin);
        return buildTab("Cube Numbers",
                "1³, 2³, 3³, 4³, …",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    CubeGenerator g = new CubeGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildFactorialTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(15, 1, 100, 1);
        inputs.add(labelFor("Count (n):")); inputs.add(nSpin);
        return buildTab("Factorial Series",
                "1!, 2!, 3!, 4!, …  (BigInteger precision)",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    FactorialGenerator g = new FactorialGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildHarmonicTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner aSpin = makeSpinner(1, 1, 1000, 1);
        JSpinner dSpin = makeSpinner(1, 1, 1000, 1);
        JSpinner nSpin = makeSpinner(15, 1, 500, 1);
        inputs.add(labelFor("AP first term (a):")); inputs.add(aSpin);
        inputs.add(labelFor("AP diff (d):")); inputs.add(dSpin);
        inputs.add(labelFor("Terms (n):")); inputs.add(nSpin);
        return buildTab("Harmonic Progression",
                "1/a, 1/(a+d), 1/(a+2d), …",
                () -> inputs,
                rec -> {
                    double a = ((Number) aSpin.getValue()).doubleValue();
                    double d = ((Number) dSpin.getValue()).doubleValue();
                    int n = (int) nSpin.getValue();
                    HarmonicGenerator g = new HarmonicGenerator();
                    return rec ? g.generateRecursive(n, a, d) : g.generateIterative(n, a, d);
                });
    }

    private JPanel buildPascalTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JSpinner nSpin = makeSpinner(8, 1, 20, 1);
        inputs.add(labelFor("Rows (n):")); inputs.add(nSpin);
        return buildTab("Pascal's Triangle",
                "Binomial coefficients arranged in triangular form",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    PascalGenerator g = new PascalGenerator();
                    return rec ? g.generateRecursive(n) : g.generateIterative(n);
                });
    }

    private JPanel buildCustomTab() {
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        JTextField seedsField = new JTextField("1, 1, 2", 18);
        seedsField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JSpinner nSpin = makeSpinner(20, 1, 500, 1);
        inputs.add(labelFor("Seed values:")); inputs.add(seedsField);
        inputs.add(labelFor("Total terms (n):")); inputs.add(nSpin);
        JLabel hint = new JLabel("Recurrence: sum of last k seeds (k = number of seeds)");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(ThemeManager.getInstance().TEXT_MUTED);
        inputs.add(hint);

        return buildTab("Custom Series",
                "Define seed values; recurrence = sum of last k terms",
                () -> inputs,
                rec -> {
                    int n = (int) nSpin.getValue();
                    String raw = seedsField.getText().trim();
                    String[] parts = raw.split("[,\\s]+");
                    double[] seeds = new double[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        seeds[i] = Double.parseDouble(parts[i].trim());
                    CustomGenerator g = new CustomGenerator();
                    return g.generateIterative(n, seeds);
                });
    }

    /* ─────────────────────────────────────────────────────────────
     *  Helper widgets
     * ───────────────────────────────────────────────────────────── */
    private JSpinner makeSpinner(int val, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setPreferredSize(new Dimension(80, 30));
        return s;
    }

    private JSpinner makeDecimalSpinner(double val, double min, double max, double step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setPreferredSize(new Dimension(90, 30));
        return s;
    }

    private JLabel labelFor(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        return l;
    }
}
