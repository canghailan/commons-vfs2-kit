package cc.whohow.vfs;

import cc.whohow.vfs.configuration.ConfigurationFile;
import cc.whohow.vfs.configuration.ProviderConfiguration;
import cc.whohow.vfs.provider.uri.UriFileName;
import cc.whohow.vfs.provider.uri.UriFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.FileContentInfoFilenameFactory;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.*;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VirtualFileSystemManager implements FileSystemManager, FileProvider, FileSystem, FileObject, VfsComponentContext {
    protected static final Pattern RESERVED = Pattern.compile("[@#&*?]");

    protected final Log log = LogFactory.getLog(VirtualFileSystemManager.class);
    protected final Map<String, Object> attributes = new ConcurrentHashMap<>();
    protected final Map<String, FileProvider> providers = new ConcurrentHashMap<>();
    protected final Map<String, List<FileOperationProvider>> operationProviders = new ConcurrentHashMap<>();
    protected final NavigableMap<String, FileObject> junctions = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    protected final ConfigurationFile configuration;
    protected UriFileProvider uriFileProvider;
    protected LocalFileProvider localFileProvider;

    public VirtualFileSystemManager(ConfigurationFile configuration) {
        this.configuration = configuration;
        try {
            init();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void init() throws FileSystemException {
        uriFileProvider = new UriFileProvider();
        localFileProvider = new DefaultLocalFileProvider();

        addProvider("conf", configuration);
        addProvider("file", localFileProvider);
        addProvider("http", uriFileProvider);
        addProvider("https", uriFileProvider);

        ProviderConfiguration[] providers = configuration.getProviderConfigurations(ProviderConfiguration[].class);
        if (providers != null) {
            for (ProviderConfiguration providerConfiguration : providers) {
                FileProvider provider = (FileProvider) newInstance(providerConfiguration.getClassName());
                initVfsComponent(provider);
                for (String scheme : providerConfiguration.getSchemes()) {
                    addProvider(scheme, provider);
                }
            }
        }

        ProviderConfiguration[] operationProviders = configuration.getOperationProviderConfigurations(ProviderConfiguration[].class);
        if (operationProviders != null) {
            for (ProviderConfiguration providerConfiguration : operationProviders) {
                FileOperationProvider provider = (FileOperationProvider) newInstance(providerConfiguration.getClassName());
                for (String scheme : providerConfiguration.getSchemes()) {
                    addOperationProvider(scheme, provider);
                }
            }
        }

        Map<String, String> junctions = configuration.getJunctions();
        if (junctions != null) {
            for (Map.Entry<String, String> junction : junctions.entrySet()) {
                addJunction(junction.getKey(), junction.getValue());
            }
        }
    }

    protected Object newInstance(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    protected void initVfsComponent(Object object) throws FileSystemException {
        if (object instanceof VfsComponent) {
            VfsComponent vfsComponent = (VfsComponent) object;
            vfsComponent.setLogger(log);
            vfsComponent.setContext(this);
            vfsComponent.init();
        }
    }

    protected void closeVfsComponent(Object object) {
        if (object instanceof VfsComponent) {
            VfsComponent vfsComponent = (VfsComponent) object;
            vfsComponent.close();
        }
    }

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
        return resolveFile(name.getURI());
    }

    @Override
    public boolean canRenameTo(FileObject newFile) {
        return false;
    }

    @Override
    public void close() throws FileSystemException {
        providers.values().forEach(this::closeVfsComponent);
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
        if (isJunction(name)) {
            for (Map.Entry<String, FileObject> junction : junctions.tailMap(name).entrySet()) {
                if (name.startsWith(junction.getKey())) {
                    return junction.getValue().resolveFile(
                            name.substring(junction.getKey().length()), NameScope.DESCENDENT_OR_SELF);
                }
            }
        }
        String scheme = UriParser.extractScheme(name);
        if (scheme != null) {
            FileProvider provider = providers.get(scheme);
            if (provider != null) {
                return provider.findFile(this, name, null);
            }
        }
        throw new FileSystemException("vfs.provider/resolve-file.error", name);
    }

    protected boolean isJunction(String name) {
        return !RESERVED.matcher(name).find();
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

    public void addJunction(String junctionPoint, String targetFile) throws FileSystemException {
        addJunction(junctionPoint, resolveFile(targetFile));
    }

    @Override
    public void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException {
        junctions.put(junctionPoint, targetFile);
        if (targetFile instanceof CanonicalNameFileObject) {
            CanonicalNameFileObject cname = (CanonicalNameFileObject) targetFile;
            for (String junction : cname.getCanonicalNames()) {
                junctions.put(junction, targetFile);
            }
        }
    }

    @Override
    public void removeJunction(String junctionPoint) throws FileSystemException {
        FileObject targetFile = junctions.remove(junctionPoint);
        if (targetFile instanceof CanonicalNameFileObject) {
            CanonicalNameFileObject cname = (CanonicalNameFileObject) targetFile;
            for (String junction : cname.getCanonicalNames()) {
                junctions.remove(junction);
            }
        }
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
        return junctions.values().stream()
                .map(FileObject::getFileSystem)
                .mapToDouble(FileSystem::getLastModTimeAccuracy)
                .max()
                .orElse(0);
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return baseFile.resolveFile(name);
    }

    @Override
    public FileObject resolveFile(String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(name);
    }

    @Override
    public FileName parseURI(String uri) throws FileSystemException {
        return resolveFile(uri).getName();
    }

    @Override
    public FileReplicator getReplicator() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public TemporaryFileStore getTemporaryFileStore() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public FileObject resolveFile(FileObject baseFile, String name) throws FileSystemException {
        return baseFile.resolveFile(name);
    }

    @Override
    public FileObject resolveFile(File baseFile, String name) throws FileSystemException {
        return resolveFile(toFileObject(baseFile), name);
    }

    @Override
    public FileName resolveName(FileName root, String name) throws FileSystemException {
        return resolveName(root, name, NameScope.FILE_SYSTEM);
    }

    @Override
    public FileName resolveName(FileName root, String name, NameScope scope) throws FileSystemException {
        return resolveFile(root).resolveFile(name, scope).getName();
    }

    @Override
    public FileObject toFileObject(File file) throws FileSystemException {
        return localFileProvider.findLocalFile(file);
    }

    @Override
    public FileObject createFileSystem(String provider, FileObject file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    public void closeFileSystem(FileSystem filesystem) {
        closeVfsComponent(filesystem);
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
        return protocol -> {
            FileProvider provider = providers.get(protocol);
            if (provider != null) {
                return new DefaultURLStreamHandler(VirtualFileSystemManager.this);
            }
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    return u.openConnection();
                }
            };
        };
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
        return CacheStrategy.MANUAL;
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
        return new FileContentInfoFilenameFactory();
    }

    @Override
    public boolean hasProvider(String scheme) {
        return scheme == null || Arrays.asList(getSchemes()).contains(scheme);
    }

    @Override
    public String[] getSchemes() {
        return providers.keySet().toArray(new String[0]);
    }

    @Override
    public Collection<Capability> getProviderCapabilities(String scheme) throws FileSystemException {
        FileProvider provider = providers.get(scheme);
        if (provider != null) {
            return provider.getCapabilities();
        }
        throw new FileSystemException("");
    }

    @Override
    public void setLogger(Log log) {

    }

    @Override
    public FileSystemConfigBuilder getFileSystemConfigBuilder(String scheme) throws FileSystemException {
        FileProvider provider = providers.get(scheme);
        if (provider != null) {
            return provider.getConfigBuilder();
        }
        throw new FileSystemException("");
    }

    @Override
    public FileName resolveURI(String uri) throws FileSystemException {
        return resolveFile(uri).getName();
    }

    public void addProvider(String scheme, FileProvider provider) {
        providers.put(scheme, provider);
    }

    public void addProvider(String[] schemes, FileProvider provider) {
        for (String scheme : schemes) {
            providers.put(scheme, provider);
        }
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
        return providers.values().stream()
                .map(FileProvider::getCapabilities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public FileName parseUri(FileName root, String uri) throws FileSystemException {
        return resolveFile(root).resolveFile(uri).getName();
    }

    @Override
    public String toString() {
        return junctions.entrySet().stream()
                .map(self -> self.getKey() + " --> " + self.getValue())
                .collect(Collectors.joining("\n"));
    }
}