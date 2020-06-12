package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class PollingWatchTask<P extends Path, F extends File<P, F>, V> implements Runnable {
    private final File<P, F> watchable;
    private final Function<F, V> version;
    private volatile Map<P, V> snapshot;
    private List<PollingFileWatchKey<P, F>> watchKeys;

    public PollingWatchTask(File<P, F> watchable, Function<F, V> version) {
        this.watchable = watchable;
        this.version = version;
    }

    @Override
    public void run() {
        Map<P, V> oldSnapshot = snapshot;
        Map<P, V> newSnapshot = takeSnapshot();

        if (oldSnapshot == null) {
            return;
        }

        for (Map.Entry<P, V> e : newSnapshot.entrySet()) {
            V newValue = e.getValue();
            V oldValue = oldSnapshot.remove(e.getKey());
            if (oldValue == null) {
                notify(new DefaultFileWatchEvent<>(FileWatchEvent.Kind.CREATE, watchable, watchable.getFileSystem().get(e.getKey())));
            } else if (!Objects.equals(newValue, oldValue)) {
                notify(new DefaultFileWatchEvent<>(FileWatchEvent.Kind.MODIFY, watchable, watchable.getFileSystem().get(e.getKey())));
            }
        }
        for (Map.Entry<P, V> e : oldSnapshot.entrySet()) {
            notify(new DefaultFileWatchEvent<>(FileWatchEvent.Kind.DELETE, watchable, watchable.getFileSystem().get(e.getKey())));
        }
    }

    protected void notify(FileWatchEvent<P, F> event) {
        for (PollingFileWatchKey<P, F> watchKey : watchKeys) {
            watchKey.accept(event);
        }
    }

    protected Map<P, V> takeSnapshot() {
        try (FileStream<F> stream = watchable.tree()) {
            Map<P, V> snapshot = new HashMap<>();
            for (F file : stream) {
                if (file.isRegularFile()) {
                    snapshot.put(file.getPath(), version.apply(file));
                }
            }
            return snapshot;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
