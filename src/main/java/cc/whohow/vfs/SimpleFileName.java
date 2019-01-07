package cc.whohow.vfs;

import cc.whohow.vfs.path.PathParser;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;

import java.net.URI;

public interface SimpleFileName extends FileName {
    @Override
    default FileType getType() {
        return getPath().endsWith(SEPARATOR) ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    default String getBaseName() {
        return new PathParser(getPath()).getLastName();
    }

    @Override
    default String getPath() {
        return URI.create(getURI()).normalize().getRawPath();
    }

    @Override
    default String getPathDecoded() throws FileSystemException {
        return URI.create(getURI()).normalize().getPath();
    }

    @Override
    default String getExtension() {
        return new PathParser(getPath()).getExtension();
    }

    @Override
    default int getDepth() {
        return new PathParser(getPath()).getNameCount();
    }

    @Override
    default String getScheme() {
        return URI.create(getURI()).getScheme();
    }

    @Override
    default boolean isAncestor(FileName ancestor) {
        return ancestor.isDescendent(this);
    }

    @Override
    default boolean isDescendent(FileName descendent) {
        return isDescendent(descendent, NameScope.FILE_SYSTEM);
    }

    @Override
    default boolean isFile() throws FileSystemException {
        return getType() == FileType.FILE;
    }

    @Override
    default String getFriendlyURI() {
        return getURI();
    }

    @Override
    default int compareTo(FileName o) {
        return getURI().compareTo(o.getURI());
    }
}
