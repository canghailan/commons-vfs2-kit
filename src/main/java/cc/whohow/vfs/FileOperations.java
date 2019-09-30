package cc.whohow.vfs;

public interface FileOperations extends org.apache.commons.vfs2.operations.FileOperations {
    <T, R> FileOperation<T, R> getOperation(Class<? extends FileOperation<T, R>> fileOperation, T args);
}
