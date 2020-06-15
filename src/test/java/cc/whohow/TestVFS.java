package cc.whohow;

import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.configuration.ConfigurationBuilder;
import cc.whohow.fs.provider.DefaultVirtualFileSystem;
import cc.whohow.fs.provider.file.LocalFileProvider;
import cc.whohow.fs.provider.http.HttpFileProvider;
import cc.whohow.vfs.FileObjects;
import cc.whohow.vfs.FileSystemManagerAdapter;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class TestVFS {
    public static VirtualFileSystem newVirtualFileSystem() {
        VirtualFileSystem virtualFileSystem = new DefaultVirtualFileSystem(new ConfigurationBuilder().build());
        virtualFileSystem.load(new LocalFileProvider());
        virtualFileSystem.load(new HttpFileProvider());
        return virtualFileSystem;
    }

    @BeforeClass
    public static void initializeVFS() {
        VFS.setManager(new FileSystemManagerAdapter(newVirtualFileSystem()));
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
                "file:///D:/a.txt",
                "http://www.baidu.com"
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
}
