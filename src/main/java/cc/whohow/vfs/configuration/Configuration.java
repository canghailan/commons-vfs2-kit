package cc.whohow.vfs.configuration;

import java.lang.reflect.Type;
import java.util.Map;

public interface Configuration {
    Map<String, String> getJunctions();

    <T> T getProviderConfigurations(Type type);

    <T> T getProviderConfiguration(String name, Type type);

    <T> T getOperationProviderConfigurations(Type type);

    <T> T getOperationProviderConfiguration(String name, Type type);
}
