package cc.whohow.vfs;

import cc.whohow.vfs.path.PathBuilder;
import cc.whohow.vfs.path.PathParser;
import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;

import java.net.URI;

public interface FileNameImpl extends FileName {
    @Override
    default String getBaseName() {
        return new PathParser(getPathDecoded()).getLastName();
    }

    @Override
    default String getPath() {
        return URI.create(getURI()).normalize().getRawPath();
    }

    @Override
    default String getPathDecoded() {
        return URI.create(getURI()).normalize().getPath();
    }

    @Override
    default String getExtension() {
        return new PathParser(getPathDecoded()).getExtension();
    }

    @Override
    default int getDepth() {
        return new PathParser(getPathDecoded()).getNameCount();
    }

    @Override
    default String getScheme() {
        return URI.create(getURI()).getScheme();
    }

    @Override
    default String getRootURI() {
        return new URIBuilder().setURI(getURI()).setPath("/").toString();
    }

    @Override
    default String getRelativeName(FileName name) throws FileSystemException {
        return PathBuilder.relativize(getURI(), name.getURI());
    }

    @Override
    default boolean isAncestor(FileName ancestor) {
        return ancestor.isDescendent(this);
    }

    @Override
    default boolean isDescendent(FileName descendent) {
        return descendent.getURI().startsWith(getURI());
    }

    @Override
    default boolean isDescendent(FileName descendent, NameScope nameScope) {
        if (isDescendent(descendent)) {
            if (nameScope == NameScope.CHILD) {
                return equals(descendent.getParent());
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean isFile() throws FileSystemException {
        return getType() == FileType.FILE;
    }

    @Override
    default FileType getType() {
        return getURI().endsWith("/") ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    default String getFriendlyURI() {
        return getURI();
    }

    @Override
    default int compareTo(FileName o) {
        return getFriendlyURI().compareTo(o.getFriendlyURI());
    }
}
