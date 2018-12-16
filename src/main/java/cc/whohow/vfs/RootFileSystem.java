package cc.whohow.vfs;

import cc.whohow.vfs.provider.uri.UriFileName;
import cc.whohow.vfs.provider.uri.UriFileObject;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;
import org.apache.commons.vfs2.provider.FileProvider;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class RootFileSystem extends AbstractVfsComponent implements FileSystemManager, FileProvider, FileSystem, FileObject {
    protected final Map<String, Object> attributes = new ConcurrentHashMap<>();
    protected final NavigableMap<String, FileObject> junctions = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    protected final Map<String, List<FileOperationProvider>> operationProviders = new ConcurrentHashMap<>();
    protected volatile Map<String, ?> schemes = new ConcurrentHashMap<>();
    protected volatile Set<Capability> capabilities = new CopyOnWriteArraySet<>();
    protected volatile Map<String, Set<Capability>> providerCapabilities = new ConcurrentHashMap<>();

    @Override
    public FileObject getBaseFile() throws FileSystemException {
        return this;
    }

    @Override
    public FileObject getRoot() throws FileSystemException {
        return this;
    }

    @Override
    public FileName getRootName() {
        return getName();
    }

    @Override
    public String getRootURI() {
        return getRootName().toString();
    }

    @Override
    public boolean hasCapability(Capability capability) {
        return getCapabilities().contains(capability);
    }

    @Override
    public FileObject getParentLayer() throws FileSystemException {
        return null;
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return attributes.get(attrName);
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {
        attributes.put(attrName, value);
    }

    @Override
    public FileObject resolveFile(FileName name) throws FileSystemException {
        return resolveFile(name.toString());
    }

    @Override
    public boolean canRenameTo(FileObject newFile) {
        return false;
    }

    @Override
    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public void createFile() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public void createFolder() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public boolean delete() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public int delete(FileSelector selector) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public int deleteAll() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public boolean exists() throws FileSystemException {
        return true;
    }

    @Override
    public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileSystem getFileSystem() {
        return this;
    }

    @Override
    public FileName getName() {
        return new UriFileName(FileName.SEPARATOR);
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        return null;
    }

    @Override
    public String getPublicURIString() {
        return FileName.SEPARATOR;
    }

    @Override
    public FileType getType() throws FileSystemException {
        return FileType.FOLDER;
    }

    @Override
    public URL getURL() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public boolean isAttached() {
        return true;
    }

    @Override
    public boolean isContentOpen() {
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
        throw new FileSystemException("");
    }

    @Override
    public void refresh() throws FileSystemException {
        // do nothing
    }

    @Override
    public FileObject resolveFile(String name) throws FileSystemException {
        for (Map.Entry<String, FileObject> junction : junctions.tailMap(name).entrySet()) {
            if (name.startsWith(junction.getKey())) {
                return junction.getValue().resolveFile(
                        name.substring(junction.getKey().length()));
            }
        }
        return new UriFileObject(name);
    }

    @Override
    public void addListener(FileObject file, FileListener listener) {
        file.getFileSystem().addListener(file, listener);
    }

    @Override
    public void removeListener(FileObject file, FileListener listener) {
        file.getFileSystem().removeListener(file, listener);
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        return resolveFile(name);
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-executable.error", this);
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-readable.error", this);
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        throw new FileSystemException("vfs.provider/set-writeable.error", this);
    }

    @Override
    public void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException {
        junctions.put(junctionPoint, targetFile);
    }

    @Override
    public void removeJunction(String junctionPoint) throws FileSystemException {
        junctions.remove(junctionPoint);
    }

    @Override
    public File replicateFile(FileObject file, FileSelector selector) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileSystemOptions getFileSystemOptions() {
        return null;
    }

    @Override
    public FileSystemManager getFileSystemManager() {
        return this;
    }

    @Override
    public double getLastModTimeAccuracy() {
        return 0;
    }

    @Override
    public FileObject resolveFile(String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(name);
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String name) throws FileSystemException {
        return baseFile.resolveFile(name);
    }

    @Override
    public FileObject resolveFile(File baseFile, String name) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileName resolveName(FileName root, String name) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileName resolveName(FileName root, String name, NameScope scope) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject toFileObject(File file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createFileSystem(String provider, FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public void closeFileSystem(FileSystem filesystem) {
    }

    @Override
    public FileObject createFileSystem(FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createVirtualFileSystem(String rootUri) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createVirtualFileSystem(FileObject rootFile) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return null;
    }

    @Override
    public boolean canCreateFileSystem(FileObject file) throws FileSystemException {
        return false;
    }

    @Override
    public FilesCache getFilesCache() {
        return null;
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return null;
    }

    @Override
    public Class<?> getFileObjectDecorator() {
        return null;
    }

    @Override
    public Constructor<?> getFileObjectDecoratorConst() {
        return null;
    }

    @Override
    public FileContentInfoFactory getFileContentInfoFactory() {
        return null;
    }

    @Override
    public boolean hasProvider(String scheme) {
        return scheme == null || Arrays.asList(getSchemes()).contains(scheme);
    }

    @Override
    public String[] getSchemes() {
        return schemes.keySet().toArray(new String[0]);
    }

    @Override
    public Collection<Capability> getProviderCapabilities(String scheme) throws FileSystemException {
        return Collections.unmodifiableCollection(providerCapabilities.computeIfAbsent(scheme, (k) -> Collections.emptySet()));
    }

    @Override
    public FileSystemConfigBuilder getFileSystemConfigBuilder(String scheme) throws FileSystemException {
        return null;
    }

    @Override
    public FileName resolveURI(String uri) throws FileSystemException {
        return resolveFile(uri).getName();
    }

    @Override
    public void addOperationProvider(String scheme, FileOperationProvider operationProvider) throws FileSystemException {
        operationProviders.computeIfAbsent(scheme, self -> new ArrayList<>()).add(operationProvider);
    }

    @Override
    public void addOperationProvider(String[] schemes, FileOperationProvider operationProvider) throws FileSystemException {
        for (String scheme : schemes) {
            addOperationProvider(scheme, operationProvider);
        }
    }

    @Override
    public FileOperationProvider[] getOperationProviders(String scheme) throws FileSystemException {
        return operationProviders.getOrDefault(scheme, Collections.emptyList()).toArray(new FileOperationProvider[0]);
    }

    @Override
    public FileObject resolveFile(URI uri) throws FileSystemException {
        return resolveFile(uri.toString());
    }

    @Override
    public FileObject resolveFile(URL url) throws FileSystemException {
        return resolveFile(url.toString());
    }

    @Override
    public int compareTo(FileObject o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public Iterator<FileObject> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return Collections.unmodifiableCollection(capabilities);
    }

    @Override
    public FileName parseUri(FileName root, String uri) throws FileSystemException {
        throw new FileSystemException("");
    }

    public Map<String, FileObject> getJunctions() {
        return Collections.unmodifiableMap(junctions);
    }
}