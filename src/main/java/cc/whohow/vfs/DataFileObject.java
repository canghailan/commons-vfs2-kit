package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * 数据文件对象，不支持rwx属性
 */
public interface DataFileObject extends FileObject {
    @Override
    default boolean isExecutable() throws FileSystemException {
        return false;
    }

    @Override
    default boolean isHidden() throws FileSystemException {
        return false;
    }

    default boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-executable.error", this);
    }
}
