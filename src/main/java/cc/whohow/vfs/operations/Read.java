package cc.whohow.vfs.operations;

import cc.whohow.vfs.FileObject;
import cc.whohow.vfs.FileOperation;

import java.nio.ByteBuffer;

public interface Read extends FileOperation<FileObject, ByteBuffer> {
}
