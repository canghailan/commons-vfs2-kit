package cc.whohow.vfs.provider.s3;

import cc.whohow.vfs.FileObjectX;
import cc.whohow.vfs.version.FileAttributeVersionProvider;
import cc.whohow.vfs.version.FileVersion;
import com.aliyun.oss.internal.OSSHeaders;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;

public class S3FileVersionProvider extends FileAttributeVersionProvider<String> {
    public S3FileVersionProvider() {
        super(OSSHeaders.ETAG);
    }

    @Override
    public FileVersion<String> getVersion(FileObjectX fileObject) {
        try {
            if (fileObject.isFile()) {
                return super.getVersion(fileObject);
            } else {
                return null;
            }
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
