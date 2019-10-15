package cc.whohow;

import cc.whohow.vfs.VirtualFileSystem;
import cc.whohow.vfs.configuration.JsonVirtualFileSystemConfiguration;
import cc.whohow.vfs.synchronize.FileSync;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Test;

import java.io.File;

public class TestFileSystem {
    @Test
    public void test() throws Exception {
        JsonVirtualFileSystemConfiguration conf = new JsonVirtualFileSystemConfiguration(new YAMLMapper().readTree(new File("vfs.yml")));
        VirtualFileSystem vfs = conf.build();

        FileSync fileSync = new FileSync(vfs, "oss://yt-temp/log/", "oss://yt-temp/test/", "cos://yt-backup-1256265957/");
        fileSync.run();

        vfs.close();
    }
}
