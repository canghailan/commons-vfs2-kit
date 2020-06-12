package cc.whohow.fs.provider.file;

import cc.whohow.fs.Path;

import java.net.URI;

public class LocalPath implements Path {
    private final java.nio.file.Path path;

    public LocalPath(java.nio.file.Path path) {
        this.path = path;
    }

    public java.nio.file.Path getPath() {
        return path;
    }

    @Override
    public URI toUri() {
        return path.toUri();
    }

    @Override
    public String toString() {
        return toUri().toString();
    }
}
