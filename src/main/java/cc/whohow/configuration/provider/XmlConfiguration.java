package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.serialize.FileValue;
import cc.whohow.vfs.serialize.XmlSerializer;
import org.w3c.dom.Document;

public class XmlConfiguration extends FileValue.Cache<Document> implements Configuration<Document> {
    public XmlConfiguration(FileObjectX fileObject) {
        super(fileObject, XmlSerializer.get());
    }
}
