package cc.whohow.fs.util;

import cc.whohow.fs.*;

import java.io.IOException;

/**
 * 文件树（深度优先）
 */
public class FileTree<P extends Path, F extends File<P, F>> implements FileStream<F> {
    private final FileSystem<P, F> fileSystem;
    private final P path;
    private final int maxDepth;
    private volatile FileIterator<F> iterator;

    public FileTree(FileSystem<P, F> fileSystem, P path, int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException();
        }
        this.fileSystem = fileSystem;
        this.path = path;
        this.maxDepth = maxDepth;
    }

    @Override
    public FileIterator<F> iterator() {
        if (iterator != null) {
            throw new IllegalStateException();
        }
        return iterator = new FileTreeIterator<>(fileSystem, path, maxDepth);
    }

    @Override
    public void close() throws IOException {
        if (iterator != null) {
            iterator.close();
        }
    }
}
