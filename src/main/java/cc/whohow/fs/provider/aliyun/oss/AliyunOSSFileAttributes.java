package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.attribute.DateAttribute;
import cc.whohow.fs.attribute.LongAttribute;
import cc.whohow.fs.attribute.StringAttribute;
import cc.whohow.fs.util.FileTimes;
import cc.whohow.fs.util.IteratorIterator;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.ObjectMetadata;

import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

public class AliyunOSSFileAttributes implements FileAttributes {
    public static final String CONTENT_ENCODING = OSSHeaders.CONTENT_ENCODING;
    public static final String CONTENT_LENGTH = OSSHeaders.CONTENT_LENGTH;
    public static final String CONTENT_MD5 = OSSHeaders.CONTENT_MD5;
    public static final String CONTENT_TYPE = OSSHeaders.CONTENT_TYPE;
    public static final String ETAG = OSSHeaders.ETAG;
    public static final String LAST_MODIFIED = OSSHeaders.LAST_MODIFIED;
    public static final String STORAGE_CLASS = OSSHeaders.STORAGE_CLASS;

    private final ObjectMetadata objectMetadata;

    public AliyunOSSFileAttributes(ObjectMetadata objectMetadata) {
        this.objectMetadata = objectMetadata;
    }

    @Override
    public FileTime lastModifiedTime() {
        Date date = objectMetadata.getLastModified();
        return (date == null) ? FileTimes.epoch() : FileTime.from(date.toInstant());
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTimes.epoch();
    }

    @Override
    public FileTime creationTime() {
        return FileTimes.epoch();
    }

    @Override
    public long size() {
        return objectMetadata.getContentLength();
    }

    public Date getLastModified() {
        return objectMetadata.getLastModified();
    }

    public long getContentLength() {
        return objectMetadata.getContentLength();
    }

    public String getContentType() {
        return objectMetadata.getContentType();
    }

    public String getContentMD5() {
        return objectMetadata.getContentMD5();
    }

    public String getContentEncoding() {
        return objectMetadata.getContentEncoding();
    }

    public String getETag() {
        return objectMetadata.getETag();
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        if (name.startsWith(OSSHeaders.OSS_USER_METADATA_PREFIX)) {
            String key = name.substring(OSSHeaders.OSS_USER_METADATA_PREFIX.length());
            String value = objectMetadata.getUserMetadata().get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(new StringAttribute(name, value));
        } else {
            Object value = objectMetadata.getRawMetadata().get(name);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(toAttribute(name, value));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue(String name) {
        if (name.startsWith(OSSHeaders.OSS_USER_METADATA_PREFIX)) {
            String key = name.substring(OSSHeaders.OSS_USER_METADATA_PREFIX.length());
            return Optional.ofNullable((T) objectMetadata.getUserMetadata().get(key));
        } else {
            return Optional.ofNullable((T) objectMetadata.getRawMetadata().get(name));
        }
    }

    private Attribute<?> toAttribute(String name, Object value) {
        if (value instanceof String) {
            return new StringAttribute(name, (String) value);
        }
        if (value instanceof Date) {
            return new DateAttribute(name, (Date) value);
        }
        if (value instanceof Long) {
            return new LongAttribute(name, (Long) value);
        }
        return new StringAttribute(name, value.toString());
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return new IteratorIterator<>(Arrays.asList(
                objectMetadata.getRawMetadata().entrySet().stream()
                        .map(e -> toAttribute(e.getKey(), e.getValue()))
                        .iterator(),
                objectMetadata.getUserMetadata().entrySet().stream()
                        .map(e -> new StringAttribute(OSSHeaders.OSS_USER_METADATA_PREFIX + e.getKey(), e.getValue()))
                        .iterator()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AliyunOSSFileAttributes) {
            AliyunOSSFileAttributes that = (AliyunOSSFileAttributes) o;
            return objectMetadata.equals(that.objectMetadata);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return objectMetadata.hashCode();
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
