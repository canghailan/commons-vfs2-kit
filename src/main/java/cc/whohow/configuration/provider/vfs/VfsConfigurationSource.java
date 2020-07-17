package cc.whohow.configuration.provider.vfs;

import cc.whohow.configuration.provider.AbstractConfiguration;
import cc.whohow.fs.util.IO;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class VfsConfigurationSource extends AbstractConfiguration<ByteBuffer> implements FileListener {
    private static final Logger log = LogManager.getLogger(VfsConfigurationSource.class);
    protected final FileObject fileObject;

    public VfsConfigurationSource(FileObject fileObject) {
        this.fileObject = fileObject;
        this.fileObject.getFileSystem().addListener(this.fileObject, this);
    }

    @Override
    public ByteBuffer get() {
        try (FileContent content = fileObject.getContent()) {
            return IO.read(content.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void set(ByteBuffer value) {
        try (FileContent content = fileObject.getContent();
             OutputStream stream = content.getOutputStream()) {
            IO.write(stream, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void close() throws Exception {
        if (listeners.isEmpty()) {
            // 引用计数为0
            try {
                fileObject.getFileSystem().removeListener(fileObject, this);
            } finally {
                fileObject.close();
            }
        } else {
            log.debug("close with listeners({})", listeners.size());
        }
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        notify(get());
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        notify(get());
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        notify(null);
    }
}
