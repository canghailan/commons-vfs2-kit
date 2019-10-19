package cc.whohow.vfs.watch;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.version.FileLastModifiedTimeVersionProvider;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionProvider;
import org.apache.commons.vfs2.FileName;

import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PollingFileWatchKey<T> implements FileWatchKey {
    private final PollingFileWatchable<T> watchable;
    private volatile Map<FileName, FileVersion<T>> versions;

    public PollingFileWatchKey(FileObjectX fileObject, FileVersionProvider<T> fileVersionProvider) {
        this(new PollingFileWatchable<>(fileObject, fileVersionProvider));
    }

    public PollingFileWatchKey(PollingFileWatchable<T> watchable) {
        this.watchable = watchable;
    }

    public static PollingFileWatchKey<Long> lastModified(FileObjectX fileObject) {
        return new PollingFileWatchKey<>(fileObject, new FileLastModifiedTimeVersionProvider());
    }

    @Override
    public Iterable<FileWatchEvent> get() {
        List<FileWatchEvent> list = new ArrayList<>();
        pollEvents(list);
        return list;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public List<WatchEvent<?>> pollEvents() {
        List<WatchEvent<?>> list = new ArrayList<>();
        pollEvents(list);
        return list;
    }

    private synchronized void pollEvents(List<? super FileWatchEvent> list) {
        try {
            Map<FileName, FileVersion<T>> oldVersions = versions;
            Map<FileName, FileVersion<T>> newVersions = watchable.get();
            versions = newVersions;

            if (oldVersions == null) {
                // first run
                return;
            }

            for (Map.Entry<FileName, FileVersion<T>> e : newVersions.entrySet()) {
                FileVersion<T> newVersion = e.getValue();
                FileVersion<T> oldVersion = oldVersions.remove(e.getKey());
                if (oldVersion == null) {
                    list.add(new FileWatchEvent.Create(newVersion.getFileObject()));
                } else if (!Objects.equals(oldVersion.getVersion(), newVersion.getVersion())) {
                    list.add(new FileWatchEvent.Modify(newVersion.getFileObject()));
                }
            }
            for (FileVersion<?> oldVersion : oldVersions.values()) {
                list.add(new FileWatchEvent.Delete(oldVersion.getFileObject()));
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public synchronized void cancel() {
        versions = null;
    }

    @Override
    public PollingFileWatchable<T> watchable() {
        return watchable;
    }

    @Override
    public String toString() {
        return watchable.toString();
    }
}
