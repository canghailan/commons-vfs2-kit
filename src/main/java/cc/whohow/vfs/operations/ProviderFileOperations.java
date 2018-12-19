package cc.whohow.vfs.operations;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperation;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperations;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderFileOperations implements FileOperations {
    private static final Map<FileOperationKey, FileOperationProvider> CACHE = new ConcurrentHashMap<>();
    private final FileObject fileObject;

    public ProviderFileOperations(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends FileOperation>[] getOperations() throws FileSystemException {
        Collection<Class<? extends FileOperation>> operations = new HashSet<>();
        for (FileOperationProvider provider : getProviders()) {
            provider.collectOperations(operations, fileObject);
        }
        return operations.toArray(new Class[0]);
    }

    @Override
    public FileOperation getOperation(Class<? extends FileOperation> operationClass) throws FileSystemException {
        FileOperationProvider provider = CACHE.computeIfAbsent(
                new FileOperationKey(fileObject.getClass(), operationClass),
                (key) -> {
                    try {
                        return getProvider(key.getOperationType());
                    } catch (FileSystemException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                });
        if (provider == null) {
            throw new FileSystemException("vfs.operation/operation-not-supported.error", operationClass.getCanonicalName());
        }
        return provider.getOperation(fileObject, operationClass);
    }

    @Override
    public boolean hasOperation(Class<? extends FileOperation> operationClass) throws FileSystemException {
        return getProvider(operationClass) != null;
    }

    private FileOperationProvider getProvider(Class<? extends FileOperation> operationClass) throws FileSystemException {
        FileOperationProvider[] providers = getProviders();
        if (providers == null || providers.length == 0) {
            return null;
        }
        for (FileOperationProvider provider : providers) {
            if (provider.getOperation(fileObject, operationClass) != null) {
                return provider;
            }
        }
        return null;
    }

    private FileOperationProvider[] getProviders() throws FileSystemException {
        return fileObject.getFileSystem().getFileSystemManager().getOperationProviders(fileObject.getName().getScheme());
    }
}
