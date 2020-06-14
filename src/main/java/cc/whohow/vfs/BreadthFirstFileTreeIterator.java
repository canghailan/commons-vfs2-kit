package cc.whohow.vfs;

import cc.whohow.vfs.selector.ImmutableFileSelectInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

import java.util.*;

public class BreadthFirstFileTreeIterator implements Iterator<FileSelectInfo> {
    protected final FileSelector fileSelector;
    protected final Deque<Iterator<? extends FileSelectInfo>> deque = new LinkedList<>();
    protected FileSelectInfo fileSelectInfo;

    public BreadthFirstFileTreeIterator(FileObject fileObject, FileSelector fileSelector) {
        this.fileSelector = fileSelector;
        this.deque.addFirst(Collections.singleton(new ImmutableFileSelectInfo(fileObject, fileObject, 0)).iterator());
    }

    @Override
    public boolean hasNext() {
        try {
            while (!deque.isEmpty()) {
                Iterator<? extends FileSelectInfo> iterator = deque.getFirst();
                if (iterator.hasNext()) {
                    fileSelectInfo = iterator.next();
                    if (fileSelectInfo.getFile().isFolder()) {
                        if (fileSelector.traverseDescendents(fileSelectInfo)) {
                            FileObject[] fileObjects = fileSelectInfo.getFile().getChildren();
                            List<FileSelectInfo> list = new ArrayList<>(fileObjects.length);
                            for (FileObject fileObject : fileObjects) {
                                list.add(new ImmutableFileSelectInfo(fileSelectInfo.getBaseFolder(), fileObject, fileSelectInfo.getDepth() + 1));
                            }
                            deque.addLast(list.iterator());
                        }
                    }
                    if (fileSelector.includeFile(fileSelectInfo)) {
                        return true;
                    } else {
                        fileSelectInfo.getFile().close();
                    }
                } else {
                    deque.removeFirst();
                }
            }
            return false;
        } catch (Exception e) {
            throw FileSystemExceptions.unchecked(e);
        }
    }

    @Override
    public FileSelectInfo next() {
        return fileSelectInfo;
    }
}
