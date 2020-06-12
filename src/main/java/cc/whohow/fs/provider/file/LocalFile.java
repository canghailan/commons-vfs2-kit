package cc.whohow.fs.provider.file;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;

public class LocalFile implements File<LocalPath, LocalFile> {
    private LocalFileSystem fileSystem;
    private LocalPath path;

    public LocalFile(LocalFileSystem fileSystem, LocalPath path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public FileSystem<LocalPath, LocalFile> getFileSystem() {
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
}
