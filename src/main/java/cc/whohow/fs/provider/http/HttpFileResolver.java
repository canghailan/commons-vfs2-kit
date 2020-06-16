package cc.whohow.fs.provider.http;

import cc.whohow.fs.FileResolver;
import cc.whohow.fs.path.UriPath;

import java.net.URI;
import java.util.Optional;

public class HttpFileResolver implements FileResolver<UriPath, HttpFile> {
    private final HttpFileSystem httpFileSystem;
    private final String base;

    public HttpFileResolver(HttpFileSystem httpFileSystem, String base) {
        this.httpFileSystem = httpFileSystem;
        this.base = base;
    }

    @Override
    public Optional<HttpFile> resolve(URI uri, CharSequence mountPoint, CharSequence path) {
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            return Optional.of(httpFileSystem.get(httpFileSystem.resolve(uri)));
        } else {
            return Optional.of(httpFileSystem.get(httpFileSystem.resolve(base + path)));
        }
    }
}
