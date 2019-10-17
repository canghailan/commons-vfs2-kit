package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PollingFileWatchTask implements RunnableFuture<Void> {
    private final PollingFileWatchKey<?> watchKey;
    private final AtomicBoolean polled = new AtomicBoolean(false);
    private final List<FileWatchListener> listeners = new CopyOnWriteArrayList<>();
    private volatile ScheduledFuture<?> future;

    public PollingFileWatchTask(PollingFileWatchKey<?> watchKey) {
        this.watchKey = watchKey;
    }

    public PollingFileWatchKey getWatchKey() {
        return watchKey;
    }

    public List<FileWatchListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void addListener(FileWatchListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(FileWatchListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void run() {
        if (listeners.isEmpty()) {
            return;
        }
        for (FileWatchEvent e : watchKey.get()) {
            for (FileListener listener : listeners) {
                try {
                    e.notify(listener);
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public String toString() {
        return watchKey.toString();
    }

    public void scheduled(ScheduledFuture<?> future) {
        if (this.future != null) {
            throw new IllegalStateException();
        }
        this.future = future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (this.future == null) {
            throw new IllegalStateException();
        }
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        if (this.future == null) {
            throw new IllegalStateException();
        }
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return listeners.isEmpty();
    }

    @Override
    public Void get() {
        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) {
        return null;
    }

    public boolean poll() {
        return polled.compareAndSet(false, true);
    }

    public boolean isPolled() {
        return polled.get();
    }
}
