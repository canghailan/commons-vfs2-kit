package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.File;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.util.FileTimes;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class FileAttributesWriter implements Closeable {
    private static final String NEW_LINE = "\n";
    private static final String SEPARATOR = "\t";
    private static final String PATH_KEY = "PK";
    protected final LongAdder count = new LongAdder();
    protected final StringBuilder buffer = new StringBuilder();
    protected final Writer writer;
    protected final File base;
    protected final List<String> attributeNames;
    protected long timestamp;
    protected String separator = SEPARATOR;
    protected String pathKey = PATH_KEY;

    public FileAttributesWriter(Writer writer, File base, List<String> attributeNames) {
        this.writer = writer;
        this.base = base;
        this.attributeNames = attributeNames;
        this.timestamp = System.currentTimeMillis();

        writeHeader();
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getPathKey() {
        return pathKey;
    }

    public void setPathKey(String pathKey) {
        this.pathKey = pathKey;
    }

    public synchronized void writeHeader() {
        try {
            writer.write("# " + ZonedDateTime.now() + "\n");
            writer.write("# " + base + "\n");
            buffer.setLength(0);
            for (String attributeName : attributeNames) {
                buffer.append(attributeName).append(separator);
            }
            buffer.append(pathKey);
            buffer.append(NEW_LINE);
            writer.write(buffer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void write(File file) {
        try {
            FileAttributes fileAttributes = file.readAttributes();

            buffer.setLength(0);
            for (String attributeName : attributeNames) {
                buffer.append(formatFileAttribute(fileAttributes, attributeName)).append(separator);
            }
            buffer.append(base.getPath().relativize(file.getPath()));
            buffer.append(NEW_LINE);

            count.increment();
            writer.write(buffer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String formatFileAttribute(FileAttributes fileAttributes, String attributeName) {
        switch (attributeName) {
            case FileAttributes.SIZE: {
                return Long.toString(fileAttributes.size());
            }
            case FileAttributes.LAST_MODIFIED_TIME: {
                return FileTimes.stringify(fileAttributes.lastModifiedTime());
            }
            case FileAttributes.LAST_ACCESS_TIME: {
                return FileTimes.stringify(fileAttributes.lastAccessTime());
            }
            case FileAttributes.CREATION_TIME: {
                return FileTimes.stringify(fileAttributes.creationTime());
            }
            default: {
                return fileAttributes.getAsString(attributeName).orElse("");
            }
        }
    }

    public long getCount() {
        return count.longValue();
    }

    @Override
    public void close() throws IOException {
        long t = System.currentTimeMillis() - timestamp;
        try {
            writer.write("# count: " + getCount() + "\n");
            writer.write("# time: " + Duration.ofMillis(t) + "\n");
            writer.flush();
        } finally {
            writer.close();
        }
    }
}
