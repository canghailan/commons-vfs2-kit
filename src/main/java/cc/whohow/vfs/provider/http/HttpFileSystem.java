package cc.whohow.vfs.provider.http;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileSystem;
import cc.whohow.vfs.CloudFileSystemProvider;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.provider.uri.UriFileName;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;
import org.apache.http.client.HttpClient;

public class HttpFileSystem extends AbstractVfsComponent implements CloudFileSystem {
    protected final HttpFileSystemProvider fileSystemProvider;
    protected final UriFileName root;
    protected final HttpClient httpClient;

    public HttpFileSystem(HttpFileSystemProvider fileSystemProvider, UriFileName root, HttpClient httpClient) {
        this.fileSystemProvider = fileSystemProvider;
        this.root = root;
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public CloudFileSystemProvider getFileSystemProvider() {
        return fileSystemProvider;
    }

    @Override
    public FileName getRootName() {
        return root;
    }

    @Override
    public VirtualFileSystem getFileSystemManager() {
        return (VirtualFileSystem) getContext().getFileSystemManager();
    }

    @Override
    public CloudFileObject resolveFile(String name) throws FileSystemException {
        return null;
    }
}
