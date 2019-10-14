package cc.whohow;

import cc.whohow.vfs.*;
import cc.whohow.vfs.configuration.JsonVirtualFileSystemConfiguration;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Test;

import java.io.File;

public class TestFileSystem {
    @Test
    public void test() throws Exception {
        JsonVirtualFileSystemConfiguration conf = new JsonVirtualFileSystemConfiguration(new YAMLMapper().readTree(new File("vfs.yml")));

        VirtualFileSystem fileSystemProvider = conf.build();

        fileSystemProvider.resolveFile("conf:/providers/").list().forEach(System.out::println);

        fileSystemProvider.resolveFile("/backup/").listRecursively().forEach(f -> {
            System.out.println(f);
            if (FileObjects.isFile(f)) {
                System.out.println(FileObjects.readUtf8(f));
            }
        });

        fileSystemProvider.close();

    }
}
