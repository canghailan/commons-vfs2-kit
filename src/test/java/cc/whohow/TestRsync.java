package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.FileBasedMountPoint;
import cc.whohow.fs.provider.file.LocalFileProvider;
import cc.whohow.fs.shell.FileShell;
import cc.whohow.fs.shell.provider.VirtualFileShell;
import cc.whohow.fs.shell.provider.rsync.Rsync;
import cc.whohow.fs.shell.script.Fish;
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
        vfs.mount(new FileBasedMountPoint("/.Rsync/", vfs.get(base + "temp/rsync/")));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    @Test
    public void testRsync() throws Exception {
        String source = base + "src/test/";
        String target = base + "temp/test/";

        String context = new Rsync().call(vfs, source, target);
        System.out.println(context);

        System.out.println(vfs.get(context + "diff.txt").readUtf8());
    }

    @Test
    public void testRsyncScript() throws Exception {
        FileShell fileShell = new VirtualFileShell(vfs);
        System.out.println(new Fish(fileShell).eval(
                vfs.get(base + "rsync.groovy")));
    }
}
