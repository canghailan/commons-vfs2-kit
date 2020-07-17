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

public class HttpFileProvider implements FileSystemProvider<UriPath, HttpFile> {
    private static final Logger log = LogManager.getLogger(HttpFileProvider.class);
    private volatile HttpFileSystem httpFileSystem;

    @Override
    public void initialize(VirtualFileSystem vfs, File metadata) throws Exception {
        log.debug("initialize HttpFileProvider: {}", metadata);

        this.httpFileSystem = new HttpFileSystem(URI.create("http:/"));

        vfs.mount(new HttpMountPoint("http://", httpFileSystem, "http://"));
        vfs.mount(new HttpMountPoint("https://", httpFileSystem, "https://"));
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
}
