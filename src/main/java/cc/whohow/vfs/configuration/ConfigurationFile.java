package cc.whohow.vfs.configuration;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 配置文件：配置以文件格式接口提供，类似Linux /proc，计划提供查询及修改功能
 */
public interface ConfigurationFile extends Configuration, FileProvider, FileSystem, FileObject, FileContent {
    @Override
    default FileObject getFile() {
        return this;
    }

    @Override
    default long getSize() throws FileSystemException {
        // TODO
        return 0;
    }

    @Override
    default long getLastModifiedTime() throws FileSystemException {
        // TODO
        return 0;
    }

    @Override
    default void setLastModifiedTime(long modTime) throws FileSystemException {
        // TODO
    }

    @Override
    default boolean hasAttribute(String attrName) throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default Map<String, Object> getAttributes() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default String[] getAttributeNames() throws FileSystemException {
        // TODO
        return new String[0];
    }

    @Override
    default void removeAttribute(String attrName) throws FileSystemException {
        // TODO
    }

    @Override
    default Certificate[] getCertificates() throws FileSystemException {
        // TODO
        return new Certificate[0];
    }

    @Override
    default InputStream getInputStream() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default OutputStream getOutputStream() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default OutputStream getOutputStream(boolean bAppend) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileContentInfo getContentInfo() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default boolean isOpen() {
        return false;
    }

    @Override
    default long write(FileContent output) throws IOException {
        // TODO
        return 0;
    }

    @Override
    default long write(FileObject file) throws IOException {
        // TODO
        return 0;
    }

    @Override
    default long write(OutputStream output) throws IOException {
        // TODO
        return 0;
    }

    @Override
    default long write(OutputStream output, int bufferSize) throws IOException {
        // TODO
        return 0;
    }

    @Override
    default boolean canRenameTo(FileObject newfile) {
        // TODO
        return false;
    }

    @Override
    default void close() throws FileSystemException {
        // TODO
    }

    @Override
    default void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        // TODO
    }

    @Override
    default void createFile() throws FileSystemException {
        // TODO
    }

    @Override
    default void createFolder() throws FileSystemException {
        // TODO
    }

    @Override
    default boolean delete() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default int delete(FileSelector selector) throws FileSystemException {
        // TODO
        return 0;
    }

    @Override
    default int deleteAll() throws FileSystemException {
        // TODO
        return 0;
    }

    @Override
    default boolean exists() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        // TODO
        return new FileObject[0];
    }

    @Override
    default void findFiles(FileSelector selector, boolean depthwise, List<FileObject> selected) throws FileSystemException {
        // TODO
    }

    @Override
    default FileObject getChild(String name) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileObject[] getChildren() throws FileSystemException {
        // TODO
        return new FileObject[0];
    }

    @Override
    default FileContent getContent() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileOperations getFileOperations() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileSystem getFileSystem() {
        // TODO
        return null;
    }

    @Override
    default FileName getName() {
        // TODO
        return null;
    }

    @Override
    default FileObject getParent() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default String getPublicURIString() {
        // TODO
        return null;
    }

    @Override
    default FileType getType() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default URL getURL() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default boolean isAttached() {
        // TODO
        return false;
    }

    @Override
    default boolean isContentOpen() {
        // TODO
        return false;
    }

    @Override
    default boolean isExecutable() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean isFile() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean isFolder() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean isHidden() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean isReadable() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean isWriteable() throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default void moveTo(FileObject destFile) throws FileSystemException {
        // TODO
    }

    @Override
    default void refresh() throws FileSystemException {
        // TODO
    }

    @Override
    default FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default boolean setExecutable(boolean executable, boolean ownerOnly) throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean setReadable(boolean readable, boolean ownerOnly) throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default boolean setWritable(boolean writable, boolean ownerOnly) throws FileSystemException {
        // TODO
        return false;
    }

    @Override
    default int compareTo(FileObject o) {
        // TODO
        return 0;
    }

    @Override
    default Iterator<FileObject> iterator() {
        // TODO
        return null;
    }

    @Override
    default FileObject getRoot() throws FileSystemException {
        // TODO
        return this;
    }

    @Override
    default FileName getRootName() {
        // TODO
        return null;
    }

    @Override
    default String getRootURI() {
        // TODO
        return "conf://";
    }

    @Override
    default boolean hasCapability(Capability capability) {
        // TODO
        return false;
    }

    @Override
    default FileObject getParentLayer() throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default Object getAttribute(String attrName) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default void setAttribute(String attrName, Object value) throws FileSystemException {
        // TODO
    }

    @Override
    default FileObject resolveFile(FileName name) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileObject resolveFile(String name) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default void addListener(FileObject file, FileListener listener) {
        // TODO
    }

    @Override
    default void removeListener(FileObject file, FileListener listener) {
        // TODO
    }

    @Override
    default void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException {
        // TODO
    }

    @Override
    default void removeJunction(String junctionPoint) throws FileSystemException {
        // TODO
    }

    @Override
    default File replicateFile(FileObject file, FileSelector selector) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileSystemOptions getFileSystemOptions() {
        // TODO
        return null;
    }

    @Override
    default FileSystemManager getFileSystemManager() {
        // TODO
        return null;
    }

    @Override
    default double getLastModTimeAccuracy() {
        // TODO
        return 0;
    }

    @Override
    default FileObject findFile(FileObject baseFile, String uri, FileSystemOptions fileSystemOptions) throws FileSystemException {
        // TODO
        return this;
    }

    @Override
    default FileObject createFileSystem(String scheme, FileObject file, FileSystemOptions fileSystemOptions) throws FileSystemException {
        // TODO
        return null;
    }

    @Override
    default FileSystemConfigBuilder getConfigBuilder() {
        // TODO
        return null;
    }

    @Override
    default Collection<Capability> getCapabilities() {
        // TODO
        return null;
    }

    @Override
    default FileName parseUri(FileName root, String uri) throws FileSystemException {
        // TODO
        return null;
    }
}
