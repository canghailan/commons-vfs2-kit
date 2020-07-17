# 虚拟文件系统 VFS

虚拟文件系统核心参考对象存储，将文件系统看做是一个URI为Key、文件对象为Value的KeyValue存储系统。

在KeyValue对象存储系统基础上，通过URI模拟文件系统层级（文件树）。



## 示例
### [配置文件 vfs.yaml](vfs.yaml.md)
```markdown
providers:
  file:
    className: cc.whohow.fs.provider.file.LocalFileProvider
  aliyun-oss:
    className: cc.whohow.fs.provider.aliyun.oss.AliyunOSSFileProvider
    profiles:
    - accessKeyId: *****
      secretAccessKey: *****
    automount: true
```


### [初始化](src/test/java/example/Examples.java#L32)
```java
public class Examples {
    // 当前目录 CurrentWorkingDirectory
    private static String cwd;
    // 虚拟文件系统 VirtualFileSystem
    private static VirtualFileSystem vfs;

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
}
```


### [文件上传](src/test/java/example/Examples.java#L54)
```java
public class Examples {
    public void upload() {
        String srcDir = cwd;
        String dstDir = "oss://yt-temp/temp/";

        vfs.copyAsync(
                vfs.get(srcDir + "pom.xml"),
                vfs.get(dstDir + "pom-upload.xml")
        ).join();
    }
}
```


### [文件下载](src/test/java/example/Examples.java#L68)
```java
public class Examples {
    public void download() {
        String srcDir = "oss://yt-temp/temp/";
        String dstDir = cwd;

        vfs.copyAsync(
                vfs.get(srcDir + "pom-upload.xml"),
                vfs.get(dstDir + "pom-download.xml")
        ).join();
    }
}
```


### [文件流式上传](src/test/java/example/Examples.java#L82)
```java
public class Examples {
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
}
```


### [文件流式下载](src/test/java/example/Examples.java#L98)
```java
public class Examples {
    public void streamDownload() throws Exception {
        String srcDir = "oss://yt-temp/temp/";

        try (FileOutputStream output = new FileOutputStream("pom-stream-download.xml")) {
            try (InputStream input = vfs
                    .get(srcDir + "pom-stream-upload.pom").newReadableChannel().stream()) {
                IO.copy(input, output);
            }
        }
    }
}
```


### [读取文本文件](src/test/java/example/Examples.java#L113)
```java
public class Examples {
    public void read() {
        System.out.println(vfs.get("oss://yt-temp/temp/pom-upload.xml").readUtf8());
    }
}
```


### [写入文本文件](src/test/java/example/Examples.java#L121)
```java
public class Examples {
    public void write() {
        vfs.get("oss://yt-temp/temp/test.txt").writeUtf8("hello world!");
    }
}
```


### [读取文件属性](src/test/java/example/Examples.java#L129)
```java
public class Examples {
    public void readAttributes() {
        FileAttributes fileAttributes = vfs.get("oss://yt-temp/temp/pom-upload.xml").readAttributes();
        System.out.println(fileAttributes);
        System.out.println(fileAttributes.size()); // 文件大小
        System.out.println(fileAttributes.lastModifiedTime()); // 文件最后修改时间
        System.out.println(fileAttributes.lastAccessTime());
        System.out.println(fileAttributes.creationTime());
    }
}
```


### [删除文件](src/test/java/example/Examples.java#L142)
```java
public class Examples {
    public void delete() {
        vfs.get(cwd  + "pom-download.xml").delete();
    }
}
```


### [下级文件列表](src/test/java/example/Examples.java#L150)
```java
public class Examples {
    public void list()  throws Exception {
        try (DirectoryStream<? extends File> list = vfs
                .get("oss://yt-temp/temp/").newDirectoryStream()) {
            for (File file : list) {
                System.out.println(file);
            }
        }
    }
}
```


### [文件树](src/test/java/example/Examples.java#L163)
```java
public class Examples {
    public void tree()  throws Exception {
        try (FileStream<? extends File> tree = vfs
                .get("oss://yt-temp/temp/").tree()) {
            tree.forEach(System.out::println);
        }
    }
}
```


### [文件夹大小](src/test/java/example/Examples.java#L174)
```java
public class Examples {
    public void getDirectorySize()  {
        System.out.println(vfs.get("oss://yt-temp/temp/").size());
    }
}
```


### [增量同步文件（每次只会同步变化的文件）](src/test/java/example/Examples.java#L182)
```java
public class Examples {
    public void rsync() throws Exception {
        // 手动挂载 /rsync/ 虚拟目录，供Rsync命令使用
        vfs.mount(new FileBasedMountPoint("/rsync/", vfs.get(cwd + "temp/rsync/")));

        String srcDir = cwd;
        String dstDir = "oss://yt-temp/temp/";
        new Rsync().call(vfs, srcDir + "src/", dstDir + "src/");
    }
}
```



## 核心对象

* VirtualFileSystem 虚拟文件系统，顶级对象，提供根据URI获取文件接口
    * MountPoint 虚拟文件系统挂载点
* File 文件，轻量级对象，持有FileSystem、Path引用，可对文件内容、属性进行读写，以及文件树遍历
    * GenericFile 通用文件基类，实现了大部分代理方法，实现新的文件系统可复用此类
* Path 路径，轻量级对象，已解析的URI字符串（针对不同的文件系统，解析出特定的字段供文件系统使用）
* FileSystem 文件系统，重量级对象，KeyValue存储系统的客户端，可通过Path进行各种操作
* FileSystemProvider 文件系统SPI，重量级对象，负责根据配置文件初始化FileSystem，同时提供同类型文件Copy、Move操作的实现
* DirectoryStream、FileStream 文件列表、文件树，**不保证可重复遍历，使用完后需关闭**
* FileReadableChannel、FileWritableChannel 文件读写通道，针对对象文件进行优化
* FileAttributes 文件属性
* ObjectFile 对象文件，轻量级对象，只提供最基础的文件内容、属性读写，不支持文件树遍历
* ObjectFileManager 对象文件管理服务，重量级对象，KeyValue存储系统的客户端，只能通过URI获取对象文件
* FileWatchService 文件监听服务，监听文件变更事件（FileEvent）


### 核心对象关系

* VirtualFileSystem （虚拟文件系统）
  * FileSystemProvider （文件系统SPI）
    * FileSystem （文件系统）
      * Path （路径，URI）
      * File （文件对象）
        * FileAttributes （文件属性，文件/目录）
        * FileReadableChannel （读通道，仅文件）
        * FileWritableChannel （写通道，仅文件）
        * DirectoryStream （下级文件列表，仅目录）
        * FileStream tree() （以自身为根节点的文件树，文件/目录）
    * FileWatchService （文件监听服务，一般同类型文件共用一个）
* ObjectFileManager （对象文件管理服务）
  * ObjectFile （对象文件）



## 文件存储支持
* 本地文件系统 - LocalFileSystem
* HTTP/HTTPS - HttpFileSystem
* 阿里云OSS - AliyunOSSFileSystem
* 腾讯云COS - QcloudCOSFileSystem
* 内存文件系统 - RamFileSystem
* **TODO** AWS S3 - S3FileSystem



## 配置中心
待补充



## Apache Commons VFS API 兼容
待补充



## Java NIO FileSystem API 兼容
TODO



## Fish脚本（FIleSHell）
基于Groovy的脚本，内置以下对象、命令：
* FISH 当前FileShell实例
* CWD 当前工作目录
* INSTALL 安装命令（参数为类全限定名）
* FILE 将字符串转为文件对象
* FILES 将字符串转为文件对象列表（每行一个文件）
* DELETE 删除文件
* LIST 读取下级文件列表（返回多行字符串，每行一个文件）
* TREE 读取文件树（返回多行字符串，每行一个文件）
* STAT 读取文件属性
* READ 读取文本文件
* WRITE 写入文本文件（文件, 文件内容）
* COPY 复制文件（源文件, 目标文件）
* MOVE 移动文件（源文件, 目标文件）


### [执行Fish脚本](src/test/java/example/Examples.java#L195) [test.groovy](test.groovy)示例
```java
public class Examples {
    public void fish() {
        System.out.println(new Fish(new VirtualFileShell(vfs))
                .eval(vfs.get(cwd + "test.groovy")));
    }
}
```



## 优化
* 通用
  * [x] 多线程文件拷贝
* 阿里云OSS
  * [x] 自动识别网络环境：内网Endpoint可使用时（同地域阿里云VPC、经典网络）自动使用内网
  * [x] OSSClient复用：同账号、同地域复用同一个OSSClient
  * [x] 文件复制优化：同账号、同地域文件使用copyObject，其他情况零拷贝流复制
  * [x] 批量删除使用deleteObjects
  * [x] 流式写入使用缓冲区合并写入操作
* 腾讯云COS
  * [x] COSClient复用：同账号、同地域复用同一个COSClient
  * [x] 文件复制优化：同账号、同地域文件使用copyObject，其他情况零拷贝流复制
  * [x] 批量删除使用deleteObjects
  * [x] 流式写入使用缓冲区合并写入操作



## 轮询监听服务
待补充



## 文件同步
差量同步。
TODO