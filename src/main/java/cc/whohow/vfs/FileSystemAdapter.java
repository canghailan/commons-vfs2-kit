package cc.whohow.vfs;

import cc.whohow.fs.Attribute;
import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.VfsComponent;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.io.File;
import java.net.URI;

public class FileSystemAdapter implements FileSystem, VfsComponent {
    protected final cc.whohow.fs.FileSystem<?, ?> fileSystem;
    protected volatile VfsComponentContext context;

    public FileSystemAdapter(cc.whohow.fs.FileSystem<?, ?> fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public FileObjectAdapter getRoot() throws FileSystemException {
        return new FileObjectAdapter(getRootName());
    }

    @Override
    public FilePath getRootName() {
        return new FilePath(this, fileSystem.get(fileSystem.getUri()));
    }

    @Override
    public String getRootURI() {
        return fileSystem.getUri().toString();
    }

    @Override
    public boolean hasCapability(Capability capability) {
        switch (capability) {
            case READ_CONTENT:
            case WRITE_CONTENT:
            case ATTRIBUTES:
            case LAST_MODIFIED:
            case GET_LAST_MODIFIED:
            case CREATE:
            case DELETE:
            case RENAME:
            case GET_TYPE:
            case LIST_CHILDREN:
            case URI:
            case FS_ATTRIBUTES: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    public FileObject getParentLayer() throws FileSystemException {
        // layered file system not supported
        return null;
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return fileSystem.readAttributes()
                .getValue(attrName)
                .orElse(null);
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
    }

    @Override
    public FileObject resolveFile(FileName name) throws FileSystemException {
        return new FileObjectAdapter(new FilePath(this, fileSystem.get(URI.create(name.getURI()))));
    }

    @Override
    public FileObject resolveFile(String name) throws FileSystemException {
        return new FileObjectAdapter(new FilePath(this, fileSystem.get(URI.create(name))));
    }

    @Override
    public void addListener(FileObject file, FileListener listener) {
        fileSystem.get(URI.create(file.getName().getURI())).watch(new FileListenerAdapter<>(this, listener));
    }

    @Override
    public void removeListener(FileObject file, FileListener listener) {
        fileSystem.get(URI.create(file.getName().getURI())).unwatch(new FileListenerAdapter<>(this, listener));
    }

    @Override
    public void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException {
        throw new FileSystemException("vfs.provider/junctions-not-supported.error", this);
    }

    @Override
    public void removeJunction(String junctionPoint) throws FileSystemException {
        throw new FileSystemException("vfs.provider/junctions-not-supported.error", this);
    }

    @Override
    public File replicateFile(FileObject file, FileSelector selector) throws FileSystemException {
        return context.getReplicator().replicateFile(file, selector);
    }

    @Override
    public FileSystemOptions getFileSystemOptions() {
        FileSystemOptions fileSystemOptions = new FileSystemOptions();
        FileSystemAdapterConfigBuilder fileSystemAdapterConfigBuilder = new FileSystemAdapterConfigBuilder();
        for (Attribute<?> attribute : fileSystem.readAttributes()) {
            fileSystemAdapterConfigBuilder.setParam(fileSystemOptions, attribute.name(), attribute.value());
        }
        return fileSystemOptions;
    }

    @Override
    public FileSystemManager getFileSystemManager() {
        return context.getFileSystemManager();
    }

    @Override
    public double getLastModTimeAccuracy() {
        return 0;
    }

    @Override
    public void setLogger(Log logger) {

    }

    @Override
    public void setContext(VfsComponentContext context) {
        this.context = context;
    }

    @Override
    public void init() throws FileSystemException {

    }

    @Override
    public void close() {
        try {
            fileSystem.close();
        } catch (Exception e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }
}
