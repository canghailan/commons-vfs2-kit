package cc.whohow.fs.util;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSystemThreadFactory implements ThreadFactory {
    private final String threadNamePrefix;
    private final AtomicInteger nextId = new AtomicInteger(1);

    public FileSystemThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable task) {
        Objects.requireNonNull(task);
        return new Thread(null, task, threadNamePrefix + nextId.getAndIncrement());
    }
}
