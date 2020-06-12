package cc.whohow.fs.util;

import cc.whohow.fs.File;
import cc.whohow.fs.FileIterator;
import cc.whohow.fs.FileSystem;
import cc.whohow.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 文件树遍历器（深度优先）
 */
public class FileTreeIterator<P extends Path, F extends File<P, F>> implements FileIterator<F> {
    private static final Logger log = LogManager.getLogger(FileTreeIterator.class);
    private final FileSystem<P, F> fileSystem;
    private final P path;
    private int maxDepth;
    private Deque<FileIterator<F>> queue = new LinkedList<>();
    private int depth;
    private F file;

    public FileTreeIterator(FileSystem<P, F> fileSystem, P path, int maxDepth) {
        this.fileSystem = fileSystem;
        this.path = path;
        this.maxDepth = maxDepth;
        this.queue.addFirst(Files.newFileIterator(Collections.singleton(fileSystem.get(path))));
        this.depth = 0;
    }

    public FileSystem<P, F> getFileSystem() {
        return fileSystem;
    }

    public P getPath() {
        return path;
    }

    @Override
    public boolean hasNext() {
        while (!queue.isEmpty()) {
            FileIterator<F> iterator = queue.getFirst();
            if (iterator.hasNext()) {
                return true;
            } else {
                try {
                    afterVisitDirectory();
                } catch (IOException e) {
                    log.warn("ignore IOException", e);
                }
            }
        }
        return false;
    }

    @Override
    public F next() {
        file = queue.getFirst().next();
        if (file.isDirectory()) {
            beforeVisitDirectory();
        }
        return file;
    }

    @Override
    public void remove() {
        if (file.isDirectory()) {
            try {
                afterVisitDirectory();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        file.delete();
    }

    @Override
    public void close() {
        while (!queue.isEmpty()) {
            try {
                afterVisitDirectory();
            } catch (IOException e) {
                log.warn("ignore IOException", e);
            }
        }
    }

    protected void beforeVisitDirectory() {
        if (depth < maxDepth) {
            DirectoryStream<F> directoryStream = file.newDirectoryStream();
            queue.addFirst(Files.newFileIterator(directoryStream, directoryStream));
            depth++;
        } else {
            log.trace("depth: {}, maxDepth: {}", depth, maxDepth);
        }
    }

    protected void afterVisitDirectory() throws IOException {
        queue.removeFirst().close();
        depth--;
    }
}
