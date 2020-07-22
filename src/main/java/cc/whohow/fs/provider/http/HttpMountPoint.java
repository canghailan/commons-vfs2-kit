package cc.whohow.fs.provider.http;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.provider.FileSystemBasedMountPoint;
import cc.whohow.fs.provider.UriPath;

import java.net.URI;
import java.util.Optional;

public class HttpMountPoint extends FileSystemBasedMountPoint<UriPath, HttpFile> {
    public HttpMountPoint(String path, FileSystem<UriPath, HttpFile> fileSystem) {
        super(path, fileSystem);
    }

    public HttpMountPoint(String path, FileSystem<UriPath, HttpFile> fileSystem, String base) {
        super(path, fileSystem, base);
    }

    @Override
    public Optional<File> resolve(URI uri) {
        String absolute = uri.toString();
        if (accept(absolute)) {
            if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
                return Optional.of(fileSystem.get(fileSystem.resolve(uri)));
            } else {
                return Optional.of(fileSystem.get(rebase(absolute)));
            }
        }
        return Optional.empty();
    }
}
