package cc.whohow.vfs;

import org.apache.commons.vfs2.provider.DefaultURLStreamHandler;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class VfsStreamHandlerFactory implements URLStreamHandlerFactory {
    protected final VfsComponentContext context;

    public VfsStreamHandlerFactory(VfsComponentContext context) {
        this.context = context;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
        return new DefaultURLStreamHandler(context);
    }
}
