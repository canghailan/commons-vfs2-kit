package cc.whohow.fs.provider.file;

import cc.whohow.fs.File;
import cc.whohow.fs.FileResolver;
import cc.whohow.fs.Provider;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.provider.DefaultFileResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class LocalFileProvider implements Provider {
    private static final Logger log = LogManager.getLogger(LocalFileProvider.class);
    private LocalFileSystem localFileSystem;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception {
        log.debug("initialize LocalFileProvider: {}", context);
        localFileSystem = new LocalFileSystem(URI.create("file:///"));

        FileResolver<LocalPath, LocalFile> localFileResolver = new DefaultFileResolver<>(localFileSystem, "file:///");
        vfs.mount("file:///", localFileResolver);
    }

    @Override
    public void close() throws Exception {
        log.debug("close LocalFileProvider");
        localFileSystem.close();
    }
}
