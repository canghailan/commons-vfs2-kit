package cc.whohow.vfs;

import cc.whohow.vfs.io.CompositeCloseable;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;

import java.io.IOException;
import java.io.OutputStream;

public class FileObjectOutputStream extends OutputStream {
    protected CompositeCloseable closeable = new CompositeCloseable();
    protected FileObject fileObject;
    protected FileContent fileContent;
    protected OutputStream output;

    public FileObjectOutputStream(String path) throws IOException {
        this(path, false);
    }

    public FileObjectOutputStream(String path, boolean append) throws IOException {
        this(VFS.getManager().resolveFile(path), append);
    }

    public FileObjectOutputStream(FileObject fileObject) throws IOException {
        this(fileObject, false);
    }

    public FileObjectOutputStream(FileObject fileObject, boolean append) throws IOException {
        try {
            this.fileObject = fileObject;
            this.closeable.compose(fileObject);
            this.fileContent = fileObject.getContent();
            this.closeable.compose(fileContent);
            this.output = fileContent.getOutputStream(append);
            this.closeable.compose(output);
        } catch (Exception e) {
            this.closeable.close();
            throw e;
        }
    }

    public FileObjectOutputStream(FileContent fileContent) throws IOException {
        this(fileContent, false);
    }

    public FileObjectOutputStream(FileContent fileContent, boolean append) throws IOException {
        try {
            this.fileContent = fileContent;
            this.closeable.compose(fileContent);
            this.output = fileContent.getOutputStream(append);
            this.closeable.compose(output);
        } catch (Exception e) {
            this.closeable.close();
            throw e;
        }
    }

    public FileObjectOutputStream(FileObject fileObject, FileContent fileContent, OutputStream output) {
        this.fileObject = fileObject;
        this.fileContent = fileContent;
        this.output = output;
        this.closeable.compose(fileObject).compose(fileContent).compose(output);
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }
}
