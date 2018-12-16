package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileWatchTask implements Runnable {
    private final FileWatcher watcher;
    private final List<FileWatchListener> listeners = new CopyOnWriteArrayList<>();

    public FileWatchTask(FileWatcher watcher) {
        this.watcher = watcher;
    }

    public FileWatcher getWatcher() {
        return watcher;
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
        for (AbstractFileChangeEvent e : watcher.call()) {
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
        return watcher.toString();
    }
}
