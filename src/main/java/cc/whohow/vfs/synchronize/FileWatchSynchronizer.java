package cc.whohow.vfs.synchronize;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.version.FileVersionProvider;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

import java.io.Closeable;
import java.io.IOException;

public class FileWatchSynchronizer extends FileSynchronizer implements FileListener, Closeable {
    public FileWatchSynchronizer(CloudFileObject source, CloudFileObject target) {
        super(source, target);
        initialize();
    }

    public FileWatchSynchronizer(CloudFileObject source, CloudFileObject target, FileVersionProvider<?> fileVersionProvider) {
        super(source, target, fileVersionProvider);
        initialize();
    }

    public FileWatchSynchronizer(CloudFileObject source,
                                 CloudFileObject target,
                                 FileVersionProvider<?> sourceVersionProvider,
                                 FileVersionProvider<?> targetVersionProvider) {
        super(source, target, sourceVersionProvider, targetVersionProvider);
        initialize();
    }

    protected void initialize() {
        source.getFileSystem().addListener(source, this);
    }

    @Override
    public void fileCreated(FileChangeEvent event) {
        String key = sourceKey(event.getFile().getName());
        create(key, (CloudFileObject) event.getFile());
    }

    @Override
    public void fileDeleted(FileChangeEvent event) {
        String key = sourceKey(event.getFile().getName());
        delete(key, targetFile(key));
    }

    @Override
    public void fileChanged(FileChangeEvent event) {
        String key = sourceKey(event.getFile().getName());
        change(key, (CloudFileObject) event.getFile(), targetFile(key));
    }

    @Override
    public void close() throws IOException {
        source.getFileSystem().removeListener(source, this);
    }
}
