package cc.whohow.configuration;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Configuration<T> extends Supplier<T>, Consumer<T>, AutoCloseable {
    void watch(Consumer<T> callback);

    void unwatch(Consumer<T> callback);

    void getAndWatch(Consumer<T> callback);

    @Override
    void close();
}
