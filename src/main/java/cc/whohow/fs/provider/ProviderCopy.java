package cc.whohow.fs.provider;

import cc.whohow.fs.Copy;
import cc.whohow.fs.File;
import cc.whohow.fs.FileSystemProvider;

public class ProviderCopy<F extends File<?, F>> implements Copy<F, F> {
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
    public F call() throws Exception {
        return provider.copyAsync(source, target).join();
    }

    @Override
    public String toString() {
        return "copy " + getSource() + " " + getTarget();
    }
}
