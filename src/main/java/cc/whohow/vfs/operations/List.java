package cc.whohow.vfs.operations;

import cc.whohow.vfs.CloudFileObject;
import cc.whohow.vfs.CloudFileOperation;

import java.nio.file.DirectoryStream;

public interface List extends CloudFileOperation<CloudFileObject, DirectoryStream<CloudFileObject>> {
}
