package cc.whohow.fs.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

public class CompletableCounter extends CompletableFuture<Long> implements LongSupplier {
    protected final LongAdder counter = new LongAdder();
    protected final AtomicLong value = new AtomicLong();

    public void increment() {
        counter.increment();
        value.getAndIncrement();
    }

    public void decrement() {
        if (value.decrementAndGet() == 0) {
            complete(getCounter());
        }
    }

    public void decrement(Object ignore, Throwable e) {
        if (e != null) {
            completeExceptionally(e);
        } else {
            decrement();
        }
    }

    @Override
    public long getAsLong() {
        return value.get();
    }

    public long getCounter() {
        return counter.longValue();
    }
}
