package cc.whohow.fs.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.FileMoveCommand;
import cc.whohow.fs.VirtualFileSystem;

public class DefaultFileMoveCommand implements FileMoveCommand {
    private final VirtualFileSystem virtualFileSystem;
    private final String[] arguments;

    public DefaultFileMoveCommand(VirtualFileSystem virtualFileSystem, String... arguments) {
        this.virtualFileSystem = virtualFileSystem;
        this.arguments = arguments;
    }

    @Override
    public int getMatchingScore() {
        return MatchingScore.LOW;
    }

    @Override
    public VirtualFileSystem getVirtualFileSystem() {
        return virtualFileSystem;
    }

    @Override
    public String[] getArguments() {
        return arguments;
    }

    @Override
    public File<?, ?> call() throws Exception {
        File<?, ?> file = virtualFileSystem.newCopyCommand(arguments[1], arguments[2]).call();
        getSource().delete();
        return file;
    }
}
