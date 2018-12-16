package cc.whohow.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;

public interface UnknownFileContent extends FileContent {
    FileContentInfo UNKNOWN = new DefaultFileContentInfo(null, null);

    @Override
    default FileContentInfo getContentInfo() throws FileSystemException {
        return UNKNOWN;
    }
}
