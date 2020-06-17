package cc.whohow.fs.provider.http;

import cc.whohow.fs.*;
import cc.whohow.fs.provider.UriPath;
import cc.whohow.fs.util.Files;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;

public class HttpFileSystem implements FileSystem<UriPath, HttpFile> {
    private static final Logger log = LogManager.getLogger(HttpFileSystem.class);
    protected final URI uri;
    protected final FileSystemAttributes attributes;
    protected final CloseableHttpClient httpClient;

    public HttpFileSystem(URI uri) {
        this(uri, Files.emptyFileSystemAttributes());
    }

    public HttpFileSystem(URI uri, FileSystemAttributes attributes) {
        this.uri = uri;
        this.attributes = attributes;
        this.httpClient = HttpClients.createDefault();
    }

    public HttpFileSystem(URI uri, FileSystemAttributes attributes, HttpClientBuilder builder) {
        this.uri = uri;
        this.attributes = attributes;
        this.httpClient = builder.build();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public FileSystemAttributes readAttributes() {
        return attributes;
    }

    public UriPath resolve(URI uri) {
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            return new UriPath(uri);
        }
        throw new IllegalArgumentException(uri.toString());
    }

    @Override
    public UriPath getParent(UriPath path) {
        return null;
    }

    @Override
    public boolean exists(UriPath path) {
        log.trace("GET {}", path);
        try (CloseableHttpResponse response = httpClient.execute(new HttpGet(path.toUri()))) {
            return response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public HttpFile get(UriPath path) {
        return new HttpFile(this, path);
    }

    @Override
    public FileAttributes readAttributes(UriPath path) {
        log.trace("GET {}", path);
        try (CloseableHttpResponse response = httpClient.execute(new HttpGet(path.toUri()))) {
            return new HttpFileAttributes(response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FileReadableChannel newReadableChannel(UriPath path) {
        CloseableHttpResponse httpResponse = null;
        try {
            log.trace("GET {}", path);
            return new HttpFileReadableChannel(httpResponse = httpClient.execute(new HttpGet(path.toUri())));
        } catch (Exception e) {
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (Exception ex) {
                log.warn("close HttpResponse error", ex);
            }
            throw UncheckedException.unchecked(e);
        }
    }

    @Override
    public FileWritableChannel newWritableChannel(UriPath path) {
        throw new UnsupportedOperationException("http");
    }

    @Override
    public DirectoryStream<HttpFile> newDirectoryStream(UriPath path) {
        throw new UnsupportedOperationException("http");
    }

    @Override
    public void delete(UriPath path) {
        throw new UnsupportedOperationException("http");
    }

    @Override
    public void close() throws Exception {
        log.debug("close HttpFileSystem: {}", uri);
        log.debug("close httpClient");
        httpClient.close();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
