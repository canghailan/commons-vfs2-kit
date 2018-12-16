package cc.whohow.vfs.provider.uri;

import cc.whohow.vfs.path.PathParser;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;

import java.net.URI;
import java.util.Objects;

public class UriFileName implements FileName {
    private final String uri;

    public UriFileName(String uri) {
        this.uri = uri;
    }

    @Override
    public String getBaseName() {
        return new PathParser(getPath()).getLastName();
    }

    @Override
    public String getPath() {
        return URI.create(uri).normalize().getPath();
    }

    @Override
    public String getPathDecoded() throws FileSystemException {
        return getPath();
    }

    @Override
    public String getExtension() {
        return new PathParser(getPath()).getExtension();
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public String getScheme() {
        return URI.create(uri).getScheme();
    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public String getRootURI() {
        return null;
    }

    @Override
    public FileName getRoot() {
        return null;
    }

    @Override
    public FileName getParent() {
        return null;
    }

    @Override
    public String getRelativeName(FileName name) throws FileSystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAncestor(FileName ancestor) {
        return false;
    }

    @Override
    public boolean isDescendent(FileName descendent) {
        return false;
    }

    @Override
    public boolean isDescendent(FileName descendent, NameScope nameScope) {
        return false;
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return true;
    }

    @Override
    public FileType getType() {
        return FileType.FILE;
    }

    @Override
    public String getFriendlyURI() {
        return uri;
    }

    @Override
    public int compareTo(FileName o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UriFileName)) return false;
        UriFileName that = (UriFileName) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return uri;
    }
}
