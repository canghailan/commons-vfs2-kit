package cc.whohow.vfs.version;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class FileLastModifiedTimeVersionProvider implements FileVersionProvider<Long> {
    @Override
    public FileVersion<Long> getVersion(FileObject fileObject) {
        try (FileContent fileContent = fileObject.getContent()) {
            return new FileVersion<>(fileObject, fileContent.getLastModifiedTime());
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
