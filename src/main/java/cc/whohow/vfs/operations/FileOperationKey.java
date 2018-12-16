package cc.whohow.vfs.operations;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.operations.FileOperation;

import java.util.Objects;

public class FileOperationKey {
    private final Class<? extends FileObject> fileType;
    private final Class<? extends FileOperation> operationType;

    public FileOperationKey(Class<? extends FileObject> fileType, Class<? extends FileOperation> operationType) {
        this.fileType = fileType;
        this.operationType = operationType;
    }

    public Class<? extends FileObject> getFileType() {
        return fileType;
    }

    public Class<? extends FileOperation> getOperationType() {
        return operationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileOperationKey)) return false;
        FileOperationKey that = (FileOperationKey) o;
        return Objects.equals(fileType, that.fileType) &&
                Objects.equals(operationType, that.operationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, operationType);
    }
}
