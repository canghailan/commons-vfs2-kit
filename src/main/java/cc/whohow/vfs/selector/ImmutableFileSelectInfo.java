package cc.whohow.vfs.selector;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;

import java.util.Objects;

public class ImmutableFileSelectInfo<T extends FileObject> implements FileSelectInfo {
    private final T baseFolder;
    private final T file;
    private final int depth;

    public ImmutableFileSelectInfo(T baseFolder, T file, int depth) {
        this.baseFolder = baseFolder;
        this.file = file;
        this.depth = depth;
    }

    @Override
    public T getBaseFolder() {
        return baseFolder;
    }

    @Override
    public T getFile() {
        return file;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableFileSelectInfo)) return false;
        ImmutableFileSelectInfo<?> that = (ImmutableFileSelectInfo<?>) o;
        return depth == that.depth &&
                Objects.equals(baseFolder, that.baseFolder) &&
                Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {

        return Objects.hash(baseFolder, file, depth);
    }
}