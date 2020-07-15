package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.configuration.JsonConfigurationParser;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestVirtualFileSystem {
    private static String base;
    private static VirtualFileSystem vfs;

    @BeforeClass
    public static void beforeClass() throws Exception {
        base = Paths.get(".").toUri().normalize().toString();
        vfs = new DefaultVirtualFileSystem(new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(new YAMLMapper().readTree(new java.io.File("vfs.yaml"))).build());
        vfs.load(new LocalFileProvider());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    @Test
    public void testFilesCopy() throws Exception {
        Files.copy(Paths.get("temp/"), Paths.get("temp-copy/"));
    }

    @Test
    public void testCopy() throws Exception {
        File source = vfs.get("/temp-oss/temp/src/");
        File target = vfs.get("/temp-cos/temp-copy/src/");

        target.delete();
        Assert.assertEquals("", TestFiles.treeFile(target));

        vfs.copyAsync(source, target).join();

        Assert.assertEquals(TestFiles.treeFile(source), TestFiles.treeFile(target));
        System.out.println(TestFiles.treeFile(target));
    }
}
