package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class FileLastModifiedTimeVersionProvider implements FileVersionProvider<Long> {
    private static final FileLastModifiedTimeVersionProvider INSTANCE = new FileLastModifiedTimeVersionProvider();

    public static FileLastModifiedTimeVersionProvider get() {
        return INSTANCE;
    }

    @Override
    public FileVersion<Long> getVersion(CloudFileObject fileObject) {
        try {
            return new FileVersion<>(fileObject, fileObject.getLastModifiedTime());
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
