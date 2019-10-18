package cc.whohow.vfs.provider.s3;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileOperation;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.io.AppendableConsumer;
import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.version.FileVersion;
import cc.whohow.vfs.version.FileVersionView;
import cc.whohow.vfs.version.FileVersionViewWriter;
import cc.whohow.vfs.watch.FileDiffEntry;
import cc.whohow.vfs.watch.FileDiffIterator;
import cc.whohow.vfs.watch.FileDiffStatistics;
import org.apache.commons.vfs2.FileSystemException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class S3FileSync implements
        Supplier<Stream<FileDiffEntry<String>>>,
        Function<FileDiffEntry<String>, CloudFileOperation<?, ?>>, Consumer<FileDiffEntry<String>>, BiConsumer<FileDiffEntry<String>, Executor>,
        Callable<FileDiffStatistics> {
    private VirtualFileSystem vfs;
    private CloudFileObject context;
    private CloudFileObject source;
    private CloudFileObject target;
    private boolean skipDelete = false;

    public S3FileSync(VirtualFileSystem vfs, String context, String source, String target) throws FileSystemException {
        this.vfs = vfs;
        this.context = vfs.resolveFile(context);
        this.source = vfs.resolveFile(source);
        this.target = vfs.resolveFile(target);
    }

    public boolean isSkipDelete() {
        return skipDelete;
    }

    public void setSkipDelete(boolean skipDelete) {
        this.skipDelete = skipDelete;
    }

    public CloudFileOperation<?, ?> copy(String path) {
        try {
            return vfs.getCopyOperation(new Copy.Options(source.resolveFile(path), target.resolveFile(path)));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CloudFileOperation<?, ?> delete(String path) {
        if (skipDelete) {
            return null;
        }
        try {
            return vfs.getRemoveOperation(target.resolveFile(path));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public CloudFileOperation<?, ?> apply(FileDiffEntry<String> diff) {
        switch (diff.getValue()) {
            case NOT_MODIFIED: {
                return null;
            }
            case CREATE:
            case MODIFY: {
                return copy(diff.getKey());
            }
            case DELETE: {
                return delete(diff.getKey());
            }
            default: {
                throw new IllegalArgumentException(diff.toString());
            }
        }
    }

    @Override
    public void accept(FileDiffEntry<String> diff) {
        CloudFileOperation<?, ?> o = apply(diff);
        if (o != null) {
            o.call();
        }
    }

    @Override
    public void accept(FileDiffEntry<String> diff, Executor executor) {
        CloudFileOperation<?, ?> o = apply(diff);
        if (o != null) {
            o.call(executor).join();
        }
    }

    @Override
    public Stream<FileDiffEntry<String>> get() {
        try {
            context.deleteAll();
            try (FileVersionViewWriter writer = new FileVersionViewWriter(context.resolveFile("new.txt").getOutputStream(), source.getName().getURI())) {
                try (Stream<FileVersion<String>> versions = new S3FileVersionProvider().getVersions(source)) {
                    versions.map(FileVersionView::of)
                            .forEach(writer);
                    writer.flush();
                }
            }
            try (FileVersionViewWriter writer = new FileVersionViewWriter(context.resolveFile("old.txt").getOutputStream(), target.getName().getURI())) {
                try (Stream<FileVersion<String>> versions = new S3FileVersionProvider().getVersions(target)) {
                    versions.map(FileVersionView::of)
                            .forEach(writer);
                    writer.flush();
                }
            }
            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(context.resolveFile("diff.txt").getOutputStream(), 2 * 1024 * 1024), StandardCharsets.UTF_8)) {
                try (Stream<FileVersionView> newList = new BufferedReader(new InputStreamReader(context.resolveFile("new.txt").getInputStream())).lines().map(FileVersionView::parse);
                     Stream<FileVersionView> oldList = new BufferedReader(new InputStreamReader(context.resolveFile("old.txt").getInputStream())).lines().map(FileVersionView::parse)) {
                    new FileDiffIterator<>(
                            FileVersionView::getName,
                            FileVersionView::getVersion,
                            String::equalsIgnoreCase,
                            new LinkedHashMap<>(),
                            newList.iterator(),
                            oldList.iterator()).stream()
                            .map(FileDiffEntry::toString)
                            .forEach(new AppendableConsumer(writer, "", "\n"));
                    writer.flush();
                }
            }
            return new BufferedReader(new InputStreamReader(context.resolveFile("diff.txt").getInputStream())).lines()
                    .map(FileDiffEntry::parse);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileDiffStatistics call() {
        try (Writer log = new OutputStreamWriter(new BufferedOutputStream(context.resolveFile("log.txt").getOutputStream(), 2 * 1024 * 1024), StandardCharsets.UTF_8)) {
            log.write("time: ");
            log.write(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
            log.write("\n");
            log.write("source: ");
            log.write(source.getName().getURI());
            log.write("\n");
            log.write("target: ");
            log.write(target.getName().getURI());
            log.write("\n\n\n");

            FileDiffStatistics statistics = new FileDiffStatistics();
            try (Stream<FileDiffEntry<String>> diff = get()) {
                diff.peek(statistics)
                        .filter(FileDiffEntry::isModified)
                        .peek(this)
                        .map(FileDiffEntry::toString)
                        .forEach(new AppendableConsumer(log, "", "\n"));
            }
            log.write("\n\n\n");

            log.write(statistics.toString());

            log.flush();

            return statistics;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
