package cc.whohow;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class TestURI {
    @Test
    public void test() {
        System.out.println(URI.create("vfs:meta:/"));
        Assert.assertEquals("vfs", URI.create("vfs:meta:/").getScheme());
        System.out.println(URI.create("vfs:meta:/").getScheme());
        Assert.assertNull(URI.create("vfs:meta:/").getHost());
        System.out.println(URI.create("vfs:meta:/").getHost());
        System.out.println(URI.create("http:/"));
        Assert.assertEquals("http", URI.create("http:/").getScheme());
        System.out.println(URI.create("http:/").getScheme());
        System.out.println(URI.create("http:///"));
        Assert.assertEquals("http", URI.create("http:///").getScheme());
        System.out.println(URI.create("http:///").getScheme());
    }

    @Test
    public void testRelativize() {
        Assert.assertEquals("http://example.com/demo", URI.create("http:///").relativize(URI.create("http://example.com/demo")).toString());
        System.out.println(URI.create("http:///").relativize(URI.create("http://example.com/demo")));
        Assert.assertEquals("D:/a.txt", URI.create("file:///").relativize(URI.create("file:///D:/a.txt")).toString());
        System.out.println(URI.create("file:///").relativize(URI.create("file:///D:/a.txt")));
    }

    @Test
    public void testResolve() {
        Assert.assertEquals("http://example.com/demo", URI.create("http:/").resolve("//example.com/demo").toString());
        System.out.println(URI.create("http:/").resolve("//example.com/demo"));
    }
}
