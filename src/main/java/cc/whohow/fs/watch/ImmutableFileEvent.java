package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileEvent;

import java.util.Objects;

public class ImmutableFileEvent implements FileEvent {
    protected final Kind kind;
    protected final File file;

    public ImmutableFileEvent(Kind kind, File file) {
        this.kind = kind;
        this.file = file;
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ImmutableFileEvent) {
            ImmutableFileEvent that = (ImmutableFileEvent) o;
            return kind == that.kind &&
                    file.equals(that.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, file);
    }

    @Override
    public String toString() {
        return kind + " " + file;
    }
}
