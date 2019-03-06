package cc.whohow.vfs;

import cc.whohow.vfs.io.CompositeCloseable;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;

import java.io.IOException;
import java.io.InputStream;

public class FileObjectInputStream extends InputStream {
    protected CompositeCloseable closeable = new CompositeCloseable();
    protected FileObject fileObject;
    protected FileContent fileContent;
    protected InputStream input;

    public FileObjectInputStream(String path) throws IOException {
        this(VFS.getManager().resolveFile(path));
    }

    public FileObjectInputStream(FileObject fileObject) throws IOException {
        try {
            this.fileObject = fileObject;
            this.closeable.compose(fileObject);
            this.fileContent = fileObject.getContent();
            this.closeable.compose(fileContent);
            this.input = fileContent.getInputStream();
            this.closeable.compose(input);
        } catch (Exception e) {
            this.closeable.close();
            throw e;
        }
    }

    public FileObjectInputStream(FileContent fileContent) throws IOException {
        try {
            this.fileContent = fileContent;
            this.closeable.compose(fileContent);
            this.input = fileContent.getInputStream();
            this.closeable.compose(input);
        } catch (Exception e) {
            this.closeable.close();
            throw e;
        }
    }

    public FileObjectInputStream(FileObject fileObject, FileContent fileContent, InputStream input) {
        this.fileObject = fileObject;
        this.fileContent = fileContent;
        this.input = input;
        this.closeable.compose(fileObject).compose(fileContent).compose(input);
    }

    @Override
    public int read() throws IOException {
        return input.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return input.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return input.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return input.skip(n);
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }

    @Override
    public void mark(int readlimit) {
        input.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        input.reset();
    }

    @Override
    public boolean markSupported() {
        return input.markSupported();
    }
}
