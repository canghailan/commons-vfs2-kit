package cc.whohow.vfs.provider.http;

import cc.whohow.vfs.*;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;

import java.util.Collection;

public class HttpFileSystemProvider extends AbstractVfsComponent implements CloudFileSystemProvider {
    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public CloudFileSystem getFileSystem(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public CloudFileSystem findFileSystem(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public FileName getFileName(String uri) throws FileSystemException {
        return null;
    }

    @Override
    public CloudFileOperations getFileOperations() throws FileSystemException {
        return null;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }
}
