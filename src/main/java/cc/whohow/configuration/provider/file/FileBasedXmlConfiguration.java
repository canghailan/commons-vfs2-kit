package cc.whohow.configuration.provider.file;

import cc.whohow.configuration.Configuration;
import cc.whohow.configuration.provider.AbstractMappingConfiguration;
import cc.whohow.fs.util.ByteBufferReadableChannel;
import cc.whohow.fs.util.ByteBufferWritableChannel;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * XML配置文件
 */
public class FileBasedXmlConfiguration extends AbstractMappingConfiguration<ByteBuffer, Document> {
    protected static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public FileBasedXmlConfiguration(Configuration<ByteBuffer> source) {
        super(source);
    }

    @Override
    protected ByteBuffer toSource(Document document) {
        try (ByteBufferWritableChannel channel = new ByteBufferWritableChannel()) {
            TRANSFORMER_FACTORY.newTransformer().transform(new DOMSource(document), new StreamResult(channel));
            return channel.getByteBuffer();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected Document toTarget(ByteBuffer byteBuffer) {
        try {
            return BUILDER_FACTORY.newDocumentBuilder().parse(new ByteBufferReadableChannel(byteBuffer));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
