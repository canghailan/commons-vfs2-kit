package cc.whohow.fs.shell.provider.gzip;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.shell.Command;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

public class Gunzip implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File gzip = fileManager.get(args[0]);
        File target = fileManager.get(args[1]);
        return call(gzip, target);
    }

    public String call(File gzip, File target) {
        Objects.requireNonNull(gzip);
        Objects.requireNonNull(target);
        if (!gzip.isRegularFile()) {
            throw new IllegalArgumentException(gzip.toString());
        }
        if (!target.isRegularFile()) {
            throw new IllegalArgumentException(target.toString());
        }
        try (GZIPInputStream input = new GZIPInputStream(gzip.newReadableChannel().stream())) {
            try (FileWritableChannel output = target.newWritableChannel()) {
                output.transferFrom(input);
                return gzip.toString();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
