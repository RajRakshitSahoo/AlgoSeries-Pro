package com.algoseries.model;
 
/**
 * Enumeration of all supported mathematical series types.
 * Each type carries a display label, category and complexity annotation.
 */
public enum SeriesType {
    FIBONACCI      ("Fibonacci",         "Sequence",    "O(n)",   "O(n)"),
    TRIBONACCI     ("Tribonacci",        "Sequence",    "O(n)",   "O(n)"),
    LUCAS          ("Lucas Sequence",    "Sequence",    "O(n)",   "O(n)"),
    PRIME          ("Prime Numbers",     "Number Theory","O(n√n)","O(n)"),
    ARITHMETIC     ("Arithmetic Prog.",  "Progression", "O(n)",   "O(1)"),
    GEOMETRIC      ("Geometric Prog.",   "Progression", "O(n)",   "O(1)"),
    SQUARE         ("Square Numbers",    "Power",       "O(n)",   "O(1)"),
    CUBE           ("Cube Numbers",      "Power",       "O(n)",   "O(1)"),
    FACTORIAL      ("Factorial Series",  "Combinatorial","O(n)",  "O(n)"),
    HARMONIC       ("Harmonic Prog.",    "Progression", "O(n)",   "O(n)"),
    PASCAL         ("Pascal Triangle",   "Combinatorial","O(n²)", "O(n²)"),
    CUSTOM         ("Custom Series",     "Custom",      "Varies", "Varies");

    private final String displayName;
    private final String category;
    private final String timeComplexity;
    private final String spaceComplexity;

    SeriesType(String displayName, String category,
               String timeComplexity, String spaceComplexity) {
        this.displayName    = displayName;
        this.category       = category;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity= spaceComplexity;
    }

    public String getDisplayName()     { return displayName;     }
    public String getCategory()        { return category;        }
    public String getTimeComplexity()  { return timeComplexity;  }
    public String getSpaceComplexity() { return spaceComplexity; }

    @Override
    public String toString() { return displayName; }
}
