package cc.whohow.vfs.synchronize;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileOperation;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.diff.Diff;
import cc.whohow.vfs.diff.MapDiffs;
import cc.whohow.vfs.io.AppendableConsumer;
import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.provider.s3.S3FileVersionProvider;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionView;
import cc.whohow.vfs.version.FileVersionViewWriter;
import org.apache.commons.vfs2.FileSystemException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FileSync implements Supplier<Stream<Diff<String>>>, Function<Diff<String>, CloudFileOperation<?, ?>>, Consumer<Diff<String>>, BiConsumer<Diff<String>, Executor>, Runnable {
    private VirtualFileSystem vfs;
    private CloudFileObject context;
    private CloudFileObject source;
    private CloudFileObject target;

    public FileSync(VirtualFileSystem vfs, String context, String source, String target) throws FileSystemException {
        this.vfs = vfs;
        this.context = vfs.resolveFile(context);
        this.source = vfs.resolveFile(source);
        this.target = vfs.resolveFile(target);
    }

    public CloudFileOperation<?, ?> copy(String path) {
        try {
            return vfs.getCopyOperation(new Copy.Options(source.resolveFile(path), target.resolveFile(path)));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CloudFileOperation<?, ?> delete(String path) {
        try {
            return vfs.getRemoveOperation(target.resolveFile(path));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public CloudFileOperation<?, ?> apply(Diff<String> diff) {
        switch (diff.getType()) {
            case Diff.EQ: {
                return null;
            }
            case Diff.ADD:
            case Diff.UPDATE: {
                return copy(diff.getKey());
            }
            case Diff.DELETE: {
                return delete(diff.getKey());
            }
            default: {
                throw new IllegalArgumentException(diff.toString());
            }
        }
    }

    @Override
    public void accept(Diff<String> diff) {
        CloudFileOperation<?, ?> o = apply(diff);
        if (o != null) {
            o.call();
        }
    }

    @Override
    public void accept(Diff<String> diff, Executor executor) {
        CloudFileOperation<?, ?> o = apply(diff);
        if (o != null) {
            o.call(executor).join();
        }
    }

    @Override
    public Stream<Diff<String>> get() {
        try {
            context.deleteAll();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
        try (Stream<FileVersion<String>> versions = new S3FileVersionProvider().getVersions(source)) {
            try (FileVersionViewWriter writer = new FileVersionViewWriter(context.resolveFile("new.txt").getOutputStream(), source.getName().getURI())) {
                versions.map(FileVersionView::of)
                        .forEach(writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (Stream<FileVersion<String>> versions = new S3FileVersionProvider().getVersions(target)) {
            try (FileVersionViewWriter writer = new FileVersionViewWriter(context.resolveFile("old.txt").getOutputStream(), target.getName().getURI())) {
                versions.map(FileVersionView::of)
                        .forEach(writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (Stream<FileVersionView> newList = new BufferedReader(new InputStreamReader(context.resolveFile("new.txt").getInputStream())).lines().map(FileVersionView::parse);
             Stream<FileVersionView> oldList = new BufferedReader(new InputStreamReader(context.resolveFile("old.txt").getInputStream())).lines().map(FileVersionView::parse)) {
            try (Stream<Diff<String>> diff = new MapDiffs<>(
                    FileVersionView::getName,
                    FileVersionView::getVersion,
                    String::equalsIgnoreCase,
                    new LinkedHashMap<>(),
                    newList.iterator(),
                    oldList.iterator()).stream()) {
                try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(context.resolveFile("diff.txt").getOutputStream(), 2 * 1024 * 1024), StandardCharsets.UTF_8)) {
                    diff.map(Diff::toString)
                            .forEach(new AppendableConsumer(writer, "", "\n"));
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            return new BufferedReader(new InputStreamReader(context.resolveFile("diff.txt").getInputStream())).lines()
                    .map(Diff::parse);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void run() {
        try (Writer log = new OutputStreamWriter(new BufferedOutputStream(context.resolveFile("log.txt").getOutputStream(), 2 * 1024 * 1024), StandardCharsets.UTF_8)) {
            log.write("source: ");
            log.write(source.getName().getURI());
            log.write("\n");
            log.write("target: ");
            log.write(target.getName().getURI());
            log.write("\n\n\n");

            try (Stream<Diff<String>> diff = get()) {
                diff.filter(Diff::isNotEq)
                        .peek(this)
                        .map(Diff::toString)
                        .forEach(new AppendableConsumer(log, "", "\n"));

                log.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
