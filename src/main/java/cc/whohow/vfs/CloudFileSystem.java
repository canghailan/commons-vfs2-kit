package cc.whohow.vfs;

import cc.whohow.vfs.watch.PollingFileWatchable;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.VfsComponent;

import java.io.UncheckedIOException;
import java.net.URI;

public interface CloudFileSystem extends FileSystemImpl, VfsComponent {
    CloudFileSystemProvider getFileSystemProvider();

    @Override
    VirtualFileSystem getFileSystemManager();

    @Override
    CloudFileObject resolveFile(String name) throws FileSystemException;

    default CloudFileObject resolveFile(URI uri) throws FileSystemException {
        return resolveFile(uri.toString());
    }

    @Override
    default CloudFileObject getRoot() throws FileSystemException {
        return resolveFile(getRootURI());
    }

    @Override
    default CloudFileObject resolveFile(FileName name) throws FileSystemException {
        return resolveFile(name.getURI());
    }

    @Override
    default boolean hasCapability(Capability capability) {
        return getFileSystemProvider().getCapabilities().contains(capability);
    }

    @Override
    default FileSystemOptions getFileSystemOptions() {
        try {
            return new VirtualFileSystemOptions(getFileSystemManager().resolveFile(
                    "conf:/providers/" + getFileSystemProvider().getScheme() + "/")).get();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    default void addListener(FileObject file, FileListener listener) {
        getFileSystemManager().getWatchService().addListener(
                new PollingFileWatchable<>((CloudFileObject) file, getFileSystemProvider().getFileVersionProvider()),
                listener);
    }

    @Override
    default void removeListener(FileObject file, FileListener listener) {
        getFileSystemManager().getWatchService().removeListener(
                new PollingFileWatchable<>((CloudFileObject) file, getFileSystemProvider().getFileVersionProvider()),
                listener);
    }
}
