package cc.whohow.vfs.operations;

import cc.whohow.vfs.CloudFileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class DefaultRemoveOperation extends AbstractFileOperation<CloudFileObject, Boolean> implements Remove {
    @Override
    public Boolean apply(CloudFileObject fileObject) {
        try {
            return fileObject.deleteAll() > 0;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
