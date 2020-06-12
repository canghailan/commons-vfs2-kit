package cc.whohow.fs.util;

import java.util.concurrent.ScheduledFuture;

public class ScheduledFutureTask<T extends Runnable> {
    private T task;
    private ScheduledFuture<?> scheduledFuture;

    public ScheduledFutureTask(T task, ScheduledFuture<?> scheduledFuture) {
        this.task = task;
        this.scheduledFuture = scheduledFuture;
    }
}
