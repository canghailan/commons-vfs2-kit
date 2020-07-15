package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileEvent;
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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

public class TestWatch {
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
    public void testWatch() throws Exception {
        String dir = "/temp-oss/temp/";

        List<String> create = Arrays.asList(
                "1.txt", "2.txt", "3.txt",
                "update/1.txt", "update/2.txt", "delete/3.txt");
        List<String> update = Arrays.asList("update/1.txt", "update/2.txt");
        List<String> delete = Arrays.asList("delete/3.txt");

        create.stream()
                .map(s -> dir + s)
                .map(vfs::get)
                .forEach(File::delete);

        for (String f : create) {
            Assert.assertFalse(vfs.get(dir + f).exists());
        }

        Collection<FileEvent> events = new ConcurrentLinkedDeque<>();
        vfs.get(dir).watch(System.out::println);
        vfs.get(dir).watch(events::add);

        for (String f : create) {
            vfs.get(dir + f).writeUtf8(RandomContent.randomString(30));
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
        }
        for (String f : update) {
            vfs.get(dir + f).writeUtf8(RandomContent.randomString(30));
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
        }
        for (String f : delete) {
            vfs.get(dir + f).delete();
            Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
        }

        Thread.sleep(5 * 1000);

        System.out.println(events);
    }
}
