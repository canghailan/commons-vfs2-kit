package cc.whohow.configuration.provider;

import cc.whohow.fs.util.ByteBufferReadableChannel;
import cc.whohow.fs.util.ByteBufferWritableChannel;
import org.apache.commons.vfs2.FileObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * XML配置文件
 */
public class XmlConfiguration extends AbstractFileBasedConfiguration<Document> {
    protected static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public XmlConfiguration(FileObject fileObject) {
        super(fileObject);
    }

    @Override
    protected ByteBuffer serialize(Document value) throws IOException {
        try (ByteBufferWritableChannel channel = new ByteBufferWritableChannel()) {
            TRANSFORMER_FACTORY.newTransformer().transform(new DOMSource(value), new StreamResult(channel));
            return channel.getByteBuffer();
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected Document deserialize(ByteBuffer bytes) throws IOException {
        try {
            return BUILDER_FACTORY.newDocumentBuilder().parse(new ByteBufferReadableChannel(bytes));
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
