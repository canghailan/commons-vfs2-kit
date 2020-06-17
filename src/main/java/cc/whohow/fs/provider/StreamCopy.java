package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.util.CompletionCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class StreamCopy<F1 extends File<?, F1>, F2 extends File<?, F2>> implements Copy<F1, F2> {
    private static final Logger log = LogManager.getLogger(StreamCopy.class);
    protected final F1 source;
    protected final F2 target;

    public StreamCopy(F1 source, F2 target) {
        this.source = source;
        this.target = target;
    }

    public F1 getSource() {
        return source;
    }

    public F2 getTarget() {
        return target;
    }

    @Override
    public F2 get() {
        try {
            return call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public F2 call() throws Exception {
        if (source.isDirectory()) {
            if (target.isDirectory()) {
                return copyDirectory(source, target);
            } else {
                throw new UnsupportedOperationException("copy directory to file: " + source + " -> " + target);
            }
        } else {
            if (target.isDirectory()) {
                return copyFileToDirectory(source, target);
            } else {
                return copyFile(source, target);
            }
        }
    }

    protected F2 copyFile(F1 source, F2 target) throws IOException {
        log.debug("stream copy: {} -> {}", source, target);
        try (FileReadableChannel readableChannel = source.newReadableChannel();
             FileWritableChannel writableChannel = target.newWritableChannel()) {
            writableChannel.transferFrom(readableChannel);
            log.trace("copy completed: {} -> {}", source, target);
            return target;
        } catch (Exception e) {
            log.trace("copy error: {} -> {}", source, target);
            throw e;
        }
    }

    protected F2 copyFileToDirectory(F1 source, F2 target) throws IOException {
        return copyFile(source, target.resolve(source.getName()));
    }

    protected F2 copyDirectory(F1 source, F2 target) throws IOException {
        try (FileStream<F1> files = source.tree()) {
            for (F1 file : files) {
                if (file.isRegularFile()) {
                    copyFile(file, target.resolve(source.getPath().relativize(file.getPath())));
                }
            }
        }
        return target;
    }

    public static class Parallel<F1 extends File<?, F1>, F2 extends File<?, F2>> extends StreamCopy<F1, F2> {
        protected ExecutorService executor;

        public Parallel(F1 source, F2 target) {
            super(source, target);
        }

        public Parallel<F1, F2> withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        @Override
        protected F2 copyDirectory(F1 source, F2 target) throws IOException {
            if (executor == null) {
                return super.copyDirectory(source, target);
            } else {
                return copyDirectory(source, target, executor);
            }
        }

        protected F2 copyDirectory(F1 source, F2 target, ExecutorService executor) throws IOException {
            CompletionCounter completionCounter = new CompletionCounter();
            completionCounter.register();
            try (FileStream<F1> files = source.tree()) {
                for (F1 file : files) {
                    if (file.isRegularFile()) {
                        completionCounter.register();
                        CompletableFuture.supplyAsync(newFileCopy(
                                file, target.resolve(source.getPath().relativize(file.getPath()))), executor)
                                .whenComplete(completionCounter::complete);
                    }
                }
            }
            completionCounter.complete();
            completionCounter.join();
            log.trace("copy files completed: {}", completionCounter.getCompleted() - 1);
            return target;
        }

        protected Copy<F1, F2> newFileCopy(F1 source, F2 target) {
            return new StreamCopy<>(source, target);
        }
    }
}
