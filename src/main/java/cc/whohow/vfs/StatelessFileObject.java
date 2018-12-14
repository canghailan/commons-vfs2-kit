package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;

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

    default void refresh() {
        // do nothing
    }

    @Override
    default void close() {
        // do nothing
    }
}
