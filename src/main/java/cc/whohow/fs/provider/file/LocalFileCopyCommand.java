package cc.whohow.fs.provider.file;

import cc.whohow.fs.File;
import cc.whohow.fs.FileCopyCommand;
import cc.whohow.fs.Path;
import cc.whohow.fs.VirtualFileSystem;

/**
 * TODO
 */
public class LocalFileCopyCommand implements FileCopyCommand {
    @Override
    public int getMatchingScore() {
        return 0;
    }

    @Override
    public VirtualFileSystem getVirtualFileSystem() {
        return null;
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public File<? extends Path, ? extends File<?, ?>> call() throws Exception {
        return null;
    }
}
