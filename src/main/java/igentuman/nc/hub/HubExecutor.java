package igentuman.nc.hub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class HubExecutor {

    private static ExecutorService executor;
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);

    private HubExecutor() {}

    public static synchronized ExecutorService get() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(1, 2, 30, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(32), r -> {
                Thread t = new Thread(r, "nc-hub-worker-" + THREAD_NUMBER.getAndIncrement());
                t.setDaemon(true);
                return t;
            });
        }
        return executor;
    }
}
