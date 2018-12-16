package cc.whohow.vfs.version;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class FileAttributeVersionProvider implements FileVersionProvider<Object> {
    private final String attribute;

    public FileAttributeVersionProvider(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public FileVersion<Object> getVersion(FileObject fileObject) {
        try (FileContent fileContent = fileObject.getContent()) {
            return new FileVersion<>(fileObject, fileContent.getAttribute(attribute));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
