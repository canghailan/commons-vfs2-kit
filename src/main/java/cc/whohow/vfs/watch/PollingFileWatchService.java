package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;

import java.io.IOException;
import java.nio.file.WatchKey;
import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PollingFileWatchService implements FileWatchService<PollingFileWatchable<?>> {
    private final NavigableMap<FileName, PollingFileWatchTask> tasks = new ConcurrentSkipListMap<>();
    private final ScheduledExecutorService executor;
    private final Duration delay;

    public PollingFileWatchService(ScheduledExecutorService executor) {
        this(executor, Duration.ofSeconds(1));
    }

    public PollingFileWatchService(ScheduledExecutorService executor, Duration delay) {
        this.executor = executor;
        this.delay = delay;
    }

    public synchronized PollingFileWatchTask getTask(FileName fileName) {
        PollingFileWatchTask task = tasks.get(fileName);
        if (task != null) {
            return task;
        }
        for (Map.Entry<FileName, PollingFileWatchTask> e : tasks.tailMap(fileName).entrySet()) {
            if (e.getKey().isDescendent(fileName)) {
                return e.getValue();
            }
        }
        return null;
    }

    public synchronized PollingFileWatchTask getOrScheduledTask(PollingFileWatchable<?> watchable) {
        FileName fileName = watchable.getFileObject().getName();
        PollingFileWatchTask scheduledTask = getTask(fileName);
        if (scheduledTask != null) {
            return scheduledTask;
        }
        PollingFileWatchTask task = new PollingFileWatchTask(new PollingFileWatchKey<>(watchable));
        task.scheduled(executor.scheduleWithFixedDelay(task, 0L, delay.toMillis(), TimeUnit.MILLISECONDS));
        tasks.put(fileName, task);
        return task;
    }

    public synchronized void removeListener(FileWatchListener listener) {
        PollingFileWatchTask task = getTask(listener.getFileName());
        if (task == null) {
            return;
        }
        task.removeListener(listener);
        if (task.isDone()) {
            task.cancel(true);
        }
    }

    @Override
    public synchronized void addListener(PollingFileWatchable<?> watchable, FileListener listener) {
        getOrScheduledTask(watchable).addListener(new FileWatchListener(
                watchable.getFileObject().getName(), listener));
    }

    @Override
    public synchronized void removeListener(PollingFileWatchable<?> watchable, FileListener listener) {
        removeListener(new FileWatchListener(
                watchable.getFileObject().getName(), listener));
    }

    @Override
    public String toString() {
        return tasks.values().stream()
                .map(PollingFileWatchTask::toString)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public WatchKey poll() {
        for (PollingFileWatchTask task : tasks.values()) {
            if (task.poll()) {
                return task.getWatchKey();
            }
        }
        return null;
    }

    @Override
    public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        long millis = unit.toMillis(timeout);
        long t = System.currentTimeMillis() + millis;
        long ts = delay.toMillis();
        if (ts > millis) {
            ts = millis;
        }
        if (ts < millis / 10) {
            ts = millis / 10;
        }
        do {
            WatchKey watchKey = poll();
            if (watchKey != null) {
                return watchKey;
            }
            Thread.sleep(ts);
        } while (System.currentTimeMillis() < t);
        return null;
    }

    @Override
    public WatchKey take() throws InterruptedException {
        while (true) {
            WatchKey watchKey = poll();
            if (watchKey != null) {
                return watchKey;
            }
        }
    }
}
