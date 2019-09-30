package cc.whohow.configuration.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.PropertiesType;

import java.util.Properties;

public class PropertiesConfiguration extends FileValue.Cache<Properties> {
    public PropertiesConfiguration(FileObject fileObject) {
        super(fileObject, PropertiesType.get());
    }
}
