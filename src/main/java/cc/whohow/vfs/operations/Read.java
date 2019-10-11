package cc.whohow.vfs.operations;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileOperation;

import java.nio.ByteBuffer;

public interface Read extends CloudFileOperation<CloudFileObject, ByteBuffer> {
}
