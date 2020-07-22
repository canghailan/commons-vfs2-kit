package cc.whohow.fs;

import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface FileManager extends ObjectFileManager {
    default File get(CharSequence uri) {
        Optional<File> file = tryGet(uri);
        if (file.isPresent()) {
            return file.get();
        }
        throw new UncheckedIOException(new NoSuchFileException(uri.toString()));
    }

    Optional<File> tryGet(CharSequence uri);

    CompletableFuture<File> copyAsync(File source, File target);

    CompletableFuture<File> moveAsync(File source, File target);

    CompletableFuture<Void> runAsync(Runnable runnable);

    <T> CompletableFuture<T> runAsync(Supplier<T> runnable);
}
