package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.util.MapReduce;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AsyncCopy<F1 extends File<?, F1>, F2 extends File<?, F2>> implements Copy<F1, F2> {
    private static final Logger log = LogManager.getLogger(AsyncCopy.class);
    protected final F1 source;
    protected final F2 target;
    protected final ExecutorService executor;

    public AsyncCopy(F1 source, F2 target, ExecutorService executor) {
        this.source = source;
        this.target = target;
        this.executor = executor;
    }

    @Override
    public F1 getSource() {
        return source;
    }

    @Override
    public F2 getTarget() {
        return target;
    }

    @Override
    public CompletableFuture<F2> get() {
        log.trace("copy {} -> {}", source, target);
        if (source.isDirectory()) {
            if (target.isDirectory()) {
                return copyDirectory(source, target);
            } else {
                CompletableFuture<F2> future = new CompletableFuture<>();
                future.completeExceptionally(new UnsupportedOperationException("copy directory to file: " + source + " -> " + target));
                return future;
            }
        } else {
            if (target.isDirectory()) {
                return copyFileToDirectory(source, target);
            } else {
                return copyFile(source, target);
            }
        }
    }

    protected CompletableFuture<F2> copyFile(F1 source, F2 target) {
        log.trace("stream copy: {} -> {}", source, target);
        CompletableFuture<F2> future = new CompletableFuture<>();
        try (FileReadableChannel readableChannel = source.newReadableChannel();
             FileWritableChannel writableChannel = target.newWritableChannel()) {
            writableChannel.transferFrom(readableChannel);
            log.trace("copy completed: {} -> {}", source, target);
            future.complete(target);
        } catch (Exception e) {
            log.trace("copy error: {} -> {}", source, target);
            future.completeExceptionally(e);
        }
        return future;
    }

    protected CompletableFuture<F2> copyFileToDirectory(F1 source, F2 target) {
        return copyFile(source, target.resolve(source.getName()));
    }

    protected CompletableFuture<F2> copyDirectory(F1 source, F2 target) {
        MapReduce<F2, F2> mapReduce = new MapReduce<>(target);
        mapReduce.map();
        try (FileStream<F1> files = source.tree()) {
            for (F1 file : files) {
                if (file.isRegularFile()) {
                    mapReduce.map(
                            CompletableFuture.supplyAsync(newFileCopy(
                                    file, target.resolve(source.getPath().relativize(file.getPath()))), executor)
                                    .thenApply(CompletableFuture::join));
                }
            }
        } catch (Exception e) {
            mapReduce.completeExceptionally(e);
        }
        mapReduce.reduce();
        return mapReduce;
    }

    protected Copy<F1, F2> newFileCopy(F1 source, F2 target) {
        return new AsyncCopy<>(source, target, executor);
    }
}
