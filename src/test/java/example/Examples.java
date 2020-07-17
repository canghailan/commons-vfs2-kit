package example;

import cc.whohow.fs.*;
import cc.whohow.fs.configuration.JsonConfigurationParser;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.FileBasedMountPoint;
import cc.whohow.fs.shell.provider.rsync.Rsync;
import cc.whohow.fs.util.IO;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Paths;

public class Examples {
    // 当前目录 CurrentWorkingDirectory
    private static String cwd;
    // 虚拟文件系统 VirtualFileSystem
    private static VirtualFileSystem vfs;

    /**
     * 初始化
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        cwd = Paths.get(".")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        File metadata = new JsonConfigurationParser()
                .parse(new YAMLMapper().readTree(
                        new java.io.File("vfs.yaml")))
                .build();
        vfs = new DefaultVirtualFileSystem(metadata);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    /**
     * 文件上传
     */
    @Test
    public void upload() {
        String srcDir = cwd;
        String dstDir = "oss://yt-temp/temp/";

        vfs.copyAsync(
                vfs.get(srcDir + "pom.xml"),
                vfs.get(dstDir + "pom-upload.xml")
        ).join();
    }

    /**
     * 文件下载
     */
    @Test
    public void download() {
        String srcDir = "oss://yt-temp/temp/";
        String dstDir = cwd;

        vfs.copyAsync(
                vfs.get(srcDir + "pom-upload.xml"),
                vfs.get(dstDir + "pom-download.xml")
        ).join();
    }

    /**
     * 文件流式上传
     */
    @Test
    public void streamUpload() throws Exception {
        String dstDir = "oss://yt-temp/temp/";

        try (FileInputStream stream = new FileInputStream("pom.xml")) {
            try (FileWritableChannel channel = vfs
                    .get(dstDir + "pom-stream-upload.pom")
                    .newWritableChannel()) {
                channel.transferFrom(stream);
            }
        }
    }

    /**
     * 文件流式下载
     */
    @Test
    public void streamDownload() throws Exception {
        String srcDir = "oss://yt-temp/temp/";

        try (FileOutputStream output = new FileOutputStream("pom-stream-download.xml")) {
            try (InputStream input = vfs
                    .get(srcDir + "pom-stream-upload.pom").newReadableChannel().stream()) {
                IO.copy(input, output);
            }
        }
    }

    /**
     * 读取文本文件
     */
    @Test
    public void read() {
        System.out.println(vfs.get("oss://yt-temp/temp/pom-upload.xml").readUtf8());
    }

    /**
     * 写入文本文件
     */
    @Test
    public void write() {
        vfs.get("oss://yt-temp/temp/test.txt").writeUtf8("hello world!");
    }

    /**
     * 读取文件属性
     */
    @Test
    public void readAttributes() {
        FileAttributes fileAttributes = vfs.get("oss://yt-temp/temp/pom-upload.xml").readAttributes();
        System.out.println(fileAttributes);
        System.out.println(fileAttributes.size());
        System.out.println(fileAttributes.lastModifiedTime());
        System.out.println(fileAttributes.lastAccessTime());
        System.out.println(fileAttributes.creationTime());
    }

    /**
     * 删除文件
     */
    @Test
    public void delete() {
        vfs.get(cwd + "pom-download.xml").delete();
    }

    /**
     * 下级文件列表
     */
    @Test
    public void list() throws Exception {
        try (DirectoryStream<? extends File> list = vfs
                .get("oss://yt-temp/temp/").newDirectoryStream()) {
            for (File file : list) {
                System.out.println(file);
            }
        }
    }

    /**
     * 文件树
     */
    @Test
    public void tree() throws Exception {
        try (FileStream<? extends File> tree = vfs
                .get("oss://yt-temp/temp/").tree()) {
            tree.forEach(System.out::println);
        }
    }

    /**
     * 文件夹大小
     */
    @Test
    public void getDirectorySize() {
        System.out.println(vfs.get("oss://yt-temp/temp/").size());
    }

    /**
     * 增量同步文件（每次只会同步变化的文件）
     */
    @Test
    public void rsync() throws Exception {
        // 手动挂载 /rsync/ 虚拟目录，供Rsync命令使用
        vfs.mount(new FileBasedMountPoint("/rsync/", vfs.get(cwd + "temp/rsync/")));

        String srcDir = cwd;
        String dstDir = "oss://yt-temp/temp/";
        new Rsync().call(vfs, srcDir + "src/", dstDir + "src/");
    }
}
