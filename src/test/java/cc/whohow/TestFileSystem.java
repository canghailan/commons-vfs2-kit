package cc.whohow;

import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.configuration.JsonVirtualFileSystemBuilder;
import cc.whohow.vfs.provider.s3.S3FileSync;
import cc.whohow.vfs.provider.s3.S3Uri;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Test;

import java.io.File;

public class TestFileSystem {
    @Test
    public void test() throws Exception {
        JsonVirtualFileSystemBuilder conf = new JsonVirtualFileSystemBuilder(new YAMLMapper().readTree(new File("vfs.yml")));
        VirtualFileSystem vfs = conf.build();

        S3FileSync s3FileSync = new S3FileSync(vfs, "oss://yt-temp/log/", "oss://yt-temp/test/", "cos://yt-backup-1256265957/");
        System.out.println(s3FileSync.call());

        vfs.close();
    }

    @Test
    public void testUri() {
        System.out.println(new S3Uri("oss", null, null, "yitong-hzy", null, "n-1 亿童幼教机构合作园《家园共育规范》家长行为规范.pdf").toURI().getPath());
    }
}
