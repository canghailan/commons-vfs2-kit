package cc.whohow.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.VfsComponent;

import java.io.UncheckedIOException;

public interface CloudFileSystem extends FileSystemImpl, VfsComponent {
    CloudFileSystemProvider getFileSystemProvider();

    @Override
    VirtualFileSystem getFileSystemManager();

    CloudFileObject resolve(CharSequence name) throws FileSystemException;

    @Override
    default CloudFileObject getRoot() throws FileSystemException {
        return resolve(getRootURI());
    }

    @Override
    default CloudFileObject resolveFile(String name) throws FileSystemException {
        return resolve(name);
    }

    @Override
    default CloudFileObject resolveFile(FileName name) throws FileSystemException {
        return resolve(name.getURI());
    }

    @Override
    default boolean hasCapability(Capability capability) {
        return getFileSystemProvider().getCapabilities().contains(capability);
    }

    @Override
    default FileSystemOptions getFileSystemOptions() {
        try {
            return new VirtualFileSystemOptions(getFileSystemManager().resolveFile(
                    "conf:/providers/" +getFileSystemProvider().getScheme()+ "/")).get();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
