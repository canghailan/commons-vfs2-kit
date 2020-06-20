package cc.whohow;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestByteBuffer {
    @Test
    public void testCopy() {
        ByteBuffer origin = RandomContent.randomByteBuffer(10);

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
        Assert.assertEquals(origin.remaining(), copy.remaining());
        for (int i = 0; i < origin.remaining(); i++) {
            Assert.assertEquals(origin.get(i), copy.get(i));
        }
    }
}
