package cc.whohow.vfs;

import org.apache.commons.vfs2.FileObject;

import java.util.Collection;

public interface CanonicalNameFileObject extends FileObject {
    Collection<String> getCanonicalNames();
}
