package cc.whohow.vfs.watch;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.version.FileLastModifiedTimeVersionProvider;
import cc.whohow.vfs.version.FileVersionProvider;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileMonitor;
import org.apache.commons.vfs2.FileName;

import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileWatchMonitor implements FileMonitor, FileListener {
    private final NavigableMap<FileName, ScheduledFutureTask> tasks = new ConcurrentSkipListMap<>();
    private final ScheduledExecutorService executor;
    private final Duration delay;

    public FileWatchMonitor(ScheduledExecutorService executor) {
        this(executor, Duration.ofSeconds(1));
    }

    public FileWatchMonitor(ScheduledExecutorService executor, Duration delay) {
        this.executor = executor;
        this.delay = delay;
    }

    public void addListener(CloudFileObject file, FileListener listener) {
        addListener(file, listener, new FileLastModifiedTimeVersionProvider());
    }

    public synchronized void addListener(CloudFileObject file, FileListener listener, FileVersionProvider<?> fileVersionProvider) {
        FileName fileName = file.getName();
        ScheduledFutureTask futureTask = getTask(fileName);
        if (futureTask == null) {
            FileWatchTask task = new FileWatchTask(new FileWatcher(file, fileVersionProvider));
            ScheduledFuture<?> future = executor.scheduleWithFixedDelay(task, 0L, delay.toMillis(), TimeUnit.MILLISECONDS);
            futureTask = new ScheduledFutureTask(task, future);
            tasks.put(fileName, futureTask);
        }
        FileWatchTask task = futureTask.task;
        task.addListener(FileWatchListener.create(task.getWatcher().getWatchable().getName(), fileName, listener));
    }

    private ScheduledFutureTask getTask(FileName fileName) {
        ScheduledFutureTask futureTask = tasks.get(fileName);
        if (futureTask != null) {
            return futureTask;
        }
        for (Map.Entry<FileName, ScheduledFutureTask> e : tasks.tailMap(fileName).entrySet()) {
            if (e.getKey().isDescendent(fileName)) {
                return e.getValue();
            }
        }
        return null;
    }

    public synchronized void removeListener(FileName fileName, FileListener listener) {
        FileWatchListener fileWatchListener = FileWatchListener.create(fileName, listener);
        ScheduledFutureTask futureTask = tasks.get(fileName);
        if (futureTask != null) {
            futureTask.task.removeListener(fileWatchListener);
        }
        for (Map.Entry<FileName, ScheduledFutureTask> e : tasks.tailMap(fileName).entrySet()) {
            if (e.getKey().isDescendent(fileName)) {
                e.getValue().task.removeListener(fileWatchListener);
            } else {
                break;
            }
        }
    }

    @Override
    public void addFile(org.apache.commons.vfs2.FileObject file) {
        addListener((CloudFileObject) file, this);
    }

    @Override
    public void removeFile(org.apache.commons.vfs2.FileObject file) {
        removeListener(file.getName(), this);
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
    }

    @Override
    public String toString() {
        return tasks.values().stream()
                .map(ScheduledFutureTask::toString)
                .collect(Collectors.joining("\n"));
    }

    static class ScheduledFutureTask {
        final FileWatchTask task;
        final ScheduledFuture<?> future;

        ScheduledFutureTask(FileWatchTask task, ScheduledFuture<?> future) {
            this.task = task;
            this.future = future;
        }

        @Override
        public String toString() {
            return task.toString();
        }
    }
}
