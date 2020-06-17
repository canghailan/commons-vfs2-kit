package cc.whohow.fs.provider.http;

import cc.whohow.fs.File;
import cc.whohow.fs.provider.UriPath;

public class HttpFile implements File<UriPath, HttpFile> {
    private final HttpFileSystem fileSystem;
    private final UriPath path;

    public HttpFile(HttpFileSystem fileSystem, UriPath path) {
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
    public String toString() {
        return getPath().toString();
    }
}
