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
    public void testCopy() throws Exception {
        String dir = Paths.get(".").toUri().normalize().toString();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:///"));

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
    public void testMove() throws Exception {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:///"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());

        vfs.moveAsync(vfs.get("file:///D:/temp-copy/"), vfs.get("file:///D:/temp-copy-2/")).join();

        vfs.close();
    }
}
