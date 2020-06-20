package cc.whohow.fs.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;

/**
 * MapReduce单机版
 */
public class MapReduce<T, S> extends CompletableFuture<T> {
    /**
     * 完成计数器
     */
    protected final LongAdder completed = new LongAdder();
    /**
     * 未完成计数器
     */
    protected final AtomicLong uncompleted = new AtomicLong();
    /**
     * 任务状态/结果
     */
    protected final AtomicReference<T> value;
    /**
     * 状态合并函数
     */
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

    /**
     * 开始（计数）
     */
    public void begin() {
        uncompleted.getAndIncrement();
    }

    /**
     * 结束
     */
    public void end() {
        completed.increment();
        if (uncompleted.decrementAndGet() == 0) {
            complete(value.get());
        }
    }

    /**
     * 增加一个子任务
     */
    public void map(CompletableFuture<S> s) {
        begin();
        s.whenComplete(this::reduce);
    }

    /**
     * 子任务完成回调
     */
    public void reduce(S s, Throwable e) {
        if (e == null) {
            update(s);
            end();
        } else {
            completeExceptionally(e);
        }
    }

    /**
     * 更新任务状态
     *
     * @see AtomicReference#updateAndGet(java.util.function.UnaryOperator)
     */
    protected void update(S s) {
        T prev, next;
        do {
            prev = value.get();
            next = reducer.apply(prev, s);
        } while (!value.compareAndSet(prev, next));
    }

    /**
     * 完成数
     */
    public long getCompleted() {
        return completed.longValue();
    }

    /**
     * 未完成数
     */
    public long getUncompleted() {
        return uncompleted.longValue();
    }
}
