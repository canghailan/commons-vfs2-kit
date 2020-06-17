package cc.whohow.fs.provider.file;

import cc.whohow.fs.File;

public class LocalFile implements File<LocalPath, LocalFile> {
    private final LocalFileSystem fileSystem;
    private final LocalPath path;

    public LocalFile(LocalFileSystem fileSystem, LocalPath path) {
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

    @Override
    public String toString() {
        return getPath().toString();
    }

    public void createDirectories() {
        getFileSystem().createDirectories(getPath());
    }
}
