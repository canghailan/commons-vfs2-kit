package cc.whohow.vfs.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Value<T> extends Supplier<T>, Consumer<T> {
    void watch(Consumer<T> callback);

    void unwatch(Consumer<T> callback);

    default void getAndWatch(Consumer<T> callback) {
        watch(callback);
        callback.accept(get());
    }
}
