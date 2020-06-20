package cc.whohow.configuration;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 配置
 */
public interface Configuration<T> extends Supplier<T>, Consumer<T>, AutoCloseable {
    /**
     * 添加监听
     */
    void watch(Consumer<T> listener);

    /**
     * 移除监听
     */
    void unwatch(Consumer<T> listener);

    /**
     * 读取并添加监听
     */
    default void getAndWatch(Consumer<T> listener) {
        watch(listener);
        listener.accept(get());
    }
}
