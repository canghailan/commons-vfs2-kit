package cc.whohow.fs;

import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 文件服务
 */
public interface FileManager extends ObjectFileManager {
    /**
     * 根据URI获取文件，文件不存在抛出java.nio.file.NoSuchFileException
     */
    default File get(CharSequence uri) {
        Optional<File> file = tryGet(uri);
        if (file.isPresent()) {
            return file.get();
        }
        throw new UncheckedIOException(new NoSuchFileException(uri.toString()));
    }

    /**
     * 根据URI获取文件，文件不存在返回java.util.Optional#empty()
     */
    Optional<File> tryGet(CharSequence uri);

    /**
     * 异步拷贝文件/文件夹
     */
    CompletableFuture<File> copyAsync(File source, File target);

    /**
     * 异步移动文件/文件夹
     */
    CompletableFuture<File> moveAsync(File source, File target);

    /**
     * 在IO线程池中执行异步任务
     */
    CompletableFuture<Void> runAsync(Runnable runnable);

    /**
     * 在IO线程池中执行异步任务
     */
    <T> CompletableFuture<T> runAsync(Supplier<T> runnable);
}
