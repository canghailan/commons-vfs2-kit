package cc.whohow.fs.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class FileCopy extends AbstractCopy<File, File> {
    public FileCopy(File source, File target) {
        super(source, target);
    }

    @Override
    protected FileStream<? extends File> tree(File directory) {
        return directory.tree();
    }

    @Override
    protected File resolve(File directory, String relative) {
        return directory.resolve(relative);
    }

    @Override
    protected CompletableFuture<File> copyFileAsync(File source, File target, ExecutorService executor) {
        return new FileCopy(source, target).copyFileAsync(executor);
    }

    @Override
    public String toString() {
        return "FileCopy " + source + " " + target;
    }
}
