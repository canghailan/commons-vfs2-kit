package cc.whohow.fs.provider;

import cc.whohow.fs.Path;
import cc.whohow.fs.util.Paths;

import java.net.URI;

public class UriPath implements Path {
    protected final URI uri;

    public UriPath(String uri) {
        this(URI.create(uri));
    }

    public UriPath(URI uri) {
//        if (!uri.isAbsolute() &&
//                !(uri.getPath() != null && uri.getPath().startsWith("/"))) {
//            throw new IllegalArgumentException(uri.toString());
//        }
        this.uri = uri;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public UriPath getParent() {
        URI parent = Paths.getParent(uri);
        if (parent == null) {
            return null;
        }
        return new UriPath(parent);
    }

    @Override
    public UriPath resolve(String relative) {
        return new UriPath(uri.resolve(relative));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && getClass() == o.getClass()) {
            UriPath that = (UriPath) o;
            return uri.equals(that.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
