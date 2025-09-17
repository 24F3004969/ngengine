package org.teavm.classlib.java.util.concurrent;

public class TExecutors {
    public static TExecutorService newFixedThreadPool(int nThreads, TThreadFactory threadFactory) {
        return new TExecutorService(nThreads);
    }

}
