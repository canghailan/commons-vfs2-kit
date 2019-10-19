package cc.whohow.vfs.configuration;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.serialize.TextSerializer;
import org.apache.commons.vfs2.FileSystemOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.util.function.Supplier;

public class VirtualFileSystemOptions implements Supplier<FileSystemOptions> {
    private static final VirtualFileSystemConfigBuilder BUILDER = VirtualFileSystemConfigBuilder.getInstance();
    private final FileObjectX configuration;

    public VirtualFileSystemOptions(FileObjectX configuration) {
        this.configuration = configuration;
    }

    @Override
    public FileSystemOptions get() {
        try (DirectoryStream<FileObjectX> list = configuration.listRecursively()) {
            FileSystemOptions fileSystemOptions = new FileSystemOptions();
            for (FileObjectX fileObject : list) {
                BUILDER.setParam(fileSystemOptions,
                        fileObject.getName().getPathDecoded(), TextSerializer.utf8().deserialize(fileObject));
            }
            return fileSystemOptions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
