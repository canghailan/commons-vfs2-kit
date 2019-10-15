package cc.whohow.vfs.version;

import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class FileAttributeVersionProvider<T> implements FileVersionProvider<T> {
    protected final String attribute;

    public FileAttributeVersionProvider(String attribute) {
        this.attribute = attribute;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileVersion<T> getVersion(CloudFileObject fileObject) {
        try {
            return new FileVersion<>(fileObject, (T) fileObject.getAttribute(attribute));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
