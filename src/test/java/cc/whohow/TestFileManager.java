package cc.whohow;

import cc.whohow.fs.FileManager;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultFileManager;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import cc.whohow.fs.provider.http.HttpFileProvider;
import org.junit.Test;

import java.net.URI;

public class TestFileManager {
    @Test
    public void test() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(URI.create("conf:///"));

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configurationBuilder.build());
        vfs.load(new LocalFileProvider());
        vfs.load(new HttpFileProvider());

        FileManager fileManager = new DefaultFileManager(vfs);

        System.out.println(fileManager.size("file:///D:/temp/"));
        System.out.println(fileManager.readAttributes("file:///D:/temp/"));
    }
}
