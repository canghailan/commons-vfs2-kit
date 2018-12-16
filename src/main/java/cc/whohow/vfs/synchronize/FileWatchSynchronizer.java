package cc.whohow.vfs.synchronize;

import cc.whohow.vfs.version.FileVersionProvider;
import cc.whohow.vfs.watch.FileWatchListener;
import cc.whohow.vfs.watch.FileWatchTask;
import cc.whohow.vfs.watch.FileWatcher;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FileWatchSynchronizer extends FileSynchronizer implements FileListener, Closeable {
    private final ScheduledExecutorService executor;
    private FileWatchTask task;
    private ScheduledFuture<?> future;
    private Duration delay = Duration.ofSeconds(1L);

    public FileWatchSynchronizer(ScheduledExecutorService executor, FileObject source, FileObject target) {
        super(source, target);
        this.executor = executor;
    }

    public FileWatchSynchronizer(ScheduledExecutorService executor, FileObject source, FileObject target, FileVersionProvider<?> fileVersionProvider) {
        super(source, target, fileVersionProvider);
        this.executor = executor;
    }

    public FileWatchSynchronizer(ScheduledExecutorService executor,
                                 FileObject source,
                                 FileObject target,
                                 FileVersionProvider<?> sourceVersionProvider,
                                 FileVersionProvider<?> targetVersionProvider) {
        super(source, target, sourceVersionProvider, targetVersionProvider);
        this.executor = executor;
    }

    @Override
    public synchronized void run() {
        task = new FileWatchTask(new FileWatcher(source, sourceVersionProvider));
        task.addListener(FileWatchListener.create(sourceFileName, this));
        future = executor.scheduleWithFixedDelay(task, 0, delay.toMillis(), TimeUnit.MILLISECONDS);
        super.run();
    }

    @Override
    public void fileCreated(FileChangeEvent event) {
        String key = sourceKey(event.getFile().getName());
        create(key, event.getFile());
    }

    @Override
    public void fileDeleted(FileChangeEvent event) {
        String key = sourceKey(event.getFile().getName());
        delete(key, targetFile(key));
    }

    @Override
    public void fileChanged(FileChangeEvent event) {
        String key = sourceKey(event.getFile().getName());
        change(key, event.getFile(), targetFile(key));
    }

    @Override
    public void close() throws IOException {
        future.cancel(true);
    }
}
