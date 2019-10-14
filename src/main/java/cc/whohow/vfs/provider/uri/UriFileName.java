package cc.whohow.vfs.provider.uri;

import cc.whohow.vfs.FileNameImpl;
import cc.whohow.vfs.path.PathBuilder;
import cc.whohow.vfs.path.URIBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;

import java.net.URI;

public class UriFileName implements FileNameImpl {
    protected final URI uri;

    public UriFileName(URI uri) {
        this.uri = uri;
    }

    public UriFileName(String uri) {
        this(URI.create(uri));
    }

    @Override
    public String getPath() {
        return uri.getRawPath();
    }

    @Override
    public String getPathDecoded() {
        return uri.getPath();
    }

    @Override
    public String getScheme() {
        return uri.getScheme();
    }

    @Override
    public String getRootURI() {
        return new URIBuilder(uri).setPath(ROOT_PATH).toString();
    }

    @Override
    public String getRelativeName(FileName name) throws FileSystemException {
        if (name instanceof UriFileName) {
            UriFileName that = (UriFileName) name;
            return uri.relativize(that.uri).toString();
        } else {
            return uri.relativize(URI.create(name.getURI())).toString();
        }
    }

    @Override
    public String getURI() {
        return uri.toString();
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

    @Override
    public String toString() {
        return uri.toString();
    }

    public URI toURI() {
        return uri;
    }
}
