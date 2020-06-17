package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;

import java.util.Objects;

public class ImmutableFileWatchEvent<P extends Path, F extends File<P, F>> implements FileWatchEvent<P, F> {
    protected final Kind kind;
    protected final File<P, F> watchable;
    protected final File<P, F> file;

    public ImmutableFileWatchEvent(Kind kind, File<P, F> watchable, File<P, F> file) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ImmutableFileWatchEvent) {
            ImmutableFileWatchEvent<?, ?> that = (ImmutableFileWatchEvent<?, ?>) o;
            return kind == that.kind &&
                    watchable.equals(that.watchable) &&
                    file.equals(that.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, watchable, file);
    }

    @Override
    public String toString() {
        return kind + " " + file;
    }
}
