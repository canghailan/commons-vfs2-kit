package cc.whohow.vfs;

import org.apache.commons.vfs2.*;

public class UnknownFileName implements FileName {
    private final FileObject fileObject;

    public UnknownFileName(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public String getBaseName() {
        return "unknown-" + fileObject.hashCode();
    }

    @Override
    public String getPath() {
        return "/" + getBaseName();
    }

    @Override
    public String getPathDecoded() throws FileSystemException {
        return getPath();
    }

    @Override
    public String getExtension() {
        return "";
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public String getScheme() {
        return "unknown";
    }

    @Override
    public String getURI() {
        return "unknown:///" + getBaseName();
    }

    @Override
    public String getRootURI() {
        return "unknown:///";
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
        return getURI();
    }

    @Override
    public int compareTo(FileName o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof UnknownFileName) {
            UnknownFileName that = (UnknownFileName) o;
            return fileObject.equals(that.fileObject);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileObject.hashCode();
    }

    @Override
    public String toString() {
        return getURI();
    }
}
