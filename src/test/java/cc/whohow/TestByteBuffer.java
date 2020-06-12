package cc.whohow;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestByteBuffer {
    @Test
    public void testBuffer() {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode("abcdefg");

//        System.out.println(buffer.position());
//        System.out.println(buffer.limit());

        ByteBuffer copy = ByteBuffer.allocate(8);
        copy.put(buffer.duplicate());
        copy.flip();

        System.out.println(StandardCharsets.UTF_8.decode(buffer.duplicate()));
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(StandardCharsets.UTF_8.decode(copy.duplicate()));
        System.out.println(copy.position());
        System.out.println(copy.limit());

    }
}
