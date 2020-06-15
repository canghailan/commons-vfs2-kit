package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.provider.file.LocalFileSystem;
import org.junit.Test;

import java.net.URI;

public class TestFileSystem {
    @Test
    public void testFileSystemAdapterRead() {
        System.out.println(URI.create("http").getScheme());

        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        System.out.println(fileSystem.resolve("D:\\a.txt").toUri());
        System.out.println(fileSystem.get(fileSystem.resolve("D:\\a.txt")).readUtf8());
        System.out.println(fileSystem.get(fileSystem.resolve("D:\\a.txt")).readAttributes());
    }

    @Test
    public void testFileSystemAdapterWrite() {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        System.out.println(fileSystem.resolve("D:\\a.txt").toUri());
        fileSystem.get(fileSystem.resolve("D:\\a.txt")).writeUtf8("abcdefg");
    }

    @Test
    public void testFileSystemAdapterList() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
//        System.out.println(fileSystem.resolve("D:\\temp\\").toUri());
        try (FileStream<? extends File<?, ?>> fileStream = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree()) {
            for (File<?, ?> file : fileStream) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testFileSystemAdapterWalk() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
//        System.out.println(fileSystem.resolve("D:\\temp\\").toUri());
        try (FileStream<? extends File<?, ?>> fileStream = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree()) {
            for (File<?, ?> file : fileStream) {
                System.out.println(file);
            }
        }
        System.out.println();
    }
}
