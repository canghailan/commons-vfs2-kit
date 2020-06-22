package cc.whohow.fs.provider.http;

import cc.whohow.fs.provider.DefaultFileResolver;
import cc.whohow.fs.provider.UriPath;

import java.net.URI;
import java.util.Optional;

public class HttpFileResolver extends DefaultFileResolver<UriPath, HttpFile> {
    public HttpFileResolver(HttpFileSystem fileSystem, String base) {
        super(fileSystem, base);
    }

    @Override
    public Optional<HttpFile> resolve(URI uri, CharSequence mountPoint, CharSequence path) {
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            return Optional.of(fileSystem.get(fileSystem.resolve(uri)));
        } else {
            return Optional.of(fileSystem.get(fileSystem.resolve(base + path)));
        }
    }
}
