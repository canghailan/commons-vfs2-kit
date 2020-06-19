package cc.whohow.fs.command;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

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
            CommandBuilder commandBuilder = new CommandBuilder(command);
            install(commandBuilder.getCommandName(), commandBuilder);
            install(commandBuilder.getSimpleCommandName(), commandBuilder);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public void install(String command, BiFunction<VirtualFileSystem, String[], ? extends Callable<?>> commandBuilder) {
        commands.put(command, commandBuilder);
    }

    @SuppressWarnings("unchecked")
    /**
     * public static void main(String[] args)
     */
    public <R> R exec(String name, String... args) {
        BiFunction<VirtualFileSystem, String[], ? extends Callable<?>> builder = commands.get(name);
        if (builder == null) {
            throw new IllegalArgumentException("command " + name + " not found");
        }
        try {
            return (R) builder.apply(vfs, args).call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public <R> Function<String[], R> newCommand(String name) {
        return new CommandProxy<>(this, name);
    }

    public <R, C extends Callable<R>> C newCommand(Class<C> commandType, String... args) {
        try {
            return commandType.getConstructor(VirtualFileSystem.class, String[].class).newInstance(vfs, args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public static class CommandProxy<R> implements Function<String[], R> {
        protected final FileShell fish;
        protected final String name;

        public CommandProxy(FileShell fish, String name) {
            this.fish = fish;
            this.name = name;
        }

        @Override
        public R apply(String[] args) {
            try {
                return fish.exec(name, args);
            } catch (Exception e) {
                throw UncheckedException.unchecked(e);
            }
        }
    }

    public static class CommandBuilder implements BiFunction<VirtualFileSystem, String[], Callable<?>> {
        protected final Class<? extends Callable<?>> type;
        protected final Constructor<? extends Callable<?>> constructor;

        public CommandBuilder(Class<? extends Callable<?>> type) throws Exception {
            this.type = type;
            this.constructor = type.getConstructor(VirtualFileSystem.class, String[].class);
        }

        public String getCommandName() {
            return type.getName();
        }

        public String getSimpleCommandName() {
            return type.getSimpleName();
        }

        @Override
        public Callable<?> apply(VirtualFileSystem fileSystem, String[] args) {
            try {
                return constructor.newInstance(fileSystem, args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw UncheckedException.unchecked(e);
            }
        }
    }
}
