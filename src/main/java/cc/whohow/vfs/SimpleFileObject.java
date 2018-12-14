package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * 简单文件对象，不支持rwx属性
 */
public interface SimpleFileObject extends FileObject {
    @Override
    default boolean isExecutable() {
        return false;
    }

    @Override
    default boolean isHidden() {
        return false;
    }

    @Override
    default boolean isReadable() {
        return true;
    }

    @Override
    default boolean isWriteable() {
        return true;
    }

    default boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-executable.error", this);
    }

    default boolean setReadable(boolean readable, boolean ownerOnly) {
        // do nothing
        return readable && !ownerOnly;
    }

    default boolean setWritable(boolean writable, boolean ownerOnly) {
        // do nothing
        return writable && !ownerOnly;
    }
}
