package cc.whohow.vfs;

import cc.whohow.vfs.io.ReadableChannel;
import cc.whohow.vfs.io.WritableChannel;
import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public interface VirtualFileSystem extends CloudFileSystemProvider, CloudFileSystem, CloudFileObject, FileSystemManagerImpl, VfsComponentContext {
    @Override
    CloudFileOperations getFileOperations() throws FileSystemException;

    @Override
    default CloudFileSystem getFileSystem() {
        return this;
    }

    @Override
    default FileName getName() {
        return new VirtualFileName("/");
    }

    @Override
    default CloudFileObject getParent() throws FileSystemException {
        return null;
    }

    @Override
    default boolean exists() throws FileSystemException {
        return true;
    }

    @Override
    default void createFile() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default void createFolder() throws FileSystemException {

    }

    @Override
    default boolean delete() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default CloudFileObject getRoot() throws FileSystemException {
        return this;
    }

    @Override
    default InputStream getInputStream() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default ReadableChannel getReadableChannel() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default WritableChannel getWritableChannel() throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default FileName getRootName() {
        return getName();
    }

    @Override
    default VirtualFileSystem getFileSystemManager() {
        return this;
    }

    @Override
    default CloudFileObject getBaseFile() throws FileSystemException {
        return this;
    }

    @Override
    default CloudFileObject resolveFile(String path) throws FileSystemException {
        return resolve(path);
    }

    @Override
    default Object getAttribute(String attrName) throws FileSystemException {
        return getAttributes().get(attrName);
    }

    @Override
    default String getPublicURIString() {
        return "/";
    }

    @Override
    default CloudFileSystemProvider getFileSystemProvider() {
        return this;
    }

    @Override
    default String getScheme() {
        return "vfs";
    }

    @Override
    default Map<String, Object> getAttributes() throws FileSystemException {
        try (CloudFileObjectList list = resolveFile("conf:/").listRecursively()) {
            Map<String, Object> attributes = new TreeMap<>();
            for (CloudFileObject fileObject : list) {
                attributes.put(fileObject.getName().getPathDecoded(), FileObjects.readUtf8(fileObject));
            }
            return attributes;
        }
    }

    @Override
    default void setAttribute(String attrName, Object value) {
        try {
            FileObjects.writeUtf8(resolveFile("conf:/").resolveFile(attrName), Objects.toString(value, null));
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    default VirtualFileSystemConfigBuilder getFileSystemConfigBuilder(String scheme) {
        return VirtualFileSystemConfigBuilder.getInstance();
    }

    @Override
    default boolean hasProvider(String scheme) {
        return getScheme().equals(scheme) || Arrays.asList(getSchemes()).contains(scheme);
    }

    @Override
    default String[] getSchemes() {
        try (CloudFileObjectList list = resolveFile("conf:/").getChild("providers").list()) {
            return list.stream().map(FileObjects::getBaseName).toArray(String[]::new);
        } catch (FileSystemException e) {
            return new String[0];
        }
    }

    @Override
    default CloudFileSystem findFileSystem(String uri) throws FileSystemException {
        return resolve(uri).getFileSystem();
    }

    @Override
    default FileName getFileName(String uri) throws FileSystemException {
        return resolve(uri).getName();
    }

    @Override
    default CloudFileObject toFileObject(File file) throws FileSystemException {
        throw new FileSystemException("");
    }

    @Override
    default CloudFileObject resolveFile(String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(getBaseFile(), name, fileSystemOptions);
    }

    @Override
    default CloudFileObject resolveFile(org.apache.commons.vfs2.FileObject baseFile, String name, FileSystemOptions fileSystemOptions) throws FileSystemException {
        return resolveFile(URIBuilder.resolve(baseFile.getName().getURI(), name));
    }

    FileName resolveURI(String uri) throws FileSystemException;

    @Override
    default FileName parseURI(String uri) throws FileSystemException {
        return resolveURI(uri);
    }

    @Override
    void close();
}
