package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.ConfigurationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractConfiguration<T> implements Configuration<T> {
    private static final Logger log = LogManager.getLogger(AbstractConfiguration.class);
    /**
     * 监听列表
     */
    protected final List<ConfigurationListener<T>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void watch(ConfigurationListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void unwatch(ConfigurationListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * 配置变化回调
     */
    protected synchronized void notify(T value) {
        for (ConfigurationListener<T> listener : listeners) {
            try {
                listener.onChange(value);
            } catch (Exception e) {
                log.warn("configuration notify error", e);
            }
        }
    }
}
