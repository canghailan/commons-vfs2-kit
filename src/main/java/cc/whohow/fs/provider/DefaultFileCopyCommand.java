package cc.whohow.fs.provider;

import cc.whohow.fs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DefaultFileCopyCommand implements FileCopyCommand {
    private static final Logger log = LogManager.getLogger(DefaultFileCopyCommand.class);
    private final VirtualFileSystem vfs;
    private final String[] arguments;

    public DefaultFileCopyCommand(VirtualFileSystem vfs, String... arguments) {
        this.vfs = vfs;
        this.arguments = arguments;
    }

    @Override
    public int getMatchingScore() {
        return MatchingScore.LOW;
    }

    @Override
    public VirtualFileSystem getVirtualFileSystem() {
        return vfs;
    }

    @Override
    public String[] getArguments() {
        return arguments;
    }

    @Override
    public File<?, ?> call() throws IOException {
        File<?, ?> src = getSource();
        File<?, ?> dst = getDestination();

        if (src.isDirectory()) {
            if (dst.isDirectory()) {
                return copyDirectory(src, dst);
            } else {
                throw new UnsupportedOperationException("Unable Copy Directory to File");
            }
        } else {
            if (dst.isDirectory()) {
                return copyFileToDirectory(src, dst);
            } else {
                return copyFile(src, dst);
            }
        }
    }

    protected File<?, ?> copyFile(File<?, ?> src, File<?, ?> dst) throws IOException {
        log.debug("stream copy: {} -> {}", src, dst);
        try (FileReadableChannel readableChannel = src.newReadableChannel();
             FileWritableChannel writableChannel = dst.newWritableChannel()) {
            writableChannel.transferFrom(readableChannel);
            log.trace("copy completed: {} -> {}", src, dst);
            return dst;
        } catch (Exception e) {
            log.trace("copy error: {} -> {}", src, dst);
            throw e;
        }
    }

    protected File<?, ?> copyDirectory(File<?, ?> src, File<?, ?> dst) throws IOException {
        Queue<CompletableFuture<File<?, ?>>> queue = new LinkedList<>();
        try (FileStream<? extends File<?, ?>> files = src.tree()) {
            for (File<?, ?> file : files) {
                if (file.isRegularFile()) {
                    queue.offer(copyFileAsync(file, dst.resolve(src.getUri().relativize(file.getUri()).toString())));
                }
            }
        }
        while (!queue.isEmpty()) {
            File<?, ?> file = queue.poll().join();
            log.trace("await copy: {}", file);
        }
        return dst;
    }

    protected File<?, ?> copyFileToDirectory(File<?, ?> src, File<?, ?> dst) throws IOException {
        return copyFile(src, dst.resolve(src.getName()));
    }

    protected CompletableFuture<File<?, ?>> copyFileAsync(File<?, ?> src, File<?, ?> dst) {
        return CompletableFuture.supplyAsync(new Task(src, dst), vfs.getExecutor());
    }

    public static class Task implements Callable<File<?, ?>>, Supplier<File<?, ?>> {
        private final File<?, ?> source;
        private final File<?, ?> destination;

        public Task(File<?, ?> source, File<?, ?> destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public File<?, ?> call() throws Exception {
            log.debug("stream copy: {} -> {}", source, destination);
            try (FileReadableChannel readableChannel = source.newReadableChannel();
                 FileWritableChannel writableChannel = destination.newWritableChannel()) {
                writableChannel.transferFrom(readableChannel);
                log.trace("copy completed: {} -> {}", source, destination);
                return destination;
            } catch (Exception e) {
                log.trace("copy error: {} -> {}", source, destination);
                throw e;
            }
        }

        @Override
        public File<?, ?> get() {
            try {
                return call();
            } catch (Exception e) {
                throw UncheckedFileSystemException.unchecked(e);
            }
        }
    }
}
