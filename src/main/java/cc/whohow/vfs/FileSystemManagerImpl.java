package cc.whohow.vfs;

import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.provider.VfsComponent;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;

public interface FileSystemManagerImpl extends FileSystemManager {
    @Override
    default FileObject resolveFile(String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(getBaseFile(), name);
    }

    @Override
    default FileObject resolveFile(FileObject baseFile, String name) throws FileSystemException {
        return resolveFile(new URIBuilder(baseFile.getName().getURI()).resolve(name).build());
    }

    @Override
    default FileObject resolveFile(File baseFile, String name) throws FileSystemException {
        return resolveFile(toFileObject(baseFile), name);
    }

    @Override
    default FileName resolveName(FileName root, String name) throws FileSystemException {
        return resolveURI(new URIBuilder(root.getURI()).resolve(name).build().toString());
    }

    @Override
    default FileName resolveName(FileName root, String name, NameScope scope) throws FileSystemException {
        FileName fileName = resolveName(root, name);
        switch (scope) {
            case FILE_SYSTEM: {
                return fileName;
            }
            case CHILD: {
                if (root.equals(fileName.getParent())) {
                    return fileName;
                }
            }
            case DESCENDENT: {
                if (root.isDescendent(fileName)) {
                    return fileName;
                }
            }
            case DESCENDENT_OR_SELF: {
                if (root.equals(fileName) || root.isDescendent(fileName)) {
                    return fileName;
                }
            }
            default: {
                throw new FileSystemException("vfs.provider/resolve-file.error", name);
            }
        }
    }

    @Override
    default FileObject toFileObject(File file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default FileObject createFileSystem(String provider, FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default FileObject createFileSystem(FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default FileObject createVirtualFileSystem(String rootUri) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default FileObject createVirtualFileSystem(FileObject rootFile) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default boolean canCreateFileSystem(FileObject file) throws FileSystemException {
        return false;
    }

    @Override
    default FilesCache getFilesCache() {
        return new NullFilesCache();
    }

    @Override
    default CacheStrategy getCacheStrategy() {
        return CacheStrategy.MANUAL;
    }

    @Override
    default Class<?> getFileObjectDecorator() {
        return null;
    }

    @Override
    default Constructor<?> getFileObjectDecoratorConst() {
        return null;
    }

    @Override
    default FileContentInfoFactory getFileContentInfoFactory() {
        return new FileAttributeContentInfoFactory();
    }

    @Override
    default FileObject resolveFile(URI uri) throws FileSystemException {
        return resolveFile(uri.toString());
    }

    @Override
    default FileObject resolveFile(URL url) throws FileSystemException {
        return resolveFile(url.toString());
    }

    @Override
    default void closeFileSystem(org.apache.commons.vfs2.FileSystem filesystem) {
        try {
            if (filesystem instanceof VfsComponent) {
                VfsComponent vfsComponent = (VfsComponent) filesystem;
                vfsComponent.close();
            } else if (filesystem instanceof AutoCloseable) {
                AutoCloseable closeable = (AutoCloseable) filesystem;
                closeable.close();
            }
        } catch (Exception ignore) {
        }
    }
}
