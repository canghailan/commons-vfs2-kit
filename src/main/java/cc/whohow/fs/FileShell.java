package cc.whohow.fs;

public interface FileShell extends FileManager {
    <R> R exec(String name, String... args);
}
