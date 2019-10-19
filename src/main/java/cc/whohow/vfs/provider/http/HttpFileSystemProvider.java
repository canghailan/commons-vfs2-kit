package cc.whohow.vfs.provider.http;

import cc.whohow.vfs.FileOperationsX;
import cc.whohow.vfs.FileSystemProviderX;
import cc.whohow.vfs.FileSystemX;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

import java.util.Collection;

public class HttpFileSystemProvider extends AbstractVfsComponent implements FileSystemProviderX {
    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public FileSystemX getFileSystem(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public FileSystemX findFileSystem(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public FileName getFileName(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public FileOperationsX getFileOperations() throws FileSystemException {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }
}
