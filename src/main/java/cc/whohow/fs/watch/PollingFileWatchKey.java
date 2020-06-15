package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class PollingFileWatchKey<P extends Path, F extends File<P, F>> implements Consumer<FileWatchEvent<P, F>> {
    private static final Logger log = LogManager.getLogger(PollingFileWatchKey.class);
    protected final F watchable;
    protected final Collection<Consumer<? super FileWatchEvent<P, F>>> listeners = new CopyOnWriteArraySet<>();

    public PollingFileWatchKey(F watchable) {
        this.watchable = watchable;
    }

    public F watchable() {
        return watchable;
    }

    public boolean isValid() {
        return !listeners.isEmpty();
    }

    public void addListener(Consumer<? super FileWatchEvent<P, F>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<? super FileWatchEvent<P, F>> listener) {
        listeners.remove(listener);
    }

    @Override
    public void accept(FileWatchEvent<P, F> event) {
        for (Consumer<? super FileWatchEvent<P, F>> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.trace("process WatchEvent error", e);
            }
        }
    }

    @Override
    public String toString() {
        return watchable.toString();
    }
}
