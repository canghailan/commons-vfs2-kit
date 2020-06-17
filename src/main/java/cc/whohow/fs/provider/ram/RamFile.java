package cc.whohow.fs.provider.ram;

import cc.whohow.fs.File;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.provider.KeyPath;
import cc.whohow.fs.util.Files;

import java.util.Objects;

public class RamFile implements File<KeyPath, RamFile> {
    protected final RamFileSystem fileSystem;
    protected final KeyPath path;

    public RamFile(RamFileSystem fileSystem, KeyPath path) {
        Objects.requireNonNull(fileSystem);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RamFile) {
            RamFile that = (RamFile) o;
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
