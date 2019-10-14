package cc.whohow.vfs;

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
        try (DirectoryStream<CloudFileObject> list = configuration.listRecursively()) {
            FileSystemOptions fileSystemOptions = new FileSystemOptions();
            for (CloudFileObject fileObject : list) {
                BUILDER.setParam(fileSystemOptions,
                        fileObject.getName().getPathDecoded(), FileObjects.readUtf8(fileObject));
            }
            return fileSystemOptions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
