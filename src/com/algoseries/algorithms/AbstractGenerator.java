package com.algoseries.algorithms;
public abstract class AbstractGenerator implements SeriesGenerator {
    protected static long measureMemory(Runnable task) {
        Runtime rt=Runtime.getRuntime(); System.gc();
        long before=rt.totalMemory()-rt.freeMemory(); task.run();
        return Math.max(0,rt.totalMemory()-rt.freeMemory()-before);
    }
}
