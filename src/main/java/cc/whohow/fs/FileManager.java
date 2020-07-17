package cc.whohow.fs;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface FileManager extends ObjectFileManager {
    File get(CharSequence uri);

    CompletableFuture<File> copyAsync(File source, File target);

    CompletableFuture<File> moveAsync(File source, File target);

    CompletableFuture<Void> runAsync(Runnable runnable);

    <T> CompletableFuture<T> runAsync(Supplier<T> runnable);
}
