package com.algoseries.algorithms;
import com.algoseries.model.SeriesResult;
public interface SeriesGenerator {
    SeriesResult generateIterative(int n, double... params);
    SeriesResult generateRecursive(int n, double... params);
}
