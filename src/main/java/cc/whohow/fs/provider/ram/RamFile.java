package cc.whohow.fs.provider.ram;

import cc.whohow.fs.File;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.path.KeyPath;
import cc.whohow.fs.util.Files;

public class RamFile implements File<KeyPath, RamFile> {
    protected final RamFileSystem fileSystem;
    protected final KeyPath path;

    public RamFile(RamFileSystem fileSystem, KeyPath path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public RamFileSystem getFileSystem() {
        return fileSystem;
    }

    public KeyPath getPath() {
        return path;
    }

    @Override
    public FileAttributes readAttributes() {
        return Files.emptyFileAttributes();
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
