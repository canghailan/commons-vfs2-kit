package cc.whohow.vfs.tree;

import cc.whohow.vfs.selector.FileSelectorFilter;
import cc.whohow.vfs.selector.ImmutableFileSelectInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FileObjectFindTree extends Tree<FileSelectInfo> {
    private final FileSelectorFilter filter;

    public FileObjectFindTree(FileObject root,
                              FileSelector selector,
                              BiFunction<Function<FileSelectInfo, ? extends Stream<? extends FileSelectInfo>>, FileSelectInfo, Iterator<FileSelectInfo>> traverse) {
        super(new ImmutableFileSelectInfo<>(root, root, 0), new FindChildren(selector), traverse);
        this.filter = new FileSelectorFilter(selector);
    }

    @Override
    public Iterator<FileSelectInfo> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<FileSelectInfo> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                traverse.apply(getChildren, root), 0), false)
                .filter(filter::accept);
    }

    private static final class FindChildren implements Function<FileSelectInfo, Stream<FileSelectInfo>> {
        private final FileSelector selector;

        private FindChildren(FileSelector selector) {
            this.selector = selector;
        }

        @Override
        public Stream<FileSelectInfo> apply(FileSelectInfo selectInfo) {
            try {
                FileObject file = selectInfo.getFile();
                if (file.getType().hasChildren() && selector.traverseDescendents(selectInfo)) {
                   return Arrays.stream(file.getChildren())
                            .map(child -> new ImmutableFileSelectInfo<>(
                                    selectInfo.getBaseFolder(), child, selectInfo.getDepth() + 1));
                }
                return null;
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}
