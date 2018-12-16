package cc.whohow.vfs.operations;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.operations.FileOperation;
import org.apache.commons.vfs2.operations.FileOperationProvider;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.function.Function;

public class FileOperationFactoryProvider<F extends FileObject, O extends FileOperation> implements FileOperationProvider {
    private final Class<F> fileType;
    private final Class<O> operationType;
    private final Function<F, O> factory;

    public FileOperationFactoryProvider(Class<F> fileType, Class<O> operationType, Function<F, O> factory) {
        this.fileType = fileType;
        this.operationType = operationType;
        this.factory = factory;
    }

    public void register(String... schemes) {
        try {
            VFS.getManager().addOperationProvider(schemes, this);
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void collectOperations(Collection<Class<? extends FileOperation>> operationsList, FileObject file) throws FileSystemException {
        if (fileType.isInstance(file)) {
            operationsList.add(operationType);
        }
    }

    @Override
    public FileOperation getOperation(FileObject file, Class<? extends FileOperation> operationClass) throws FileSystemException {
        if (fileType.isInstance(file) && operationClass.isAssignableFrom(operationType)) {
            return factory.apply(fileType.cast(file));
        }
        return null;
    }
}
