package cc.whohow.vfs.configuration;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 文件系统配置
 */
public interface Configuration {
    /**
     * 挂载点列表
     */
    Map<String, String> getJunctions();

    /**
     * 文件服务配置
     */
    <T> T getProviderConfigurations(Type type);

    /**
     * 文件服务配置
     */
    <T> T getProviderConfiguration(String name, Type type);

    /**
     * 文件操作服务配置
     */
    <T> T getOperationProviderConfigurations(Type type);

    /**
     * 文件操作服务配置
     */
    <T> T getOperationProviderConfiguration(String name, Type type);
}
