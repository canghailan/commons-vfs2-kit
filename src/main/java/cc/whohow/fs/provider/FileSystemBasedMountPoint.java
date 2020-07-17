package cc.whohow.fs.provider;

import cc.whohow.fs.*;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public class FileSystemBasedMountPoint<P extends Path, F extends GenericFile<P, F>> implements MountPoint {
    protected final String path;
    protected final FileSystem<P, F> fileSystem;
    protected final String base;

    public FileSystemBasedMountPoint(String path, FileSystem<P, F> fileSystem) {
        this(path, fileSystem, "");
    }

    public FileSystemBasedMountPoint(String path, FileSystem<P, F> fileSystem, String base) {
        Objects.requireNonNull(fileSystem);
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException(path);
        }
        this.path = path;
        this.fileSystem = fileSystem;
        this.base = (base == null) ? "" : base;
    }

    @Override
    public String getPath() {
        return path;
    }

    protected boolean accept(String absolute) {
        return absolute.startsWith(path);
    }

    protected String rebase(String absolute) {
        if (base.isEmpty()) {
            return absolute.substring(path.length());
        } else {
            return base + absolute.substring(path.length());
        }
    }

    @Override
    public Optional<? extends File> resolve(URI uri) {
        String absolute = uri.toString();
        if (accept(absolute)) {
            return Optional.of(fileSystem.get(rebase(absolute)));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return path + " -> " + fileSystem + "#" + base;
    }
}
