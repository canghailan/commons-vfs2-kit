package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;

import java.nio.file.DirectoryStream;
import java.util.Set;
import java.util.TreeSet;

public class TestFiles {
    public static String list(File<?, ? extends File<?, ?>> file) throws Exception {
        Set<String> files = new TreeSet<>();
        try (DirectoryStream<? extends File<?, ?>> stream = file.newDirectoryStream()) {
            for (File<?, ?> f : stream) {
                files.add(file.getPath().relativize(f.getPath()));
            }
        }
        return String.join("\n", files);
    }

    public static String tree(File<?, ? extends File<?, ?>> file) throws Exception {
        Set<String> files = new TreeSet<>();
        try (FileStream<? extends File<?, ?>> stream = file.tree()) {
            for (File<?, ?> f : stream) {
                files.add(file.getPath().relativize(f.getPath()));
            }
        }
        return String.join("\n", files);
    }

    public static String treeFile(File<?, ? extends File<?, ?>> file) throws Exception {
        Set<String> files = new TreeSet<>();
        try (FileStream<? extends File<?, ?>> stream = file.tree()) {
            for (File<?, ?> f : stream) {
                if (f.isRegularFile()) {
                    files.add(file.getPath().relativize(f.getPath()));
                }
            }
        }
        return String.join("\n", files);
    }
}
