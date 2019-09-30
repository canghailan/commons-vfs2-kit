package cc.whohow.vfs.operations.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.operations.AbstractFileOperation;
import cc.whohow.vfs.operations.Remove;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class DefaultRemoveOperation extends AbstractFileOperation<FileObject, Boolean> implements Remove {
    @Override
    public Boolean apply(FileObject fileObject) {
        try {
            return fileObject.deleteAll() > 0;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
