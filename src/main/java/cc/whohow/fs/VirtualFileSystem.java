package cc.whohow.fs;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public interface VirtualFileSystem extends AutoCloseable {
    File<?, ?> getContext();

    ExecutorService getExecutor();

    ScheduledExecutorService getScheduledExecutor();

    Map<String, String> getVfsConfiguration();

    Collection<Provider> getProviders();

    Collection<FileSystem<?, ?>> getFileSystems();

    File<?, ?> get(String uri);

    void load(Provider provider);

    void load(Provider provider, File<?, ?> configuration);

    void mount(String uri, FileResolver<?, ?> fileResolver);

    void umount(String uri);

    void registerFileSystem(FileSystem<?, ?> fileSystem);

    void unregisterFileSystem(FileSystem<?, ?> fileSystem);

    void installCommand(String name, FileCommandBuilder<? extends FileCommand<?>> commandBuilder);

    void installCopyCommand(FileCommandBuilder<? extends FileCopyCommand> commandBuilder);

    void installMoveCommand(FileCommandBuilder<? extends FileMoveCommand> commandBuilder);

    FileCommand<?> newCommand(String... arguments);

    /**
     * copy [source] [destination]
     */
    FileCopyCommand newCopyCommand(String source, String destination);

    /**
     * move [source] [destination]
     */
    FileMoveCommand newMoveCommand(String source, String destination);
}
