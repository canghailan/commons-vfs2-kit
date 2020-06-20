package cc.whohow.fs;

import java.util.function.Consumer;

/**
 * 文件监听服务
 */
public interface FileWatchService<P extends Path, F extends File<P, F>> extends AutoCloseable {
    /**
     * 添加监听
     */
    void watch(F file, Consumer<FileWatchEvent<P, F>> listener);

    /**
     * 移除监听
     */
    void unwatch(F file, Consumer<FileWatchEvent<P, F>> listener);
}
