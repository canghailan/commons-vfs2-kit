package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * 无状态文件对象
 */
public interface StatelessFileObject extends FileObject {
    @Override
    default boolean isAttached() {
        return true;
    }

    @Override
    default boolean isContentOpen() {
        return true;
    }

    default void refresh() throws FileSystemException {
        // do nothing
    }

    @Override
    default void close() throws FileSystemException {
        // do nothing
    }
}
