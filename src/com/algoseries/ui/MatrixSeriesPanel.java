package com.algoseries.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Matrix Series Operations Panel
 * Generate matrices (multiplication table, identity, magic square,
 * Pascal 2D, spiral) and view them in an interactive table.
 */
public class MatrixSeriesPanel extends JPanel {

    private JComboBox<String> typeCombo;
    private JSpinner sizeSpinner;
    private JTable   table;
    private DefaultTableModel tableModel;
    private JLabel   titleLabel, descLabel;
    private JTextArea formulaArea;

    private static final String[] MATRIX_TYPES = {
        "Multiplication Table",
        "Pascal Triangle (Matrix)",
        "Identity Matrix",
        "Magic Square",
        "Spiral Matrix",
        "Fibonacci Matrix",
        "Addition Table",
        "Power Matrix (n^k)",
    };

    public MatrixSeriesPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildInfo(),   BorderLayout.SOUTH);

        // Generate AFTER buildInfo() so titleLabel and descLabel are non-null
        generate();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.add(new UIComponents.SectionLabel("Matrix Series Operations"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        typeCombo = new JComboBox<>(MATRIX_TYPES);
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeCombo.setPreferredSize(new Dimension(220, 30));
        typeCombo.addActionListener(e -> generate());

        JLabel szLbl = lbl("Size (n):");
        sizeSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 15, 1));
        sizeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sizeSpinner.setPreferredSize(new Dimension(70, 30));

        UIComponents.StyledButton genBtn = new UIComponents.StyledButton("⬡ Generate", UIComponents.StyledButton.Style.PRIMARY);
        genBtn.addActionListener(e -> generate());

        right.add(typeCombo);
        right.add(szLbl); right.add(sizeSpinner);
        right.add(genBtn);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCenter() {
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                ThemeManager tm = ThemeManager.getInstance();
                if (isRowSelected(row))      c.setBackground(tm.ACCENT_DIM);
                else if (row % 2 == 0)       c.setBackground(tm.BG_CARD);
                else                         c.setBackground(tm.BG_SECONDARY);
                c.setForeground(tm.TEXT_PRIMARY);
                return c;
            }
        };
        table.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(ThemeManager.getInstance().BORDER);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(ThemeManager.getInstance().BG_SECONDARY);
        table.getTableHeader().setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ((DefaultTableCellRenderer)table.getDefaultRenderer(Object.class)).setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new UIComponents.RoundBorder(ThemeManager.getInstance().BORDER, 8));
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(ThemeManager.getInstance().BG_CARD);

        UIComponents.CardPanel card = new UIComponents.CardPanel("Matrix View");
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(card, BorderLayout.CENTER);

        // NOTE: generate() is NOT called here intentionally.
        // It is called from the constructor AFTER buildInfo() creates titleLabel/descLabel.
        return p;
    }

    private JPanel buildInfo() {
        UIComponents.CardPanel card = new UIComponents.CardPanel("Formula & Description");
        card.setLayout(new BorderLayout(0, 4));
        card.setPreferredSize(new Dimension(0, 80));

        titleLabel = new JLabel("Multiplication Table");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(ThemeManager.getInstance().ACCENT);

        descLabel = new JLabel("M[i][j] = i × j   —   Time: O(n²), Space: O(n²)");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        return card;
    }

    private void generate() {
        int n = (int) sizeSpinner.getValue();
        String type = (String) typeCombo.getSelectedItem();
        int[][] matrix = buildMatrix(type, n);
        int cols = matrix[0].length;

        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        // Column headers
        for (int c = 0; c < cols; c++) tableModel.addColumn(c == 0 ? "n" : String.valueOf(c));

        // Rows
        for (int r = 0; r < matrix.length; r++) {
            Object[] row = new Object[cols];
            for (int c = 0; c < cols; c++) row[c] = matrix[r][c];
            tableModel.addRow(row);
        }

        // Auto-fit column widths
        for (int c = 0; c < table.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(60);
        }

        updateInfo(type);
    }

    private int[][] buildMatrix(String type, int n) {
        return switch(type) {
            case "Multiplication Table" -> multiTable(n);
            case "Pascal Triangle (Matrix)" -> pascalMatrix(n);
            case "Identity Matrix"      -> identity(n);
            case "Magic Square"         -> magicSquare(n % 2 == 0 ? n+1 : n); // magic squares need odd n
            case "Spiral Matrix"        -> spiral(n);
            case "Fibonacci Matrix"     -> fibMatrix(n);
            case "Addition Table"       -> addTable(n);
            case "Power Matrix (n^k)"   -> powerMatrix(n);
            default                     -> new int[][]{{0}};
        };
    }

    private int[][] multiTable(int n) {
        int[][] m = new int[n][n+1];
        for (int i = 0; i < n; i++) {
            m[i][0] = i+1;
            for (int j = 0; j < n; j++) m[i][j+1] = (i+1)*(j+1);
        }
        return m;
    }

    private int[][] addTable(int n) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) m[i][j] = (i+1)+(j+1);
        return m;
    }

    private int[][] identity(int n) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) m[i][i] = 1;
        return m;
    }

    private int[][] pascalMatrix(int n) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            m[i][0] = 1;
            for (int j = 1; j <= i; j++)
                m[i][j] = m[i-1][j-1] + (j<i ? m[i-1][j] : 0);
        }
        return m;
    }

    private int[][] magicSquare(int n) {
        if (n % 2 == 0) n++;  // ensure odd
        int[][] m = new int[n][n];
        int r = 0, c = n/2;
        for (int num = 1; num <= n*n; num++) {
            m[r][c] = num;
            int nr = (r-1+n)%n, nc = (c+1)%n;
            if (m[nr][nc] != 0) { nr = (r+1)%n; nc = c; }
            r = nr; c = nc;
        }
        return m;
    }

    private int[][] spiral(int n) {
        int[][] m = new int[n][n];
        int top=0,bottom=n-1,left=0,right=n-1,num=1;
        while(top<=bottom && left<=right) {
            for(int i=left;i<=right;i++)   m[top][i]=num++;
            top++;
            for(int i=top;i<=bottom;i++)   m[i][right]=num++;
            right--;
            if(top<=bottom) { for(int i=right;i>=left;i--) m[bottom][i]=num++; bottom--; }
            if(left<=right) { for(int i=bottom;i>=top;i--) m[i][left]=num++; left++; }
        }
        return m;
    }

    private int[][] fibMatrix(int n) {
        int[][] m = new int[n][n];
        // Fill with Fibonacci numbers
        long a=0,b=1;
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                m[i][j]=(int)Math.min(a, Integer.MAX_VALUE);
                long c=a+b; a=b; b=c;
            }
        }
        return m;
    }

    private int[][] powerMatrix(int n) {
        int[][] m = new int[n][n];
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++) {
                long val = (long)Math.pow(i+1, j+1);
                m[i][j] = val > Integer.MAX_VALUE ? -1 : (int)val;
            }
        return m;
    }

    private void updateInfo(String type) {
        String[] info = switch(type) {
            case "Multiplication Table" -> new String[]{"Multiplication Table", "M[i][j] = i × j  |  Time: O(n²), Space: O(n²)"};
            case "Pascal Triangle (Matrix)" -> new String[]{"Pascal Triangle (Matrix)", "C(i,j) = C(i-1,j-1)+C(i-1,j)  |  Time: O(n²)"};
            case "Identity Matrix"      -> new String[]{"Identity Matrix", "M[i][j] = 1 if i==j else 0  |  Used in linear algebra"};
            case "Magic Square"         -> new String[]{"Magic Square (Siamese method)", "All rows, cols, diagonals sum to n(n²+1)/2  |  Odd n only"};
            case "Spiral Matrix"        -> new String[]{"Spiral Matrix", "Numbers 1..n² placed in clockwise spiral  |  Time: O(n²)"};
            case "Fibonacci Matrix"     -> new String[]{"Fibonacci Fill Matrix", "Elements are consecutive Fibonacci numbers"};
            case "Addition Table"       -> new String[]{"Addition Table", "M[i][j] = i + j  |  Time: O(n²), Space: O(n²)"};
            case "Power Matrix (n^k)"   -> new String[]{"Power Matrix", "M[i][j] = i^j  |  Row=base, Col=exponent"};
            default                     -> new String[]{type, ""};
        };
        titleLabel.setText(info[0]);
        descLabel.setText(info[1]);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeManager.getInstance().TEXT_SECONDARY);
        return l;
    }
}
