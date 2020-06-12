package cc.whohow;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.provider.file.LocalFileSystem;
import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.configuration.JsonVirtualFileSystemBuilder;
import cc.whohow.vfs.provider.aliyun.oss.AliyunOSSEndpoints;
import cc.whohow.vfs.provider.s3.S3FileSync;
import cc.whohow.vfs.provider.s3.S3Uri;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Test;

import java.net.URI;

public class TestFileSystem {
    @Test
    public void test() throws Exception {
        JsonVirtualFileSystemBuilder conf = new JsonVirtualFileSystemBuilder(new YAMLMapper().readTree(new java.io.File("vfs.yml")));
        VirtualFileSystem vfs = conf.build();

        S3FileSync s3FileSync = new S3FileSync(vfs, "oss://yt-temp/log/", "oss://yt-temp/test/", "cos://yt-backup-1256265957/");
        System.out.println(s3FileSync.call());

        vfs.close();
    }

    @Test
    public void testUri() {
        System.out.println(new S3Uri("oss", null, null, "yitong-hzy", null, "n-1 亿童幼教机构合作园《家园共育规范》家长行为规范.pdf").toURI().getPath());
    }

    @Test
    public void testAliyunOSSEndpoint() {
        System.out.println(AliyunOSSEndpoints.getRegion("oss-cn-hangzhou.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getRegion("oss-cn-hangzhou-internal.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.isInternal("oss-cn-hangzhou.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.isInternal("oss-cn-hangzhou-internal.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getExtranetEndpoint("oss-cn-hangzhou.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getIntranetEndpoint("oss-cn-hangzhou.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getExtranetEndpoint("oss-cn-hangzhou-internal.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getIntranetEndpoint("oss-cn-hangzhou-internal.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getEndpoint("oss-cn-hangzhou.aliyuncs.com"));
        System.out.println(AliyunOSSEndpoints.getEndpoint("oss-cn-hangzhou-internal.aliyuncs.com"));
    }

    @Test
    public void testFileSystemAdapterRead() {
        System.out.println(URI.create("http").getScheme());

        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        System.out.println(fileSystem.resolve("D:\\a.txt").toUri());
        System.out.println(fileSystem.get(fileSystem.resolve("D:\\a.txt")).readUtf8());
        System.out.println(fileSystem.get(fileSystem.resolve("D:\\a.txt")).readAttributes());
    }

    @Test
    public void testFileSystemAdapterWrite() {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
        System.out.println(fileSystem.resolve("D:\\a.txt").toUri());
        fileSystem.get(fileSystem.resolve("D:\\a.txt")).writeUtf8("abcdefg");
    }

    @Test
    public void testFileSystemAdapterList() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
//        System.out.println(fileSystem.resolve("D:\\temp\\").toUri());
        try (FileStream<? extends File<?, ?>> fileStream = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree()) {
            for (File<?, ?> file : fileStream) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    @Test
    public void testFileSystemAdapterWalk() throws Exception {
        LocalFileSystem fileSystem = new LocalFileSystem(URI.create("file:///"));
//        System.out.println(fileSystem.resolve("D:\\temp\\").toUri());
        try (FileStream<? extends File<?, ?>> fileStream = fileSystem.get(fileSystem.resolve("D:\\temp\\")).tree()) {
            for (File<?, ?> file : fileStream) {
                System.out.println(file);
            }
        }
        System.out.println();
    }
}
