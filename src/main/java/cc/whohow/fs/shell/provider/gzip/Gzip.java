package cc.whohow.fs.shell.provider.gzip;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.FileReadableChannel;
import cc.whohow.fs.shell.Command;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

public class Gzip implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File source = fileManager.get(args[0]);
        File gzip = (args.length == 1) ?
                fileManager.get(args[0] + ".gz") :
                fileManager.get(args[1]);
        return call(source, gzip);
    }

    public String call(File source, File gzip) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(gzip);
        if (!source.isRegularFile()) {
            throw new IllegalArgumentException(source.toString());
        }
        if (!gzip.isRegularFile()) {
            throw new IllegalArgumentException(gzip.toString());
        }
        try (FileReadableChannel input = source.newReadableChannel()) {
            try (GZIPOutputStream output = new GZIPOutputStream(gzip.newWritableChannel().stream())) {
                input.transferTo(output);
                return source.toString();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
