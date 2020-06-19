package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.command.FileShell;
import cc.whohow.fs.command.provider.Checksum;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

public class TestFileShell {
    private static String base;
    private static VirtualFileSystem vfs;
    private static FileShell fish;

    @BeforeClass
    public static void beforeClass() {
        base = Paths.get(".").toUri().normalize().toString();
        vfs = new DefaultVirtualFileSystem(new ConfigurationBuilder().build());
        vfs.load(new LocalFileProvider());
        fish = new FileShell(vfs);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    @Test
    public void testChecksum() throws Exception {
        fish.install(Checksum.class);

        String file = base + "/src/main/java/cc/whohow/fs/command/FileShell.java";
        String[] algorithms = {"MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

        // Windows: certutil -hashfile [file] md5
        for (String algorithm : algorithms) {
            String checksum = fish.exec("Checksum", algorithm, file);
            System.out.println(algorithm + ": " + checksum);
        }
    }

    @Test
    public void testNewCommandProxy() throws Exception {
        fish.install(Checksum.class);

        String file = base + "/src/main/java/cc/whohow/fs/command/FileShell.java";
        String[] algorithms = {"MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

        for (String algorithm : algorithms) {
            String checksum = fish.<String>newCommand("Checksum").apply(new String[]{algorithm, file});
            System.out.println(algorithm + ": " + checksum);
        }
    }

    @Test
    public void testNewCommand() throws Exception {
        String file = base + "/src/main/java/cc/whohow/fs/command/FileShell.java";
        String[] algorithms = {"MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

        for (String algorithm : algorithms) {
            String checksum = fish.newCommand(Checksum.class, algorithm, file).call();
            System.out.println(algorithm + ": " + checksum);
        }
    }
}
