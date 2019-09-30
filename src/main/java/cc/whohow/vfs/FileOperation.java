package cc.whohow.vfs;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public interface FileOperation<T, R> extends org.apache.commons.vfs2.operations.FileOperation, Function<T, R>, Runnable, Callable<R> {
    FileOperation<T, R> with(T options);

    T getOptions();

    @Override
    default R call() {
        return apply(getOptions());
    }

    default CompletableFuture<R> call(Executor executor) {
        return CompletableFuture.supplyAsync(this::call, executor);
    }

    default void run() {
        call();
    }

    @Override
    default void process() {
        run();
    }
}
