package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class FileLastModifiedTimeVersionProvider implements FileVersionProvider<Long> {
    @Override
    public FileVersion<Long> getVersion(CloudFileObject fileObject) {
        try {
            return new FileVersion<>(fileObject, fileObject.getLastModifiedTime());
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
