package cc.whohow.fs.shell.provider.zip;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.shell.Command;
import cc.whohow.fs.util.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File zip = fileManager.get(args[0]);
        File source = fileManager.get(args[1]);
        return call(zip, source);
    }

    public String call(File zip, File source) {
        Objects.requireNonNull(zip);
        Objects.requireNonNull(source);
        if (!zip.isRegularFile()) {
            throw new IllegalArgumentException(zip.toString());
        }
        String base = source.isDirectory() ? source.getName() + "/" : source.getName();
        try (FileStream<? extends File> tree = source.tree()) {
            try (ZipOutputStream stream = new ZipOutputStream(zip.newWritableChannel().stream())) {
                StringJoiner files = new StringJoiner("\n");
                for (File file : tree) {
                    if (file.isRegularFile()) {
                        String name = base + source.getPath().relativize(file.getPath());
                        ZipEntry zipEntry = new ZipEntry(name);
                        stream.putNextEntry(zipEntry);
                        try (InputStream input = file.newReadableChannel().stream()) {
                            IO.copy(input, stream);
                        }
                        files.add(name);
                    }
                }
                return files.toString();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
