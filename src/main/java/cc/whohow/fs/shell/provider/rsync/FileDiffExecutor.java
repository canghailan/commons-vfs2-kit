package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.util.MapReduce;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.LongAdder;

public class FileDiffExecutor {
    private static final Logger log = LogManager.getLogger(FileDiffExecutor.class);
    protected final LongAdder copyCount = new LongAdder();
    protected final LongAdder deleteCount = new LongAdder();
    protected final FileManager fileManager;
    protected final File source;
    protected final File target;
    protected final MapReduce<File, File> mapReduce;
    protected boolean skipDelete = true;

    public FileDiffExecutor(FileManager fileManager, File source, File target) {
        this.fileManager = fileManager;
        this.source = source;
        this.target = target;
        this.mapReduce = new MapReduce<>(target);
        this.mapReduce.begin();
    }

    public boolean apply(FileDiff fileDiff) {
        switch (fileDiff.getKind()) {
            case FileDiff.CREATE:
            case FileDiff.MODIFY: {
                log.trace("apply {}", fileDiff);
                mapReduce.map(copy(fileDiff.getFile()));
                return true;
            }
            case FileDiff.DELETE: {
                if (skipDelete) {
                    log.trace("skip apply {}", fileDiff);
                    return false;
                } else {
                    log.trace("apply {}", fileDiff);
                    mapReduce.map(delete(fileDiff.getFile()));
                    return true;
                }
            }
            default: {
                return false;
            }
        }
    }

    public CompletableFuture<?> future() {
        mapReduce.end();
        return mapReduce;
    }

    protected CompletableFuture<File> copy(String file) {
        copyCount.increment();
        return fileManager.copyAsync(source.resolve(file), target.resolve(file));
    }

    protected CompletableFuture<File> delete(String file) {
        File f = target.resolve(file);
        return fileManager.runAsync(f::delete).thenApply((v) -> f);
    }

    public long getCopyCount() {
        return copyCount.longValue();
    }

    public long getDeleteCount() {
        return deleteCount.longValue();
    }
}
