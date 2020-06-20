package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.command.FileShell;
import cc.whohow.fs.command.provider.Checksum;
import cc.whohow.fs.command.script.Fish;
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
    private static FileShell shell;

    @BeforeClass
    public static void beforeClass() {
        base = Paths.get(".").toUri().normalize().toString();
        vfs = new DefaultVirtualFileSystem(new ConfigurationBuilder().build());
        vfs.load(new LocalFileProvider());
        shell = new FileShell(vfs);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    @Test
    public void testChecksum() throws Exception {
        shell.install(Checksum.class);

        String file = base + "/src/main/java/cc/whohow/fs/command/FileShell.java";
        String[] algorithms = {"MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

        // Windows: certutil -hashfile [file] md5
        for (String algorithm : algorithms) {
            String checksum = shell.exec("Checksum", algorithm, file);
            System.out.println(algorithm + ": " + checksum);
        }
    }

    @Test
    public void testNewCommand() throws Exception {
        shell.install(Checksum.class);

        String file = base + "/src/main/java/cc/whohow/fs/command/FileShell.java";
        String[] algorithms = {"MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"};

        for (String algorithm : algorithms) {
            String checksum1 = shell.<String>newCommand("Checksum").apply(new String[]{algorithm, file});
            System.out.println(algorithm + "(func ): " + checksum1);
            String checksum2 = shell.<String>newCommand("Checksum", algorithm, file).call();
            System.out.println(algorithm + "(call ): " + checksum2);
            String checksum3 = shell.newCommand(Checksum.class, algorithm, file).call();
            System.out.println(algorithm + "(class): " + checksum3);
        }
    }

    @Test
    public void testScript() throws Exception {
        String file = base + "/src/main/java/cc/whohow/fs/command/FileShell.java";

        Fish fish = new Fish(shell);
        fish.put("file", file);

        System.out.println(fish.eval(String.join("\n",
                "INSTALL 'cc.whohow.fs.command.provider.Checksum'",
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
}