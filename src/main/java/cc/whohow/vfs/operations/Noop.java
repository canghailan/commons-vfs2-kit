package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileOperationX;

public class Noop<T, R> implements FileOperationX<T, R> {
    private static Noop INSTANCE = new Noop();

    @SuppressWarnings("unchecked")
    public static <T, R> Noop<T, R> get() {
        return INSTANCE;
    }

    @Override
    public FileOperationX<T, R> with(T options) {
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
