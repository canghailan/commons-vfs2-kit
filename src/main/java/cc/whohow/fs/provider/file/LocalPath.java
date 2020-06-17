package cc.whohow.fs.provider.file;

import cc.whohow.fs.Path;

import java.net.URI;
import java.nio.file.Paths;

public class LocalPath implements Path {
    private final URI uri;
    private volatile java.nio.file.Path filePath;

    public LocalPath(URI uri) {
        this.uri = uri;
    }

    public LocalPath(java.nio.file.Path filePath) {
        this.uri = filePath.toUri();
        this.filePath = filePath;
    }

    public java.nio.file.Path getFilePath() {
        if (filePath == null) {
            synchronized (this) {
                if (filePath == null) {
                    filePath = Paths.get(uri);
                }
            }
        }
        return filePath;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public LocalPath getParent() {
        java.nio.file.Path parent = getFilePath().getParent();
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
