package cc.whohow;

import cc.whohow.fs.util.Hex;
import org.junit.Assert;
import org.junit.Test;


public class TestHex {
    @Test
    public void test() {
        byte[] random = RandomContent.randomBytes(20);
        String hex = Hex.encode(random);
        System.out.println(hex);
        Assert.assertEquals(org.apache.commons.codec.binary.Hex.encodeHexString(random), hex);

        byte[] decode = Hex.decode(hex);
        Assert.assertArrayEquals(random, decode);
    }
}
