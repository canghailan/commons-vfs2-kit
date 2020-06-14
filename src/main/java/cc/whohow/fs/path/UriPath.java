package cc.whohow.fs.path;

import cc.whohow.fs.Path;

import java.net.URI;

public class UriPath implements Path {
    private final URI uri;

    public UriPath(String uri) {
        this(URI.create(uri));
    }

    public UriPath(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public UriPath getParent() {
        // TODO
        return null;
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
        if (o instanceof UriPath) {
            UriPath that = (UriPath) o;
            return that.uri.equals(this.uri);
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
