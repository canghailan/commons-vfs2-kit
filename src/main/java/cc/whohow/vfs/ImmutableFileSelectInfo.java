package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;

import java.util.Objects;

public class ImmutableFileSelectInfo implements FileSelectInfo {
    private final FileObject baseFolder;
    private final FileObject file;
    private final int depth;

    public ImmutableFileSelectInfo(FileObject baseFolder, FileObject file, int depth) {
        this.baseFolder = baseFolder;
        this.file = file;
        this.depth = depth;
    }

    public ImmutableFileSelectInfo(FileSelectInfo parent, FileObject file) {
        this.baseFolder = parent.getBaseFolder();
        this.file = file;
        this.depth = parent.getDepth() + 1;
    }

    @Override
    public FileObject getBaseFolder() {
        return baseFolder;
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ImmutableFileSelectInfo) {
            ImmutableFileSelectInfo that = (ImmutableFileSelectInfo) o;
            return depth == that.depth &&
                    Objects.equals(file, that.file) &&
                    Objects.equals(baseFolder, that.baseFolder);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseFolder, file, depth);
    }

    @Override
    public String toString() {
        return file.toString();
    }
}