package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;

public class DefaultFileWatchEvent<P extends Path, F extends File<P, F>> implements FileWatchEvent<P, F> {
    protected final Kind kind;
    protected final File<P, F> watchable;
    protected final File<P, F> file;

    public DefaultFileWatchEvent(Kind kind, File<P, F> watchable, File<P, F> file) {
        this.kind = kind;
        this.watchable = watchable;
        this.file = file;
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public File<P, F> watchable() {
        return watchable;
    }

    @Override
    public File<P, F> file() {
        return file;
    }
}
