package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.file.FileBasedXmlConfiguration;
import org.w3c.dom.Document;

import java.nio.ByteBuffer;

/**
 * XML配置文件
 */
public class XmlConfiguration extends CacheableConfiguration<Document> {
    public XmlConfiguration(Configuration<ByteBuffer> source) {
        super(new FileBasedXmlConfiguration(source));
    }
}
