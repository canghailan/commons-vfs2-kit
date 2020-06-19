package cc.whohow.fs.command;

import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.command.provider.StandardCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FileShell {
    private static final Logger log = LogManager.getLogger(FileShell.class);
    protected final VirtualFileSystem vfs;
    protected final Map<String, BiFunction<VirtualFileSystem, String[], ?>> commands = new ConcurrentHashMap<>();

    public FileShell(VirtualFileSystem vfs) {
        this.vfs = vfs;
        this.install("INSTALL", this::install);
        this.install("FILE", StandardCommands::file);
        this.install("LIST", StandardCommands::list);
        this.install("TREE", StandardCommands::tree);
        this.install("COPY", StandardCommands::copy);
        this.install("MOVE", StandardCommands::move);
        this.install("DELETE", StandardCommands::delete);
        this.install("READ", StandardCommands::read);
        this.install("WRITE", StandardCommands::write);
    }

    public VirtualFileSystem getVirtualFileSystem() {
        return vfs;
    }

    public Collection<String> getCommands() {
        return Collections.unmodifiableCollection(commands.keySet());
    }

    protected String install(VirtualFileSystem vfs, String... args) {
        StringJoiner buffer = new StringJoiner("\n");
        for (String className : args) {
            install(className);
            buffer.add(className);
        }
        return buffer.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void install(String className) {
        try {
            install((Class) Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public <R> void install(Class<? extends Callable<R>> command) {
        try {
            CallableCommand<R> callableCommand = new CallableCommand<>(command);
            install(callableCommand.getCommandName(), callableCommand);
            install(callableCommand.getSimpleCommandName(), callableCommand);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public void install(String name, BiFunction<VirtualFileSystem, String[], ?> command) {
        log.info("INSTALL {}", name);
        commands.put(name, command);
    }

    /**
     * public static void main(String[] args)
     */
    @SuppressWarnings("unchecked")
    public <R> R exec(String name, String... args) {
        BiFunction<VirtualFileSystem, String[], ?> command = commands.get(name);
        if (command == null) {
            throw new IllegalArgumentException("command " + name + " not found");
        }
        try {
            return (R) command.apply(vfs, args);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public <R> Function<String[], R> newCommand(String name) {
        return new CommandFunction<>(this, name);
    }

    public <R> Callable<R> newCommand(String name, String... args) {
        return new Command<>(this, name, args);
    }

    public <R, C extends Callable<R>> C newCommand(Class<C> command, String... args) {
        return CallableCommand.build(command, vfs, args);
    }

    public static class CommandFunction<R> implements Function<String[], R> {
        protected final FileShell fish;
        protected final String name;

        public CommandFunction(FileShell fish, String name) {
            this.fish = fish;
            this.name = name;
        }

        @Override
        public R apply(String[] args) {
            return fish.exec(name, args);
        }
    }

    public static class Command<R> implements Callable<R> {
        protected final FileShell fish;
        protected final String name;
        protected final String[] args;

        public Command(FileShell fish, String name, String[] args) {
            this.fish = fish;
            this.name = name;
            this.args = args;
        }

        @Override
        public R call() {
            return fish.exec(name, args);
        }
    }
}
