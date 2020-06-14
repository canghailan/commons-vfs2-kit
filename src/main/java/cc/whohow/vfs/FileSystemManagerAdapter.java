package cc.whohow.vfs;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.TemporaryFileStore;
import org.apache.commons.vfs2.provider.VfsComponent;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileSystemManagerAdapter implements FileSystemManager, VfsComponentContext {
    private static final Logger log = LogManager.getLogger(FileSystemManagerAdapter.class);
    protected final cc.whohow.fs.VirtualFileSystem vfs;
    protected final Map<String, List<FileOperationProvider>> fileOperationProvider = new ConcurrentHashMap<>();
    protected volatile DefaultFileReplicator defaultFileReplicator;
    protected volatile Log logger;

    public FileSystemManagerAdapter(cc.whohow.fs.VirtualFileSystem vfs) {
        this.vfs = vfs;
    }

    @Override
    public void addOperationProvider(String scheme, FileOperationProvider operationProvider) throws FileSystemException {
        fileOperationProvider.computeIfAbsent(scheme, (key) -> new CopyOnWriteArrayList<>())
                .add(operationProvider);
    }

    @Override
    public void addOperationProvider(String[] schemes, FileOperationProvider operationProvider) throws FileSystemException {
        for (String scheme : schemes) {
            addOperationProvider(scheme, operationProvider);
        }
    }

    @Override
    public boolean canCreateFileSystem(FileObject file) throws FileSystemException {
        // not supported
        return false;
    }

    @Override
    public void closeFileSystem(FileSystem fileSystem) {
        log.trace("closeFileSystem({})", fileSystem);
        if (fileSystem instanceof VfsComponent) {
            VfsComponent vfsComponent = (VfsComponent) fileSystem;
            vfsComponent.close();
        }
    }

    @Override
    public FileObject createFileSystem(FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createFileSystem(String provider, FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createVirtualFileSystem(FileObject rootFile) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createVirtualFileSystem(String rootUri) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject getBaseFile() throws FileSystemException {
        return new FileObjectAdapter(resolveURI("/"));
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return CacheStrategy.ON_CALL;
    }

    @Override
    public FileContentInfoFactory getFileContentInfoFactory() {
        return new FileAttributeContentInfoFactory();
    }

    @Override
    public Class<?> getFileObjectDecorator() {
        // no decorator
        return null;
    }

    @Override
    public Constructor<?> getFileObjectDecoratorConst() {
        // no decorator
        return null;
    }

    @Override
    public FilesCache getFilesCache() {
        return new NullFilesCache();
    }

    @Override
    public FileSystemConfigBuilder getFileSystemConfigBuilder(String scheme) throws FileSystemException {
        return new FileSystemAdapterConfigBuilder();
    }

    @Override
    public FileOperationProvider[] getOperationProviders(String scheme) throws FileSystemException {
        List<FileOperationProvider> list = fileOperationProvider.get(scheme);
        if (list == null) {
            return new FileOperationProvider[0];
        }
        return list.toArray(new FileOperationProvider[0]);
    }

    @Override
    public Collection<Capability> getProviderCapabilities(String scheme) throws FileSystemException {
        return Arrays.asList(
                Capability.READ_CONTENT,
                Capability.WRITE_CONTENT,
                Capability.ATTRIBUTES,
                Capability.LAST_MODIFIED,
                Capability.GET_LAST_MODIFIED,
                Capability.CREATE,
                Capability.DELETE,
                Capability.RENAME,
                Capability.GET_TYPE,
                Capability.LIST_CHILDREN,
                Capability.URI,
                Capability.FS_ATTRIBUTES
        );
    }

    @Override
    public String[] getSchemes() {
        return vfs.getFileSystems().stream()
                .map(cc.whohow.fs.FileSystem::getUri)
                .map(URI::getScheme)
                .distinct()
                .toArray(String[]::new);
    }

    @Override
    public URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return new VfsStreamHandlerFactory(this);
    }

    @Override
    public boolean hasProvider(String scheme) {
        return vfs.getFileSystems().stream()
                .map(cc.whohow.fs.FileSystem::getUri)
                .map(URI::getScheme)
                .anyMatch(scheme::equals);
    }

    @Override
    public FileObject resolveFile(File baseFile, String name) throws FileSystemException {
        return resolveFile(toFileObject(baseFile), name);
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String name) throws FileSystemException {
        return new FileObjectAdapter(resolveName(baseFile.getName(), name));
    }

    @Override
    public FileObject resolveFile(String name) throws FileSystemException {
        return resolveFile(getBaseFile(), name);
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(baseFile, name);
    }

    @Override
    public FileObject resolveFile(String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(getBaseFile(), name);
    }

    @Override
    public FileName parseURI(String uri) throws FileSystemException {
        return resolveURI(uri);
    }

    @Override
    public synchronized FileReplicator getReplicator() throws FileSystemException {
        if (defaultFileReplicator == null) {
            defaultFileReplicator = new DefaultFileReplicator();
            defaultFileReplicator.setContext(this);
            defaultFileReplicator.setLogger(logger);
            defaultFileReplicator.init();
        }
        return defaultFileReplicator;
    }

    @Override
    public synchronized TemporaryFileStore getTemporaryFileStore() throws FileSystemException {
        if (defaultFileReplicator == null) {
            defaultFileReplicator = new DefaultFileReplicator();
            defaultFileReplicator.setContext(this);
            defaultFileReplicator.setLogger(logger);
            defaultFileReplicator.init();
        }
        return defaultFileReplicator;
    }

    @Override
    public FileObject resolveFile(URI uri) throws FileSystemException {
        return new FileObjectAdapter(resolveURI(uri.toString()));
    }

    @Override
    public FileObject resolveFile(URL url) throws FileSystemException {
        return new FileObjectAdapter(resolveURI(url.toString()));
    }

    @Override
    public FilePath resolveName(FileName root, String name) throws FileSystemException {
        return resolveName(root, name, NameScope.FILE_SYSTEM);
    }

    @Override
    public FilePath resolveName(FileName root, String name, NameScope scope) throws FileSystemException {
        FilePath fileName = resolveURI(URI.create(root.getURI()).resolve(name).toString());
        switch (scope) {
            case FILE_SYSTEM: {
                return fileName;
            }
            case CHILD: {
                if (fileName.getParent().equals(root)) {
                    return fileName;
                }
                throw new FileSystemException("");
            }
            case DESCENDENT: {
                if (fileName.isAncestor(root)) {
                    return fileName;
                }
                throw new FileSystemException("");
            }
            case DESCENDENT_OR_SELF: {
                if (fileName.equals(root) || fileName.isAncestor(root)) {
                    return fileName;
                }
                throw new FileSystemException("");
            }
            default: {
                throw new AssertionError();
            }
        }
    }

    @Override
    public FilePath resolveURI(String uri) throws FileSystemException {
        cc.whohow.fs.File<?, ?> file = vfs.get(uri);
        FileSystemAdapter fileSystem = new FileSystemAdapter(file.getFileSystem());
        return new FilePath(fileSystem, file);
    }

    @Override
    public void setLogger(Log log) {
        this.logger = log;
    }

    @Override
    public FileObject toFileObject(File file) throws FileSystemException {
        return new FileObjectAdapter(resolveURI(file.toURI().toString()));
    }

    @Override
    public FileSystemManager getFileSystemManager() {
        return this;
    }
}
