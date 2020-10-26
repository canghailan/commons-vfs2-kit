package cc.whohow.fs.shell.provider.zip;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.shell.Command;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File zip = fileManager.get(args[0]);
        File target = fileManager.get(args[1]);
        return call(zip, target);
    }

    public String call(File zip, File target) {
        Objects.requireNonNull(zip);
        Objects.requireNonNull(target);
        if (!zip.isRegularFile()) {
            throw new IllegalArgumentException(zip.toString());
        }
        if (!target.isDirectory()) {
            throw new IllegalArgumentException(target.toString());
        }
        try (ZipInputStream stream = new ZipInputStream(zip.newReadableChannel().stream())) {
            StringJoiner files = new StringJoiner("\n");
            while (true) {
                ZipEntry zipEntry = stream.getNextEntry();
                if (zipEntry == null) {
                    break;
                }
                if (zipEntry.isDirectory()) {
                    continue;
                }
                try (FileWritableChannel channel = target.resolve(zipEntry.getName()).newWritableChannel()) {
                    channel.transferFrom(new FilterInputStream(stream) {
                        @Override
                        public void close() throws IOException {
                            // ignore
                        }
                    });
                }
                files.add(zipEntry.getName());
                stream.closeEntry();
            }
            return files.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
