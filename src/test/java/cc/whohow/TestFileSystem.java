package cc.whohow;

import cc.whohow.vfs.FileObjects;
import cc.whohow.vfs.provider.cos.QcloudCOSFileObjectX;
import org.junit.Test;

public class TestFileSystem {
    @Test
    public void test() throws Exception {
//        CloudFileSystemProvider fileSystemProvider = new QcloudCOSFileSystemProvider();
//        fileSystemProvider.init();
//
//        System.out.println(fileSystemProvider);
//
//        CloudFileSystem fileSystem = fileSystemProvider.findFileSystem("cos://yt-backup-1256265957");
//        System.out.println(fileSystem);
//
//        CloudFileObject fileObject = fileSystem.resolve("test.txt");
//        System.out.println(fileObject);
//
//        fileSystem.resolve("a").listRecursively().forEach(f -> {
//            System.out.println(f);
//            if (FileObjects.isFile(f)) {
//                System.out.println(FileObjects.readUtf8(f));
//            }
//        });
//
//        fileSystemProvider.close();

        System.out.println("test.txt");
        try (QcloudCOSFileObjectX f = new QcloudCOSFileObjectX("cos://AKIDjDRijI1i7JGyV4gRmvoTrmS6VebHImIY:B6rb5C3e0C9vTlqgJirpjRt9AzXx8g1i@yt-backup-1256265957.ap-shanghai/test.txt")) {
            System.out.println(FileObjects.readUtf8(f));
        }
    }
}
