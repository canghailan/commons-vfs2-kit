package cc.whohow.vfs.provider.uri;

import cc.whohow.vfs.FileNameImpl;
import cc.whohow.vfs.path.PathBuilder;
import org.apache.commons.vfs2.FileName;

public class SimpleUriFileName implements FileNameImpl {
    protected String schema;
    protected String path;

    public SimpleUriFileName(String schema, String path) {
        this.schema = schema;
        this.path = path;
    }

    @Override
    public String getURI() {
        return schema + ":" + path;
    }

    @Override
    public FileName getRoot() {
        return new SimpleUriFileName(schema, "/");
    }

    @Override
    public FileName getParent() {
        if ("/".equals(path)) {
            return null;
        }
        return new SimpleUriFileName(schema, new PathBuilder(path).removeLast().endsWithSeparator(true).build().toString());
    }
}
