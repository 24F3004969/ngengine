package org.teavm.classlib.java.util.concurrent;


public class TExecutorService extends TScheduledThreadPoolExecutor{

    public TExecutorService(int nThreads) {
        super(nThreads);
        
    }

    public static TExecutorService newFixedThreadPool(int nThreads, TThreadFactory threadFactory) {
        return new TExecutorService(nThreads);
    }

}
