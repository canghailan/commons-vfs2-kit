package cc.whohow.fs.shell;

import cc.whohow.fs.FileManager;

import java.util.Map;

public interface FileShell extends FileManager {
    void install(Command<?> command);

    Map<String, Command<?>> getCommands();

    <R> R exec(String name, String... args);
}
