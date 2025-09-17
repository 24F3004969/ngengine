package org.teavm.classlib.java.util.concurrent;

 import java.lang.Override;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
 import java.util.concurrent.TimeUnit;

public class TScheduledThreadPoolExecutor implements Executor {

    


    private class ExecutorThread implements Executor {

        private boolean closed = false;
        private List<Runnable> tasks = new LinkedList<>();
        private Thread threadPool[];

        public ExecutorThread(int n){
            threadPool = new Thread[n];
            for(int i=0; i<n; ++i){
                threadPool[i] = new Thread(this::run);
                threadPool[i].setName("ExecutorThread "+i);
                threadPool[i].setDaemon(true);
            }
        }

        public void start(){
            for(Thread t : threadPool){
                t.start();
            }
        }

        public void setName(String name){
            for(int i=0; i<threadPool.length; ++i){
                threadPool[i].setName(name+"-"+i);
            }
        }



        public void run() {
            while (!closed) {
                Runnable task = null;
                synchronized (tasks) {
                    if (!tasks.isEmpty()) {
                        task = tasks.remove(0);
                    }
                }
                if (task != null) {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        synchronized (tasks) {
                            if (tasks.isEmpty() && !closed) {
                                tasks.wait(100);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            
            }
        }

        @Override
        public void execute(Runnable command) {
            synchronized (tasks) {
                tasks.add(command);
                tasks.notifyAll();
            }
        }

        public void close() {
            closed = true;
            synchronized (tasks) {
                tasks.notifyAll();
            }
        }
    }


    private  volatile boolean running = true;
    private final ExecutorThread thread;


    public TScheduledThreadPoolExecutor(int poolSize) {
        this.thread = new ExecutorThread(poolSize);
        this.thread.setName("ScheduledThreadPoolExecutor Thread");
        thread.start();
    }
    

    public <V> TFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if(delay == 0){ // shortcut for tasks without delay
            TFutureTask<V> futureTask = new TFutureTask<V>();
            this.thread.execute(()->{
                try {
                    V res = callable.call();
                    futureTask.setResult(res);
                } catch (Exception e) {
                    futureTask.setException(e);
                }
            });
            return futureTask;
        }

        long ms = unit.toMillis(delay);
        TFutureTask<V> futureTask = new TFutureTask<V>();
        Thread t = new Thread(()->{ // ensure it runs on the teavms suspendable context
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.thread.execute(()->{
                try {
                    V res = callable.call();
                    futureTask.setResult(res);
                } catch (Exception e) {
                    futureTask.setException(e);
                }
            });
        });
        // get stacktrace
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        t.setName("Schedule Waiter "+( stack.length>2?(" at "+stack[2].toString()):""));
        t.start();        
        return futureTask;

    }
    @Override
    public void execute(Runnable command) {
        submit(command);
    }

    public <T> TFuture<T> submit(Runnable task,T result) {
        TFuture<T> res = schedule(() -> {
            try{
                task.run();
                afterExecute(task, null);
            }catch(Exception e){
                e.printStackTrace();
                afterExecute(task, e);

            }
            return result;
        }, 0, TimeUnit.MILLISECONDS);
        return res;
    
    }
    
     public <T> TFuture<T> submit(Runnable task) {
        return submit(task, null);
    }

    public <T> TFuture<T> submit(Callable<T> task) {
        return schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    protected void afterExecute(Runnable r, Throwable t) {
    }

    public  List<Runnable> shutdownNow() {
        running = false;
        return new ArrayList<Runnable>();
    }
    
    public void shutdown() {
        running = false;
        this.thread.close();
    }
}
