package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

import java.util.Objects;

public class ReadCommand implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        Args arguments = new Args();
        arguments.file = fileManager.get(args[0]);
        return call(arguments);
    }

    public String call(Args args) {
        Objects.requireNonNull(args.file);
        return args.file.readUtf8();
    }

    public static class Args {
        private File file;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }
    }
}
