package cc.whohow.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystemException;

public interface StatelessFileContent extends FileContent {
    @Override
    default void close() throws FileSystemException {
    }

    @Override
    default boolean isOpen() {
        return true;
    }
}
