package cc.whohow.configuration;

@FunctionalInterface
public interface ConfigurationListener<T> {
    void onChange(T value);
}
