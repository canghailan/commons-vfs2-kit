package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import org.junit.Test;

import java.net.URI;

public class TestCommand {
    @Test
    public void testCopy() throws Exception {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:///"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());

//        vfs.newCopyCommand("file:///D:/a.txt", "file:///D:/b.txt").call();
        vfs.newCopyCommand("file:///D:/app/Fira_Code/", "file:///D:/temp-copy/").call();

        vfs.close();
    }

    @Test
    public void testMove() throws Exception {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:///"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());

        vfs.newMoveCommand("file:///D:/temp-copy/", "file:///D:/temp-copy-2/").call();

        vfs.close();
    }
}
