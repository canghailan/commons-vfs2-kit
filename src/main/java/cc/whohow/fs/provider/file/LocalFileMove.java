package cc.whohow.fs.provider.file;

import cc.whohow.fs.Move;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LocalFileMove implements Move<LocalFile, LocalFile> {
    private static final Logger log = LogManager.getLogger(LocalFileMove.class);
    protected final LocalFile source;
    protected final LocalFile target;

    public LocalFileMove(LocalFile source, LocalFile target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public LocalFile getSource() {
        return source;
    }

    @Override
    public LocalFile getTarget() {
        return target;
    }

    @Override
    public LocalFile call() throws Exception {
        log.debug("move: {} -> {}", source, target);
        if (source.isDirectory()) {
            if (target.isDirectory()) {
                target.createDirectories();
                log.debug("Files.move: {} -> {}", source, target);
                Files.move(source.getPath().getFilePath(), target.getPath().getFilePath(),
                        StandardCopyOption.REPLACE_EXISTING);
                return target;
            } else {
                throw new UnsupportedOperationException("move directory to file: " + source + " -> " + target);
            }
        } else {
            target.createDirectories();
            if (target.isDirectory()) {
                LocalFile file = target.resolve(source.getName());
                log.debug("Files.move: {} -> {}", source, file);
                Files.move(source.getPath().getFilePath(), file.getPath().getFilePath(),
                        StandardCopyOption.REPLACE_EXISTING);
                return file;
            } else {
                log.debug("Files.move: {} -> {}", source, target);
                Files.move(source.getPath().getFilePath(), target.getPath().getFilePath(),
                        StandardCopyOption.REPLACE_EXISTING);
                return target;
            }
        }
    }

    @Override
    public String toString() {
        return "mv " + source + " " + target;
    }
}
