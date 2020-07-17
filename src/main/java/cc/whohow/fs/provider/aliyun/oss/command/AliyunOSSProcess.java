package cc.whohow.fs.provider.aliyun.oss.command;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.provider.aliyun.oss.AliyunOSSFile;
import cc.whohow.fs.shell.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AliyunOSSProcess implements Command<String> {
    private static final Logger log = LogManager.getLogger(AliyunOSSProcess.class);

    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File file = fileManager.get(args[0]);
        String process = args[1];
        if (file instanceof AliyunOSSFile) {
            return call((AliyunOSSFile) file, process);
        } else {
            throw new IllegalArgumentException(file + " is not oss file");
        }
    }

    public String call(AliyunOSSFile file, String process) {
        log.trace("{} {} {}", getName(), file, process);
        return file.getPublicUri() + "?x-oss-process=" + process;
    }
}
