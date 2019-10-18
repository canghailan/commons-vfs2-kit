package cc.whohow.vfs.version;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class FileVersionViewWriter implements Consumer<FileVersionView>, Closeable {
    private final ZoneId zoneId = ZoneId.systemDefault();
    private final Writer writer;
    private final String prefix;

    public FileVersionViewWriter(OutputStream stream) {
        this(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
    }

    public FileVersionViewWriter(OutputStream stream, String prefix) {
        this(new OutputStreamWriter(stream, StandardCharsets.UTF_8), prefix);
    }

    public FileVersionViewWriter(Writer writer) {
        this(writer, "");
    }

    public FileVersionViewWriter(Writer writer, String prefix) {
        this.writer = writer;
        this.prefix = prefix;
    }

    public synchronized void write(FileVersionView fileVersionView) throws IOException {
        if (fileVersionView.getVersion() != null) {
            writer.append(fileVersionView.getVersion());
        }
        writer.append('\t');
        if (fileVersionView.getSize() >= 0) {
            writer.append(Long.toString(fileVersionView.getSize()));
        }
        writer.append('\t');
        if (fileVersionView.getLastModifiedTime() > 0) {
            writer.append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(fileVersionView.getLastModifiedTime()), zoneId)));
        }
        writer.append('\t');
        writer.append(fileVersionView.getName(), prefix.length(), fileVersionView.getName().length());
        writer.append('\n');
    }

    @Override
    public void accept(FileVersionView fileVersionView) {
        try {
            write(fileVersionView);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            writer.close();
        }
    }
}
