package cc.whohow.fs.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UriBuilder {
    // Components of all URIs: [<scheme>:]<scheme-specific-part>[#<fragment>]
    private String scheme;            // null ==> relative URI
    private String fragment;

    // Server-based authority: [<userInfo>@]<host>[:<port>]
    private String userInfo;
    private String host;              // null ==> registry-based
    private int port = -1;            // -1 ==> undefined

    // Remaining components of hierarchical URIs
    private String path;              // null ==> opaque
    private String query;

    public UriBuilder() {
    }

    public UriBuilder(String uri) {
        setURI(uri);
    }

    public UriBuilder(URI uri) {
        setURI(uri);
    }

    public UriBuilder setURI(String uri) {
        return setURI(URI.create(uri));
    }

    public UriBuilder setURI(URI uri) {
        this.scheme = uri.getScheme();
        this.fragment = uri.getFragment();
        this.userInfo = uri.getUserInfo();
        this.host = uri.getHost();
        this.port = uri.getPort();
        this.path = uri.getPath();
        this.query = uri.getQuery();
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public UriBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getFragment() {
        return fragment;
    }

    public UriBuilder setFragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public UriBuilder setUserInfo(String userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public String getHost() {
        return host;
    }

    public UriBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public UriBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public String getPath() {
        return path;
    }

    public UriBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public UriBuilder setQuery(String query) {
        this.query = query;
        return this;
    }

    public UriBuilder resolve(String path) {
        this.path = new PathBuilder(this.path).resolve(path).toString();
        return this;
    }

    public URI build() {
        try {
            return new URI(scheme, userInfo, host, port, path, query, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return build().toString();
    }
}
