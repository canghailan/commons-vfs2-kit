package cc.whohow.vfs.configuration;

import java.util.List;

/**
 * 服务基础配置
 */
public class ProviderConfiguration {
    /**
     * 类名
     */
    private String className;
    /**
     * 支持的scheme列表
     */
    private List<String> schemes;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<String> schemes) {
        this.schemes = schemes;
    }
}
