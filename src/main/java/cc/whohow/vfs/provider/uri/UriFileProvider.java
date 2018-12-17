package cc.whohow.vfs.provider.uri;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.FileProvider;

import java.util.Collection;

public class UriFileProvider implements FileProvider {
    @Override
    public FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return new UriFileObject(uri);
    }

    @Override
    public FileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return null;
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }

    @Override
    public FileName parseUri(FileName root, String uri) throws FileSystemException {
        return null;
    }
}
