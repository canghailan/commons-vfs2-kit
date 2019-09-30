package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileOperation;

public abstract class AbstractFileOperation<T, R> implements FileOperation<T, R> {
    protected T options;

    @Override
    public FileOperation<T, R> with(T options) {
        this.options = options;
        return this;
    }

    @Override
    public T getOptions() {
        return options;
    }
}
