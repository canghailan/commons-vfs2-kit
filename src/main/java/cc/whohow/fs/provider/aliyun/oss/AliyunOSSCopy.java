package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.provider.GenericFileCopy;
import cc.whohow.fs.provider.s3.S3Uri;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AliyunOSSCopy extends GenericFileCopy<AliyunOSSFile, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSCopy.class);
    protected final long multipartThreshold = 512 * 1024 * 1024; // 512MB
    protected final long partSize = 64 * 1024 * 1024; // 64MB

    public AliyunOSSCopy(AliyunOSSFile source, AliyunOSSFile target) {
        super(source, target);
    }

    protected AliyunOSSFile copyFile(AliyunOSSFile source, AliyunOSSFile target) throws Exception {
        if (source.getFileSystem().getOSS().equals(target.getFileSystem().getOSS())) {
            OSS oss = source.getFileSystem().getOSS();
            S3Uri sourceUri = source.getPath();
            S3Uri targetUri = target.getPath();
            ObjectMetadata objectMetadata = oss.getObjectMetadata(sourceUri.getBucketName(), sourceUri.getKey());
            if (objectMetadata.getContentLength() <= multipartThreshold) {
                log.trace("copyObject: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                source.getFileSystem().getOSS().copyObject(
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
            } else {
                log.trace("uploadPartCopy: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                List<PartETag> partETags = new ArrayList<>();
                InitiateMultipartUploadResult initiateMultipartUploadResult = oss.initiateMultipartUpload(
                        new InitiateMultipartUploadRequest(targetUri.getBucketName(), targetUri.getKey()));
                while (true) {
                    int partNumber = partETags.size() + 1;
                    long beginIndex = partETags.size() * partSize;
                    long endIndex = beginIndex + partSize;
                    if (endIndex < objectMetadata.getContentLength()) {
                        UploadPartCopyResult uploadPartCopyResult = oss.uploadPartCopy(new UploadPartCopyRequest(
                                sourceUri.getBucketName(), sourceUri.getKey(),
                                targetUri.getBucketName(), targetUri.getKey(),
                                initiateMultipartUploadResult.getUploadId(),
                                partNumber, beginIndex, partSize));
                        partETags.add(uploadPartCopyResult.getPartETag());
                    } else {
                        UploadPartCopyResult uploadPartCopyResult = oss.uploadPartCopy(new UploadPartCopyRequest(
                                sourceUri.getBucketName(), sourceUri.getKey(),
                                targetUri.getBucketName(), targetUri.getKey(),
                                initiateMultipartUploadResult.getUploadId(),
                                partNumber, beginIndex, objectMetadata.getContentLength() - beginIndex));
                        partETags.add(uploadPartCopyResult.getPartETag());
                        break;
                    }
                }
                oss.completeMultipartUpload(new CompleteMultipartUploadRequest(
                        initiateMultipartUploadResult.getBucketName(),
                        initiateMultipartUploadResult.getKey(),
                        initiateMultipartUploadResult.getUploadId(),
                        partETags
                ));
            }
            return target;
        } else {
            return transferFile(source, target);
        }
    }

    @Override
    protected CompletableFuture<AliyunOSSFile> copyFileAsync(AliyunOSSFile source, AliyunOSSFile target, ExecutorService executor) {
        return new AliyunOSSCopy(source, target).copyFileAsync(executor);
    }

    @Override
    public String toString() {
        return "AliyunOSSCopy " + source + " " + target;
    }
}
