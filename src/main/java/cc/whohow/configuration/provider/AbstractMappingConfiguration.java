package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.ConfigurationListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractMappingConfiguration<S, T> extends AbstractConfiguration<T> implements ConfigurationListener<S> {
    private static final Logger log = LogManager.getLogger(AbstractMappingConfiguration.class);
    protected final Configuration<S> source;

    public AbstractMappingConfiguration(Configuration<S> source) {
        this.source = source;
        this.source.watch(this);
    }

    @Override
    public void close() throws Exception {
        try {
            source.unwatch(this);
        } finally {
            source.close();
        }
        if (!listeners.isEmpty()) {
            log.warn("close with listeners({})", listeners.size());
        }
    }

    @Override
    public synchronized void onChange(S s) {
        if (s == null) {
            notify(null);
        } else {
            notify(toTarget(s));
        }
    }

    @Override
    public T get() {
        return toTarget(source.get());
    }

    @Override
    public void set(T value) {
        source.set(toSource(value));
    }

    protected abstract T toTarget(S s);

    protected abstract S toSource(T t);

    @Override
    public String toString() {
        return source.toString();
    }
}
