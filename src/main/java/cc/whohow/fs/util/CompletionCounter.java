package cc.whohow.fs.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class CompletionCounter extends CompletableFuture<Long> {
    protected final LongAdder completed = new LongAdder();
    protected final AtomicLong uncompleted = new AtomicLong();

    public void register() {
        uncompleted.getAndIncrement();
    }

    public boolean complete() {
        completed.increment();
        if (uncompleted.decrementAndGet() == 0) {
            return complete(getCompleted());
        } else {
            return false;
        }
    }

    public boolean complete(Object ignore, Throwable e) {
        if (e == null) {
            return complete();
        } else {
            return completeExceptionally(e);
        }
    }

    public long getCompleted() {
        return completed.longValue();
    }

    public long getUncompleted() {
        return uncompleted.longValue();
    }
}
