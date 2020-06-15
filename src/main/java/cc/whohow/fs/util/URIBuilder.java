package cc.whohow.fs.util;

import org.apache.commons.vfs2.FileName;

import java.net.URI;
import java.net.URISyntaxException;

public class URIBuilder {
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

    public URIBuilder() {
    }

    public URIBuilder(String uri) {
        setURI(uri);
    }

    public URIBuilder(URI uri) {
        setURI(uri);
    }

    public static String resolve(String baseUri, String uri) {
        return isRelative(uri) ? new URIBuilder(baseUri).resolve(uri).toString() : uri;
    }

    /**
     * 是否是相对路径
     */
    public static boolean isRelative(String uri) {
        return isRelative(URI.create(uri));
    }

    /**
     * 是否是相对路径
     */
    public static boolean isRelative(URI uri) {
        return uri.getScheme() == null &&
                uri.getHost() == null &&
                uri.getPath() != null &&
                !uri.getPath().startsWith(FileName.SEPARATOR);
    }

    public URIBuilder setURI(String uri) {
        return setURI(URI.create(uri));
    }

    public URIBuilder setURI(URI uri) {
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

    public URIBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getFragment() {
        return fragment;
    }

    public URIBuilder setFragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public URIBuilder setUserInfo(String userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    public String getHost() {
        return host;
    }

    public URIBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public URIBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public String getPath() {
        return path;
    }

    public URIBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public URIBuilder setQuery(String query) {
        this.query = query;
        return this;
    }

    public URIBuilder resolve(String path) {
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
