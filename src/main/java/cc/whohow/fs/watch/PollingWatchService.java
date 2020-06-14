package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;
import cc.whohow.fs.util.ScheduledFutureTask;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public class PollingWatchService<P extends Path, F extends File<P, F>, V> {
    private final ScheduledExecutorService scheduledExecutor;
    private final Function<F, V> compareKey;
    private final Map<Path, ScheduledFutureTask<PollingWatchTask<P, F, V>>> tasks = new ConcurrentSkipListMap<>();

    public PollingWatchService(ScheduledExecutorService scheduledExecutor, Function<F, V> compareKey) {
        this.scheduledExecutor = scheduledExecutor;
        this.compareKey = compareKey;
    }

    public void watch(F file, Consumer<FileWatchEvent<P, F>> listener) {

    }

    public void unwatch(F file, Consumer<FileWatchEvent<P, F>> listener) {

    }
}
