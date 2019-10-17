package cc.whohow.vfs.serialize;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XmlSerializer implements Serializer<Document> {
    private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final XmlSerializer INSTANCE = new XmlSerializer();

    public static Serializer<Document> get() {
        return INSTANCE;
    }

    @Override
    public Document deserialize(InputStream stream) throws IOException {
        try {
            return BUILDER_FACTORY.newDocumentBuilder().parse(stream);
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void serialize(OutputStream stream, Document value) throws IOException {
        try {
            TRANSFORMER_FACTORY.newTransformer().transform(new DOMSource(value), new StreamResult(stream));
        } catch (TransformerException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
