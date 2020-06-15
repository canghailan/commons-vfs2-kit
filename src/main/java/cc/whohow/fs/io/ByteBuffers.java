package cc.whohow.fs.io;

import java.nio.ByteBuffer;

public class ByteBuffers {
    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

    /**
     * 空Buffer引用
     */
    public static ByteBuffer empty() {
        return EMPTY;
    }

    public static ByteBuffer resize(ByteBuffer byteBuffer, int newCapacity) {
        ByteBuffer src = byteBuffer.duplicate();
        ByteBuffer dst = ByteBuffer.allocate(newCapacity);
        int position = byteBuffer.position();
        src.flip();
        dst.put(src);
        dst.position(position);
        dst.limit(dst.capacity());
        return dst;
    }
}
