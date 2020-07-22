package cc.whohow.fs.shell.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.shell.Command;
import cc.whohow.fs.shell.FileShell;
import cc.whohow.fs.shell.provider.standard.*;
import cc.whohow.fs.util.ComposeRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class VirtualFileShell implements FileShell {
    private static final Logger log = LogManager.getLogger(VirtualFileShell.class);
    protected final Map<String, Command<?>> commands = new ConcurrentHashMap<>();
    protected final VirtualFileSystem vfs;
    protected volatile Runnable onClose;

    public VirtualFileShell(VirtualFileSystem vfs) {
        this.vfs = vfs;
        install("INSTALL", this::install);
        install("FILE", new FileCommand());
        install("FILES", new FilesCommand());
        install("DELETE", new DeleteCommand());
        install("LIST", new ListCommand());
        install("TREE", new TreeCommand());
        install("STAT", new StatCommand());
        install("READ", new ReadCommand());
        install("WRITE", new WriteCommand());
        install("COPY", new CopyCommand());
        install("MOVE", new MoveCommand());
    }

    public synchronized void onClose(Runnable onClose) {
        if (this.onClose == null) {
            this.onClose = onClose;
        } else {
            this.onClose = new ComposeRunnable(this.onClose, onClose);
        }
    }

    @Override
    public void install(Command<?> command) {
        install(command.getName(), command);
    }

    public void install(String name, Command<?> command) {
        log.debug("INSTALL {}", name);
        commands.put(name, command);
    }

    /**
     * public static void main(String[] args)
     */
    @SuppressWarnings("unchecked")
    public <R> R exec(String name, String... args) {
        Command<?> command = commands.get(name);
        if (command == null) {
            throw new IllegalArgumentException("command " + name + " not found");
        }
        try {
            return (R) command.call(this, args);
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public String getScheme() {
        return vfs.getScheme();
    }

    @Override
    public File get(CharSequence uri) {
        return vfs.get(uri);
    }

    @Override
    public Optional<File> tryGet(CharSequence uri) {
        return vfs.tryGet(uri);
    }

    @Override
    public CompletableFuture<File> copyAsync(File source, File target) {
        return vfs.copyAsync(source, target);
    }

    @Override
    public CompletableFuture<File> moveAsync(File source, File target) {
        return vfs.moveAsync(source, target);
    }

    @Override
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return vfs.runAsync(runnable);
    }

    @Override
    public <T> CompletableFuture<T> runAsync(Supplier<T> runnable) {
        return vfs.runAsync(runnable);
    }

    @Override
    public synchronized void close() throws Exception {
        log.debug("close VirtualFileShell: {}", this);
        if (onClose != null) {
            onClose.run();
            onClose = null;
        }
    }

    public Map<String, Command<?>> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void install(String className) {
        try {
            install((Class) Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public <R> void install(Class<? extends Command<R>> commandType) {
        try {
            install(commandType.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    protected String install(FileManager fileManager, String... args) {
        StringJoiner buffer = new StringJoiner("\n");
        for (String className : args) {
            install(className);
            buffer.add(className);
        }
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "VFiSh(" + commands.size() + ")";
    }
}
