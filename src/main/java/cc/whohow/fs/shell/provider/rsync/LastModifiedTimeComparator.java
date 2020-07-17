package cc.whohow.fs.shell.provider.rsync;

import cc.whohow.fs.FileAttributes;

import java.util.Comparator;

public class LastModifiedTimeComparator implements Comparator<FileAttributes> {
    @Override
    public int compare(FileAttributes f1, FileAttributes f2) {
        if (f1.size() != f2.size()) {
            return 1;
        }
        if (f1.lastModifiedTime().toMillis() > f2.lastModifiedTime().toMillis()) {
            return 1;
        }
        return 0;
    }
}
