package cc.whohow.fs.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.MountPoint;

import java.net.URI;
import java.util.Optional;

public class FileBasedMountPoint implements MountPoint {
    protected final String path;
    protected final File file;

    public FileBasedMountPoint(String path, File file) {
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException(path);
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(file.toString());
        }
        this.path = path;
        this.file = file;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Optional<File> resolve(URI uri) {
        String absolute = uri.toString();
        if (absolute.startsWith(path)) {
            return Optional.of(file.resolve(absolute.substring(path.length())));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return path + " -> " + file;
    }
}
