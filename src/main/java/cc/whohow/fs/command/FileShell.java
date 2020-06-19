package cc.whohow.fs.command;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    @SuppressWarnings("unchecked")
    public void install(String className) {
        try {
            install((Class<? extends Callable<?>>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public void install(Class<? extends Callable<?>> command) {
        try {
            install(command.getSimpleName(), new CommandBuilder(command));
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
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

    public static class CommandBuilder implements BiFunction<VirtualFileSystem, String[], Callable<?>> {
        protected final Constructor<? extends Callable<?>> builder;

        public CommandBuilder(Class<? extends Callable<?>> type) throws Exception {
            this.builder = type.getConstructor(VirtualFileSystem.class, String[].class);
        }

        @Override
        public Callable<?> apply(VirtualFileSystem fileSystem, String[] args) {
            try {
                return builder.newInstance(fileSystem, args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw UncheckedException.unchecked(e);
            }
        }
    }
}
