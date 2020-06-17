package cc.whohow.fs.provider.file;

import cc.whohow.fs.Copy;
import cc.whohow.fs.provider.StreamCopy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LocalFileCopy extends StreamCopy.Parallel<LocalFile, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileCopy.class);

    public LocalFileCopy(LocalFile source, LocalFile target) {
        super(source, target);
    }

    @Override
    protected LocalFile copyFile(LocalFile source, LocalFile target) throws IOException {
        log.debug("Files.copy: {} -> {}", source, target);
        Files.copy(source.getPath().getFilePath(), target.getPath().getFilePath(),
                StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    @Override
    protected Copy<LocalFile, LocalFile> newFileCopy(LocalFile source, LocalFile target) {
        return new LocalFileCopy(source, target);
    }

    @Override
    public String toString() {
        return "cp " + source + " " + target;
    }
}
