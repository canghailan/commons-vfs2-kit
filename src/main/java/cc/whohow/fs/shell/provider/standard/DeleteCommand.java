package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;
import cc.whohow.fs.util.MapReduce;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteCommand implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        Args arguments = new Args();
        arguments.files = Arrays.stream(args)
                .distinct()
                .map(fileManager::get)
                .collect(Collectors.toList());
        return call(arguments);
    }

    public String call(Args args) {
        if (args.files == null || args.files.isEmpty()) {
            throw new IllegalArgumentException();
        }
        String files = args.files.stream()
                .map(File::toString)
                .collect(Collectors.joining("\n"));
        if (args.fileManager != null) {
            MapReduce<String, Void> mapReduce = new MapReduce<>(files);
            try {
                mapReduce.begin();
                for (File file : args.files) {
                    mapReduce.map(args.fileManager.runAsync(file::delete));
                }
                mapReduce.end();
            } catch (Throwable e) {
                mapReduce.completeExceptionally(e);
            }
            return mapReduce.join();
        } else {
            for (File file : args.files) {
                file.delete();
            }
            return files;
        }
    }

    public static class Args {
        private FileManager fileManager;
        private List<File> files;

        public FileManager getFileManager() {
            return fileManager;
        }

        public void setFileManager(FileManager fileManager) {
            this.fileManager = fileManager;
        }

        public List<File> getFiles() {
            return files;
        }

        public void setFiles(List<File> files) {
            this.files = files;
        }
    }
}
