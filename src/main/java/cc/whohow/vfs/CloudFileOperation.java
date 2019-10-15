package cc.whohow.vfs;

import org.apache.commons.vfs2.operations.FileOperation;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public interface CloudFileOperation<T, R> extends FileOperation, Function<T, R>, Runnable, Callable<R> {
    CloudFileOperation<T, R> with(T options);

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
