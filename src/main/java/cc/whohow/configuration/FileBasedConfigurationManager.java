package cc.whohow.configuration;

import org.apache.commons.vfs2.FileObject;

import java.util.List;

/**
 * 基于文件的配置管理器
 */
public interface FileBasedConfigurationManager {
    /**
     * 获取配置文件
     */
    FileObject get(String key);

    /**
     * 下级配置文件/文件夹列表
     */
    List<String> list(String key);
}
