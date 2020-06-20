package cc.whohow.fs.provider.file;

import cc.whohow.fs.provider.UriPath;

import java.net.URI;
import java.nio.file.Paths;

public class LocalPath extends UriPath {
    private volatile java.nio.file.Path filePath;

    public LocalPath(URI uri) {
        super(uri);
    }

    public LocalPath(java.nio.file.Path filePath) {
        super(filePath.toUri());
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
    public LocalPath resolve(String relative) {
        return new LocalPath(uri.resolve(relative));
    }
}
