package cc.whohow.configuration;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 基于文件的配置管理器
 */
public interface FileBasedConfigurationManager extends AutoCloseable {
    /**
     * 下级配置文件/文件夹列表
     */
    List<String> list(String key);

    /**
     * 获取配置文件
     */
    Configuration<ByteBuffer> get(String key);
}
