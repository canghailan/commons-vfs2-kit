package cc.whohow.fs.shell.provider;

import cc.whohow.fs.*;
import cc.whohow.fs.shell.Command;
import cc.whohow.fs.shell.FileShell;
import cc.whohow.fs.util.ComposeRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VirtualFileShell implements FileShell {
    private static final Logger log = LogManager.getLogger(VirtualFileShell.class);
    protected final Map<String, Command<?>> commands = new ConcurrentHashMap<>();
    protected final VirtualFileSystem vfs;
    protected volatile Runnable onClose;

    public VirtualFileShell(VirtualFileSystem vfs) {
        this.vfs = vfs;
        this.install("INSTALL", this::install);
        this.install("FILE", this::file);
        this.install("LIST", this::list);
        this.install("TREE", this::tree);
        this.install("COPY", this::copy);
        this.install("MOVE", this::move);
        this.install("DELETE", this::delete);
        this.install("READ", this::read);
        this.install("WRITE", this::write);
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
    public File get(CharSequence uri) {
        return vfs.get(uri);
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
            install(commandType.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
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

    protected File file(FileManager fileManager, String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return fileManager.get(args[0]);
    }

    protected String list(FileManager fileManager, String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File file = fileManager.get(args[0]);
        try (DirectoryStream<? extends File> stream = file.newDirectoryStream()) {
            StringJoiner buffer = new StringJoiner("\n");
            for (File f : stream) {
                buffer.add(f.getPublicUri());
            }
            return buffer.toString();
        } catch (IOException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    protected String tree(FileManager fileManager, String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File file = fileManager.get(args[0]);
        try (FileStream<? extends File> stream = file.tree()) {
            StringJoiner buffer = new StringJoiner("\n");
            for (File f : stream) {
                buffer.add(f.getPublicUri());
            }
            return buffer.toString();
        } catch (IOException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    protected File copy(FileManager fileManager, String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File source = fileManager.get(args[0]);
        File target = fileManager.get(args[1]);
        return fileManager.copyAsync(source, target).join();
    }

    protected File move(FileManager fileManager, String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File source = fileManager.get(args[0]);
        File target = fileManager.get(args[1]);
        return fileManager.moveAsync(source, target).join();
    }

    protected String delete(FileManager fileManager, String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return Arrays.stream(args)
                .map(fileManager::get)
                .peek(File::delete)
                .map(File::getPublicUri)
                .collect(Collectors.joining("\n"));
    }

    protected String read(FileManager fileManager, String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File file = fileManager.get(args[0]);
        return file.readUtf8();
    }

    protected String write(FileManager fileManager, String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File file = fileManager.get(args[0]);
        String lines = String.join("\n", Arrays.asList(args).subList(1, args.length));
        file.writeUtf8(lines);
        return lines;
    }

    @Override
    public String toString() {
        return "VFiSh(" + commands.size() + ")";
    }
}
