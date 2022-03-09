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
import java.util.stream.Collectors;

public class AliyunOSSCopy extends GenericFileCopy<AliyunOSSFile, AliyunOSSFile> {
    private static final Logger log = LogManager.getLogger(AliyunOSSCopy.class);
    protected final long multipartThreshold = 64 * 1024 * 1024; // 64MB
    protected final long partSize = 8 * 1024 * 1024; // 8MB

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
                oss.copyObject(
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
            } else {
                log.trace("uploadPartCopy: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                List<UploadPartCopyResult> parts = new ArrayList<>();
                InitiateMultipartUploadResult initiateMultipartUploadResult = oss.initiateMultipartUpload(
                        new InitiateMultipartUploadRequest(targetUri.getBucketName(), targetUri.getKey()));
                while (true) {
                    int partNumber = parts.size() + 1;
                    long beginIndex = parts.size() * partSize;
                    long endIndex = beginIndex + partSize;
                    if (endIndex < objectMetadata.getContentLength()) {
                        UploadPartCopyResult uploadPartCopyResult = oss.uploadPartCopy(new UploadPartCopyRequest(
                                sourceUri.getBucketName(), sourceUri.getKey(),
                                targetUri.getBucketName(), targetUri.getKey(),
                                initiateMultipartUploadResult.getUploadId(),
                                partNumber, beginIndex, partSize));
                        parts.add(uploadPartCopyResult);
                    } else {
                        UploadPartCopyResult uploadPartCopyResult = oss.uploadPartCopy(new UploadPartCopyRequest(
                                sourceUri.getBucketName(), sourceUri.getKey(),
                                targetUri.getBucketName(), targetUri.getKey(),
                                initiateMultipartUploadResult.getUploadId(),
                                partNumber, beginIndex, objectMetadata.getContentLength() - beginIndex));
                        parts.add(uploadPartCopyResult);
                        break;
                    }
                }
                oss.completeMultipartUpload(new CompleteMultipartUploadRequest(
                        initiateMultipartUploadResult.getBucketName(),
                        initiateMultipartUploadResult.getKey(),
                        initiateMultipartUploadResult.getUploadId(),
                        parts.stream().map(UploadPartCopyResult::getPartETag).collect(Collectors.toList())
                ));
            }
            return target;
        } else {
            return transferFile(source, target);
        }
    }

    @Override
    protected CompletableFuture<AliyunOSSFile> copyFileAsync(ExecutorService executor) {
        return copyFileAsync(source, target, executor);
    }

    @Override
    protected CompletableFuture<AliyunOSSFile> copyFileAsync(AliyunOSSFile source, AliyunOSSFile target, ExecutorService executor) {
        OSS oss = target.getFileSystem().getOSS();
        S3Uri sourceUri = source.getPath();
        S3Uri targetUri = target.getPath();
        ObjectMetadata objectMetadata = source.getFileSystem().getOSS().getObjectMetadata(sourceUri.getBucketName(), sourceUri.getKey());
        if (source.getFileSystem().getOSS().equals(target.getFileSystem().getOSS())) {
            if (objectMetadata.getContentLength() <= multipartThreshold) {
                log.trace("copyObject: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                return CompletableFuture.supplyAsync(() -> {
                    oss.copyObject(
                            sourceUri.getBucketName(), sourceUri.getKey(),
                            targetUri.getBucketName(), targetUri.getKey());
                    return target;
                }, executor);
            } else {
                log.trace("uploadPartCopyAsync: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                List<CompletableFuture<UploadPartCopyResult>> parts = new ArrayList<>();
                InitiateMultipartUploadResult initiateMultipartUploadResult = oss.initiateMultipartUpload(
                        new InitiateMultipartUploadRequest(targetUri.getBucketName(), targetUri.getKey()));
                while (true) {
                    int partNumber = parts.size() + 1;
                    long beginIndex = parts.size() * partSize;
                    long endIndex = beginIndex + partSize;
                    if (endIndex < objectMetadata.getContentLength()) {
                        parts.add(CompletableFuture.supplyAsync(() -> {
                            log.trace("uploadPartCopyAsync: oss://{}/{} -> oss://{}/{} #{}",
                                    sourceUri.getBucketName(), sourceUri.getKey(),
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    partNumber);
                            return oss.uploadPartCopy(new UploadPartCopyRequest(
                                    sourceUri.getBucketName(), sourceUri.getKey(),
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    initiateMultipartUploadResult.getUploadId(),
                                    partNumber, beginIndex, partSize));
                        }, executor));
                    } else {
                        parts.add(CompletableFuture.supplyAsync(() -> {
                            log.trace("uploadPartCopyAsync: oss://{}/{} -> oss://{}/{} #{}",
                                    sourceUri.getBucketName(), sourceUri.getKey(),
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    partNumber);
                            return oss.uploadPartCopy(new UploadPartCopyRequest(
                                    sourceUri.getBucketName(), sourceUri.getKey(),
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    initiateMultipartUploadResult.getUploadId(),
                                    partNumber, beginIndex, objectMetadata.getContentLength() - beginIndex));
                        }, executor));
                        break;
                    }
                }
                return CompletableFuture.allOf(parts.toArray(new CompletableFuture[0])).thenApplyAsync((ignore) -> {
                    List<PartETag> partETags = parts.stream()
                            .map(CompletableFuture::join)
                            .map(UploadPartCopyResult::getPartETag)
                            .collect(Collectors.toList());
                    log.trace("completeMultipartUploadCopyAsync: oss://{}/{} -> oss://{}/{}",
                            sourceUri.getBucketName(), sourceUri.getKey(),
                            targetUri.getBucketName(), targetUri.getKey());
                    oss.completeMultipartUpload(new CompleteMultipartUploadRequest(
                            initiateMultipartUploadResult.getBucketName(),
                            initiateMultipartUploadResult.getKey(),
                            initiateMultipartUploadResult.getUploadId(),
                            partETags
                    ));
                    return target;
                }, executor);
            }
        } else {
            if (objectMetadata.getContentLength() <= multipartThreshold) {
                log.trace("putObject: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                return CompletableFuture.supplyAsync(() -> {
                    oss.putObject(
                            targetUri.getBucketName(), targetUri.getKey(),
                            source.getFileSystem().getOSS().getObject(
                                    sourceUri.getBucketName(), sourceUri.getKey()).getObjectContent());
                    return target;
                }, executor);
            } else {
                log.trace("uploadPartAsync: oss://{}/{} -> oss://{}/{}",
                        sourceUri.getBucketName(), sourceUri.getKey(),
                        targetUri.getBucketName(), targetUri.getKey());
                OSS src = source.getFileSystem().getOSS();
                List<CompletableFuture<UploadPartResult>> parts = new ArrayList<>();
                InitiateMultipartUploadResult initiateMultipartUploadResult = oss.initiateMultipartUpload(
                        new InitiateMultipartUploadRequest(targetUri.getBucketName(), targetUri.getKey()));
                while (true) {
                    int partNumber = parts.size() + 1;
                    long beginIndex = parts.size() * partSize;
                    long endIndex = beginIndex + partSize;
                    if (endIndex < objectMetadata.getContentLength()) {
                        parts.add(CompletableFuture.supplyAsync(() -> {
                            log.trace("uploadPartAsync: oss://{}/{} -> oss://{}/{} #{}",
                                    sourceUri.getBucketName(), sourceUri.getKey(),
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    partNumber);

                            GetObjectRequest getObjectRequest = new GetObjectRequest(sourceUri.getBucketName(), sourceUri.getKey());
                            getObjectRequest.setRange(beginIndex, endIndex - 1);

                            return oss.uploadPart(new UploadPartRequest(
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    initiateMultipartUploadResult.getUploadId(),
                                    partNumber,
                                    src.getObject(getObjectRequest).getObjectContent(),
                                    partSize));
                        }, executor));
                    } else {
                        parts.add(CompletableFuture.supplyAsync(() -> {
                            log.trace("uploadPartAsync: oss://{}/{} -> oss://{}/{} #{}",
                                    sourceUri.getBucketName(), sourceUri.getKey(),
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    partNumber);

                            GetObjectRequest getObjectRequest = new GetObjectRequest(sourceUri.getBucketName(), sourceUri.getKey());
                            getObjectRequest.setRange(beginIndex, objectMetadata.getContentLength() - 1);

                            return oss.uploadPart(new UploadPartRequest(
                                    targetUri.getBucketName(), targetUri.getKey(),
                                    initiateMultipartUploadResult.getUploadId(),
                                    partNumber,
                                    src.getObject(getObjectRequest).getObjectContent(),
                                    objectMetadata.getContentLength() - beginIndex));
                        }, executor));
                        break;
                    }
                }
                return CompletableFuture.allOf(parts.toArray(new CompletableFuture[0])).thenApplyAsync((ignore) -> {
                    List<PartETag> partETags = parts.stream()
                            .map(CompletableFuture::join)
                            .map(UploadPartResult::getPartETag)
                            .collect(Collectors.toList());
                    log.trace("completeMultipartUploadAsync: oss://{}/{} -> oss://{}/{}",
                            sourceUri.getBucketName(), sourceUri.getKey(),
                            targetUri.getBucketName(), targetUri.getKey());
                    oss.completeMultipartUpload(new CompleteMultipartUploadRequest(
                            initiateMultipartUploadResult.getBucketName(),
                            initiateMultipartUploadResult.getKey(),
                            initiateMultipartUploadResult.getUploadId(),
                            partETags
                    ));
                    return target;
                }, executor);
            }
        }
    }

    @Override
    public String toString() {
        return "AliyunOSSCopy " + source + " " + target;
    }
}
