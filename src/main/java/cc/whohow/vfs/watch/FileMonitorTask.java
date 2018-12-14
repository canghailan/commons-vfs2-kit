package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileMonitorTask implements Runnable {
    private final FileChangeEventProducer watcher;
    private final List<FileMonitorListener> listeners = new CopyOnWriteArrayList<>();

    public FileMonitorTask(FileChangeEventProducer watcher) {
        this.watcher = watcher;
    }

    public List<FileMonitorListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void addListener(FileMonitorListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(FileMonitorListener listener) {
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
}
