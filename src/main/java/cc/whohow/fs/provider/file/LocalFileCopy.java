package cc.whohow.fs.provider.file;

import cc.whohow.fs.Copy;
import cc.whohow.fs.provider.AsyncCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class LocalFileCopy extends AsyncCopy<LocalFile, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileCopy.class);

    public LocalFileCopy(LocalFile source, LocalFile target, ExecutorService executor) {
        super(source, target, executor);
    }

    @Override
    protected CompletableFuture<LocalFile> copyFile(LocalFile source, LocalFile target) {
        CompletableFuture<LocalFile> future = new CompletableFuture<>();
        try {
            target.createDirectories();
            log.debug("Files.copy: {} -> {}", source, target);
            Files.copy(source.getPath().getFilePath(), target.getPath().getFilePath(),
                    StandardCopyOption.REPLACE_EXISTING);
            future.complete(target);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    protected Copy<LocalFile, LocalFile> newFileCopy(LocalFile source, LocalFile target) {
        return new LocalFileCopy(source, target, executor);
    }

    @Override
    public String toString() {
        return "cp " + source + " " + target;
    }
}
