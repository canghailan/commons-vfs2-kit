package cc.whohow;

import cc.whohow.vfs.path.URIBuilder;
import org.junit.Test;

public class TestHttpFileSystem {
    @Test
    public void test() throws Exception {
//        JsonVirtualFileSystem file = new JsonVirtualFileSystem(null, "/", new ObjectMapper().readTree("{\"a\": {\"b\":[1,2]}}"));
//        System.out.println(FileObjects.readUtf8(file.resolveFile("a").resolveFile("b/1")));
        URIBuilder uriBuilder = new URIBuilder("conf:/");
        System.out.println(uriBuilder);
    }
}
