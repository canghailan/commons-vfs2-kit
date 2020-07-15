package cc.whohow.fs;

import java.util.concurrent.CompletableFuture;

public interface FileManager extends ObjectFileManager {
    File get(CharSequence uri);

    CompletableFuture<? extends File> copyAsync(File source, File target);

    CompletableFuture<? extends File> moveAsync(File source, File target);
}
