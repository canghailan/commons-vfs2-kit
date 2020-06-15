package cc.whohow.configuration;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Configuration<T> extends Supplier<T>, Consumer<T>, AutoCloseable {
    void watch(Consumer<T> listener);

    void unwatch(Consumer<T> listener);

    void getAndWatch(Consumer<T> listener);
}
