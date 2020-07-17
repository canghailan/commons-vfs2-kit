package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.ConfigurationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheableConfiguration<T> extends AbstractConfiguration<T> implements ConfigurationListener<T> {
    private static final Logger log = LogManager.getLogger(CacheableConfiguration.class);
    protected final Configuration<T> configuration;
    protected volatile T cached;

    public CacheableConfiguration(Configuration<T> configuration) {
        this.configuration = configuration;
        this.configuration.watch(this);
    }

    public T get() {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = configuration.get();
                }
            }
        }
        return cached;
    }

    public synchronized void set(T value) {
        try {
            configuration.set(value);
        } finally {
            cached = null;
        }
    }

    @Override
    public synchronized void onChange(T value) {
        cached = value;
        notify(cached);
    }

    @Override
    public void close() throws Exception {
        try {
            configuration.unwatch(this);
        } finally {
            configuration.close();
        }
        if (!listeners.isEmpty()) {
            log.warn("close with listeners({})", listeners.size());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CacheableConfiguration) {
            CacheableConfiguration<?> that = (CacheableConfiguration<?>) o;
            return configuration.equals(that.configuration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return configuration.hashCode();
    }

    @Override
    public String toString() {
        return "CacheableConfiguration(" + configuration + ")";
    }
}
