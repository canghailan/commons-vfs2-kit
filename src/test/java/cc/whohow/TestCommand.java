package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestCommand {
    private static String base;
    private static VirtualFileSystem vfs;

    @BeforeClass
    public static void beforeClass() {
        base = Paths.get(".").toUri().normalize().toString();
        vfs = new DefaultVirtualFileSystem(new ConfigurationBuilder().build());
        vfs.load(new LocalFileProvider());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    private String tree(File<?, ? extends File<?, ?>> file) throws Exception {
        StringBuilder buffer = new StringBuilder();
        try (FileStream<? extends File<?, ?>> stream = file.tree()) {
            for (File<?, ?> f : stream) {
                buffer.append(file.getPath().relativize(f.getPath())).append("\n");
            }
        }
        return buffer.toString();
    }

    @Test
    public void testCopyFile() throws Exception {
        File<?, ?> source = vfs.get(base + "test.txt");
        File<?, ?> target = vfs.get(base + "temp/test.txt");

        source.writeUtf8(RandomContent.randomString(30));
        System.out.println(source.readUtf8());

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isRegularFile());
        Assert.assertTrue(target.isRegularFile());

        System.out.println("target.exists: " + target.exists());
        target.delete();
        Assert.assertFalse(target.exists());
        System.out.println("target.exists: " + target.exists());

        vfs.copyAsync(source, target).join();

        Assert.assertTrue(target.exists());
        Assert.assertEquals(source.readUtf8(), target.readUtf8());

        System.out.println(target.readUtf8());
    }

    @Test
    public void testCopyDir() throws Exception {
        File<?, ?> source = vfs.get(base + "src/");
        File<?, ?> target = vfs.get(base + "temp/");

        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(target.isDirectory());

        System.out.println("target.exists: " + target.exists());
        target.delete();
        Assert.assertFalse(target.exists());
        System.out.println("target.exists: " + target.exists());

        vfs.copyAsync(source, target).join();

        Assert.assertTrue(target.exists());
        Assert.assertEquals(tree(source), tree(target));

        System.out.println(tree(source));
    }

    @Test
    public void testCopyFileToDir() throws Exception {
        File<?, ?> source = vfs.get(base + "test.txt");
        File<?, ?> target = vfs.get(base + "temp/");

        source.writeUtf8(RandomContent.randomString(30));
        System.out.println(source.readUtf8());

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isRegularFile());

        target.delete();
        Assert.assertFalse(target.exists());
        System.out.println("target.exists: " + target.exists());

        vfs.copyAsync(source, target).join();

        Assert.assertTrue(source.exists());
        Assert.assertTrue(target.resolve(source.getName()).exists());
        Assert.assertEquals(source.readUtf8(), target.resolve(source.getName()).readUtf8());

        System.out.println(target.resolve(source.getName()).readUtf8());
    }

    @Test
    public void testMoveFile() throws Exception {
        File<?, ?> source = vfs.get(base + "test.txt");
        File<?, ?> target = vfs.get(base + "temp/test.txt");

        source.writeUtf8(RandomContent.randomString(30));
        String content = source.readUtf8();
        System.out.println(source.readUtf8());

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isRegularFile());
        Assert.assertTrue(target.isRegularFile());

        System.out.println("target.exists: " + target.exists());
        target.delete();
        Assert.assertFalse(target.exists());
        System.out.println("target.exists: " + target.exists());

        vfs.moveAsync(source, target).join();

        Assert.assertFalse(source.exists());
        Assert.assertTrue(target.exists());
        Assert.assertEquals(content, target.readUtf8());

        System.out.println(target.readUtf8());
    }

    @Test
    public void testMoveDir() throws Exception {
        File<?, ?> source = vfs.get(base + "temp/");
        File<?, ?> target = vfs.get(base + "temp-move/");

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(target.isDirectory());

        System.out.println("target.exists: " + target.exists());
        target.delete();
        System.out.println("target.exists: " + target.exists());
        Assert.assertFalse(target.exists());

        vfs.moveAsync(source, target).join();
    }

    @Test
    public void testMoveFileToDir() throws Exception {
        File<?, ?> source = vfs.get(base + "test.txt");
        File<?, ?> target = vfs.get(base + "temp/");

        source.writeUtf8(RandomContent.randomString(30));
        String content = source.readUtf8();
        System.out.println(source.readUtf8());

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isRegularFile());

        target.delete();
        Assert.assertFalse(target.exists());
        Assert.assertTrue(target.isDirectory());
        System.out.println("target.exists: " + target.exists());

        vfs.moveAsync(source, target).join();

        Assert.assertFalse(source.exists());
        Assert.assertTrue(target.resolve(source.getName()).exists());
        Assert.assertEquals(content, target.resolve(source.getName()).readUtf8());

        System.out.println(target.resolve(source.getName()).readUtf8());
    }

    @Test
    public void testDeadlock() throws Exception {
        File<?, ?> source = vfs.get(base + "src/");
        File<?, ?> target = vfs.get(base + "temp/");

        target.delete();

        List<CompletableFuture<? extends File<?, ?>>> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(vfs.copyAsync(source, target.resolve(i + "/")));
        }

        list.forEach(CompletableFuture::join);
        target.delete();
    }

    @Test
    public void clean() {
        File<?, ?> target = vfs.get(base + "temp/");
        target.delete();
    }
}
