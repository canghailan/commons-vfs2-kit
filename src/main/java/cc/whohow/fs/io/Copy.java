package cc.whohow.fs.io;

import cc.whohow.fs.*;
import cc.whohow.fs.util.CompletableCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class Copy<F1 extends File<?, F1>, F2 extends File<?, F2>> implements Command<F2> {
    private static final Logger log = LogManager.getLogger(Copy.class);
    protected final F1 source;
    protected final F2 target;

    public Copy(F1 source, F2 target) {
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

    public static class Parallel<F1 extends File<?, F1>, F2 extends File<?, F2>> extends Copy<F1, F2> {
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
            CompletableCounter completableCounter = new CompletableCounter();
            try (FileStream<F1> files = source.tree()) {
                for (F1 file : files) {
                    if (file.isRegularFile()) {
                        completableCounter.increment();
                        CompletableFuture.supplyAsync(newFileCopyCommand(
                                file, target.resolve(source.getPath().relativize(file.getPath()))), executor)
                                .whenComplete(completableCounter::decrement);
                    }
                }
            }
            Long count = completableCounter.join();
            log.trace("copy completed: {}", count);
            return target;
        }

        protected Command<F2> newFileCopyCommand(F1 source, F2 target) {
            return new Copy<>(source, target);
        }
    }
}
