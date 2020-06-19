package cc.whohow.fs.command.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystem;

import java.util.concurrent.Callable;

public class Move implements Callable<File<?, ?>> {
    protected final VirtualFileSystem vfs;
    protected final File<?, ?> source;
    protected final File<?, ?> target;

    public Move(VirtualFileSystem vfs, String... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        this.vfs = vfs;
        this.source = vfs.get(args[1]);
        this.target = vfs.get(args[2]);
    }

    public Move(VirtualFileSystem vfs, File<?, ?> source, File<?, ?> target) {
        this.vfs = vfs;
        this.source = source;
        this.target = target;
    }

    @Override
    public File<?, ?> call() throws Exception {
        return vfs.moveAsync(source, target).join();
    }
}
