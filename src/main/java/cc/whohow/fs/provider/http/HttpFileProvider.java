package cc.whohow.fs.provider.http;

import cc.whohow.fs.File;
import cc.whohow.fs.Provider;
import cc.whohow.fs.VirtualFileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class HttpFileProvider implements Provider {
    private static final Logger log = LogManager.getLogger(HttpFileProvider.class);
    private volatile HttpFileSystem httpFileSystem;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception {
        log.debug("initialize HttpFileProvider: {}", context);
        httpFileSystem = new HttpFileSystem(URI.create("http:///"));

        vfs.mount("http://", new HttpFileResolver(httpFileSystem, "http://"));
        vfs.mount("https://", new HttpFileResolver(httpFileSystem, "https://"));
    }

    @Override
    public void close() throws Exception {
        log.debug("close HttpFileProvider");
        httpFileSystem.close();
    }
}
