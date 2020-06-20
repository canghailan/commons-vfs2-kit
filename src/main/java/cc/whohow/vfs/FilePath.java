package cc.whohow.vfs;

import cc.whohow.fs.File;
import cc.whohow.fs.util.Paths;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

public class FilePath implements FileName {
    protected final FileSystemAdapter fileSystem;
    protected final File<?, ?> file;

    public FilePath(FileSystemAdapter fileSystem, File<?, ?> file) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(file);
        this.fileSystem = fileSystem;
        this.file = file;
    }

    /**
     * @see Path#getFileSystem()
     */
    public FileSystemAdapter getFileSystem() {
        return fileSystem;
    }

    /**
     * @see Path#toFile()
     */
    public File<?, ?> toFile() {
        return file;
    }

    @Override
    public String getBaseName() {
        return file.getName();
    }

    @Override
    public String getPath() {
        return file.getUri().getRawPath();
    }

    @Override
    public String getPathDecoded() throws FileSystemException {
        return file.getUri().getPath();
    }

    @Override
    public String getExtension() {
        return file.getExtension();
    }

    @Override
    public int getDepth() {
        return Paths.getNameCount(file.getUri().getPath());
    }

    @Override
    public String getScheme() {
        return file.getUri().getScheme();
    }

    @Override
    public String getURI() {
        return file.getUri().toString();
    }

    @Override
    public String getRootURI() {
        return fileSystem.getRootURI();
    }

    @Override
    public FileName getRoot() {
        return fileSystem.getRootName();
    }

    @Override
    public FilePath getParent() {
        File<?, ?> parent = file.getParent();
        if (parent == null) {
            return null;
        }
        return new FilePath(fileSystem, parent);
    }

    @Override
    public String getRelativeName(FileName name) throws FileSystemException {
        return Paths.relativize(file.getUri(), URI.create(name.getURI()));
    }

    @Override
    public boolean isAncestor(FileName ancestor) {
        return Paths.startsWith(URI.create(ancestor.getURI()), file.getUri());
    }

    @Override
    public boolean isDescendent(FileName descendent) {
        return Paths.startsWith(URI.create(descendent.getURI()), file.getUri());
    }

    @Override
    public boolean isDescendent(FileName descendent, NameScope nameScope) {
        if (nameScope == NameScope.CHILD) {
            return equals(descendent.getParent());
        } else {
            return Paths.startsWith(URI.create(descendent.getURI()), file.getUri());
        }
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return file.isRegularFile();
    }

    @Override
    public FileType getType() {
        return file.isDirectory() ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    public String getFriendlyURI() {
        return file.getPublicUri();
    }

    @Override
    public int compareTo(FileName o) {
        return getURI().compareTo(o.getURI());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof FilePath) {
            FilePath that = (FilePath) o;
            return file.equals(that.file);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return file.getPath().toString();
    }
}
