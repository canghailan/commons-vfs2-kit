package cc.whohow.vfs.provider.uri;

import cc.whohow.vfs.FileNameImpl;
import cc.whohow.vfs.path.PathBuilder;
import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.FileName;

public class UriFileName implements FileNameImpl {
    protected final String uri;

    public UriFileName(String uri) {
        this.uri = uri;
    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public FileName getRoot() {
        return new UriFileName(getRootURI());
    }

    @Override
    public FileName getParent() {
        String parent = new PathBuilder(getPathDecoded()).removeLast().toString();
        if (parent.isEmpty()) {
            return null;
        }
        return new UriFileName(new URIBuilder().setURI(getURI()).setPath(parent).toString());
    }
}
