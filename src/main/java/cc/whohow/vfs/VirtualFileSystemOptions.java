package cc.whohow.vfs;

import cc.whohow.vfs.type.TextType;
import org.apache.commons.vfs2.FileSystemOptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.util.function.Supplier;

public class VirtualFileSystemOptions implements Supplier<FileSystemOptions> {
    private static final VirtualFileSystemConfigBuilder BUILDER = VirtualFileSystemConfigBuilder.getInstance();
    private final CloudFileObject configuration;

    public VirtualFileSystemOptions(CloudFileObject configuration) {
        this.configuration = configuration;
    }

    @Override
    public FileSystemOptions get() {
        TextType utf8 = TextType.utf8();
        try (DirectoryStream<CloudFileObject> list = configuration.listRecursively()) {
            FileSystemOptions fileSystemOptions = new FileSystemOptions();
            for (CloudFileObject fileObject : list) {
                BUILDER.setParam(fileSystemOptions,
                        fileObject.getName().getPathDecoded(), new FileValue<>(fileObject, utf8).get());
            }
            return fileSystemOptions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
