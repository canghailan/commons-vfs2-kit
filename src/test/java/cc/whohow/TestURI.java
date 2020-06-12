package cc.whohow;

import org.junit.Test;

import java.net.URI;

public class TestURI {
    @Test
    public void test() {
        System.out.println(URI.create("meta:vfs:/"));
        System.out.println(URI.create("meta:vfs:/").getScheme());
        System.out.println(URI.create("meta:vfs:/").getHost());
        System.out.println(URI.create("http:///"));
        System.out.println(URI.create("http:///").getScheme());
    }
}
