package cc.whohow.fs.provider.file;

import cc.whohow.fs.provider.GenericFileCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class LocalFileCopy extends GenericFileCopy<LocalFile, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileCopy.class);

    public LocalFileCopy(LocalFile source, LocalFile target) {
        super(source, target);
    }

    protected LocalFile copyFile(LocalFile source, LocalFile target) {
        try {
            target.createDirectories();
            log.debug("Files.copy: {} -> {}", source, target);
            Files.copy(
                    source.getPath().getFilePath(),
                    target.getPath().getFilePath(),
                    StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected CompletableFuture<LocalFile> copyFileAsync(LocalFile source, LocalFile target, ExecutorService executor) {
        return new LocalFileCopy(source, target).copyFileAsync(executor);
    }

    @Override
    public String toString() {
        return "cp " + source + " " + target;
    }
}
