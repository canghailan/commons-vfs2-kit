package cc.whohow.fs.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.FileResolver;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.Path;

import java.net.URI;
import java.util.Optional;

public class DefaultFileResolver<P extends Path, F extends File<P, F>> implements FileResolver<P, F> {
    protected final FileSystem<P, F> fileSystem;
    protected final String base;

    public DefaultFileResolver(File<P, F> file) {
        this(file.getFileSystem(), file.getFileSystem().getUri().relativize(file.getUri()).toString());
    }

    public DefaultFileResolver(FileSystem<P, F> fileSystem) {
        this(fileSystem, "");
    }

    public DefaultFileResolver(FileSystem<P, F> fileSystem, String base) {
        this.fileSystem = fileSystem;
        this.base = (base == null) ? "" : base;
    }

    public FileSystem<P, F> getFileSystem() {
        return fileSystem;
    }

    public String getBase() {
        return base;
    }

    @Override
    public Optional<F> resolve(URI uri, CharSequence path) {
        try {
            if (base.isEmpty()) {
                return Optional.of(fileSystem.get(path));
            } else {
                return Optional.of(fileSystem.get(base + path));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
