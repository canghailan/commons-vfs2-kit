package cc.whohow.fs.command;

import cc.whohow.fs.File;
import cc.whohow.fs.FileMoveCommand;
import cc.whohow.fs.VirtualFileSystem;

public class DefaultFileMoveCommand implements FileMoveCommand {
    private final VirtualFileSystem vfs;
    private final String[] arguments;

    public DefaultFileMoveCommand(VirtualFileSystem vfs, String... arguments) {
        this.vfs = vfs;
        this.arguments = arguments;
    }

    @Override
    public int getMatchingScore() {
        return MatchingScore.LOW;
    }

    @Override
    public VirtualFileSystem getVirtualFileSystem() {
        return vfs;
    }

    @Override
    public String[] getArguments() {
        return arguments;
    }

    @Override
    public File<?, ?> call() throws Exception {
        File<?, ?> file = vfs.newCopyCommand(arguments[1], arguments[2]).call();
        getSource().delete();
        return file;
    }
}
