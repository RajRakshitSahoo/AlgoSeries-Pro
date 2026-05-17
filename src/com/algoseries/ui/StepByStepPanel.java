package com.algoseries.ui;

import com.algoseries.algorithms.*;
import com.algoseries.model.SeriesType;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Step-By-Step Explanation Panel
 * Shows how each series is computed one term at a time with animated steps.
 */
public class StepByStepPanel extends JPanel {

    private JComboBox<String> seriesCombo;
    private JSpinner nSpinner;
    private JTextArea stepsArea;
    private JList<String> stepList;
    private DefaultListModel<String> stepModel;
    private JLabel formulaLabel;
    private JLabel currentStepLabel;
    private JButton nextBtn, prevBtn, autoBtn, resetBtn;
    private JProgressBar progressBar;
    private javax.swing.Timer autoTimer;
    private List<String> steps = new ArrayList<>();
    private int currentStep = 0;

    private static final String[] SERIES_NAMES = {
        "Fibonacci", "Tribonacci", "Lucas", "Prime (Trial)",
        "Arithmetic Prog.", "Geometric Prog.", "Factorial",
        "Pascal Triangle", "Square Numbers", "Cube Numbers",
        "Harmonic Prog."
    };

    public StepByStepPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        UIComponents.SectionLabel title = new UIComponents.SectionLabel("Step-by-Step Explanation");
        p.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        controls.setOpaque(false);

        seriesCombo = new JComboBox<>(SERIES_NAMES);
        seriesCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        seriesCombo.setPreferredSize(new Dimension(180, 30));

        nSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 20, 1));
        nSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nSpinner.setPreferredSize(new Dimension(70, 30));

        UIComponents.StyledButton generateBtn = new UIComponents.StyledButton("▶ Explain", UIComponents.StyledButton.Style.PRIMARY);
        generateBtn.addActionListener(e -> generateSteps());

        JLabel serLbl = makeLabel("Series:");
        JLabel nLbl   = makeLabel("Terms:");
        controls.add(serLbl); controls.add(seriesCombo);
        controls.add(nLbl);   controls.add(nSpinner);
        controls.add(generateBtn);
        p.add(controls, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setOpaque(false);

        // Formula card
        UIComponents.CardPanel formulaCard = new UIComponents.CardPanel("Formula & Complexity");
        formulaCard.setLayout(new BorderLayout(0, 6));
        formulaLabel = new JLabel("<html><b>Select a series and click Explain to begin.</b></html>");
        formulaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formulaLabel.setForeground(ThemeManager.getInstance().TEXT_PRIMARY);
        formulaLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        formulaCard.add(formulaLabel, BorderLayout.CENTER);
        formulaCard.setPreferredSize(new Dimension(0, 80));
        p.add(formulaCard, BorderLayout.NORTH);

        // Step list
        stepModel = new DefaultListModel<>();
        stepList = new JList<>(stepModel);
        stepList.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        stepList.setBackground(ThemeManager.getInstance().INPUT_BG);
        stepList.setForeground(ThemeManager.getInstance().INPUT_TEXT);
        stepList.setSelectionBackground(ThemeManager.getInstance().ACCENT_DIM);
        stepList.setSelectionForeground(ThemeManager.getInstance().TEXT_PRIMARY);
        stepList.setCellRenderer(new StepCellRenderer());
        stepList.setFixedCellHeight(30);

        JScrollPane scroll = new JScrollPane(stepList);
        scroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        UIComponents.CardPanel listCard = new UIComponents.CardPanel("Computation Steps");
        listCard.setLayout(new BorderLayout());
        listCard.add(scroll, BorderLayout.CENTER);

        currentStepLabel = new JLabel("Step 0 / 0");
        currentStepLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        currentStepLabel.setForeground(ThemeManager.getInstance().ACCENT);
        currentStepLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        listCard.add(currentStepLabel, BorderLayout.SOUTH);

        p.add(listCard, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildControls() {
        UIComponents.CardPanel card = new UIComponents.CardPanel();
        card.setLayout(new BorderLayout(12, 6));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setForeground(ThemeManager.getInstance().ACCENT);
        progressBar.setBackground(ThemeManager.getInstance().BG_HOVER);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setPreferredSize(new Dimension(0, 22));
        card.add(progressBar, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);

        resetBtn = makeBtn("⏮ Reset",   UIComponents.StyledButton.Style.SECONDARY);
        prevBtn  = makeBtn("◀ Previous", UIComponents.StyledButton.Style.SECONDARY);
        nextBtn  = makeBtn("Next ▶",     UIComponents.StyledButton.Style.PRIMARY);
        autoBtn  = makeBtn("▶▶ Auto",   UIComponents.StyledButton.Style.SUCCESS);

        resetBtn.addActionListener(e -> resetSteps());
        prevBtn.addActionListener(e -> prevStep());
        nextBtn.addActionListener(e -> nextStep());
        autoBtn.addActionListener(e -> toggleAuto());

        btnRow.add(resetBtn); btnRow.add(prevBtn);
        btnRow.add(nextBtn);  btnRow.add(autoBtn);
        card.add(btnRow, BorderLayout.CENTER);
        return card;
    }

    private JButton makeBtn(String text, UIComponents.StyledButton.Style style) {
        return new UIComponents.StyledButton(text, style);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        return l;
    }

    private void generateSteps() {
        stopAuto();
        steps.clear();
        stepModel.clear();
        currentStep = 0;

        String name = (String) seriesCombo.getSelectedItem();
        int n = (int) nSpinner.getValue();
        buildSteps(name, n);
        updateFormula(name, n);
        updateProgress();
        updateNavButtons();

        if (!steps.isEmpty()) {
            stepList.setSelectedIndex(0);
            currentStepLabel.setText("Step 1 / " + steps.size());
        }
    }

    private void buildSteps(String name, int n) {
        switch (name) {
            case "Fibonacci"       -> buildFibSteps(n);
            case "Tribonacci"      -> buildTribSteps(n);
            case "Lucas"           -> buildLucasSteps(n);
            case "Prime (Trial)"   -> buildPrimeSteps(n);
            case "Arithmetic Prog."-> buildAPSteps(n);
            case "Geometric Prog." -> buildGPSteps(n);
            case "Factorial"       -> buildFactSteps(n);
            case "Pascal Triangle" -> buildPascalSteps(n);
            case "Square Numbers"  -> buildSquareSteps(n);
            case "Cube Numbers"    -> buildCubeSteps(n);
            case "Harmonic Prog."  -> buildHarmonicSteps(n);
        }
    }

    private void buildFibSteps(int n) {
        addStep("INIT", "Initialize: F(0)=0, F(1)=1");
        long a = 0, b = 1;
        addStep("TERM 1", "a(1) = 0  [seed]");
        if (n >= 2) addStep("TERM 2", "a(2) = 1  [seed]");
        for (int i = 2; i < n; i++) {
            long c = a + b;
            addStep("TERM " + (i+1), String.format("a(%d) = a(%d) + a(%d) = %d + %d = %d",
                    i+1, i, i-1, b, a, c));
            a = b; b = c;
        }
        addStep("DONE", "Fibonacci series of " + n + " terms complete ✓");
    }

    private void buildTribSteps(int n) {
        addStep("INIT", "Initialize: T(0)=0, T(1)=1, T(2)=1");
        long a=0,b=1,c=1;
        addStep("TERM 1", "a(1) = 0  [seed]");
        if(n>=2) addStep("TERM 2", "a(2) = 1  [seed]");
        if(n>=3) addStep("TERM 3", "a(3) = 1  [seed]");
        for(int i=3;i<n;i++){
            long d=a+b+c;
            addStep("TERM "+(i+1), String.format("a(%d) = %d + %d + %d = %d", i+1, a, b, c, d));
            a=b; b=c; c=d;
        }
        addStep("DONE", "Tribonacci series of " + n + " terms complete ✓");
    }

    private void buildLucasSteps(int n) {
        addStep("INIT", "Initialize: L(0)=2, L(1)=1  (different seeds from Fibonacci!)");
        long a=2,b=1;
        addStep("TERM 1","a(1) = 2  [seed]");
        if(n>=2) addStep("TERM 2","a(2) = 1  [seed]");
        for(int i=2;i<n;i++){
            long c=a+b;
            addStep("TERM "+(i+1), String.format("a(%d) = a(%d)+a(%d) = %d+%d = %d", i+1, i, i-1, b, a, c));
            a=b; b=c;
        }
        addStep("DONE","Lucas series of "+n+" terms complete ✓");
    }

    private void buildPrimeSteps(int n) {
        addStep("INIT","Start checking integers from 2 upward");
        int count=0, num=2;
        while(count<n){
            boolean prime=true;
            String reason="";
            for(int d=2;d*d<=num;d++){
                if(num%d==0){ prime=false; reason=" divisible by "+d; break; }
            }
            if(prime){
                count++;
                addStep("PRIME #"+count, num + " → is prime ✓ (no divisor found up to √"+num+"≈"+((int)Math.sqrt(num))+")");
            } else {
                addStep("SKIP", num + " → NOT prime" + reason);
            }
            num++;
        }
        addStep("DONE","First "+n+" primes found ✓");
    }

    private void buildAPSteps(int n){
        double a=1, d=3;
        addStep("INIT",String.format("a=%.0f, d=%.0f  →  formula: a(n) = a + (n-1)*d",a,d));
        for(int i=0;i<n;i++){
            double val=a+i*d;
            addStep("TERM "+(i+1),String.format("a(%d) = %.0f + (%d-1)*%.0f = %.0f",i+1,a,i+1,d,val));
        }
        addStep("DONE","Arithmetic progression of "+n+" terms ✓");
    }

    private void buildGPSteps(int n){
        double a=1, r=2;
        addStep("INIT",String.format("a=%.0f, r=%.0f  →  formula: a(n) = a * r^(n-1)",a,r));
        double cur=a;
        for(int i=0;i<n;i++){
            addStep("TERM "+(i+1),String.format("a(%d) = %.0f * %.0f^%d = %.0f",i+1,a,r,i,(long)cur));
            cur*=r;
        }
        addStep("DONE","Geometric progression of "+n+" terms ✓");
    }

    private void buildFactSteps(int n){
        addStep("INIT","0! = 1 by convention. n! = n × (n-1)!");
        long f=1;
        for(int i=1;i<=n;i++){
            f*=i;
            if(i==1) addStep("TERM 1","1! = 1");
            else addStep("TERM "+i, String.format("%d! = %d × %d! = %d", i, i, i-1, f));
        }
        addStep("DONE","Factorial series of "+n+" terms ✓");
    }

    private void buildPascalSteps(int n){
        addStep("INIT","Each element C(row,col) = C(row-1,col-1) + C(row-1,col)");
        long[][] tri=new long[n][n];
        for(int row=0;row<n;row++){
            StringBuilder sb=new StringBuilder("Row "+row+": ");
            for(int col=0;col<=row;col++){
                tri[row][col]=(col==0||col==row)?1:tri[row-1][col-1]+tri[row-1][col];
                sb.append(tri[row][col]).append("  ");
            }
            addStep("ROW "+row, sb.toString().trim());
        }
        addStep("DONE","Pascal triangle with "+n+" rows ✓");
    }

    private void buildSquareSteps(int n){
        addStep("INIT","Square numbers: a(n) = n²");
        for(int i=1;i<=n;i++) addStep("TERM "+i, String.format("a(%d) = %d² = %d",i,i,i*i));
        addStep("DONE","Square numbers series of "+n+" terms ✓");
    }

    private void buildCubeSteps(int n){
        addStep("INIT","Cube numbers: a(n) = n³");
        for(int i=1;i<=n;i++) addStep("TERM "+i, String.format("a(%d) = %d³ = %d",i,i,i*i*i));
        addStep("DONE","Cube numbers series of "+n+" terms ✓");
    }

    private void buildHarmonicSteps(int n){
        addStep("INIT","Harmonic Progression: a(n) = 1/(a + (n-1)*d),  a=1, d=1");
        for(int i=1;i<=n;i++){
            int denom=i;
            addStep("TERM "+i, String.format("a(%d) = 1/%d ≈ %.6f", i, denom, 1.0/denom));
        }
        addStep("DONE","Harmonic progression of "+n+" terms ✓");
    }

    private void addStep(String tag, String description) {
        steps.add(String.format("%-12s %s", "["+tag+"]", description));
        stepModel.addElement(steps.get(steps.size()-1));
    }

    private void updateFormula(String name, int n) {
        String html = switch(name) {
            case "Fibonacci"       -> "<html><b>F(n) = F(n-1) + F(n-2)</b>, F(0)=0, F(1)=1 &nbsp;|&nbsp; Time: <b>O(n)</b>  Space: <b>O(n)</b></html>";
            case "Tribonacci"      -> "<html><b>T(n) = T(n-1)+T(n-2)+T(n-3)</b>, seeds:0,1,1 &nbsp;|&nbsp; Time: <b>O(n)</b></html>";
            case "Lucas"           -> "<html><b>L(n) = L(n-1)+L(n-2)</b>, L(0)=2, L(1)=1 &nbsp;|&nbsp; Time: <b>O(n)</b></html>";
            case "Prime (Trial)"   -> "<html>Trial Division: check divisors up to √n &nbsp;|&nbsp; Time: <b>O(n√n)</b>  Sieve: <b>O(n log log n)</b></html>";
            case "Arithmetic Prog."-> "<html><b>a(n) = a + (n-1)×d</b>, first=1, diff=3 &nbsp;|&nbsp; Time: <b>O(n)</b>  Space: <b>O(1)</b></html>";
            case "Geometric Prog." -> "<html><b>a(n) = a × r^(n-1)</b>, first=1, ratio=2 &nbsp;|&nbsp; Time: <b>O(n)</b></html>";
            case "Factorial"       -> "<html><b>n! = n × (n-1)!</b>, 0!=1 &nbsp;|&nbsp; Time: <b>O(n)</b>  Values grow super-exponentially</html>";
            case "Pascal Triangle" -> "<html><b>C(r,c) = C(r-1,c-1) + C(r-1,c)</b> &nbsp;|&nbsp; Time: <b>O(n²)</b>  Space: <b>O(n²)</b></html>";
            case "Square Numbers"  -> "<html><b>a(n) = n²</b> &nbsp;|&nbsp; Time: <b>O(n)</b>  Space: <b>O(1)</b> per term</html>";
            case "Cube Numbers"    -> "<html><b>a(n) = n³</b> &nbsp;|&nbsp; Time: <b>O(n)</b>  Space: <b>O(1)</b> per term</html>";
            case "Harmonic Prog."  -> "<html><b>a(n) = 1/(a+(n-1)d)</b> &nbsp;|&nbsp; Time: <b>O(n)</b> &nbsp; Series diverges but slowly</html>";
            default                -> "<html>Select a series to see formula</html>";
        };
        formulaLabel.setText(html);
    }

    private void nextStep() {
        if (steps.isEmpty() || currentStep >= steps.size()-1) return;
        currentStep++;
        highlightStep(currentStep);
        updateProgress();
    }

    private void prevStep() {
        if (steps.isEmpty() || currentStep <= 0) return;
        currentStep--;
        highlightStep(currentStep);
        updateProgress();
    }

    private void resetSteps() {
        stopAuto();
        if (steps.isEmpty()) return;
        currentStep = 0;
        highlightStep(0);
        updateProgress();
    }

    private void highlightStep(int idx) {
        stepList.setSelectedIndex(idx);
        stepList.ensureIndexIsVisible(idx);
        currentStepLabel.setText("Step " + (idx+1) + " / " + steps.size());
    }

    private void updateProgress() {
        if (steps.isEmpty()) { progressBar.setValue(0); progressBar.setString("Ready"); return; }
        int pct = (int)(100.0 * (currentStep+1) / steps.size());
        progressBar.setValue(pct);
        progressBar.setString(pct + "% — Step " + (currentStep+1) + " of " + steps.size());
    }

    private void toggleAuto() {
        if (autoTimer != null && autoTimer.isRunning()) {
            stopAuto();
            autoBtn.setText("▶▶ Auto");
        } else {
            autoBtn.setText("⏸ Pause");
            autoTimer = new javax.swing.Timer(700, e -> {
                if (currentStep < steps.size()-1) { currentStep++; highlightStep(currentStep); updateProgress(); }
                else { stopAuto(); autoBtn.setText("▶▶ Auto"); }
            });
            autoTimer.start();
        }
    }

    private void stopAuto() {
        if (autoTimer != null) { autoTimer.stop(); autoTimer = null; }
        autoBtn.setText("▶▶ Auto");
    }

    private void updateNavButtons() {
        boolean hasSteps = !steps.isEmpty();
        nextBtn.setEnabled(hasSteps);
        prevBtn.setEnabled(hasSteps);
        resetBtn.setEnabled(hasSteps);
        autoBtn.setEnabled(hasSteps);
    }

    /** Custom cell renderer that colours different step types */
    private class StepCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ThemeManager tm = ThemeManager.getInstance();
            String text = value.toString();
            if (!isSelected) {
                if (text.contains("[INIT]"))  lbl.setForeground(tm.INFO);
                else if (text.contains("[DONE]"))  lbl.setForeground(tm.SUCCESS);
                else if (text.contains("[SKIP]"))  lbl.setForeground(tm.TEXT_MUTED);
                else if (text.contains("[PRIME"))  lbl.setForeground(tm.SUCCESS);
                else if (index == currentStep)     lbl.setForeground(tm.ACCENT);
                else                               lbl.setForeground(tm.TEXT_PRIMARY);
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            return lbl;
        }
    }
}
