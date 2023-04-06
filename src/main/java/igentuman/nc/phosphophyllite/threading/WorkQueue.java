package igentuman.nc.phosphophyllite.threading;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
public class WorkQueue {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    
    private final ArrayList<DequeueThread> dequeueThreads = new ArrayList<>();
    
    private RuntimeException toRethrow;
    
    public WorkQueue() {
        // workaround for FML issue
        // triggers the classloading here instead of the finalizer
        dequeueThreads.forEach(DequeueThread::finish);
    }
    
    private class DequeueThread implements Runnable {
        private final WeakReference<BlockingQueue<Runnable>> queue;
        private final AtomicBoolean stop = new AtomicBoolean(false);
        
        public DequeueThread(BlockingQueue<Runnable> queue, String name) {
            this.queue = new WeakReference<>(queue);
            Thread thread = new Thread(this);
            if (name != null) {
                thread.setName(name);
            }
            thread.setDaemon(true); // just, because, shouldn't be necessary, but just because
            thread.start();
        }
        
        public void run() {
            while (!stop.get()) {
                BlockingQueue<Runnable> queue = this.queue.get();
                if (queue == null) {
                    return;
                }
                Runnable nextItem;
                try {
                    nextItem = queue.take();
                } catch (InterruptedException e) {
                    return;
                }
                try {
                    nextItem.run();
                } catch (RuntimeException e) {
                    WorkQueue.this.toRethrow = e;
                    e.printStackTrace();
                } catch (Throwable e) {
                    // this should be impossible, but just in case
                    e.printStackTrace();
                    Minecraft.crash(new CrashReport("Exception rolled back to Phosphophyllite WorkQueue", e));
                }
            }
            
        }
        
        public void finish() {
            stop.set(true);
        }
    }
    
    public WorkQueue addProcessingThread() {
        return addProcessingThreads(1, null);
    }
    
    public WorkQueue addProcessingThreads(int threads) {
        return addProcessingThreads(threads, null);
    }
    
    public WorkQueue addProcessingThreads(int threads, String name) {
        for (int i = 0; i < threads; i++) {
            dequeueThreads.add(new DequeueThread(queue, name == null ? null : name + i));
        }
        return this;
    }
    
    public void finish() {
        dequeueThreads.forEach(DequeueThread::finish);
        synchronized (queue) {
            queue.notifyAll();
        }
    }
    
    @Override
    protected void finalize() {
        finish();
    }
    
    private static class WorkItem implements Runnable {
        final Event waitEvent = new Event();
        final Event readyEvent = new Event();
        final Runnable work;
        final AtomicLong unTriggeredWaitEvents = new AtomicLong(Long.MAX_VALUE);
        
        WorkItem(final Queue<Runnable> queue, final Runnable work, final Event[] waitEvents) {
            this.work = work;
            if (waitEvents.length == 0) {
                queue.add(this);
                return;
            }
            unTriggeredWaitEvents.set(waitEvents.length);
            for (Event event : waitEvents) {
                if (event == null) {
                    if (unTriggeredWaitEvents.decrementAndGet() == 0) {
                        readyEvent.trigger();
                    }
                }
                assert event != null;
                event.registerCallback(() -> {
                    if (unTriggeredWaitEvents.decrementAndGet() == 0) {
                        synchronized (readyEvent) {
                            readyEvent.trigger();
                        }
                    }
                });
            }
            readyEvent.registerCallback(() -> {
                queue.add(this);
            });
        }
        
        @Override
        public void run() {
            try {
                work.run();
            } finally {
                waitEvent.trigger();
            }
        }
    }
    
    public Event enqueue(Runnable runnable, Event... events) {
        if (toRethrow != null) {
            throw toRethrow;
        }
        WorkItem item = new WorkItem(queue, runnable, events);
        return item.waitEvent;
    }
    
    public void enqueueUntracked(Runnable runnable) {
        queue.add(runnable);
    }
    
    public boolean runOne() {
        if (!dequeueThreads.isEmpty()) {
            return false;
        }
        Runnable toRun = queue.poll();
        if (toRun != null) {
            toRun.run();
            return true;
        }
        return false;
    }
    
    public boolean runAll() {
        if (!dequeueThreads.isEmpty()) {
            return false;
        }
        Runnable toRun;
        boolean ranSomething = false;
        while ((toRun = queue.poll()) != null) {
            toRun.run();
            ranSomething = true;
        }
        return ranSomething;
    }
}
