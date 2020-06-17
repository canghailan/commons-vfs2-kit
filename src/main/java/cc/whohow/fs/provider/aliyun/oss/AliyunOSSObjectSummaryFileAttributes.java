package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Attribute;
import cc.whohow.fs.Attributes;
import cc.whohow.fs.FileAttributes;
import cc.whohow.fs.attribute.DateAttribute;
import cc.whohow.fs.attribute.LongAttribute;
import cc.whohow.fs.attribute.StringAttribute;
import cc.whohow.fs.util.FileTimes;
import com.aliyun.oss.model.OSSObjectSummary;

import java.nio.file.attribute.FileTime;
import java.util.*;

public class AliyunOSSObjectSummaryFileAttributes implements FileAttributes {
    private final OSSObjectSummary objectSummary;

    public AliyunOSSObjectSummaryFileAttributes(OSSObjectSummary objectSummary) {
        this.objectSummary = objectSummary;
    }

    @Override
    public FileTime lastModifiedTime() {
        Date date = objectSummary.getLastModified();
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
        return objectSummary.getSize();
    }

    public Date getLastModified() {
        return objectSummary.getLastModified();
    }

    public String getETag() {
        return objectSummary.getETag();
    }

    public String getStorageClass() {
        return objectSummary.getStorageClass();
    }

    @Override
    public Optional<? extends Attribute<?>> get(String name) {
        Objects.requireNonNull(name);
        switch (name) {
            case AliyunOSSFileAttributes.LAST_MODIFIED:
                return Optional.of(new DateAttribute(AliyunOSSFileAttributes.LAST_MODIFIED, getLastModified()));
            case SIZE:
                return Optional.of(new LongAttribute(SIZE, size()));
            case AliyunOSSFileAttributes.ETAG:
                return Optional.of(new StringAttribute(AliyunOSSFileAttributes.ETAG, getETag()));
            case AliyunOSSFileAttributes.STORAGE_CLASS:
                return Optional.of(new StringAttribute(AliyunOSSFileAttributes.STORAGE_CLASS, getStorageClass()));
            default:
                return Optional.empty();
        }
    }

    @Override
    public Iterator<Attribute<?>> iterator() {
        return Arrays.<Attribute<?>>asList(
                new DateAttribute(AliyunOSSFileAttributes.LAST_MODIFIED, objectSummary.getLastModified()),
                new LongAttribute(SIZE, size()),
                new StringAttribute(AliyunOSSFileAttributes.ETAG, getETag()),
                new StringAttribute(AliyunOSSFileAttributes.STORAGE_CLASS, getStorageClass())
        ).iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AliyunOSSObjectSummaryFileAttributes) {
            AliyunOSSObjectSummaryFileAttributes that = (AliyunOSSObjectSummaryFileAttributes) o;
            return objectSummary.equals(that.objectSummary);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return objectSummary.hashCode();
    }

    @Override
    public String toString() {
        return Attributes.toString(this);
    }
}
