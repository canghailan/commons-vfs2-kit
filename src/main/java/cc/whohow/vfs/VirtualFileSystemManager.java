package cc.whohow.vfs;

import cc.whohow.vfs.operations.DefaultFileOperations;
import cc.whohow.vfs.provider.kv.KeyValueFileObject;
import cc.whohow.vfs.provider.uri.UriFileName;
import cc.whohow.vfs.serialize.TextSerializer;
import cc.whohow.vfs.tree.FileObjectList;
import cc.whohow.vfs.watch.PollingFileWatchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.TemporaryFileStore;

import java.net.URI;
import java.net.URLStreamHandlerFactory;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class VirtualFileSystemManager implements VirtualFileSystem {
    protected final Log logger = LogFactory.getLog(VirtualFileSystemManager.class);
    protected final NavigableMap<String, String> conf = new ConcurrentSkipListMap<>();
    protected final NavigableMap<String, String> data = new ConcurrentSkipListMap<>();
    protected final NavigableMap<String, FileObjectX> vfs = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    protected final NavigableMap<String, FileSystemProviderX> providers = new ConcurrentSkipListMap<>();
    protected DefaultFileOperations operations = new DefaultFileOperations();
    protected ScheduledExecutorService executor;
    protected PollingFileWatchService watchService;

    public VirtualFileSystemManager() {
        vfs.put("/", this);
        vfs.put("vfs:/", this);
        vfs.put("vfm:/", new KeyValueFileObject<>(this, TextSerializer.utf8(), data, new UriFileName("vfm:/")));
        vfs.put("conf:/", new KeyValueFileObject<>(this, TextSerializer.utf8(), conf, new UriFileName("conf:/")));
        providers.put("vfs", this);
        providers.put("vfm", this);
        providers.put("conf", this);
        executor = Executors.newScheduledThreadPool(2);
        watchService = new PollingFileWatchService(executor);
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return conf.get(attrName);
    }

    @Override
    public void setAttribute(String attrName, Object value) {
        conf.put(attrName, Objects.toString(value, null));
    }

    @Override
    public Map<String, Object> getAttributes() throws FileSystemException {
        Map<String, Object> attributes = new TreeMap<>();
        for (Map.Entry<String, String> e : conf.entrySet()) {
            attributes.put(e.getKey(), e.getValue());
        }
        return attributes;
    }

    @Override
    public DirectoryStream<FileObjectX> list() throws FileSystemException {
        return new FileObjectList(vfs.values());
    }

    @Override
    public FileObjectX resolveFile(String name) throws FileSystemException {
        return resolveFile(URI.create(name));
    }

    @Override
    public FileObjectX resolveFile(URI u) throws FileSystemException {
        URI uri = u.normalize();
        String s = uri.toString();
        for (Map.Entry<String, FileObjectX> e : vfs.entrySet()) {
            if (s.startsWith(e.getKey())) {
                return e.getValue().resolveFile(s.substring(e.getKey().length()));
            }
        }
        FileSystemProviderX fileSystemProvider = providers.get(uri.getScheme());
        if (fileSystemProvider == null) {
            return null;
        }
        return fileSystemProvider.getFileObject(uri);
    }

    @Override
    public FileSystemX getFileSystem(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public FileOperationsX getFileOperations() throws FileSystemException {
        return operations;
    }

    @Override
    public URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return null;
    }

    @Override
    public String[] getSchemes() {
        return providers.keySet().toArray(new String[0]);
    }

    @Override
    public Collection<Capability> getProviderCapabilities(String scheme) throws FileSystemException {
        return providers.get(scheme).getCapabilities();
    }

    @Override
    public void init() throws FileSystemException {
        try (DirectoryStream<FileObjectX> list = resolveFile("conf:/providers/").list()) {
            for (FileObjectX provider : list) {
                String className = TextSerializer.utf8().deserialize(provider.resolveFile("className"));
                FileSystemProviderX fileSystemProvider = (FileSystemProviderX) Class.forName(className)
                        .getDeclaredConstructor().newInstance();

                fileSystemProvider.setContext(this);
                fileSystemProvider.setLogger(logger);
                fileSystemProvider.init();

                FileObjectX scheme = provider.resolveFile("scheme");
                if (scheme.exists()) {
                    System.out.println("scheme");
                }
                providers.putIfAbsent(fileSystemProvider.getScheme(), fileSystemProvider);
            }
        } catch (FileSystemException e) {
            throw e;
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public void addJunction(String junction, FileObject file) throws FileSystemException {
        vfs.put(junction, (FileObjectX) file);
    }

    @Override
    public FileName resolveURI(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void addOperationProvider(String scheme, FileOperationProvider operationProvider) throws FileSystemException {

    }

    @Override
    public void addOperationProvider(String[] schemes, FileOperationProvider operationProvider) throws FileSystemException {

    }

    @Override
    public FileOperationProvider[] getOperationProviders(String scheme) throws FileSystemException {
        return new FileOperationProvider[0];
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }

    @Override
    public FileReplicator getReplicator() throws FileSystemException {
        return null;
    }

    @Override
    public TemporaryFileStore getTemporaryFileStore() throws FileSystemException {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<String, String> e : data.entrySet()) {
            buffer.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        return buffer.toString();
    }

    public PollingFileWatchService getWatchService() {
        return watchService;
    }
}