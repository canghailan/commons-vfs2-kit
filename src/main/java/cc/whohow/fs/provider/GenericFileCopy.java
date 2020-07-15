package cc.whohow.fs.provider;

import cc.whohow.fs.FileStream;
import cc.whohow.fs.GenericFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class GenericFileCopy<F1 extends GenericFile<?, F1>, F2 extends GenericFile<?, F2>> extends AbstractCopy<F1, F2> {
    public GenericFileCopy(F1 source, F2 target) {
        super(source, target);
    }

    @Override
    protected FileStream<? extends F1> tree(F1 directory) {
        return directory.tree();
    }

    @Override
    protected F2 resolve(F2 directory, String relative) {
        return directory.resolve(relative);
    }

    @Override
    protected CompletableFuture<F2> copyFileAsync(F1 source, F2 target, ExecutorService executor) {
        return new GenericFileCopy<>(source, target).copyFileAsync(executor);
    }
}
