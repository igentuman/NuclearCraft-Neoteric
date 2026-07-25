package igentuman.nc.multiblock;

import igentuman.nc.NuclearCraft;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class MultiblockExecutorManager {

    private static final int MAX_THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    private static final int CORE_THREAD_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    private static final int MAX_QUEUE_SIZE = 1000;

    private static ThreadPoolExecutor executor;

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "NC-Multiblocks-" + threadNumber.getAndIncrement());
            thread.setPriority(Thread.MAX_PRIORITY - 1);
            thread.setDaemon(true);
            return thread;
        }
    };

    private MultiblockExecutorManager() {}

    public static synchronized ExecutorService getExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
            executor = new ThreadPoolExecutor(
                    CORE_THREAD_COUNT,
                    MAX_THREAD_COUNT,
                    600L,
                    TimeUnit.SECONDS,
                    workQueue,
                    THREAD_FACTORY,
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            executor.prestartAllCoreThreads();
            executor.setRejectedExecutionHandler((r, e) -> {
                if (!e.isShutdown()) {
                    r.run();
                }
            });
        }
        return executor;
    }

    public static int getQueueSize() {
        return executor != null ? executor.getQueue().size() : 0;
    }

    public static int getActiveThreadCount() {
        return executor != null ? executor.getActiveCount() : 0;
    }

    public static synchronized void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
