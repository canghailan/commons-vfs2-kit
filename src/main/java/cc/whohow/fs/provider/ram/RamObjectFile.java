package cc.whohow.fs.provider.ram;

import cc.whohow.fs.ObjectFile;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface RamObjectFile extends ObjectFile {
    @Override
    default boolean exists() {
        return true;
    }

    RamObjectFile asBinaryObjectFile();

    /**
     * @see ByteBuffer#asCharBuffer()
     */
    RamObjectFile asTextObjectFile(Charset charset);
}
