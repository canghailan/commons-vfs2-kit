# 虚拟文件系统

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


### [文件上传](src/test/java/example/Upload.java)
```java
public class Upload {
    public static void main(String[] args) throws Exception {
        JsonNode configuration = new YAMLMapper().readTree(new java.io.File("vfs.yaml"));
        File metadata = new JsonConfigurationParser().parse(configuration).build();

        String srcDir = Paths.get(".").toAbsolutePath().normalize().toUri().toString();
        String dstDir = "oss://xxx-temp/temp/";

        try (VirtualFileSystem vfs = new DefaultVirtualFileSystem(metadata)) {
            File file = vfs.copyAsync(
                    vfs.get(srcDir + "pom.xml"),
                    vfs.get(dstDir + "backup-pom.xml")
            ).join();
            System.out.println(file);
        }
    }
}
```
待补充


## 核心对象
VFS核心参考对象存储，可以看做是一个URI为Key、文件对象为Value的KeyValue存储系统。
在此基础上，通过URI地址模拟文件系统层级（文件树）。
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
基于Groovy的脚本，已注入VFS等对象。
待补充



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