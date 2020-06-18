package cc.whohow.fs.provider;

import cc.whohow.fs.Copy;
import cc.whohow.fs.File;
import cc.whohow.fs.FileSystemProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class ProviderCopy<F extends File<?, F>> implements Copy<F, F> {
    private static final Logger log = LogManager.getLogger(ProviderCopy.class);
    protected final FileSystemProvider<?, F> provider;
    protected final F source;
    protected final F target;

    public ProviderCopy(FileSystemProvider<?, F> provider, F source, F target) {
        this.provider = provider;
        this.source = source;
        this.target = target;
    }

    @Override
    public F getSource() {
        return source;
    }

    @Override
    public F getTarget() {
        return target;
    }

    @Override
    public CompletableFuture<F> get() {
        log.trace("copy {} -> {}", source, target);
        return provider.copyAsync(source, target);
    }

    @Override
    public String toString() {
        return "copy " + getSource() + " " + getTarget();
    }
}
