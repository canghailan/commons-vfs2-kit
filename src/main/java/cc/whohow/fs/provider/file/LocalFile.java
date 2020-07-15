package cc.whohow.fs.provider.file;

import cc.whohow.fs.GenericFile;

import java.util.Objects;

public class LocalFile implements GenericFile<LocalPath, LocalFile> {
    private final LocalFileSystem fileSystem;
    private final LocalPath path;

    public LocalFile(LocalFileSystem fileSystem, LocalPath path) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(path);
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public LocalFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public LocalPath getPath() {
        return path;
    }

    public void createDirectories() {
        getFileSystem().createDirectories(getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof LocalFile) {
            LocalFile that = (LocalFile) o;
            return fileSystem.equals(that.fileSystem) &&
                    path.equals(that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, path);
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
