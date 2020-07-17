package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.util.UncheckedCloseable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;

public class FileDiffReader implements Closeable {
    protected final BufferedReader reader;

    public FileDiffReader(BufferedReader reader) {
        this.reader = reader;
    }

    public Stream<FileDiff> read() {
        return reader.lines()
                .filter(line -> !isEmptyOrComment(line))
                .map(this::readFileDiff)
                .onClose(new UncheckedCloseable(this));
    }

    protected boolean isEmptyOrComment(String line) {
        return line.isEmpty() || line.startsWith("#");
    }

    protected FileDiff readFileDiff(String line) {
        return new FileDiff(line);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
