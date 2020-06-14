package cc.whohow.fs.util;

import java.util.concurrent.ScheduledFuture;

public class ScheduledFutureTask<T extends Runnable> {
    private final T task;
    private final ScheduledFuture<?> scheduledFuture;

    public ScheduledFutureTask(T task, ScheduledFuture<?> scheduledFuture) {
        this.task = task;
        this.scheduledFuture = scheduledFuture;
    }
}
