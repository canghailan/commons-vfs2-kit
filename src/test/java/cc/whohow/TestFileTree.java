package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.provider.file.LocalFileSystem;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestFileTree {
    @Test
    public void testTree() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends File<?, ?>> tree = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree()) {
            for (File<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree0() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends File<?, ?>> tree = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree(0)) {
            for (File<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree1() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends File<?, ?>> tree = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree(1)) {
            for (File<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree2() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends File<?, ?>> tree = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree(2)) {
            for (File<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree3() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends File<?, ?>> tree = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree(3)) {
            for (File<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree4() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends File<?, ?>> tree = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree(4)) {
            for (File<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTreeWalker() throws Exception {
        try (Stream<Path> tree = Files.walk(Paths.get("D:\\temp\\"))) {
            tree.forEach(System.out::println);
        }
        System.out.println();
    }
}
