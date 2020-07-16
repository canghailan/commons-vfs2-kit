package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.configuration.JsonConfigurationParser;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.vfs.FileObjects;
import cc.whohow.vfs.VirtualFileSystemAdapter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.vfs2.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestCommonsVFS {
    private static String base;
    private static VirtualFileSystem vfs;

    @BeforeClass
    public static void beforeClass() throws Exception {
        base = Paths.get(".").toUri().normalize().toString();
        vfs = new DefaultVirtualFileSystem(new JsonConfigurationParser(new ConfigurationBuilder())
                .parse(new YAMLMapper().readTree(new java.io.File("vfs.yaml"))).build());
        VFS.setManager(new VirtualFileSystemAdapter(vfs));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        vfs.close();
    }

    @Test
    public void testManager() throws Exception {
        FileSystemManager fileSystemManager = VFS.getManager();

        System.out.println(fileSystemManager);
        System.out.println(fileSystemManager.getBaseFile());
        System.out.println(fileSystemManager.getBaseFile().getName());
        System.out.println(fileSystemManager.getBaseFile().getFileSystem());
        System.out.println(fileSystemManager.getBaseFile().getFileSystem().getFileSystemOptions());
        System.out.println(fileSystemManager.getBaseFile().getFileSystem().getRoot());
        System.out.println(fileSystemManager.getBaseFile().getFileSystem().getRootName());
        System.out.println(fileSystemManager.getBaseFile().getFileSystem().getRootURI());
        System.out.println(fileSystemManager.getBaseFile().getParent());
        System.out.println(fileSystemManager.getCacheStrategy());
        System.out.println(fileSystemManager.getFileContentInfoFactory());
        System.out.println(fileSystemManager.getFileObjectDecorator());
        System.out.println(fileSystemManager.getFileObjectDecoratorConst());
        System.out.println(fileSystemManager.getFilesCache());
        System.out.println(Arrays.toString(fileSystemManager.getSchemes()));
        System.out.println(fileSystemManager.getURLStreamHandlerFactory());
    }

    @Test
    public void testResolveFile() throws Exception {
        String[] path = {
                Paths.get("src/main/java/cc/whohow/fs/File.java").toUri().toString(),
                "http://www.baidu.com",
                "/temp-oss/temp/src/main/java/cc/whohow/fs/File.java",
                "/temp-cos/temp/src/main/java/cc/whohow/fs/File.java"
        };
        for (String p : path) {
            FileObject fileObject = VFS.getManager().resolveFile(p);
            System.out.println("name: " + fileObject.getName());
            System.out.println("parent: " + fileObject.getParent());
            System.out.println("type: " + fileObject.getType());
            System.out.println("uri: " + fileObject.getPublicURIString());
            System.out.println("content: " + FileObjects.readUtf8(fileObject));
            System.out.println();
        }
    }

    @Test
    public void testCopy() throws Exception {
        FileObject source = VFS.getManager().resolveFile("/temp-oss/temp/src/");
        FileObject target = VFS.getManager().resolveFile(Paths.get(".").toUri()).resolveFile("temp/");
        FileObject target2 = VFS.getManager().resolveFile("/temp-cos/temp-copy/");

        target.deleteAll();
        target.copyFrom(source, new PatternFileSelector(".*File.*.java"));

        target2.deleteAll();
        target2.copyFrom(target, Selectors.SELECT_ALL);
    }

    @Test
    public void testFind() throws Exception {
        FileObject folder = VFS.getManager().resolveFile(Paths.get(".").toUri()).resolveFile("src/");

        List<FileObject> depthFirst = new ArrayList<>();
        folder.findFiles(new PatternFileSelector(".*Configuration.*.java"), true, depthFirst);
        List<FileObject> breadthFirst = new ArrayList<>();
        folder.findFiles(new PatternFileSelector(".*Configuration.*.java"), false, breadthFirst);

        System.out.println("depthFirst: ");
        depthFirst.forEach(System.out::println);
        System.out.println("breadthFirst: ");
        breadthFirst.forEach(System.out::println);
    }

    @Test
    public void testWatch() throws Exception {
        FileObject dir = VFS.getManager().resolveFile("/temp-oss/temp/src/");

        VFS.getManager().resolveFile("/temp-oss/temp/src/temp.txt").delete();

        dir.getFileSystem().addListener(dir, new FileListener() {
            @Override
            public void fileCreated(FileChangeEvent event) throws Exception {
                System.out.println("fileCreated: " + event.getFileObject());
            }

            @Override
            public void fileDeleted(FileChangeEvent event) throws Exception {
                System.out.println("fileDeleted: " + event.getFileObject());
            }

            @Override
            public void fileChanged(FileChangeEvent event) throws Exception {
                System.out.println("fileChanged: " + event.getFileObject());
            }
        });

        FileObjects.writeUtf8(
                VFS.getManager().resolveFile("/temp-oss/temp/src/temp.txt"),
                RandomContent.randomString(100));

        Thread.sleep(ThreadLocalRandom.current().nextInt(3000, 9000));

        FileObjects.writeUtf8(
                VFS.getManager().resolveFile("/temp-oss/temp/src/temp.txt"),
                RandomContent.randomString(100));

        Thread.sleep(ThreadLocalRandom.current().nextInt(3000, 9000));

        VFS.getManager().resolveFile("/temp-oss/temp/src/temp.txt").delete();

        Thread.sleep(15 * 1000);
    }
}
