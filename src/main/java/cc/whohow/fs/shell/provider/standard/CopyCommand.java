package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

import java.util.Objects;

public class CopyCommand implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        Args arguments = new Args();
        arguments.fileManager = fileManager;
        arguments.source = fileManager.get(args[0]);
        arguments.target = fileManager.get(args[1]);
        return call(arguments);
    }

    public String call(Args args) {
        Objects.requireNonNull(args.fileManager);
        Objects.requireNonNull(args.source);
        Objects.requireNonNull(args.target);
        return args.fileManager.copyAsync(args.source, args.target).join().toString();
    }

    public static class Args {
        private FileManager fileManager;
        private File source;
        private File target;

        public FileManager getFileManager() {
            return fileManager;
        }

        public void setFileManager(FileManager fileManager) {
            this.fileManager = fileManager;
        }

        public File getSource() {
            return source;
        }

        public void setSource(File source) {
            this.source = source;
        }

        public File getTarget() {
            return target;
        }

        public void setTarget(File target) {
            this.target = target;
        }
    }
}
