package cc.whohow.fs.watch;

import cc.whohow.fs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PollingWatchService<P extends Path, F extends GenericFile<P, F>, V> implements FileWatchService<P, F> {
    private static final Logger log = LogManager.getLogger(PollingWatchService.class);
    protected final List<PollingFileWatchKey<P, F>> watchKeys = new CopyOnWriteArrayList<>();
    protected final ScheduledFuture<?> scheduled;

    public PollingWatchService(ScheduledExecutorService scheduler, Duration interval, Function<F, V> compareKey) {
        this.scheduled = scheduler.scheduleWithFixedDelay(
                new PollingWatchTask<>(compareKey, watchKeys, this::onWatchEvent),
                0, interval.toMillis(), TimeUnit.MILLISECONDS
        );
    }

    protected void onWatchEvent(FileEvent.Kind kind, P path) {
        for (PollingFileWatchKey<P, F> watchKey : watchKeys) {
            if (path.startsWith(watchKey.watchable().getPath())) {
                F file = watchKey.watchable().getFileSystem().get(path);
                watchKey.accept(new ImmutableFileEvent(kind, file));
            }
        }
    }

    protected PollingFileWatchKey<P, F> getWatchKey(Path path) {
        for (PollingFileWatchKey<P, F> watchKey : watchKeys) {
            if (watchKey.watchable().getPath().equals(path)) {
                return watchKey;
            }
        }
        return null;
    }

    @Override
    public synchronized void watch(F file, FileListener listener) {
        PollingFileWatchKey<P, F> watchKey = getWatchKey(file.getPath());
        if (watchKey == null) {
            watchKey = new PollingFileWatchKey<>(file);
            watchKeys.add(watchKey);
        }
        watchKey.addListener(listener);
    }

    @Override
    public synchronized void unwatch(F file, FileListener listener) {
        PollingFileWatchKey<P, F> watchKey = getWatchKey(file.getPath());
        if (watchKey == null) {
            throw new IllegalStateException();
        }
        watchKey.removeListener(listener);
        if (!watchKey.isValid()) {
            watchKeys.remove(watchKey);
        }
    }

    @Override
    public void close() throws Exception {
        log.trace("close PollingWatchService: {}", this);
        try {
            log.trace("cancel scheduled: {}", scheduled);
            scheduled.cancel(true);
        } catch (Exception e) {
            log.warn("cancel scheduled error", e);
        }
    }

    @Override
    public String toString() {
        return watchKeys.stream()
                .map(PollingFileWatchKey::toString)
                .collect(Collectors.joining("\n"));
    }
}
