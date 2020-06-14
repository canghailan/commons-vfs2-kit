package cc.whohow.fs.provider.file;

import cc.whohow.fs.Path;

import java.net.URI;
import java.nio.file.Paths;

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
    public LocalPath getParent() {
        java.nio.file.Path parent = path.getParent();
        if (parent == null) {
            return null;
        }
        return new LocalPath(parent);
    }

    @Override
    public LocalPath resolve(String relative) {
        return new LocalPath(Paths.get(path.toUri().resolve(relative)));
    }

    @Override
    public String toString() {
        return toUri().toString();
    }
}
