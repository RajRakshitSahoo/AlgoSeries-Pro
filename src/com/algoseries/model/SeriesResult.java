package com.algoseries.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Immutable result object returned by every series generator.
 * Carries the computed values together with performance metadata.
 */
public class SeriesResult {

    private final SeriesType type;
    private final List<Double> values;
    private final String algorithm;        // "Iterative" or "Recursive"
    private final long executionTimeNs;    // nanoseconds
    private final long memoryUsedBytes;
    private final String parameters;       // human-readable params summary
    private final LocalDateTime timestamp;
    private final String extraInfo;        // e.g. Pascal triangle formatted

    /* ── Constructor ─────────────────────────────────────────────── */
    public SeriesResult(SeriesType type,
                        List<Double> values,
                        String algorithm,
                        long executionTimeNs,
                        long memoryUsedBytes,
                        String parameters,
                        String extraInfo) {
        this.type             = type;
        this.values           = new ArrayList<>(values);
        this.algorithm        = algorithm;
        this.executionTimeNs  = executionTimeNs;
        this.memoryUsedBytes  = memoryUsedBytes;
        this.parameters       = parameters;
        this.timestamp        = LocalDateTime.now();
        this.extraInfo        = extraInfo;
    }

    /* ── Accessors ──────────────────────────────────────────────── */
    public SeriesType getType()           { return type;            }
    public List<Double> getValues()       { return new ArrayList<>(values); }
    public String getAlgorithm()          { return algorithm;       }
    public long getExecutionTimeNs()      { return executionTimeNs; }
    public long getMemoryUsedBytes()      { return memoryUsedBytes; }
    public String getParameters()         { return parameters;      }
    public LocalDateTime getTimestamp()   { return timestamp;       }
    public String getExtraInfo()          { return extraInfo;       }
    public int getCount()                 { return values.size();   }

    /** Formatted execution time (auto-selects ns / µs / ms). */
    public String getFormattedTime() {
        if (executionTimeNs < 1_000)
            return executionTimeNs + " ns";
        if (executionTimeNs < 1_000_000)
            return String.format("%.2f µs", executionTimeNs / 1_000.0);
        return String.format("%.3f ms", executionTimeNs / 1_000_000.0);
    }

    /** Formatted memory usage. */
    public String getFormattedMemory() {
        if (memoryUsedBytes < 1024)
            return memoryUsedBytes + " B";
        if (memoryUsedBytes < 1024 * 1024)
            return String.format("%.2f KB", memoryUsedBytes / 1024.0);
        return String.format("%.2f MB", memoryUsedBytes / (1024.0 * 1024));
    }

    /** Comma-separated values string. */
    public String getValuesAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            double v = values.get(i);
            if (v == Math.floor(v) && !Double.isInfinite(v))
                sb.append((long) v);
            else
                sb.append(String.format("%.4f", v));
            if (i < values.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    /** Formatted timestamp string. */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %d terms | %s",
                type.getDisplayName(), algorithm, values.size(), getFormattedTimestamp());
    }
}
