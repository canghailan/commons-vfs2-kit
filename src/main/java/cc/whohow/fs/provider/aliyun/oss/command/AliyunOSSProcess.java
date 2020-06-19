package cc.whohow.fs.provider.aliyun.oss.command;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.provider.aliyun.oss.AliyunOSSFile;

import java.util.concurrent.Callable;

public class AliyunOSSProcess implements Callable<String> {
    protected final File<?, ?> file;
    protected final String process;

    public AliyunOSSProcess(VirtualFileSystem vfs, String... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        this.file = vfs.get(args[1]);
        this.process = args[2];
    }

    public AliyunOSSProcess(File<?, ?> file, String process) {
        this.file = file;
        this.process = process;
    }

    @Override
    public String call() throws Exception {
        if (file instanceof AliyunOSSFile) {
            return file.getPublicUri() + "?x-oss-process=" + process;
        }
        throw new IllegalArgumentException(file + " is not oss file");
    }
}
