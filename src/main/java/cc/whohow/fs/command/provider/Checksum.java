package cc.whohow.fs.command.provider;

import cc.whohow.fs.File;
import cc.whohow.fs.VirtualFileSystem;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.util.concurrent.Callable;

public class Checksum implements Callable<String> {
    protected final String algorithm;
    protected final File<?, ?> file;

    public Checksum(VirtualFileSystem vfs, String... args) {
        if (args.length != 3) {
            throw new IllegalArgumentException(String.join(" ", args));
        }
        this.algorithm = args[1];
        this.file = vfs.get(args[2]);
    }

    public Checksum(String algorithm, File<?, ?> file) {
        this.algorithm = algorithm;
        this.file = file;
    }

    @Override
    public String call() throws Exception {
        try (InputStream stream = file.newReadableChannel().stream()) {
            return Hex.encodeHexString(DigestUtils.digest(DigestUtils.getDigest(algorithm), stream));
        }
    }
}
