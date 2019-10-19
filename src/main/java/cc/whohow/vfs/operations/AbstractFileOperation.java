package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileOperationX;

public abstract class AbstractFileOperation<T, R> implements FileOperationX<T, R> {
    protected T options;

    @Override
    public FileOperationX<T, R> with(T options) {
        this.options = options;
        return this;
    }

    @Override
    public T getOptions() {
        return options;
    }
}
