package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WriteCommand implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        Args arguments = new Args();
        arguments.file = fileManager.get(args[0]);
        arguments.lines = Arrays.asList(args).subList(1, args.length);
        return call(arguments);
    }

    public String call(Args args) {
        Objects.requireNonNull(args.file);
        Objects.requireNonNull(args.lines);
        String lines = String.join("\n", args.lines);
        args.file.writeUtf8(lines);
        return lines;
    }

    public static class Args {
        private File file;
        private List<String> lines;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public List<String> getLines() {
            return lines;
        }

        public void setLines(List<String> lines) {
            this.lines = lines;
        }
    }
}
