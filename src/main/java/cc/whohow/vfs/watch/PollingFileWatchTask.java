package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PollingFileWatchTask implements Runnable {
    private final PollingFileWatchKey<?> watchKey;
    private final List<FileWatchListener> listeners = new CopyOnWriteArrayList<>();

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
        for (FileWatchEvent e : watchKey.call()) {
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
}
