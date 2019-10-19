package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileObjectX;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class DefaultRemoveOperation extends AbstractFileOperation<FileObjectX, Boolean> implements Remove {
    @Override
    public Boolean apply(FileObjectX fileObject) {
        try {
            return fileObject.deleteAll() > 0;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
