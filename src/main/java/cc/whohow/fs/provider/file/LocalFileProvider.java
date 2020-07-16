package cc.whohow.fs.provider.file;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.FileSystemBasedMountPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class LocalFileProvider implements FileSystemProvider<LocalPath, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileProvider.class);
    private volatile LocalFileSystem localFileSystem;

    @Override
    public void initialize(VirtualFileSystem vfs, File metadata) throws Exception {
        log.debug("initialize LocalFileProvider: {}", metadata);

        localFileSystem = new LocalFileSystem(URI.create("file:/"));

        vfs.mount(new FileSystemBasedMountPoint<>("file:/", localFileSystem, "file:/"));
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
    public Copy<LocalFile, LocalFile> copy(LocalFile source, LocalFile target) {
        return new LocalFileCopy(source, target);
    }

    @Override
    public Move<LocalFile, LocalFile> move(LocalFile source, LocalFile target) {
        return new LocalFileMove(source, target);
    }
}
