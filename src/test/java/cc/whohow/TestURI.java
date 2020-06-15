package cc.whohow;

import org.junit.Test;

import java.net.URI;

public class TestURI {
    @Test
    public void test() {
        System.out.println(URI.create("meta:vfs:/"));
        System.out.println(URI.create("meta:vfs:/").getScheme());
        System.out.println(URI.create("meta:vfs:/").getHost());
        System.out.println(URI.create("http:/"));
        System.out.println(URI.create("http:/").getScheme());
        System.out.println(URI.create("http:///"));
        System.out.println(URI.create("http:///").getScheme());
    }

    @Test
    public void testRelativize() {
        System.out.println(URI.create("http:///").relativize(URI.create("http://example.com/demo")));
        System.out.println(URI.create("file:///").relativize(URI.create("file:///D:/a.txt")));
    }

    @Test
    public void testResolve() {
        System.out.println(URI.create("file:///").resolve("D:/a.txt"));
        System.out.println(URI.create("http:/").resolve("//example.com/demo"));
    }
}
