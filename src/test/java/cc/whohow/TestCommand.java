package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Paths;

public class TestCommand {
    @Test
    public void testCopyDir() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:/"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());

        File<?, ?> source = vfs.get(dir + "src/");
        File<?, ?> target = vfs.get(dir + "temp/");

        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(target.isDirectory());

        System.out.println(target.exists());
        target.delete();
        System.out.println(target.exists());
        Assert.assertFalse(target.exists());

        vfs.copyAsync(source, target).join();

        vfs.close();
    }

    @Test
    public void testMoveFileToDir() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:/"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());


        File<?, ?> source = vfs.get(dir + "test.txt");
        File<?, ?> target = vfs.get(dir + "temp-move/");

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isRegularFile());
        Assert.assertTrue(target.isDirectory());
        System.out.println(target.exists());

        vfs.moveAsync(source, target).join();

        vfs.close();
    }

    @Test
    public void testMoveDir() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:/"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());


        File<?, ?> source = vfs.get(dir + "temp/");
        File<?, ?> target = vfs.get(dir + "temp-move/");

        Assert.assertTrue(source.exists());
        Assert.assertTrue(source.isDirectory());
        Assert.assertTrue(target.isDirectory());

        System.out.println(target.exists());
        target.delete();
        System.out.println(target.exists());
        Assert.assertFalse(target.exists());

        vfs.moveAsync(source, target).join();

        vfs.close();
    }
}
