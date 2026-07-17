package igentuman.nc.block.bomb.sim;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BombSimulationExecutor {

    private static ThreadPoolExecutor executor;
    private static final int MAX_QUEUE_SIZE = 32;
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "NCN-Bomb-Sim-" + threadNumber.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setDaemon(true);
            return thread;
        }
    };

    private BombSimulationExecutor() {}

    public static synchronized ExecutorService getExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
            executor = new ThreadPoolExecutor(
                    1, 1,
                    600L, TimeUnit.SECONDS,
                    workQueue,
                    THREAD_FACTORY,
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            executor.prestartCoreThread();
        }
        return executor;
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
