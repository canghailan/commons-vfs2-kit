package cc.whohow.fs.provider.file;

import cc.whohow.fs.Path;

import java.net.URI;
import java.nio.file.Paths;

public class LocalPath implements Path {
    private final URI uri;
    private volatile java.nio.file.Path path;

    public LocalPath(URI uri) {
        this.uri = uri;
    }

    public LocalPath(java.nio.file.Path path) {
        this.uri = path.toUri();
        this.path = path;
    }

    public java.nio.file.Path getPath() {
        if (path == null) {
            synchronized (this) {
                if (path == null) {
                    path = Paths.get(uri);
                }
            }
        }
        return path;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public LocalPath getParent() {
        java.nio.file.Path parent = getPath().getParent();
        if (parent == null) {
            return null;
        }
        return new LocalPath(parent);
    }

    @Override
    public LocalPath resolve(String relative) {
        return new LocalPath(uri.resolve(relative));
    }

    @Override
    public String toString() {
        return toUri().toString();
    }
}
