package cc.whohow.fs.watch;

import cc.whohow.fs.FileEvent;
import cc.whohow.fs.FileListener;
import cc.whohow.fs.GenericFile;
import cc.whohow.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class PollingFileWatchKey<P extends Path, F extends GenericFile<P, F>> implements Consumer<FileEvent> {
    private static final Logger log = LogManager.getLogger(PollingFileWatchKey.class);
    protected final F watchable;
    protected final Collection<FileListener> listeners = new CopyOnWriteArraySet<>();

    public PollingFileWatchKey(F watchable) {
        this.watchable = watchable;
    }

    public F watchable() {
        return watchable;
    }

    public boolean isValid() {
        return !listeners.isEmpty();
    }

    public void addListener(FileListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FileListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void accept(FileEvent event) {
        for (FileListener listener : listeners) {
            try {
                listener.handleEvent(event);
            } catch (Exception e) {
                log.warn("handle FileEvent error", e);
            }
        }
    }

    @Override
    public String toString() {
        return watchable.toString();
    }
}
