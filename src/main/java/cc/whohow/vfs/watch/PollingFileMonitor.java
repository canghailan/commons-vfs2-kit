package cc.whohow.vfs.watch;

import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileMonitor;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;

import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.*;

public class PollingFileMonitor implements FileMonitor {
    private final NavigableMap<FileName, FileMonitorTask> tasks = new ConcurrentSkipListMap<>();
    private final Map<FileName, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor;
    private final Duration delay = Duration.ofSeconds(1);

    public PollingFileMonitor(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public synchronized void addListener(FileObject file, FileListener listener) {
        FileName fileName = file.getName();
        FileName taskFileName = fileName;
        FileMonitorTask task = tasks.get(fileName);
        if (task == null) {
            for (Map.Entry<FileName, FileMonitorTask> e : tasks.tailMap(fileName).entrySet()) {
                if (e.getKey().isDescendent(fileName)) {
                    task = e.getValue();
                    taskFileName = e.getKey();
                    break;
                }
            }
            if (task == null) {
                task = new FileMonitorTask(new FileChangeEventProducer(file));
                tasks.put(fileName, task);
                runningTasks.put(fileName,
                        executor.scheduleWithFixedDelay(task, 0L, delay.toMillis(), TimeUnit.MILLISECONDS));
            }
        }
        task.addListener(FileMonitorListener.create(taskFileName, fileName, listener));
    }

    public synchronized void removeListener(FileObject file, FileListener listener) {
        FileName fileName = file.getName();
        FileMonitorListener fileMonitorListener = FileMonitorListener.create(fileName, fileName, listener);
        for (Map.Entry<FileName, FileMonitorTask> e : tasks.tailMap(fileName).entrySet()) {
            if (e.getKey().isDescendent(fileName)) {
                e.getValue().removeListener(fileMonitorListener);
            } else {
                break;
            }
        }
    }

    @Override
    public void addFile(FileObject file) {
    }

    @Override
    public void removeFile(FileObject file) {
    }
}
