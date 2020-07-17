package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import cc.whohow.fs.shell.provider.VirtualFileShell;
import cc.whohow.fs.shell.provider.checksum.Checksum;
import cc.whohow.fs.shell.provider.gzip.Gunzip;
import cc.whohow.fs.shell.provider.gzip.Gzip;
import cc.whohow.fs.shell.provider.zip.Unzip;
import cc.whohow.fs.shell.provider.zip.Zip;
import cc.whohow.fs.shell.script.Fish;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

public class TestFileShell {
    private static String base;
    private static VirtualFileSystem vfs;
    private static VirtualFileShell shell;

    @BeforeClass
    public static void beforeClass() {
        base = Paths.get(".").toUri().normalize().toString();
        vfs = new DefaultVirtualFileSystem(new ConfigurationBuilder().build());
        vfs.load(new LocalFileProvider());
        shell = new VirtualFileShell(vfs);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    @Test
    public void testChecksum() throws Exception {
        shell.install(Checksum.class);

        String file = base + "/src/main/java/cc/whohow/fs/shell/FileShell.java";
        String[] algorithms = {"MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

        // Windows: certutil -hashfile [file] md5
        for (String algorithm : algorithms) {
            String checksum = shell.exec("Checksum", algorithm, file);
            System.out.println(algorithm + ": " + checksum);
        }
    }

    @Test
    public void testScript() throws Exception {
        String file = base + "/src/main/java/cc/whohow/fs/shell/FileShell.java";

        Fish fish = new Fish(shell);
        fish.put("file", file);

        System.out.println(fish.eval(String.join("\n",
                "INSTALL 'cc.whohow.fs.shell.provider.checksum.Checksum'",
                "checksum = Checksum 'md5', file",
                "println checksum"
        )));

        Object checksum = fish.get("checksum");
        System.out.println(checksum.getClass());
        System.out.println(checksum);
    }

    @Test
    public void testScriptFile() throws Exception {
        System.out.println(new Fish(shell).eval(
                Paths.get("test.groovy").toUri().toURL()));
    }

    @Test
    public void testZip() throws Exception {
        shell.install(new Zip());
        shell.install(new Unzip());

        System.out.println((String) shell.exec("Zip", base + "temp.zip", base + "temp/"));
        System.out.println((String) shell.exec("Zip", base + "test.zip", base + "test.txt"));
        System.out.println((String) shell.exec("Unzip", base + "temp.zip", base + "unzip/"));
        System.out.println((String) shell.exec("Unzip", base + "test.zip", base + "unzip/"));
    }

    @Test
    public void testGzip() throws Exception {
        shell.install(new Gzip());
        shell.install(new Gunzip());

        System.out.println((String) shell.exec("Gzip", base + "test.txt"));
        System.out.println((String) shell.exec("Gunzip", base + "test.txt.gz", base + "test-gunzip.txt"));
    }
}
