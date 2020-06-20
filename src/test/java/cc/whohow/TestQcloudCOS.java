package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.command.provider.Checksum;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.configuration.JsonConfigurationParser;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import cc.whohow.fs.util.IO;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public class TestQcloudCOS {
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
    public void testQcloudCOSFileWritableChannel() throws Exception {
        File<?, ?> source = vfs.get(base + "test.txt");
        File<?, ?> target = vfs.get("/temp-cos/test.txt");

        source.delete();
        Assert.assertFalse(source.exists());

        System.out.println("file.exists: " + target.exists());
        target.delete();
        Assert.assertFalse(target.exists());
        System.out.println("file.exists: " + target.exists());

        int maxSize = 64 * 1024 * 1024;
        int maxBufferSize = 1024 * 1024;

        long size = 0;
        try (FileChannel local = new FileOutputStream("test.txt").getChannel();
             FileWritableChannel object = target.newWritableChannel()) {
            System.out.println(object.getClass());
            for (int i = 0; size < maxSize; i++) {
                System.out.println("loop" + i + ":");
                ByteBuffer buffer1 = RandomContent.randomByteBuffer(1, 1024);

                System.out.println(size + " + " + buffer1.remaining());
                size += buffer1.remaining();

                IO.write(local, buffer1.duplicate());
                IO.write(object, buffer1.duplicate());

                ByteBuffer buffer2 = RandomContent.randomByteBuffer(1, maxBufferSize);

                System.out.println(size + " + " + buffer2.remaining());
                size += buffer2.remaining();

                IO.write(local, buffer2.duplicate());
                IO.write(object, buffer2.duplicate());
            }
        }

        Assert.assertTrue(target.exists());

        Assert.assertEquals(size, target.size());
        System.out.println("size: " + target.size());

        String md5 = new Checksum("MD5", target).call();
        Assert.assertEquals(new Checksum("MD5", source).call(), md5);
        System.out.println("md5: " + md5);
    }
}
