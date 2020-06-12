package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.File;
import cc.whohow.fs.FileCopyCommand;
import cc.whohow.fs.VirtualFileSystem;

public class AliyunOSSFileCopyCommand implements FileCopyCommand {
    @Override
    public int getMatchingScore() {
        return 0;
    }

    @Override
    public VirtualFileSystem getVirtualFileSystem() {
        return null;
    }

    @Override
    public String[] getArguments() {
        return null;
    }

    @Override
    public File<?, ?> call() throws Exception {
        return null;
    }
}
