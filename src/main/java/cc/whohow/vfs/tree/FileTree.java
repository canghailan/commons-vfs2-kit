package cc.whohow.vfs.tree;

import java.io.File;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FileTree extends Tree<File> {
    public FileTree(File root,
                    BiFunction<Function<File, ? extends Iterator<? extends File>>, File, Iterator<File>> traverse) {
        super(root, File::isDirectory, File::listFiles, traverse);
    }
}
