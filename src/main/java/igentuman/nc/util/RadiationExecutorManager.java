package igentuman.nc.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.NuclearCraft.debugLog;

public class RadiationExecutorManager {
    private static ThreadPoolExecutor executor;
    private static final int MAX_THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    private static final int CORE_THREAD_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    private static final int MAX_QUEUE_SIZE = 1000;
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "NCN-Radiation-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };

    public static synchronized ExecutorService getExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
            
            executor = new ThreadPoolExecutor(
                CORE_THREAD_COUNT,
                MAX_THREAD_COUNT,
                60L,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
            executor.prestartAllCoreThreads();
            // Add monitoring for large queue sizes
            executor.setRejectedExecutionHandler((r, e) -> {
                debugLog("Warning: Radiation task queue is full! Running task in main thread.");
                if (!e.isShutdown()) {
                    r.run();
                }
            });
        }
        return executor;
    }
    
    /**
     * Returns the current size of the task queue
     * Can be used to monitor performance
     */
    public static int getQueueSize() {
        return executor != null ? executor.getQueue().size() : 0;
    }
    
    /**
     * Returns the current number of active threads
     * Can be used to monitor performance
     */
    public static int getActiveThreadCount() {
        return executor != null ? executor.getActiveCount() : 0;
    }

    public static synchronized void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                // Wait a bit for tasks to complete
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // Force shutdown if tasks don't complete in time
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}