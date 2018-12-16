package cc.whohow.vfs.provider.uri;

import cc.whohow.vfs.*;
import cc.whohow.vfs.operations.ProviderFileOperations;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.operations.FileOperations;

import java.net.MalformedURLException;
import java.net.URL;

public class UriFileObject implements DataFileObject, FreeFileObject, ReadonlyFileObject, StatelessFileObject  {
    private final String uri;

    public UriFileObject(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean exists() throws FileSystemException {
        return false;
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        return new UriFileContent(this);
    }

    @Override
    public FileOperations getFileOperations() throws FileSystemException {
        return new ProviderFileOperations(this);
    }

    @Override
    public FileName getName() {
        return new UriFileName(uri);
    }

    @Override
    public String getPublicURIString() {
        return uri;
    }

    @Override
    public URL getURL() throws FileSystemException {
        try {
            return new URL(uri);
        } catch (MalformedURLException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public String toString() {
        return uri;
    }
}
