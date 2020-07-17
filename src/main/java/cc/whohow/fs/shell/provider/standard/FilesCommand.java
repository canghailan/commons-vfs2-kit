package cc.whohow.fs.shell.provider.standard;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilesCommand implements Command<List<File>> {
    private static final Pattern NEW_LINE = Pattern.compile("[\r\n]+");

    @Override
    public List<File> call(FileManager fileManager, String... args) throws Exception {
        return Arrays.stream(args)
                .flatMap(NEW_LINE::splitAsStream)
                .map(fileManager::get)
                .collect(Collectors.toList());
    }
}
