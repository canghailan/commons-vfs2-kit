package cc.whohow.vfs;

public interface CloudFileOperations extends org.apache.commons.vfs2.operations.FileOperations {
    <T, R> CloudFileOperation<T, R> getOperation(Class<? extends CloudFileOperation<T, R>> fileOperation, T args);
}
