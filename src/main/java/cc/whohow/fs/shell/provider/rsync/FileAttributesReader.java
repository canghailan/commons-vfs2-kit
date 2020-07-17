package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.attribute.FileAttributesMap;
import cc.whohow.fs.attribute.StringAttribute;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileAttributesReader implements Closeable {
    private static final Pattern SEPARATOR = Pattern.compile("\t");
    protected final BufferedReader reader;
    protected final Pattern separator;
    protected List<String> attributeNames;

    public FileAttributesReader(BufferedReader reader) {
        this(reader, SEPARATOR);
    }

    public FileAttributesReader(BufferedReader reader, Pattern separator) {
        this.reader = reader;
        this.separator = separator;
    }

    public synchronized Stream<FileAttributes> read() {
        try {
            while (true) {
                String header = reader.readLine();
                if (header == null) {
                    return Stream.empty();
                }
                if (isEmptyOrComment(header)) {
                    continue;
                }
                attributeNames = Arrays.asList(separator.split(header));
                break;
            }

            return reader.lines()
                    .filter(line -> !isEmptyOrComment(line))
                    .map(this::readFileAttributes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected boolean isEmptyOrComment(String line) {
        return line.isEmpty() || line.startsWith("#");
    }

    protected FileAttributes readFileAttributes(String line) {
        String[] attributes = separator.split(line);
        if (attributes.length != attributeNames.size()) {
            throw new IllegalStateException(attributeNames + ": " + line);
        }
        Map<String, Attribute<?>> attributeMap = new LinkedHashMap<>(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String attributeName = attributeNames.get(i);
            attributeMap.put(attributeName, new StringAttribute(attributeName, attributes[i]));
        }
        return new FileAttributesMap(attributeMap);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
