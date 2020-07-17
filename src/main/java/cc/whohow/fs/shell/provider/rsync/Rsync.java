package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.File;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;
import cc.whohow.fs.util.UncheckedCloseable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class Rsync implements Command<String> {
    private static final Logger log = LogManager.getLogger(Rsync.class);

    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        Args arguments = new Args();
        arguments.fileManager = fileManager;
        arguments.context = fileManager.get(getRsyncContextPath());
        arguments.source = fileManager.get(args[0]);
        arguments.target = fileManager.get(args[1]);
        arguments.sourceAttributes = Arrays.asList(FileAttributes.SIZE, FileAttributes.LAST_MODIFIED_TIME);
        arguments.targetAttributes = Arrays.asList(FileAttributes.SIZE, FileAttributes.LAST_MODIFIED_TIME);
        arguments.comparator = new LastModifiedTimeComparator();
        return call(arguments);
    }

    public String call(Args args) {
        log.trace("Rsync {} {}", args.source, args.target);

        // Step 1: stat source
        log.trace("save source.csv:{} {}", args.sourceAttributes, args.source);
        File sourceAttributes = args.context.resolve("source.csv");
        saveFileAttributes(args.source, args.sourceAttributes, sourceAttributes);
        log.trace("save source.csv OK: {}", sourceAttributes);

        // Step 2: stat target
        log.trace("save target.csv:{} {}", args.targetAttributes, args.target);
        File targetAttributes = args.context.resolve("target.csv");
        saveFileAttributes(args.target, args.targetAttributes, targetAttributes);
        log.trace("save target.csv OK: {}", sourceAttributes);

        // Step 3: diff
        log.trace("save diff.txt");
        File diff = args.context.resolve("diff.txt");
        try (Stream<FileAttributes> source = readFileAttributes(sourceAttributes);
             Stream<FileAttributes> target = readFileAttributes(targetAttributes)) {
            long t0 = System.currentTimeMillis();
            try (MapFileComparator comparator = new MapFileComparator.Builder()
                    .index(new HashMap<>())
                    .comparator(args.comparator)
                    .source(source.iterator())
                    .target(target.iterator())
                    .build()) {
                try (Writer writer = new OutputStreamWriter(
                        diff.newWritableChannel().stream(), StandardCharsets.UTF_8)) {
                    writer.write("# date: " + ZonedDateTime.now() + "\n");
                    writer.write("# source: " + args.source + "\n");
                    writer.write("# target: " + args.target + "\n");
                    while (comparator.hasNext()) {
                        FileDiff fileDiff = comparator.next();
                        writer.write(fileDiff + "\n");
                    }
                    long t = System.currentTimeMillis() - t0;
                    writer.write("# sum:\n");
                    writer.write("#   source: " + comparator.getSourceCount() + "\n");
                    writer.write("#   target: " + comparator.getTargetCount() + "\n");
                    writer.write("#   create: " + comparator.getCreateCount() + "\n");
                    writer.write("#   modify: " + comparator.getModifyCount() + "\n");
                    writer.write("#   delete: " + comparator.getDeleteCount() + "\n");
                    writer.write("#   notModified: " + comparator.getNotModifiedCount() + "\n");
                    writer.write("# time: " + Duration.ofMillis(t) + "\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        log.trace("save diff.txt OK: {}", diff);

        // Step 4: apply diff
        log.trace("apply diff.txt");
        File logger = args.context.resolve("rsync.log");
        try (Stream<FileDiff> stream = readFileDiff(diff)) {
            long t0 = System.currentTimeMillis();
            FileDiffExecutor executor = new FileDiffExecutor(args.fileManager, args.source, args.target);
            try (Writer writer = new OutputStreamWriter(
                    logger.newWritableChannel().stream(), StandardCharsets.UTF_8)) {
                writer.write("# date: " + ZonedDateTime.now() + "\n");
                writer.write("# source: " + args.source + "\n");
                writer.write("# target: " + args.target + "\n");
                Iterator<FileDiff> iterator = stream.iterator();
                while (iterator.hasNext()) {
                    FileDiff fileDiff = iterator.next();
                    if (executor.apply(fileDiff)) {
                        writer.write(fileDiff + "\n");
                    }
                }
                executor.future().join();
                long t = System.currentTimeMillis() - t0;
                writer.write("# sum:\n");
                writer.write("#   copy: " + executor.getCopyCount() + "\n");
                writer.write("#   delete: " + executor.getDeleteCount() + "\n");
                writer.write("# time: " + Duration.ofMillis(t) + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        log.trace("apply diff.txt OK: {}", diff);

        log.trace("Rsync OK: {} -> {} ({})", args.source, args.target, diff);
        return args.context.toString();
    }

    /**
     * 保存文件属性
     */
    protected void saveFileAttributes(File root, List<String> attributeNames, File writable) {
        try (Stream<? extends File> stream = root.tree().stream().filter(File::isRegularFile)) {
            try (FileAttributesWriter writer = new FileAttributesWriter(new OutputStreamWriter(
                    writable.newWritableChannel().stream(), StandardCharsets.UTF_8),
                    root, attributeNames)) {
                stream.forEach(writer::write);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 读取文件属性
     */
    protected Stream<FileAttributes> readFileAttributes(File file) {
        FileAttributesReader reader = new FileAttributesReader(new BufferedReader(new InputStreamReader(
                file.newReadableChannel().stream(), StandardCharsets.UTF_8)));
        return reader.read()
                .onClose(new UncheckedCloseable(reader));
    }

    /**
     * 读取文件变化
     */
    protected Stream<FileDiff> readFileDiff(File file) {
        FileDiffReader reader = new FileDiffReader(new BufferedReader(new InputStreamReader(
                file.newReadableChannel().stream(), StandardCharsets.UTF_8)));
        return reader.read()
                .onClose(new UncheckedCloseable(reader));
    }

    /**
     * 生成上下文文件夹
     */
    protected String getRsyncContextPath() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        return "/rsync/" + dateTime + "-" + UUID.randomUUID() + "/";
    }

    /**
     * 参数
     */
    public static class Args {
        private FileManager fileManager;
        private File context;
        private File source;
        private File target;
        private List<String> sourceAttributes;
        private List<String> targetAttributes;
        private Comparator<FileAttributes> comparator;

        public FileManager getFileManager() {
            return fileManager;
        }

        public void setFileManager(FileManager fileManager) {
            this.fileManager = fileManager;
        }

        public File getContext() {
            return context;
        }

        public void setContext(File context) {
            this.context = context;
        }

        public File getSource() {
            return source;
        }

        public void setSource(File source) {
            this.source = source;
        }

        public File getTarget() {
            return target;
        }

        public void setTarget(File target) {
            this.target = target;
        }

        public List<String> getSourceAttributes() {
            return sourceAttributes;
        }

        public void setSourceAttributes(List<String> sourceAttributes) {
            this.sourceAttributes = sourceAttributes;
        }

        public List<String> getTargetAttributes() {
            return targetAttributes;
        }

        public void setTargetAttributes(List<String> targetAttributes) {
            this.targetAttributes = targetAttributes;
        }

        public Comparator<FileAttributes> getComparator() {
            return comparator;
        }

        public void setComparator(Comparator<FileAttributes> comparator) {
            this.comparator = comparator;
        }
    }
}
