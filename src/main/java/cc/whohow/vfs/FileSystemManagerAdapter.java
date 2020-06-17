package cc.whohow.vfs;

import cc.whohow.fs.FileStream;
import cc.whohow.fs.FileSystemProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.TemporaryFileStore;
import org.apache.commons.vfs2.provider.VfsComponent;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileSystemManagerAdapter implements FileSystemManager, FileSystem, FileObject, VfsComponentContext {
    private static final Logger log = LogManager.getLogger(FileSystemManagerAdapter.class);
    private static final FileName ROOT = new UriFileName(URI.create("/"));
    protected final cc.whohow.fs.VirtualFileSystem vfs;
    protected final Map<String, List<FileOperationProvider>> fileOperationProvider = new ConcurrentHashMap<>();
    protected final Map<cc.whohow.fs.FileSystem<?, ?>, FileSystemAdapter> fileSystemCache = new ConcurrentHashMap<>();
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
    public void close() {
        if (defaultFileReplicator != null) {
            defaultFileReplicator.close();
        }
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
        throw new FileSystemException("vfs.provider/not-layered-fs.error", file.getName().getScheme());
    }

    @Override
    public FileObject createFileSystem(String provider, FileObject file) throws FileSystemException {
        throw new FileSystemException("vfs.provider/not-layered-fs.error", file.getName().getScheme());
    }

    @Override
    public FileObject createVirtualFileSystem(FileObject rootFile) throws FileSystemException {
        throw new FileSystemException("vfs.impl/create-junction.error", rootFile);
    }

    @Override
    public FileObject createVirtualFileSystem(String rootUri) throws FileSystemException {
        throw new FileSystemException("vfs.impl/create-junction.error", rootUri);
    }

    @Override
    public FileObject getBaseFile() throws FileSystemException {
        return this;
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
        return vfs.getProviders().stream()
                .map(FileSystemProvider::getScheme)
                .filter(Objects::nonNull)
                .distinct()
                .toArray(String[]::new);
    }

    @Override
    public URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return new VfsStreamHandlerFactory(this);
    }

    @Override
    public boolean hasProvider(String scheme) {
        return vfs.getProviders().stream()
                .map(FileSystemProvider::getScheme)
                .anyMatch(scheme::equals);
    }

    @Override
    public FileObject resolveFile(File baseFile, String name) throws FileSystemException {
        return resolveFile(toFileObject(baseFile), name);
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String name) throws FileSystemException {
        if (baseFile == null) {
            return new FileObjectAdapter(resolveURI(name));
        } else {
            return new FileObjectAdapter(resolveName(baseFile.getName(), name));
        }
    }

    @Override
    public FileObject getRoot() throws FileSystemException {
        return this;
    }

    @Override
    public FileName getRootName() {
        return ROOT;
    }

    @Override
    public String getRootURI() {
        return getRootName().getURI();
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
        // no parent layer
        return null;
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return vfs.getContext().resolve(attrName).readUtf8();
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-attribute-not-supported.error");
    }

    @Override
    public FileObject resolveFile(FileName name) throws FileSystemException {
        return new FileObjectAdapter(resolveURI(name.getURI()));
    }

    @Override
    public boolean canRenameTo(FileObject file) {
        return false;
    }

    @Override
    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        throw new FileSystemException("vfs.provider/copy-file.error", srcFile, this);
    }

    @Override
    public void createFile() throws FileSystemException {
        throw new FileSystemException("vfs.provider/create-file.error", this);
    }

    @Override
    public void createFolder() throws FileSystemException {
        // do nothing
    }

    @Override
    public boolean delete() throws FileSystemException {
        throw new FileSystemException("vfs.provider/delete-not-supported.error");
    }

    @Override
    public int delete(FileSelector selector) throws FileSystemException {
        throw new FileSystemException("vfs.provider/delete-not-supported.error");
    }

    @Override
    public int deleteAll() throws FileSystemException {
        throw new FileSystemException("vfs.provider/delete-not-supported.error");
    }

    @Override
    public boolean exists() throws FileSystemException {
        return true;
    }

    @Override
    public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        throw new FileSystemException("vfs.provider/find-files.error", this);
    }

    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        throw new FileSystemException("vfs.provider/find-files.error", this);
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        throw new FileSystemException("vfs.provider/list-children.error", this);
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        throw new FileSystemException("vfs.provider/list-children.error", this);
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        throw new FileSystemException("vfs.provider/read-not-file.error", this);
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    public FileSystem getFileSystem() {
        return this;
    }

    @Override
    public FileName getName() {
        return ROOT;
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        // no parent
        return null;
    }

    @Override
    public String getPublicURIString() {
        return ROOT.getURI();
    }

    @Override
    public FileType getType() throws FileSystemException {
        return FileType.FOLDER;
    }

    @Override
    public URL getURL() throws FileSystemException {
        throw new FileSystemException("vfs.provider/get-url.error", this);
    }

    @Override
    public boolean isAttached() {
        // stateless
        return false;
    }

    @Override
    public boolean isContentOpen() {
        // stateless
        return false;
    }

    @Override
    public boolean isExecutable() throws FileSystemException {
        return false;
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return false;
    }

    @Override
    public boolean isFolder() throws FileSystemException {
        return true;
    }

    @Override
    public boolean isHidden() throws FileSystemException {
        return false;
    }

    @Override
    public boolean isReadable() throws FileSystemException {
        return false;
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        return false;
    }

    @Override
    public void moveTo(FileObject destFile) throws FileSystemException {
        throw new FileSystemException("vfs.provider/rename-not-supported.error");
    }

    @Override
    public void refresh() throws FileSystemException {
        // stateless, do nothing
    }

    @Override
    public FileObject resolveFile(String name) throws FileSystemException {
        return resolveFile(name, NameScope.FILE_SYSTEM);
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        if (scope == NameScope.FILE_SYSTEM) {
            return new FileObjectAdapter(resolveURI(name));
        } else {
            throw new FileSystemException("vfs.provider/resolve-file.error", name);
        }
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-executable.error", this);
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-writeable.error", this);
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-readable.error", this);
    }

    @Override
    public void addListener(FileObject file, FileListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(FileObject file, FileListener listener) {
        throw new UnsupportedOperationException();
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
        throw new FileSystemException("vfs.provider/replicate-file.error", file);
    }

    @Override
    public FileSystemOptions getFileSystemOptions() {
        try (FileStream<? extends cc.whohow.fs.File<?, ?>> stream = vfs.getContext().tree()) {
            FileSystemOptions fileSystemOptions = new FileSystemOptions();
            FileSystemAdapterConfigBuilder fileSystemAdapterConfigBuilder = new FileSystemAdapterConfigBuilder();
            for (cc.whohow.fs.File<?, ?> file : stream) {
                if (file.isRegularFile()) {
                    fileSystemAdapterConfigBuilder.setParam(fileSystemOptions,
                            vfs.getContext().getPath().relativize(file.getPath()), file.readUtf8());
                }
            }
            return fileSystemOptions;
        } catch (IOException e) {
            throw FileSystemExceptions.unchecked(e);
        }
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
                if (root.equals(fileName.getParent())) {
                    return fileName;
                }
                throw new FileSystemException("vfs.provider/resolve-file.error", fileName);
            }
            case DESCENDENT: {
                if (root.isDescendent(fileName)) {
                    return fileName;
                }
                throw new FileSystemException("vfs.provider/resolve-file.error", fileName);
            }
            case DESCENDENT_OR_SELF: {
                if (root.equals(fileName) || root.isDescendent(fileName)) {
                    return fileName;
                }
                throw new FileSystemException("vfs.provider/resolve-file.error", fileName);
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public FilePath resolveURI(String uri) throws FileSystemException {
        cc.whohow.fs.File<?, ?> file = vfs.get(uri);
        return new FilePath(fileSystemCache.computeIfAbsent(file.getFileSystem(), FileSystemAdapter::new), file);
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

    @Override
    public double getLastModTimeAccuracy() {
        // default
        return 0;
    }

    @Override
    public int compareTo(FileObject o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public Iterator<FileObject> iterator() {
        return Collections.<FileObject>singleton(this).iterator();
    }

    @Override
    public String toString() {
        return ROOT.toString();
    }
}
