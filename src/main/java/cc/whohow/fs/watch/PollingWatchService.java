package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.FileWatchService;
import cc.whohow.fs.Path;
import cc.whohow.fs.util.ScheduledFutureTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class PollingWatchService<P extends Path, F extends File<P, F>, V> implements FileWatchService<P, F> {
    private static final Logger log = LogManager.getLogger(PollingWatchService.class);
    protected final ScheduledExecutorService scheduler;
    protected final Duration interval;
    protected final Function<F, V> diffKey;
    protected final Map<Path, ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>>> tasks = new ConcurrentSkipListMap<>();

    public PollingWatchService(ScheduledExecutorService scheduler, Duration interval, Function<F, V> diffKey) {
        this.scheduler = scheduler;
        this.interval = interval;
        this.diffKey = diffKey;
    }

    @Override
    public synchronized void watch(F file, Consumer<FileWatchEvent<P, F>> listener) {
        ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>> scheduledTask = getScheduledTask(file);
        if (scheduledTask == null) {
            AggregatedPollingFileWatchKey<P, F, V> watchKey =
                    new AggregatedPollingFileWatchKey<>(diffKey, file);
            watchKey.addListener(file, listener);

            schedule(watchKey);
        } else {
            AggregatedPollingFileWatchKey<P, F, V> watchKey = scheduledTask.getTask();
            P oldKey = watchKey.getRoot().getPath();
            watchKey.addListener(file, listener);
            P newKey = watchKey.getRoot().getPath();
            if (!oldKey.equals(newKey)) {
                log.debug("adjust task key: {} -> {}", oldKey, newKey);
                tasks.remove(oldKey);
                tasks.put(newKey, scheduledTask);
            }
        }
    }

    @Override
    public synchronized void unwatch(F file, Consumer<FileWatchEvent<P, F>> listener) {
        ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>> scheduledTask = getScheduledTask(file);
        if (scheduledTask == null) {
            throw new IllegalStateException();
        }
        AggregatedPollingFileWatchKey<P, F, V> watchKey = scheduledTask.getTask();
        P oldKey = watchKey.getRoot().getPath();
        watchKey.removeListener(file, listener);
        P newKey = watchKey.getRoot().getPath();
        if (!watchKey.isValid()) {
            cancel(scheduledTask);
        } else if (!oldKey.equals(newKey)) {
            log.debug("adjust task key: {} -> {}", oldKey, newKey);
            tasks.remove(oldKey);
            tasks.put(newKey, scheduledTask);
        }
    }

    protected synchronized void schedule(AggregatedPollingFileWatchKey<P, F, V> watchKey) {
        log.debug("schedule: {}", watchKey);
        ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>> scheduledTask =
                new ScheduledFutureTask<>(watchKey,
                        scheduler.scheduleWithFixedDelay(
                                watchKey, 0, interval.toMillis(), TimeUnit.MILLISECONDS));
        tasks.put(scheduledTask.getTask().getRoot().getPath(), scheduledTask);
    }

    protected synchronized void cancel(ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>> scheduledTask) {
        log.debug("cancel schedule: {}", scheduledTask.getTask());
        try {
            scheduledTask.getScheduledFuture().cancel(true);
        } finally {
            tasks.remove(scheduledTask.getTask().getRoot().getPath(), scheduledTask);
        }
    }

    protected synchronized ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>> getScheduledTask(F file) {
        for (Map.Entry<Path, ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>>> e : tasks.entrySet()) {
            if (file.getPath().startsWith(e.getKey()) ||
                    e.getKey().startsWith(file.getPath())) {
                return e.getValue();
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        log.trace("close PollingWatchService: {}", this);
        for (ScheduledFutureTask<AggregatedPollingFileWatchKey<P, F, V>> task : tasks.values()) {
            try {
                cancel(task);
            } catch (Exception e) {
                log.debug("close error", e);
            }
        }
    }

    @Override
    public String toString() {
        return tasks.keySet().toString();
    }
}
