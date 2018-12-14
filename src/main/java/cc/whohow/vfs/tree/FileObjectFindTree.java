package cc.whohow.vfs.tree;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FileObjectFindTree extends Tree<FileSelectInfo> {
    public FileObjectFindTree(FileObject root,
                              FileSelector selector,
                              BiFunction<Function<FileSelectInfo, ? extends Iterator<? extends FileSelectInfo>>, FileSelectInfo, Iterator<FileSelectInfo>> traverse) {
        super(new ImmutableFileSelectInfo<>(root, root, 0), new FindChildren(selector), traverse);
    }

    private static final class ImmutableFileSelectInfo<T extends FileObject> implements FileSelectInfo {
        private final T baseFolder;
        private final T file;
        private final int depth;

        public ImmutableFileSelectInfo(T baseFolder, T file, int depth) {
            this.baseFolder = baseFolder;
            this.file = file;
            this.depth = depth;
        }

        @Override
        public T getBaseFolder() {
            return baseFolder;
        }

        @Override
        public T getFile() {
            return file;
        }

        @Override
        public int getDepth() {
            return depth;
        }
    }

    private static final class FindChildren implements Function<FileSelectInfo, Iterator<FileSelectInfo>> {
        private final FileSelector selector;

        private FindChildren(FileSelector selector) {
            this.selector = selector;
        }

        @Override
        public Iterator<FileSelectInfo> apply(FileSelectInfo selectInfo) {
            try {
                FileObject file = selectInfo.getFile();
                if (file.getType().hasChildren() && selector.traverseDescendents(selectInfo)) {
                    FileObject[] children = file.getChildren();
                    List<FileSelectInfo> list = new ArrayList<>(children.length);
                    for (FileObject child : children) {
                        FileSelectInfo childSelectInfo = new ImmutableFileSelectInfo<>(
                                selectInfo.getBaseFolder(), child, selectInfo.getDepth() + 1);
                        if (selector.includeFile(childSelectInfo)) {
                            list.add(childSelectInfo);
                        }
                    }
                    return list.iterator();
                }
                return null;
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}
