package cc.whohow.vfs.version;

import cc.whohow.vfs.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class FileAttributeVersionProvider implements FileVersionProvider<Object> {
    private final String attribute;

    public FileAttributeVersionProvider(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public FileVersion<Object> getVersion(FileObject fileObject) {
        try {
            return new FileVersion<>(fileObject, fileObject.getAttribute(attribute));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
