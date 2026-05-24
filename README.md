# 🔢 AlgoSeries-Pro

[![Java](https://img.shields.io/badge/Java-21-blue?logo=java)](https://openjdk.org/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-orange)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![JFreeChart](https://img.shields.io/badge/Charts-JFreeChart-green)](https://www.jfree.org/jfreechart/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Final%20Year%20Project-brightgreen)]()

> **Advanced Mathematical Series Manipulator** — A professional, dashboard-style Java Swing desktop application for generating, visualizing, analyzing, and exporting mathematical series and algorithms.

---

## 📸 Screenshots

| Dashboard | Series Generator | Graph Viewer |
|-----------|-----------------|--------------|
| Modern dark/light dashboard with quick stats | 12 algorithms with iterative & recursive modes | Interactive custom charts with zoom & hover |

| Step-by-Step | Sorting Visualizer | Complexity Chart |
|---|---|---|
| Watch each computation unfold | Animated sort bars with comparisons/swaps | Big-O growth curves side by side |

---

## ✨ Features

### 📊 Series Generation (12 Algorithms)
| Series | Formula | Complexity |
|--------|---------|------------|
| Fibonacci | F(n) = F(n-1) + F(n-2) | O(n) |
| Tribonacci | T(n) = T(n-1)+T(n-2)+T(n-3) | O(n) |
| Lucas Sequence | L(n)=L(n-1)+L(n-2), seeds 2,1 | O(n) |
| Prime Numbers | Sieve of Eratosthenes | O(n log log n) |
| Arithmetic Progression | a + (n-1)d | O(n) |
| Geometric Progression | a × r^(n-1) | O(n) |
| Square Numbers | n² | O(n) |
| Cube Numbers | n³ | O(n) |
| Factorial Series | n! (BigInteger) | O(n) |
| Harmonic Progression | 1/(a+(n-1)d) | O(n) |
| Pascal's Triangle | C(r,c)=C(r-1,c-1)+C(r-1,c) | O(n²) |
| Custom Series | User-defined seeds & recurrence | Varies |

### 🧠 Algorithm Modes
- **Iterative** — optimal time/space
- **Recursive (memoised)** — demonstrates memoization
- **Performance timing** in ns / µs / ms
- **Memory usage** display per generation

### 📈 Graph & Visualization
- Custom Java 2D line chart and bar chart
- Mouse-wheel **zoom** and drag **pan**
- Hover **tooltip** with exact values
- Multi-series **comparison** overlay
- Export chart as **PNG**

### 👣 Step-by-Step Explanation
- Watch every computation step unfold
- Color-coded steps (INIT / TERM / DONE)
- Auto-play with adjustable speed
- Progress bar

### 🔀 Sorting Algorithm Visualizer
- Bubble, Selection, Insertion, Quick, Merge Sort
- Real-time animated bar chart
- Comparison & swap counters
- Pause / Resume / Reset

### 📐 Time Complexity Visualizer
- O(1), O(log n), O(n), O(n log n), O(n²), O(2ⁿ)
- Toggle individual curves
- Adjustable n range

### 🔢 Matrix Series Operations
- Multiplication Table, Pascal Matrix, Identity Matrix
- Magic Square (Siamese method)
- Spiral Matrix, Fibonacci Fill, Power Matrix

### 💾 Export
- **TXT** — formatted report with branding
- **CSV** — importable into Excel / Google Sheets
- **PNG** — chart image export

### 🎨 UI & UX
- Dark / Light mode (persisted via Preferences)
- Sidebar navigation with hover effects
- Keyboard shortcuts (Ctrl+1…9, Ctrl+T, Ctrl+Q)
- Multi-threaded generation (non-blocking UI)
- Session history (last 500 generations)

---

## 🛠️ Technologies

| Technology | Purpose |
|-----------|---------|
| Java 21 | Core language |
| Java Swing | GUI framework |
| Java 2D | Custom chart rendering |
| JFreeChart 1.x | Additional charting support |
| BigInteger | Exact factorial computation |
| SwingWorker | Background threading |
| java.util.prefs | Persisting theme preference |

---

## 🚀 Installation & Running

### Prerequisites
- Java 11 or higher (Java 21 recommended)
- No Maven/Gradle needed for the pre-built JAR

### Run the pre-built JAR (easiest)
```bash
java -jar AlgoSeries-Pro.jar
```

### Build from source (Linux/macOS)
```bash
chmod +x build.sh
./build.sh
java -jar AlgoSeries-Pro.jar
```

### Build from source (Windows)
```bat
build.bat
java -jar AlgoSeries-Pro.jar
```

---

## 📁 Project Structure

```
AlgoSeries-Pro/
├── src/
│   └── com/algoseries/
│       ├── Main.java                      # Entry point
│       ├── algorithms/                    # Series generators
│       │   ├── SeriesGenerator.java       # Interface
│       │   ├── AbstractGenerator.java     # Base class
│       │   ├── FibonacciGenerator.java
│       │   ├── TribonacciGenerator.java
│       │   ├── LucasGenerator.java
│       │   ├── PrimeGenerator.java
│       │   ├── APGenerator.java
│       │   ├── GPGenerator.java
│       │   ├── SquareGenerator.java
│       │   ├── CubeGenerator.java
│       │   ├── FactorialGenerator.java
│       │   ├── HarmonicGenerator.java
│       │   ├── PascalGenerator.java
│       │   └── CustomGenerator.java
│       ├── chart/
│       │   └── SeriesChartPanel.java      # Custom Java 2D charts
│       ├── model/
│       │   ├── SeriesResult.java          # Immutable result object
│       │   └── SeriesType.java            # Enum of series types
│       ├── ui/                            # All Swing panels
│       │   ├── MainFrame.java
│       │   ├── Sidebar.java
│       │   ├── ThemeManager.java
│       │   ├── UIComponents.java
│       │   ├── DashboardPanel.java
│       │   ├── SeriesPanel.java
│       │   ├── GraphPanel.java
│       │   ├── ComparePanel.java
│       │   ├── PatternsPanel.java
│       │   ├── PerformancePanel.java
│       │   ├── HistoryPanel.java
│       │   ├── ExportPanel.java
│       │   ├── StepByStepPanel.java       # NEW: step-by-step
│       │   ├── MatrixSeriesPanel.java     # NEW: matrix operations
│       │   ├── SortingVisualizerPanel.java# NEW: sort visualizer
│       │   └── ComplexityVisualizerPanel.java # NEW: Big-O chart
│       └── utils/
│           └── HistoryManager.java
├── lib/
│   ├── jfreechart.jar
│   └── jcommon.jar
├── AlgoSeries-Pro.jar                     # Pre-built runnable JAR
├── build.sh                               # Linux/macOS build script
├── build.bat                              # Windows build script
└── README.md
```

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|---------|--------|
| Ctrl+1 | Dashboard |
| Ctrl+2 | Series Generator |
| Ctrl+3 | Graph Viewer |
| Ctrl+4 | Compare Series |
| Ctrl+5 | Patterns |
| Ctrl+T | Toggle Dark/Light mode |
| Ctrl+S | Export (TXT) |
| Ctrl+E | Export (CSV) |
| Ctrl+P | Performance Benchmark |
| Ctrl+H | History |
| Ctrl+N | New Session |
| Ctrl+Q | Exit |

---

## 🧮 Algorithms — Quick Reference

### Fibonacci vs Tribonacci
- Fibonacci sums the **last 2** terms; Tribonacci sums the **last 3**
- Both implemented iteratively (O(1) extra space) and recursively with memoization

### Sieve of Eratosthenes (Primes)
- Upper bound estimated as `n(ln n + ln ln n)` via prime counting function
- All composites marked in O(n log log n) total

### Memoization
- Recursive variants cache previously computed values in a `HashMap`
- Eliminates redundant sub-problems; turns exponential recursion into O(n)

### Magic Squares
- Uses the Siamese (De la Loubère) method for odd-order squares
- Every row, column, and diagonal sums to `n(n²+1)/2`

---

## 📈 Future Improvements

- [ ] PDF export with embedded graphs (Apache PDFBox)
- [ ] Animated series progression on graph panel
- [ ] User-defined formula parser
- [ ] Database persistence for history
- [ ] Unit tests (JUnit 5)
- [ ] Modular JavaFX version

---

## 👥 Contributors

| Name | Role |
|------|------|
| Raj Rakshit Sahoo | Algorithm design, GUI, architecture |

---

## 📄 License

MIT License — free for educational and portfolio use.

---

*Built with ❤️ using Java Swing · Final Year Project · GitHub Portfolio Ready*
