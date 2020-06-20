package cc.whohow.fs.command.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystem;
import cc.whohow.fs.util.Hex;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.Callable;

/**
 * 文件MD5、SHA-1、SHA-256等计算工具
 */
public class Checksum implements Callable<String> {
    private final String algorithm;
    private final File<?, ?> file;

    public Checksum(VirtualFileSystem vfs, String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        this.algorithm = args[0];
        this.file = vfs.get(args[1]);
    }

    public Checksum(String algorithm, File<?, ?> file) {
        this.algorithm = algorithm;
        this.file = file;
    }

    @Override
    public String call() throws Exception {
        try (InputStream stream = file.newReadableChannel().stream()) {
            byte[] buffer = new byte[8 * 1024];
            MessageDigest md = MessageDigest.getInstance(algorithm);
            while (true) {
                int n = stream.read(buffer);
                if (n < 0) {
                    return Hex.encode(md.digest());
                }
                if (n > 0) {
                    md.update(buffer, 0, n);
                }
            }
        }
    }
}
