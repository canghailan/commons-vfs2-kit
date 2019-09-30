package cc.whohow.vfs.operations.provider;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.operations.AbstractFileOperation;
import cc.whohow.vfs.operations.Read;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class DefaultReadOperation extends AbstractFileOperation<FileObject, ByteBuffer> implements Read {
    @Override
    public ByteBuffer apply(FileObject fileObject) {
        try (InputStream stream = fileObject.getInputStream()) {
            return IO.read(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
