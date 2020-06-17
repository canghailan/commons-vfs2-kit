package cc.whohow.vfs;

import cc.whohow.fs.util.Paths;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;

import java.net.URI;
import java.util.Objects;

public class UriFileName implements FileName {
    protected final URI uri;

    public UriFileName(URI uri) {
        Objects.requireNonNull(uri);
        this.uri = uri;
    }

    @Override
    public String getBaseName() {
        return Paths.getName(uri.getPath());
    }

    @Override
    public String getPath() {
        return uri.getRawPath();
    }

    @Override
    public String getPathDecoded() throws FileSystemException {
        return uri.getPath();
    }

    @Override
    public String getExtension() {
        return Paths.getExtension(getBaseName());
    }

    @Override
    public int getDepth() {
        return Paths.getNameCount(uri.getPath());
    }

    @Override
    public String getScheme() {
        return uri.getScheme();
    }

    @Override
    public String getURI() {
        return uri.toString();
    }

    @Override
    public String getRootURI() {
        // TODO
        return null;
    }

    @Override
    public FileName getRoot() {
        // TODO
        return null;
    }

    @Override
    public FileName getParent() {
        // TODO
        return null;
    }

    @Override
    public String getRelativeName(FileName name) throws FileSystemException {
        return Paths.relativize(uri, URI.create(name.getURI()));
    }

    @Override
    public boolean isAncestor(FileName ancestor) {
        return Paths.startsWith(URI.create(ancestor.getURI()), uri);
    }

    @Override
    public boolean isDescendent(FileName descendent) {
        return Paths.startsWith(URI.create(descendent.getURI()), uri);
    }

    @Override
    public boolean isDescendent(FileName descendent, NameScope nameScope) {
        if (nameScope == NameScope.CHILD) {
            return equals(descendent.getParent());
        } else {
            return Paths.startsWith(URI.create(descendent.getURI()), uri);
        }
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return getType() == FileType.FILE;
    }

    @Override
    public FileType getType() {
        return (uri.getPath() != null && uri.getPath().endsWith("/")) ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    public String getFriendlyURI() {
        return uri.toString();
    }

    @Override
    public int compareTo(FileName o) {
        return getURI().compareTo(o.getURI());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UriFileName) {
            UriFileName that = (UriFileName) o;
            return uri.equals(that.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
