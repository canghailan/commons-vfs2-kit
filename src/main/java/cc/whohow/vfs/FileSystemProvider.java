package cc.whohow.vfs;

import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.operations.Move;
import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.provider.VfsComponent;

public interface FileSystemProvider extends FileProvider, VfsComponent {
    FileObject getConfiguration();

    String getScheme();

    FileSystem getFileSystem(String uri) throws FileSystemException;

    FileSystem findFileSystem(String uri) throws FileSystemException;

    FileName getFileName(String uri) throws FileSystemException;

    default FileObject getFileObject(String uri) throws FileSystemException {
        return findFileSystem(uri).resolveFile(uri);
    }

    FileOperations getFileOperations() throws FileSystemException;

    default void copy(Copy.Options options) throws FileSystemException {
        getFileOperations().getOperation(Copy.class, options).call();
    }

    default void move(Move.Options options) throws FileSystemException {
        getFileOperations().getOperation(Move.class, options).call();
    }

    @Override
    default FileObject findFile(org.apache.commons.vfs2.FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return getFileObject(URIBuilder.resolve(baseFile.getName().getURI(), uri));
    }

    @Override
    default FileObject createFileSystem(String scheme, org.apache.commons.vfs2.FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
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