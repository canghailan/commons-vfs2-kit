package cc.whohow.vfs;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;

import java.net.URLConnection;
import java.util.Optional;

public class FileAttributeContentInfoFactory implements FileContentInfoFactory {
    @Override
    public FileContentInfo create(FileContent fileContent) throws FileSystemException {
        String contentType = getAttribute(fileContent, "Content-Type", "contentType")
                .map(Object::toString)
                .orElse(probeContentType(fileContent));
        String contentEncoding = getAttribute(fileContent, "Content-Encoding", "contentEncoding")
                .map(Object::toString)
                .orElse(null);
        return new DefaultFileContentInfo(contentType, contentEncoding);
    }

    protected Optional<?> getAttribute(FileContent fileContent, String... attrNames) throws FileSystemException {
        for (String attrName : attrNames) {
            Object attr = fileContent.getAttribute(attrName);
            if (attr != null) {
                return Optional.of(attr);
            }
        }
        return Optional.empty();
    }

    protected String probeContentType(FileContent fileContent) {
        return URLConnection.getFileNameMap().getContentTypeFor(fileContent.getFile().getName().getBaseName());
    }
}
