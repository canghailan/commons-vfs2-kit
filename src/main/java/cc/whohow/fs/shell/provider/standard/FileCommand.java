package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

import java.util.Objects;

public class FileCommand implements Command<File> {
    @Override
    public File call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        Objects.requireNonNull(args[0]);
        return fileManager.get(args[0].trim());
    }
}
