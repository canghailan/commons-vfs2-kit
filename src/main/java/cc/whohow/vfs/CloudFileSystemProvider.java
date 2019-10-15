package cc.whohow.vfs;

import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.operations.Move;
import cc.whohow.vfs.operations.Remove;
import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.provider.VfsComponent;

import java.net.URI;

public interface CloudFileSystemProvider extends FileProvider, VfsComponent {
    String getScheme();

    CloudFileSystem getFileSystem(String uri) throws FileSystemException;

    CloudFileSystem findFileSystem(String uri) throws FileSystemException;

    FileName getFileName(String uri) throws FileSystemException;

    default CloudFileObject getFileObject(URI uri) throws FileSystemException {
        return getFileObject(uri.toString());
    }

    default CloudFileObject getFileObject(String uri) throws FileSystemException {
        return findFileSystem(uri).resolveFile(uri);
    }

    CloudFileOperations getFileOperations() throws FileSystemException;

    default Remove getRemoveOperation(CloudFileObject fileObject) throws FileSystemException {
        return getFileOperations().getOperation(Remove.class, fileObject);
    }

    default Copy getCopyOperation(Copy.Options options) throws FileSystemException {
        return getFileOperations().getOperation(Copy.class, options);
    }

    default Move getMoveOperation(Move.Options options) throws FileSystemException {
        return getFileOperations().getOperation(Move.class, options);
    }

    default void copy(Copy.Options options) throws FileSystemException {
        getCopyOperation(options).call();
    }

    default void move(Move.Options options) throws FileSystemException {
        getMoveOperation(options).call();
    }

    @Override
    default CloudFileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return getFileObject(URIBuilder.resolve(baseFile.getName().getURI(), uri));
    }

    @Override
    default CloudFileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default FileSystemConfigBuilder getConfigBuilder() {
        return VirtualFileSystemConfigBuilder.getInstance();
    }

    @Override
    default FileName parseUri(org.apache.commons.vfs2.FileName root, String uri) throws FileSystemException {
        return getFileName(URIBuilder.resolve(root.getURI(), uri));
    }
}
