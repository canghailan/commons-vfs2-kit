package cc.whohow.configuration;

import java.util.function.Supplier;

/**
 * 配置
 */
public interface Configuration<T> extends Supplier<T>, AutoCloseable {
    /**
     * 修改配置
     */
    void set(T value);

    /**
     * 添加监听
     */
    void watch(ConfigurationListener<T> listener);

    /**
     * 移除监听
     */
    void unwatch(ConfigurationListener<T> listener);

    /**
     * 读取并添加监听
     */
    default void getAndWatch(ConfigurationListener<T> listener) {
        watch(listener);
        listener.onChange(get());
    }
}
