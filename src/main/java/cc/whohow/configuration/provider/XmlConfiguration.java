package cc.whohow.configuration.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileValue;
import cc.whohow.vfs.type.XmlType;
import org.w3c.dom.Document;

public class XmlConfiguration extends FileValue.Cache<Document> {
    public XmlConfiguration(FileObject fileObject) {
        super(fileObject, XmlType.get());
    }
}
