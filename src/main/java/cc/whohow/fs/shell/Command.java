package cc.whohow.fs.shell;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;

@FunctionalInterface
public interface Command<R> {
    default String getName() {
        return getClass().getSimpleName();
    }

    default void initialize(FileManager fileManager, File configuration) {
    }

    R call(FileManager fileManager, String... args) throws Exception;
}
