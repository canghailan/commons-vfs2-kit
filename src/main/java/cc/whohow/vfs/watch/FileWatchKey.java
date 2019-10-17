package cc.whohow.vfs.watch;

import java.nio.file.WatchKey;
import java.util.function.Supplier;

public interface FileWatchKey extends WatchKey, Supplier<Iterable<FileWatchEvent>> {
    @Override
    FileWatchable watchable();
}
