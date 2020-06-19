package cc.whohow.fs.command;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class FileShell {
    protected final VirtualFileSystem vfs;
    protected final Map<String, BiFunction<VirtualFileSystem, String[], ? extends Callable<?>>> commands = new ConcurrentHashMap<>();

    public FileShell(VirtualFileSystem vfs) {
        this.vfs = vfs;
    }

    public void install(String command, BiFunction<VirtualFileSystem, String[], ? extends Callable<?>> commandBuilder) {
        commands.put(command, commandBuilder);
    }

    @SuppressWarnings("unchecked")
    public <R> R exec(String... commandLine) {
        if (commandLine.length == 0) {
            throw new IllegalArgumentException();
        }
        BiFunction<VirtualFileSystem, String[], ? extends Callable<?>> builder = commands.get(commandLine[0]);
        if (builder == null) {
            throw new IllegalArgumentException("command " + commandLine[0] + " not found");
        }
        try {
            return (R) builder.apply(vfs, commandLine).call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }
}
