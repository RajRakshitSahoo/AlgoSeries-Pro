package com.algoseries.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Sorting Algorithm Visualizer
 * Animates Bubble, Selection, Insertion, Merge, and Quick sort with bar charts.
 */
public class SortingVisualizerPanel extends JPanel {

    private int[] array;
    private int[] highlights;   // indices to color differently
    private volatile boolean running = false;
    private volatile boolean paused  = false;
    private int speed = 80;     // ms delay

    private JComboBox<String> algoCombo;
    private JSlider  sizeSlider, speedSlider;
    private JButton  startBtn, pauseBtn, resetBtn;
    private JLabel   statusLabel, cmpLabel, swapLabel;
    private SortCanvas canvas;
    private volatile long comparisons = 0, swaps = 0;

    private static final String[] ALGORITHMS = {
        "Bubble Sort", "Selection Sort", "Insertion Sort", "Quick Sort", "Merge Sort"
    };

    private static final Color[] BAR_COLORS = {
        new Color(99, 102, 241),  // normal
        new Color(251, 191, 36),  // comparing
        new Color(239, 68, 68),   // swapping
        new Color(52, 211, 153),  // sorted
    };

    public SortingVisualizerPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        canvas = new SortCanvas();
        UIComponents.CardPanel canvasCard = new UIComponents.CardPanel("Visualization");
        canvasCard.setLayout(new BorderLayout());
        canvasCard.add(canvas, BorderLayout.CENTER);
        add(canvasCard, BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);

        generateArray(40);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        add(new UIComponents.SectionLabel("Sorting Algorithm Visualizer"), BorderLayout.NORTH);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        algoCombo = new JComboBox<>(ALGORITHMS);
        algoCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        algoCombo.setPreferredSize(new Dimension(160, 30));

        JLabel szLbl = label("Size:");
        sizeSlider = new JSlider(10, 100, 40);
        sizeSlider.setOpaque(false);
        sizeSlider.setPreferredSize(new Dimension(120, 30));
        sizeSlider.addChangeListener(e -> { if(!running) generateArray(sizeSlider.getValue()); });

        right.add(new UIComponents.SectionLabel("Sorting Algorithm Visualizer"));
        right.add(algoCombo);
        right.add(szLbl); right.add(sizeSlider);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildControls() {
        UIComponents.CardPanel card = new UIComponents.CardPanel();
        card.setLayout(new BorderLayout(10, 6));

        // Stats row
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        stats.setOpaque(false);
        cmpLabel  = statLabel("Comparisons: 0");
        swapLabel = statLabel("Swaps/Moves: 0");
        statusLabel = statLabel("Status: Ready");
        stats.add(cmpLabel); stats.add(swapLabel); stats.add(statusLabel);
        card.add(stats, BorderLayout.NORTH);

        // Speed + buttons
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        row.setOpaque(false);

        JLabel spLbl = label("Speed:");
        speedSlider = new JSlider(10, 300, 80);
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(120, 28));
        speedSlider.setInverted(true); // higher = faster (lower ms)
        speedSlider.addChangeListener(e -> speed = speedSlider.getValue());

        startBtn = new UIComponents.StyledButton("▶ Sort",   UIComponents.StyledButton.Style.PRIMARY);
        pauseBtn = new UIComponents.StyledButton("⏸ Pause", UIComponents.StyledButton.Style.SECONDARY);
        resetBtn = new UIComponents.StyledButton("⟳ Reset", UIComponents.StyledButton.Style.GHOST);
        pauseBtn.setEnabled(false);

        startBtn.addActionListener(e -> startSort());
        pauseBtn.addActionListener(e -> togglePause());
        resetBtn.addActionListener(e -> { stopSort(); generateArray(sizeSlider.getValue()); });

        row.add(spLbl); row.add(speedSlider);
        row.add(startBtn); row.add(pauseBtn); row.add(resetBtn);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private void generateArray(int size) {
        array = new int[size];
        for (int i = 0; i < size; i++) array[i] = 10 + (int)(Math.random() * 300);
        highlights = new int[]{};
        comparisons = 0; swaps = 0;
        updateStats();
        statusLabel.setText("Status: Ready");
        canvas.repaint();
    }

    private void startSort() {
        if (running) return;
        running = true; paused = false;
        comparisons = 0; swaps = 0;
        startBtn.setEnabled(false);
        pauseBtn.setEnabled(true);
        algoCombo.setEnabled(false);
        sizeSlider.setEnabled(false);
        statusLabel.setText("Status: Sorting...");

        Thread t = new Thread(() -> {
            String algo = (String) algoCombo.getSelectedItem();
            try {
                switch(algo) {
                    case "Bubble Sort"    -> bubbleSort();
                    case "Selection Sort" -> selectionSort();
                    case "Insertion Sort" -> insertionSort();
                    case "Quick Sort"     -> quickSort(0, array.length-1);
                    case "Merge Sort"     -> mergeSort(0, array.length-1);
                }
            } catch(InterruptedException ignored){}
            running = false;
            SwingUtilities.invokeLater(() -> {
                startBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
                algoCombo.setEnabled(true);
                sizeSlider.setEnabled(true);
                highlights = new int[]{};
                canvas.repaint();
                statusLabel.setText("Status: Done ✓");
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void stopSort() {
        running = false; paused = false;
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        algoCombo.setEnabled(true);
        sizeSlider.setEnabled(true);
    }

    private void togglePause() {
        paused = !paused;
        pauseBtn.setText(paused ? "▶ Resume" : "⏸ Pause");
        statusLabel.setText(paused ? "Status: Paused" : "Status: Sorting...");
    }

    private void delay() throws InterruptedException {
        while(paused) Thread.sleep(50);
        Thread.sleep(speed);
    }

    private void mark(int... idx) { highlights = idx; canvas.repaint(); updateStats(); }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            cmpLabel.setText("Comparisons: " + comparisons);
            swapLabel.setText("Swaps/Moves: " + swaps);
        });
    }

    private void swap(int i, int j) { int t=array[i]; array[i]=array[j]; array[j]=t; swaps++; }

    /* ── Sort algorithms ─────────────────────────────────────────── */
    private void bubbleSort() throws InterruptedException {
        int n = array.length;
        for(int i=0; i<n-1; i++){
            for(int j=0; j<n-i-1; j++){
                comparisons++;
                mark(j, j+1);
                delay();
                if(!running) return;
                if(array[j]>array[j+1]){ swap(j,j+1); }
            }
        }
    }

    private void selectionSort() throws InterruptedException {
        int n = array.length;
        for(int i=0; i<n-1; i++){
            int minIdx=i;
            for(int j=i+1; j<n; j++){
                comparisons++;
                mark(minIdx, j);
                delay();
                if(!running) return;
                if(array[j]<array[minIdx]) minIdx=j;
            }
            swap(i, minIdx); mark(i);
        }
    }

    private void insertionSort() throws InterruptedException {
        int n = array.length;
        for(int i=1; i<n; i++){
            int key=array[i], j=i-1;
            while(j>=0 && array[j]>key){
                comparisons++;
                array[j+1]=array[j]; swaps++;
                mark(j, j+1);
                delay();
                if(!running) return;
                j--;
            }
            array[j+1]=key;
        }
    }

    private void quickSort(int lo, int hi) throws InterruptedException {
        if(lo<hi && running){
            int p=partition(lo,hi);
            quickSort(lo,p-1);
            quickSort(p+1,hi);
        }
    }

    private int partition(int lo, int hi) throws InterruptedException {
        int pivot=array[hi], i=lo-1;
        for(int j=lo;j<hi;j++){
            comparisons++;
            mark(j, hi);
            delay();
            if(!running) return lo;
            if(array[j]<=pivot){ i++; swap(i,j); }
        }
        swap(i+1,hi);
        return i+1;
    }

    private void mergeSort(int lo, int hi) throws InterruptedException {
        if(lo<hi && running){
            int mid=(lo+hi)/2;
            mergeSort(lo,mid);
            mergeSort(mid+1,hi);
            merge(lo,mid,hi);
        }
    }

    private void merge(int lo, int mid, int hi) throws InterruptedException {
        int n1=mid-lo+1, n2=hi-mid;
        int[] L=Arrays.copyOfRange(array,lo,mid+1);
        int[] R=Arrays.copyOfRange(array,mid+1,hi+1);
        int i=0,j=0,k=lo;
        while(i<n1&&j<n2){
            comparisons++;
            mark(lo+i,mid+1+j);
            delay();
            if(!running) return;
            if(L[i]<=R[j]) { array[k++]=L[i++]; }
            else            { array[k++]=R[j++]; }
            swaps++;
        }
        while(i<n1){ array[k++]=L[i++]; swaps++; }
        while(j<n2){ array[k++]=R[j++]; swaps++; }
    }

    private JLabel label(String t) {
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        return l;
    }

    private JLabel statLabel(String t) {
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(ThemeManager.getInstance().ACCENT);
        return l;
    }

    /* ── Canvas ─────────────────────────────────────────────────── */
    private class SortCanvas extends JPanel {
        SortCanvas() { setOpaque(false); setPreferredSize(new Dimension(600, 340)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (array == null || array.length == 0) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ThemeManager tm = ThemeManager.getInstance();

            int w = getWidth(), h = getHeight();
            int n = array.length;
            int barW = Math.max(2, w/n - 1);
            int maxVal = 310;

            Set<Integer> hl = new HashSet<>();
            if (highlights != null) for (int idx : highlights) hl.add(idx);

            for (int i = 0; i < n; i++) {
                int barH = (int)((double)array[i] / maxVal * (h - 20));
                int x = i * (barW + 1);
                int y = h - barH;
                Color c;
                if (hl.contains(i))      c = new Color(239, 68, 68);
                else if (!running)        c = tm.SUCCESS;
                else                      c = tm.ACCENT;
                g2.setColor(c);
                g2.fillRoundRect(x, y, barW, barH, 2, 2);
                g2.setColor(new Color(0,0,0,20));
                g2.drawRoundRect(x, y, barW, barH, 2, 2);
            }
        }
    }
}
