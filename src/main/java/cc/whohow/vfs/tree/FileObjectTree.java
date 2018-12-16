package cc.whohow.vfs.tree;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class FileObjectTree extends Tree<FileObject> {
    public FileObjectTree(FileObject root,
                          BiFunction<Function<FileObject, ? extends Stream<? extends FileObject>>, FileObject, Iterator<FileObject>> traverse) {
        super(root, FileObjectTree::hasChildren, FileObjectTree::getChildren, traverse);
    }

    private static boolean hasChildren(FileObject fileObject) {
        try {
            return fileObject.getType().hasChildren();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static FileObject[] getChildren(FileObject fileObject) {
        try {
            return fileObject.getChildren();
        } catch (FileSystemException e) {
            throw new UncheckedIOException(e);
        }
    }
}
