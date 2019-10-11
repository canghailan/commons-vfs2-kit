package cc.whohow.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;

import java.io.UncheckedIOException;
import java.util.function.Supplier;

public class VirtualFileSystemOptions implements Supplier<FileSystemOptions> {
    private static final VirtualFileSystemConfigBuilder BUILDER = VirtualFileSystemConfigBuilder.getInstance();
    private final CloudFileObject configuration;

    public VirtualFileSystemOptions(CloudFileObject configuration) {
        this.configuration = configuration;
    }

    @Override
    public FileSystemOptions get() {
        try (CloudFileObjectList list = configuration.listRecursively()) {
            FileSystemOptions fileSystemOptions = new FileSystemOptions();
            for (CloudFileObject fileObject : list) {
                BUILDER.setParam(fileSystemOptions,
                        fileObject.getName().getPathDecoded(), FileObjects.readUtf8(fileObject));
            }
            return fileSystemOptions;
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
