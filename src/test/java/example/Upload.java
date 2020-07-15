package example;

import cc.whohow.fs.File;
import cc.whohow.fs.GenericFile;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.JsonConfigurationParser;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.nio.file.Paths;

public class Upload {
    public static void main(String[] args) throws Exception {
        JsonNode configuration = new YAMLMapper().readTree(new java.io.File("vfs.yaml"));
        GenericFile<?, ?> metadata = new JsonConfigurationParser().parse(configuration).build();

        String srcDir = Paths.get(".").toAbsolutePath().normalize().toUri().toString();
        String dstDir = "oss://yt-temp/temp/";

        try (VirtualFileSystem vfs = new DefaultVirtualFileSystem(metadata)) {
            File file = vfs.copyAsync(
                    vfs.get(srcDir + "pom.xml"),
                    vfs.get(dstDir + "pom1.xml")
            ).join();
            System.out.println(file);
        }
    }
}
