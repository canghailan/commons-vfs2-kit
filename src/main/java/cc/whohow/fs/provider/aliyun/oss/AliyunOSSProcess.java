package cc.whohow.fs.provider.aliyun.oss;

import cc.whohow.fs.Command;

public class AliyunOSSProcess implements Command<String> {
    private final AliyunOSSFile file;

    public AliyunOSSProcess(AliyunOSSFile file) {
        this.file = file;
    }

    @Override
    public String call() throws Exception {
        // TODO
        return null;
    }

    @Override
    public String toString() {
        // TODO
        return "aliyun-oss-process " + file;
    }
}
