package cc.whohow;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class TestByteBuffer {
    private ByteBuffer randomBuffer(int length) {
        Random random = new Random();
        ByteBuffer buffer = ByteBuffer.allocate(length);
        for (int i = 0; i < length; i++) {
            buffer.put((byte) ('a' + random.nextInt(26)));
        }
        buffer.flip();
        return buffer;
    }

    @Test
    public void testCopy() {
        ByteBuffer origin = randomBuffer(10);

        ByteBuffer copy = ByteBuffer.allocate(origin.remaining());
        copy.put(origin.duplicate());
        copy.flip();

        System.out.println("origin: ");
        System.out.println("data: " + StandardCharsets.UTF_8.decode(origin.duplicate()));
        System.out.println("position: " + origin.position());
        System.out.println("limit: " + origin.limit());

        System.out.println("copy: ");
        System.out.println("data: " + StandardCharsets.UTF_8.decode(copy.duplicate()));
        System.out.println("position: " + copy.position());
        System.out.println("limit: " + copy.limit());

        Assert.assertEquals(origin.position(), copy.position());
        Assert.assertEquals(origin.limit(), copy.limit());
        for (int i = 0; i < origin.remaining(); i++) {
            Assert.assertEquals(origin.get(i), copy.get(i));
        }
    }
}
