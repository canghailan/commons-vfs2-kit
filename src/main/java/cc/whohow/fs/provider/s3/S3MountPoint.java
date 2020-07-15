package cc.whohow.fs.provider.s3;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.GenericFile;
import cc.whohow.fs.Path;
import cc.whohow.fs.provider.FileSystemBasedMountPoint;

import java.net.URI;
import java.util.Optional;

public class S3MountPoint<P extends Path, F extends GenericFile<P, F>> extends FileSystemBasedMountPoint<P, F> {
    public S3MountPoint(String path, FileSystem<P, F> fileSystem) {
        super(path, fileSystem);
    }

    public S3MountPoint(String path, FileSystem<P, F> fileSystem, String base) {
        super(path, fileSystem, base);
    }

    @Override
    public Optional<? extends File> resolve(URI uri) {
        // 不支持Query及Fragment
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return Optional.empty();
        }
        return super.resolve(uri);
    }
}
