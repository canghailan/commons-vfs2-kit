package cc.whohow.fs;

import java.util.function.Consumer;

public interface FileWatchService<P extends Path, F extends File<P, F>> extends AutoCloseable {
    void watch(F file, Consumer<FileWatchEvent<P, F>> listener);

    void unwatch(F file, Consumer<FileWatchEvent<P, F>> listener);
}
