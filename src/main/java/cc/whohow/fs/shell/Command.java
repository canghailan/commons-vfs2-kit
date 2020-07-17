package cc.whohow.fs.shell;

import cc.whohow.fs.FileManager;

@FunctionalInterface
public interface Command<R> {
    default String getName() {
        return getClass().getSimpleName();
    }

    R call(FileManager fileManager, String... args) throws Exception;
}
