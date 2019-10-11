package cc.whohow.vfs.operations;

import cc.whohow.vfs.CloudFileOperation;

public abstract class AbstractFileOperation<T, R> implements CloudFileOperation<T, R> {
    protected T options;

    @Override
    public CloudFileOperation<T, R> with(T options) {
        this.options = options;
        return this;
    }

    @Override
    public T getOptions() {
        return options;
    }
}
