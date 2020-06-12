package cc.whohow.fs.provider.memory;

import cc.whohow.fs.ObjectFile;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface MemoryObjectFile extends ObjectFile {
    @Override
    default boolean exists() {
        return true;
    }

    MemoryObjectFile asBinaryObjectFile();

    /**
     * @see ByteBuffer#asCharBuffer()
     */
    MemoryObjectFile asTextObjectFile(Charset charset);
}
