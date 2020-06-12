package cc.whohow.fs.provider.memory;

import cc.whohow.fs.File;
import cc.whohow.fs.path.KeyPath;

public class MemoryFile implements File<KeyPath, MemoryFile> {
    protected final MemoryFileSystem fileSystem;
    protected final KeyPath path;

    public MemoryFile(MemoryFileSystem fileSystem, KeyPath path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public MemoryFileSystem getFileSystem() {
        return fileSystem;
    }

    public KeyPath getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
