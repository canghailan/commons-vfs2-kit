package cc.whohow.vfs.operations;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.FileOperation;
import org.apache.commons.vfs2.operations.FileOperationProvider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class FileOperationsProvider implements FileOperationProvider {
    private final Set<Class<? extends FileOperation>> operations = new CopyOnWriteArraySet<>();
    private final Map<FileOperationKey, MethodHandle> cache = new ConcurrentHashMap<>();

    public FileOperationsProvider(Class<? extends FileOperation>... operations) {
        this.operations.addAll(Arrays.asList(operations));
    }

    @Override
    public void collectOperations(Collection<Class<? extends FileOperation>> operationsList, FileObject file) throws FileSystemException {
        for (Class<? extends FileOperation> c : operations) {
            MethodHandle m = getOperationFactory(new FileOperationKey(file.getClass(), c));
            if (m != null) {
                operationsList.add(c);
            }
        }
    }

    @Override
    public FileOperation getOperation(FileObject file, Class<? extends FileOperation> operationClass) throws FileSystemException {
        MethodHandle m = getOperationFactory(new FileOperationKey(file.getClass(), operationClass));
        if (m == null) {
            return null;
        }
        try {
            return (FileOperation) m.invoke(file);
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private MethodHandle getOperationFactory(FileOperationKey fileOperationKey) {
        return cache.computeIfAbsent(fileOperationKey,
                (key) -> getOperationFactory(key.getOperationType(), key.getFileType()));
    }

    private MethodHandle getOperationFactory(Class<? extends FileOperation> operationClass, Class<? extends FileObject> fileObjectClass) {
        if (!operationClass.isInterface() && !Modifier.isAbstract(operationClass.getModifiers())) {
            MethodHandle m = lookupOperationFactory(operationClass, fileObjectClass);
            if (m != null) {
                return m;
            }
        }
        for (Class<?> o : operations) {
            if (operationClass.isAssignableFrom(o)) {
                MethodHandle m = lookupOperationFactory(operationClass, fileObjectClass);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }

    private MethodHandle lookupOperationFactory(Class<? extends FileOperation> operationClass, Class<? extends FileObject> fileObjectClass) {
        try {
            return MethodHandles.publicLookup()
                    .findConstructor(operationClass, MethodType.methodType(void.class, fileObjectClass));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }
}
