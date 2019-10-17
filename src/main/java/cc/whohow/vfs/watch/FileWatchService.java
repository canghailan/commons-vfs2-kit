package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;

import java.nio.file.WatchService;

public interface FileWatchService<T extends FileWatchable> extends WatchService {
    void addListener(T watchable, FileListener listener);

    void removeListener(T watchable, FileListener listener);
}
