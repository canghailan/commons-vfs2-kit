package cc.whohow.configuration.provider;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.XmlType;
import org.w3c.dom.Document;

public class XmlConfiguration extends FileValue.Cache<Document> {
    public XmlConfiguration(CloudFileObject fileObject) {
        super(fileObject, XmlType.get());
    }
}
