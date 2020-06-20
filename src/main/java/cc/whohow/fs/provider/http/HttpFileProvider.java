package cc.whohow.fs.provider.http;

import cc.whohow.fs.File;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.FileSystemProvider;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.provider.UriPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

public class HttpFileProvider implements FileSystemProvider<UriPath, HttpFile> {
    private static final Logger log = LogManager.getLogger(HttpFileProvider.class);
    private volatile VirtualFileSystem vfs;
    private volatile HttpFileSystem httpFileSystem;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> metadata) throws Exception {
        this.vfs = vfs;
        log.debug("initialize HttpFileProvider: {}", metadata);

        httpFileSystem = new HttpFileSystem(URI.create("http:/"));

        vfs.mount("http://", new HttpFileResolver(httpFileSystem, "http://"));
        vfs.mount("https://", new HttpFileResolver(httpFileSystem, "https://"));
    }

    @Override
    public void close() throws Exception {
        log.debug("close HttpFileProvider");
        httpFileSystem.close();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public FileSystem<UriPath, HttpFile> getFileSystem(URI uri) {
        return httpFileSystem;
    }

    @Override
    public Collection<? extends FileSystem<UriPath, HttpFile>> getFileSystems() {
        return Collections.singleton(httpFileSystem);
    }

    @Override
    public ExecutorService getExecutor() {
        return vfs.getExecutor();
    }
}
