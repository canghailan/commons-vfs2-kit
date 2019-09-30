package cc.whohow.vfs.watch;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.version.FileLastModifiedTimeVersionProvider;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionProvider;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.events.AbstractFileChangeEvent;
import org.apache.commons.vfs2.events.ChangedEvent;
import org.apache.commons.vfs2.events.CreateEvent;
import org.apache.commons.vfs2.events.DeleteEvent;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileWatcher implements Callable<Iterable<? extends AbstractFileChangeEvent>> {
    private final Lock lock = new ReentrantLock();
    private final FileObject watchable;
    private final FileVersionProvider<?> fileVersionProvider;
    private volatile Map<FileName, FileVersion<?>> fileVersions;

    public FileWatcher(FileObject watchable) {
        this(watchable, new FileLastModifiedTimeVersionProvider());
    }

    public FileWatcher(FileObject watchable, FileVersionProvider<?> fileVersionProvider) {
        this.watchable = watchable;
        this.fileVersionProvider = fileVersionProvider;
    }

    public FileObject getWatchable() {
        return watchable;
    }

    @Override
    public Iterable<? extends AbstractFileChangeEvent> call() {
        lock.lock();
        try {
            return pollEvents();
        } finally {
            lock.unlock();
        }
    }

    private Iterable<? extends AbstractFileChangeEvent> pollEvents() {
        try (Stream<? extends FileVersion<?>> stream = fileVersionProvider.getVersions(watchable)) {
            Map<FileName, FileVersion<?>> oldFileVersions = fileVersions;
            Map<FileName, FileVersion<?>> newFileVersions = stream
                    .collect(Collectors.toMap(self -> self.getFileObject().getName(), Function.identity()));
            fileVersions = newFileVersions;

            if (oldFileVersions == null) {
                // first run
                return Collections.emptyList();
            }

            List<AbstractFileChangeEvent> list = new ArrayList<>();
            for (Map.Entry<FileName, FileVersion<?>> e : newFileVersions.entrySet()) {
                FileVersion<?> newVersion = e.getValue();
                FileVersion<?> oldVersion = oldFileVersions.remove(e.getKey());
                if (oldVersion == null) {
                    list.add(new CreateEvent(newVersion.getFileObject()));
                } else if (!Objects.equals(oldVersion.getVersion(), newVersion.getVersion())) {
                    list.add(new ChangedEvent(newVersion.getFileObject()));
                }
            }
            for (FileVersion<?> oldVersion : oldFileVersions.values()) {
                list.add(new DeleteEvent(oldVersion.getFileObject()));
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        return watchable.toString();
    }
}
