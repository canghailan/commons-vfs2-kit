package cc.whohow.vfs.operations.provider;

import cc.whohow.vfs.CloudFileOperation;

public class Noop<T, R> implements CloudFileOperation<T, R> {
    private static Noop INSTANCE = new Noop();

    @SuppressWarnings("unchecked")
    public static <T, R> Noop<T, R> get() {
        return INSTANCE;
    }

    @Override
    public CloudFileOperation<T, R> with(T options) {
        return this;
    }

    @Override
    public T getOptions() {
        return null;
    }

    @Override
    public R apply(T t) {
        return null;
    }
}
