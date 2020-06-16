package cc.whohow.fs.provider.file;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.DefaultFileResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

public class LocalFileProvider implements FileSystemProvider<LocalPath, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileProvider.class);
    private volatile VirtualFileSystem vfs;
    private volatile LocalFileSystem localFileSystem;

    @Override
    public void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception {
        this.vfs = vfs;
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

    @Override
    public String getScheme() {
        return "file";
    }

    @Override
    public FileSystem<LocalPath, LocalFile> getFileSystem(URI uri) {
        return localFileSystem;
    }

    @Override
    public Collection<? extends FileSystem<LocalPath, LocalFile>> getFileSystems() {
        return Collections.singleton(localFileSystem);
    }

    @Override
    public ExecutorService getExecutor() {
        return vfs.getExecutor();
    }
}
