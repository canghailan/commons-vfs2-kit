package cc.whohow.fs.provider.s3;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.Path;
import cc.whohow.fs.provider.DefaultFileResolver;

import java.net.URI;
import java.util.Optional;

public class S3FileResolver<P extends Path, F extends File<P, F>> extends DefaultFileResolver<P, F> {
    public S3FileResolver(FileSystem<P, F> fileSystem) {
        super(fileSystem);
    }

    public S3FileResolver(FileSystem<P, F> fileSystem, String base) {
        super(fileSystem, base);
    }

    @Override
    public Optional<F> resolve(URI uri, CharSequence mountPoint, CharSequence path) {
        // 不支持Query及Fragment
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return Optional.empty();
        }
        return super.resolve(uri, mountPoint, path);
    }
}
