package com.algoseries.utils;

import com.algoseries.model.SeriesResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton that maintains an in-session history of every generated series.
 * UI panels observe changes via a simple listener pattern.
 */
public class HistoryManager {

    private static HistoryManager instance;
    private final List<SeriesResult> history = new ArrayList<>();
    private final List<Runnable> listeners   = new ArrayList<>();

    private HistoryManager() {}

    public static HistoryManager getInstance() {
        if (instance == null) instance = new HistoryManager();
        return instance;
    }

    /** Add a result and notify listeners. */
    public void add(SeriesResult result) {
        history.add(0, result);          // newest first
        if (history.size() > 500)
            history.remove(history.size() - 1);  // cap history
        notifyListeners();
    }

    /** Unmodifiable view of the history list. */
    public List<SeriesResult> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int size() { return history.size(); }

    public void clear() {
        history.clear();
        notifyListeners();
    }

    public void addChangeListener(Runnable r) {
        listeners.add(r);
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
