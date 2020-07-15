package cc.whohow.fs.provider.http;

import cc.whohow.fs.GenericFile;
import cc.whohow.fs.provider.UriPath;

import java.util.Objects;

public class HttpFile implements GenericFile<UriPath, HttpFile> {
    private final HttpFileSystem fileSystem;
    private final UriPath path;

    public HttpFile(HttpFileSystem fileSystem, UriPath path) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(path);
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public HttpFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public UriPath getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof HttpFile) {
            HttpFile httpFile = (HttpFile) o;
            return fileSystem.equals(httpFile.fileSystem) &&
                    path.equals(httpFile.path);
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
