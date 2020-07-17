package cc.whohow.fs.shell.provider.find;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Find implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length != 3 && !"-name".equals(args[1])) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File file = fileManager.get(args[0]);
        String name = args[2];
        return call(file, Pattern.compile(name));
    }

    public String call(File file, Pattern name) {
        return call(file, new NameFilter(name.asPredicate()));
    }

    public String call(File file, Predicate<File> filter) {
        try (Stream<? extends File> tree = file.tree().stream()) {
            return tree.filter(filter)
                    .map(File::toString)
                    .collect(Collectors.joining("\n"));
        }
    }

    static class NameFilter implements Predicate<File> {
        private final Predicate<String> name;

        public NameFilter(Predicate<String> name) {
            this.name = name;
        }

        @Override
        public boolean test(File file) {
            return file.isRegularFile() && name.test(file.getName());
        }
    }
}
