package cc.whohow.fs;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface FileManager {
    /**
     * 拼接URI
     */
    String resolve(String... paths);

    /**
     * 获取文件标准URI
     */
    String getUri(String uri);

    /**
     * 获取文件公开URI（对外展示，常用）
     */
    String getPublicUri(String uri);

    /**
     * 获取文件所有URI
     */
    Collection<String> getUris(String uri);

    /**
     * 获取文件名
     */
    String getName(String uri);

    /**
     * 获取文件扩展名，无返回""
     */
    String getExtension(String uri);

    /**
     * 文件是否存在
     */
    boolean exists(String uri);

    /**
     * 是否是文件
     */
    boolean isRegularFile(String uri);

    /**
     * 是否是目录
     */
    boolean isDirectory(String uri);

    /**
     * 获取文件属性
     */
    FileAttributes readAttributes(String uri);

    /**
     * 获取文件系统属性
     */
    FileSystemAttributes readFileSystemAttributes(String uri);

    /**
     * 获取文件/目录大小
     */
    long size(String uri);

    /**
     * 获取文件/目录最后修改时间
     */
    FileTime getLastModifiedTime(String uri);

    /**
     * 读取文件流
     */
    InputStream newInputStream(String uri);

    /**
     * 写入文件流
     */
    OutputStream newOutputStream(String uri);

    /**
     * 读取文件通道
     */
    ReadableByteChannel newReadableChannel(String uri);

    /**
     * 写入文件通道
     */
    WritableByteChannel newWritableChannel(String uri);

    /**
     * 读取文件内容
     */
    ByteBuffer read(String uri);

    /**
     * 写入文件内容
     */
    void write(String uri, ByteBuffer content);

    /**
     * 读取文本文件内容
     */
    String read(String uri, Charset charset);

    /**
     * 写入文本文件内容
     */
    void write(String uri, CharSequence content, Charset charset);

    /**
     * 读取文本文件内容，UTF-8编码
     */
    String readUtf8(String uri);

    /**
     * 写入文本文件内容，UTF-8编码
     */
    void writeUtf8(String uri, CharSequence content);

    /**
     * 删除文件
     */
    void delete(String uri);

    /**
     * 删除文件（忽略错误）
     */
    void deleteQuietly(String uri);

    /**
     * 复制文件，返回文件PublicUri
     */
    String copy(String source, String destination);

    /**
     * 异步复制文件，返回文件PublicUri
     */
    CompletableFuture<String> copyAsync(String source, String destination);

    /**
     * 剪切文件，返回文件PublicUri
     */
    String move(String source, String destination);

    /**
     * 异步剪切文件，返回文件PublicUri
     */
    CompletableFuture<String> moveAsync(String source, String destination);

    /**
     * 执行文件命令
     */
    <T> T exec(String... args);

    /**
     * 异步执行文件命令
     */
    <T> CompletableFuture<T> execAsync(String... args);
}
