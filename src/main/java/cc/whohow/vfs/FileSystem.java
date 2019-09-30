package cc.whohow.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.VfsComponent;

public interface FileSystem extends FileSystemImpl, VfsComponent {
    FileSystemProvider getFileSystemProvider();

    FileObject resolve(CharSequence name) throws FileSystemException;

    @Override
    default FileObject getRoot() throws FileSystemException {
        return resolve(getRootURI());
    }

    @Override
    default FileObject resolveFile(String name) throws FileSystemException {
        return resolve(name);
    }

    @Override
    default FileObject resolveFile(FileName name) throws FileSystemException {
        return resolve(name.getURI());
    }

    @Override
    default boolean hasCapability(Capability capability) {
        return getFileSystemProvider().getCapabilities().contains(capability);
    }
}
