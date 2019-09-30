package cc.whohow.vfs.provider;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;

import java.util.Objects;

public class FileAttributeContentInfoFactory implements FileContentInfoFactory {
    @Override
    public FileContentInfo create(FileContent fileContent) throws FileSystemException {
        return new DefaultFileContentInfo(
                Objects.toString(fileContent.getAttribute("contentType"), null),
                Objects.toString(fileContent.getAttribute("contentEncoding"), null)
        );
    }
}
