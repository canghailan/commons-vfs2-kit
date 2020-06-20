package cc.whohow.fs.command.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.UncheckedException;
import cc.whohow.fs.VirtualFileSystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class StandardCommands {
    public static File<?, ?> file(VirtualFileSystem vfs, String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return vfs.get(args[0]);
    }

    public static String list(VirtualFileSystem vfs, String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File<?, ?> file = vfs.get(args[0]);
        try (DirectoryStream<? extends File<?, ?>> stream = file.newDirectoryStream()) {
            StringJoiner buffer = new StringJoiner("\n");
            for (File<?, ?> f : stream) {
                buffer.add(f.getPublicUri());
            }
            return buffer.toString();
        } catch (IOException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public static String tree(VirtualFileSystem vfs, String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File<?, ?> file = vfs.get(args[0]);
        try (FileStream<? extends File<?, ?>> stream = file.tree()) {
            StringJoiner buffer = new StringJoiner("\n");
            for (File<?, ?> f : stream) {
                buffer.add(f.getPublicUri());
            }
            return buffer.toString();
        } catch (IOException e) {
            throw UncheckedException.unchecked(e);
        }
    }

    public static String copy(VirtualFileSystem vfs, String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File<?, ?> source = vfs.get(args[0]);
        File<?, ?> target = vfs.get(args[1]);
        return vfs.copyAsync(source, target).join().getPublicUri();
    }

    public static String move(VirtualFileSystem vfs, String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        File<?, ?> source = vfs.get(args[0]);
        File<?, ?> target = vfs.get(args[1]);
        return vfs.moveAsync(source, target).join().getPublicUri();
    }

    public static String delete(VirtualFileSystem vfs, String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return Arrays.stream(args)
                .map(vfs::get)
                .peek(File::delete)
                .map(File::getPublicUri)
                .collect(Collectors.joining("\n"));
    }

    public static String read(VirtualFileSystem vfs, String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return vfs.get(args[0]).readUtf8();
    }

    public static String write(VirtualFileSystem vfs, String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        String lines = String.join("\n", Arrays.asList(args).subList(1, args.length));
        vfs.get(args[0]).writeUtf8(lines);
        return lines;
    }
}
