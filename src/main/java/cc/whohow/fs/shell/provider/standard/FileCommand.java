package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

public class FileCommand implements Command<File> {
    @Override
    public File call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return fileManager.get(args[0]);
    }
}
