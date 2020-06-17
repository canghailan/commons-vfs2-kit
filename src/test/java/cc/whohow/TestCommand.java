package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

public class TestCommand {
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
    public void testCopyDir() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());

        File<?, ?> source = vfs.get(dir + "src/");
        File<?, ?> target = vfs.get(dir + "temp/");

        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(target.isDirectory());

        System.out.println("target.exists: " + target.exists());
        target.delete();
        System.out.println("target.exists: " + target.exists());
        Assert.assertFalse(target.exists());

        vfs.copyAsync(source, target).join();

        Assert.assertTrue(target.exists());
        Assert.assertEquals(tree(source), tree(target));

        System.out.println(tree(source));

        vfs.close();
    }

    @Test
    public void testMoveFileToDir() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());


        File<?, ?> source = vfs.get(dir + "test.txt");
        File<?, ?> target = vfs.get(dir + "temp-move/");

        String random = RandomContent.randomString(10);
        System.out.println(random);

        source.writeUtf8(random);

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isRegularFile());
        Assert.assertEquals(random, source.readUtf8());

        Assert.assertTrue(target.isDirectory());
        System.out.println("target.exists: " + target.exists());

        vfs.moveAsync(source, target).join();

        Assert.assertFalse(source.exists());
        Assert.assertTrue(target.resolve(source.getName()).exists());
        Assert.assertEquals(random, target.resolve(source.getName()).readUtf8());

        vfs.close();
    }

    @Test
    public void testMoveDir() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());


        File<?, ?> source = vfs.get(dir + "temp/");
        File<?, ?> target = vfs.get(dir + "temp-move/");

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(target.isDirectory());

        System.out.println("target.exists: " + target.exists());
        target.delete();
        System.out.println("target.exists: " + target.exists());
        Assert.assertFalse(target.exists());

        vfs.moveAsync(source, target).join();

        vfs.close();
    }
}
