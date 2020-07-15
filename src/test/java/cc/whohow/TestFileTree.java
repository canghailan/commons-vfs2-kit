package cc.whohow;

import cc.whohow.fs.FileStream;
import cc.whohow.fs.GenericFile;
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
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree()) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree0() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(0)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree1() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(1)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree2() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(2)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree3() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(3)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree4() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(4)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree5() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(5)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTree6() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        try (FileStream<? extends GenericFile<?, ?>> tree = fileSystem.get(fileSystem.resolve(Paths.get("src/").toAbsolutePath().toUri())).tree(6)) {
            for (GenericFile<?, ?> file : tree) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testTreeWalker() throws Exception {
        try (Stream<Path> tree = Files.walk(Paths.get(Paths.get("src/").toAbsolutePath().toUri()))) {
            tree.forEach(System.out::println);
        }
        System.out.println();
    }
}
