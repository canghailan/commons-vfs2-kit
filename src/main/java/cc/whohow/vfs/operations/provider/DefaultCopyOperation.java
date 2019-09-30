package cc.whohow.vfs.operations.provider;

import cc.whohow.vfs.io.IO;
import cc.whohow.vfs.operations.AbstractFileOperation;
import cc.whohow.vfs.operations.Copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class DefaultCopyOperation extends AbstractFileOperation<Copy.Options, Object> implements Copy {
    @Override
    public Object apply(Options options) {
        try (InputStream src = options.getSource().getInputStream();
             OutputStream dst = options.getDestination().getOutputStream()) {
            IO.transfer(src, dst);
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
