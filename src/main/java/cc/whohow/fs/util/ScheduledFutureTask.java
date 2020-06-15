package cc.whohow.fs.util;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

public class ScheduledFutureTask<T extends Runnable> {
    protected final T task;
    protected final ScheduledFuture<?> scheduledFuture;

    public ScheduledFutureTask(T task, ScheduledFuture<?> scheduledFuture) {
        Objects.requireNonNull(task);
        Objects.requireNonNull(scheduledFuture);
        this.task = task;
        this.scheduledFuture = scheduledFuture;
    }

    public T getTask() {
        return task;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ScheduledFutureTask) {
            ScheduledFutureTask<?> that = (ScheduledFutureTask<?>) o;
            return that.task.equals(task);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }

    @Override
    public String toString() {
        return task.toString();
    }
}
