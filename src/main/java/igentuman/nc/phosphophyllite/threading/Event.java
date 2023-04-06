package igentuman.nc.phosphophyllite.threading;

import java.util.ArrayList;

public class Event {
    private final ArrayList<Runnable> callbacks = new ArrayList<>();
    private volatile boolean wasTriggered = false;
    
    public boolean ready() {
        return wasTriggered;
    }
    
    @SuppressWarnings("unused")
    public void join() {
        if (wasTriggered) {
            return;
        }
        synchronized (this) {
            if (wasTriggered) {
                return;
            }
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
    }
    
    @SuppressWarnings("unused")
    public boolean join(int timeout) {
        if (wasTriggered) {
            return true;
        }
        synchronized (this) {
            if (wasTriggered) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ignored) {
            }
        }
        
        return wasTriggered;
    }
    
    public synchronized void trigger() {
        if (wasTriggered) {
            return;
        }
        callbacks.forEach(Runnable::run);
        wasTriggered = true;
        notifyAll();
    }
    
    public synchronized void registerCallback(Runnable runnable) {
        if (wasTriggered) {
            runnable.run();
            return;
        }
        callbacks.add(runnable);
    }
    
    @Override
    protected void finalize() {
        trigger();
    }
}
