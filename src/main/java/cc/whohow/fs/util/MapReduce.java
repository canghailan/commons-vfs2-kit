package cc.whohow.fs.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;

public class MapReduce<T, S> extends CompletableFuture<T> {
    protected final LongAdder completed = new LongAdder();
    protected final AtomicLong uncompleted = new AtomicLong();
    protected final AtomicReference<T> value;
    protected final BiFunction<T, S, T> reducer;

    public MapReduce(T value) {
        this(value, MapReduce::noop);
    }

    public MapReduce(T value, BiFunction<T, S, T> reducer) {
        this.value = new AtomicReference<>(value);
        this.reducer = reducer;
    }

    private static <T, S> T noop(T t, S s) {
        return t;
    }

    public void map() {
        uncompleted.getAndIncrement();
    }

    public void map(CompletableFuture<S> s) {
        map();
        s.whenComplete(this::reduce);
    }

    public void reduce() {
        completed.increment();
        if (uncompleted.decrementAndGet() == 0) {
            complete(value.get());
        }
    }

    public void reduce(S s, Throwable e) {
        if (e == null) {
            update(s);
            reduce();
        } else {
            completeExceptionally(e);
        }
    }

    /**
     * @see AtomicReference#updateAndGet(java.util.function.UnaryOperator)
     */
    protected void update(S s) {
        T prev, next;
        do {
            prev = value.get();
            next = reducer.apply(prev, s);
        } while (!value.compareAndSet(prev, next));
    }

    public long getCompleted() {
        return completed.longValue();
    }

    public long getUncompleted() {
        return uncompleted.longValue();
    }
}
