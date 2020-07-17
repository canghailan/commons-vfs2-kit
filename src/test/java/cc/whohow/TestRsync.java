package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.FileBasedMountPoint;
import cc.whohow.fs.provider.file.LocalFileProvider;
import cc.whohow.fs.shell.provider.rsync.Rsync;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

public class TestRsync {
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

    @Test
    public void testRsync() throws Exception {
        String rsync = base + "temp/rsync/";
        String source = base + "src/test/";
        String target = base + "temp/test/";

        vfs.mount(new FileBasedMountPoint("/rsync/", vfs.get(rsync)));

        String context = new Rsync().call(vfs, source, target);
        System.out.println(context);

        System.out.println(vfs.get(context + "diff.txt").readUtf8());
    }
}
