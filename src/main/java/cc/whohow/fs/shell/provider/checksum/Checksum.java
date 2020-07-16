package cc.whohow.fs.shell.provider.checksum;

import cc.whohow.fs.File;
import cc.whohow.fs.FileManager;
import cc.whohow.fs.shell.Command;
import cc.whohow.fs.util.Hex;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 文件MD5、SHA-1、SHA-256等计算工具
 */
public class Checksum implements Command<String> {
    @Override
    public String call(FileManager fileManager, String... args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        return call(args[0], fileManager.get(args[1]));
    }

    public String call(String algorithm, File file) throws Exception {
        try (InputStream stream = file.newReadableChannel().stream()) {
            byte[] buffer = new byte[8 * 1024];
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            while (true) {
                int n = stream.read(buffer);
                if (n < 0) {
                    return Hex.encode(messageDigest.digest());
                }
                if (n > 0) {
                    messageDigest.update(buffer, 0, n);
                }
            }
        }
    }
}
