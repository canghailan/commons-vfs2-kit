package cc.whohow.fs;

import java.util.function.Consumer;

/**
 * 文件监听服务
 */
public interface FileWatchService<P extends Path, F extends GenericFile<P, F>> extends AutoCloseable {
    /**
     * 添加监听
     */
    void watch(F file, Consumer<FileEvent> listener);

    /**
     * 移除监听
     */
    void unwatch(F file, Consumer<FileEvent> listener);
}
