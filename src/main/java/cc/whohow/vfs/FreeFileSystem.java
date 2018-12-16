package cc.whohow.vfs;

import org.apache.commons.vfs2.*;

import java.io.File;
import java.io.UncheckedIOException;

public class FreeFileSystem implements FileSystem {
    private static final FreeFileSystem INSTANCE = new FreeFileSystem();

    public static FreeFileSystem getInstance() {
        return INSTANCE;
    }

    @Override
    public FileObject getRoot() throws FileSystemException {
        return null;
    }

    @Override
    public FileName getRootName() {
        return null;
    }

    @Override
    public String getRootURI() {
        return null;
    }

    @Override
    public boolean hasCapability(Capability capability) {
        return false;
    }

    @Override
    public FileObject getParentLayer() throws FileSystemException {
        return null;
    }

    @Override
    public Object getAttribute(String attrName) throws FileSystemException {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws FileSystemException {

    }

    @Override
    public FileObject resolveFile(FileName name) throws FileSystemException {
        return null;
    }

    @Override
    public FileObject resolveFile(String name) throws FileSystemException {
        return null;
    }

    @Override
    public void addListener(FileObject file, FileListener listener) {

    }

    @Override
    public void removeListener(FileObject file, FileListener listener) {

    }

    @Override
    public void addJunction(String junctionPoint, FileObject targetFile) throws FileSystemException {

    }

    @Override
    public void removeJunction(String junctionPoint) throws FileSystemException {

    }

    @Override
    public File replicateFile(FileObject file, FileSelector selector) throws FileSystemException {
        return null;
    }

    @Override
    public FileSystemOptions getFileSystemOptions() {
        return null;
    }

    @Override
    public FileSystemManager getFileSystemManager() {
        try {
            return VFS.getManager();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public double getLastModTimeAccuracy() {
        return 0;
    }
}
