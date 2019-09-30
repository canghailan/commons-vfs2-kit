package cc.whohow;

import cc.whohow.vfs.path.URIBuilder;
import org.junit.Test;

public class TestFileSystem {
    @Test
    public void test() throws Exception {
        URIBuilder uriBuilder = new URIBuilder("conf:/");
        System.out.println(uriBuilder);
    }
}
