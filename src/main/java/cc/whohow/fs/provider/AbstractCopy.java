package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.util.MapReduce;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class AbstractCopy<F1 extends File, F2 extends File> implements Copy<F1, F2> {
    private static final Logger log = LogManager.getLogger(AbstractCopy.class);
    protected final F1 source;
    protected final F2 target;

    public AbstractCopy(F1 source, F2 target) {
        this.source = source;
        this.target = target;
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
    public F2 call() throws Exception {
        log.trace("copy {} -> {}", source, target);
        if (source.isDirectory()) {
            if (target.isDirectory()) {
                return copyDirectory();
            } else {
                throw copyDirectoryToFile(source, target);
            }
        } else {
            if (target.isDirectory()) {
                return copyFileToDirectory();
            } else {
                return copyFile();
            }
        }
    }

    @Override
    public F2 callUnchecked() {
        log.trace("copy {} -> {}", source, target);
        if (source.isDirectory()) {
            if (target.isDirectory()) {
                return copyDirectoryUnchecked();
            } else {
                throw UncheckedException.unchecked(copyDirectoryToFile(source, target));
            }
        } else {
            if (target.isDirectory()) {
                return copyFileToDirectoryUnchecked();
            } else {
                return copyFileUnchecked();
            }
        }
    }

    @Override
    public CompletableFuture<F2> callAsync(ExecutorService executor) {
        log.trace("copy {} -> {}", source, target);
        if (source.isDirectory()) {
            if (target.isDirectory()) {
                return copyDirectoryAsync(executor);
            } else {
                CompletableFuture<F2> future = new CompletableFuture<>();
                future.completeExceptionally(copyDirectoryToFile(source, target));
                return future;
            }
        } else {
            if (target.isDirectory()) {
                return copyFileToDirectoryAsync(executor);
            } else {
                return copyFileAsync(executor);
            }
        }
    }

    protected Exception copyDirectoryToFile(F1 source, F2 target) {
        return new UnsupportedOperationException("copy directory to file: " + source + " -> " + target);
    }

    protected F2 copyFile() throws Exception {
        return copyFile(source, target);
    }

    protected F2 copyFileToDirectory() throws Exception {
        return copyFile(source, resolve(target, source.getName()));
    }

    protected F2 copyDirectory() throws Exception {
        try (FileStream<? extends F1> sourceTree = tree(source)) {
            for (F1 file : sourceTree) {
                if (file.isRegularFile()) {
                    String relative = source.getPath().relativize(file.getPath());
                    copyFile(file, resolve(target, relative));
                }
            }
            return target;
        }
    }

    protected F2 copyFileUnchecked() {
        try {
            return copyFile();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    protected F2 copyFileToDirectoryUnchecked() {
        try {
            return copyFileToDirectory();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    protected F2 copyDirectoryUnchecked() {
        try {
            return copyDirectory();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    protected CompletableFuture<F2> copyFileAsync(ExecutorService executor) {
        return CompletableFuture.supplyAsync(this::copyFileUnchecked, executor);
    }

    protected CompletableFuture<F2> copyFileToDirectoryAsync(ExecutorService executor) {
        return CompletableFuture.supplyAsync(this::copyFileToDirectoryUnchecked, executor);
    }

    protected CompletableFuture<F2> copyDirectoryAsync(ExecutorService executor) {
        MapReduce<F2, F2> mapReduce = new MapReduce<>(target);
        mapReduce.begin();
        try (FileStream<? extends F1> sourceTree = tree(source)) {
            for (F1 file : sourceTree) {
                if (file.isRegularFile()) {
                    // 异步并行拷贝，拆分成单文件拷贝子任务
                    String relative = source.getPath().relativize(file.getPath());
                    mapReduce.map(copyFileAsync(file, resolve(target, relative), executor));
                }
            }
            mapReduce.end();
        } catch (Throwable e) {
            mapReduce.completeExceptionally(e);
        }
        return mapReduce;
    }

    protected abstract FileStream<? extends F1> tree(F1 directory);

    protected abstract F2 resolve(F2 directory, String relative);

    protected abstract CompletableFuture<F2> copyFileAsync(F1 source, F2 target, ExecutorService executor);

    protected F2 copyFile(F1 source, F2 target) throws Exception {
        return transferFile(source, target);
    }

    protected F2 transferFile(F1 source, F2 target) throws IOException {
        log.trace("transfer: {} -> {}", source, target);
        try (FileReadableChannel readableChannel = source.newReadableChannel();
             FileWritableChannel writableChannel = target.newWritableChannel()) {
            writableChannel.transferFrom(readableChannel);
            log.trace("transfer completed: {} -> {}", source, target);
            return target;
        }
    }

    @Override
    public String toString() {
        return "Copy " + getSource() + " " + getTarget();
    }
}
