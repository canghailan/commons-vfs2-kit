package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.FileWritableChannel;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.configuration.JsonConfigurationParser;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

public class TestVirtualFileSystem {
    private JsonNode loadConfiguration() throws Exception {
        return new YAMLMapper().readTree(new java.io.File("vfs.yml"));
    }

    private void print(File<?, ?> dir) throws Exception {
        try (FileStream<? extends File<?, ?>> stream = dir.tree()) {
            for (File<?, ?> file : stream) {
                System.out.println(file);
                if (file.isRegularFile()) {
                    System.out.println("```");
                    System.out.println(file.readUtf8());
                    System.out.println("```");
                }
            }
        }
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        System.out.println(loadConfiguration());
    }

    @Test
    public void testBuildConfiguration() throws Exception {
        File<?, ?> configuration = new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(loadConfiguration())
                .build();

        print(configuration);
    }

    @Test
    public void testVfsOSS() throws Exception {
        File<?, ?> configuration = new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(loadConfiguration())
                .build();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configuration);
        vfs.load(new LocalFileProvider());

        print(configuration);

        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").readUtf8());
        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").getUris());
        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").getName());
        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").exists());
        System.out.println(vfs.get("oss://yt-temp/temp/c.txt").exists());
        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").readAttributes());
//        System.out.println(vfs.get("oss://yt-temp/temp/c.txt").getAttributes());
        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").getFileSystem().readAttributes());

        String random = UUID.randomUUID().toString();
        System.out.println(random);
        System.out.println(vfs.get("oss://yt-temp/temp/b.txt").exists());
        vfs.get("oss://yt-temp/temp/b.txt").writeUtf8(random);
        System.out.println(vfs.get("oss://yt-temp/temp/b.txt").readUtf8());
        vfs.get("oss://yt-temp/temp/b.txt").delete();

        System.out.println("newDirectoryStream");
        try (DirectoryStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/c/").newDirectoryStream()) {
            for (File<?, ?> file : stream) {
                System.out.println(file);
            }
        }

        System.out.println("tree1");
        try (FileStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/c/").tree(1)) {
            for (File<?, ?> file : stream) {
                System.out.println(file);
            }
        }
        System.out.println("tree");
        try (FileStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/").tree()) {
            for (File<?, ?> file : stream) {
                System.out.println(file);
            }
        }
        System.out.println("tree2");
        try (FileStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/").tree(2)) {
            for (File<?, ?> file : stream) {
                System.out.println(file);
            }
        }

        vfs.close();
    }

    @Test
    public void testVfsCOS() throws Exception {
        File<?, ?> configuration = new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(loadConfiguration())
                .build();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configuration);
        vfs.load(new LocalFileProvider());

        print(configuration);

//        System.out.println(vfs.get("/temp-cos/a.txt").readUtf8());
//        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").getUris());
//        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").getName());
//        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").exists());
//        System.out.println(vfs.get("oss://yt-temp/temp/c.txt").exists());
//        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").readAttributes());
////        System.out.println(vfs.get("oss://yt-temp/temp/c.txt").getAttributes());
//        System.out.println(vfs.get("oss://yt-temp/temp/a.txt").getFileSystem().readAttributes());
//
        String random = UUID.randomUUID().toString();
        System.out.println(random);
        System.out.println(vfs.get("/temp-cos/temp/b.txt").exists());
        vfs.get("/temp-cos/temp/b.txt").writeUtf8(random);
        System.out.println(vfs.get("/temp-cos/temp/b.txt").readUtf8());
        vfs.get("/temp-cos/temp/b.txt").delete();
//
//        System.out.println("newDirectoryStream");
//        try (DirectoryStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/c/").newDirectoryStream()) {
//            for (File<?, ?> file : stream) {
//                System.out.println(file);
//            }
//        }
//
//        System.out.println("tree1");
//        try (FileStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/c/").tree(1)) {
//            for (File<?, ?> file : stream) {
//                System.out.println(file);
//            }
//        }
//        System.out.println("tree");
//        try (FileStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/").tree()) {
//            for (File<?, ?> file : stream) {
//                System.out.println(file);
//            }
//        }
//        System.out.println("tree2");
//        try (FileStream<? extends File<?, ?>> stream = vfs.get("oss://yt-temp/temp/").tree(2)) {
//            for (File<?, ?> file : stream) {
//                System.out.println(file);
//            }
//        }

        vfs.close();
    }

    @Test
    public void testCopy() throws Exception {
        File<?, ?> configuration = new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(loadConfiguration())
                .build();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configuration);
        vfs.load(new LocalFileProvider());

        vfs.newCopyCommand("file:///D:/temp/", "/temp-cos/").call();

        vfs.close();
    }

    @Test
    public void testQcloudCOSFileWritableChannel() throws Exception {
        File<?, ?> configuration = new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(loadConfiguration())
                .build();

        VirtualFileSystem vfs = new DefaultVirtualFileSystem(configuration);
        vfs.load(new LocalFileProvider());

        StringBuilder buffer = new StringBuilder();

        Random random = new Random();
        try (FileWritableChannel channel = vfs.get("/temp-cos/test-wc.txt").newWritableChannel()) {
            int i = 0;
            while (buffer.length() < 32 * 1024 * 1024) {
                int n = random.nextInt(4 * 1024 * 1024);
                System.out.println("loop" + (i++) + ": " + n);
                ByteBuffer buf = ByteBuffer.allocate(n);
                while (buf.hasRemaining()) {
                    char c = Character.forDigit(random.nextInt(36), 36);
                    buf.put((byte) c);
                }
                buf.flip();
                channel.write(buf.duplicate());
                buffer.append(StandardCharsets.US_ASCII.decode(buf.duplicate()));
            }
        }

        try (FileChannel channel = new FileOutputStream("D:\\test-wc.txt").getChannel()) {
            ByteBuffer buf = StandardCharsets.US_ASCII.encode(CharBuffer.wrap(buffer));
            while (buf.hasRemaining()) {
                channel.write(buf);
            }
        }

        vfs.close();
    }
}
