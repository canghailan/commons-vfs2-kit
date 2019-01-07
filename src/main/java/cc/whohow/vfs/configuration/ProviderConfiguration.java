package cc.whohow.vfs.configuration;

import java.util.List;

public class ProviderConfiguration {
    private String className;
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
