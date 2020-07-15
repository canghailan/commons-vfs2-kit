package cc.whohow.vfs;

import cc.whohow.fs.File;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

public class FileNameAdapter extends UriFileName {
    protected File file;

    public FileNameAdapter(File file) {
        super(file.getUri());
        this.file = file;
    }

    @Override
    public String getBaseName() {
        return file.getName();
    }

    @Override
    public String getExtension() {
        return file.getExtension();
    }

    @Override
    public FileName getParent() {
        File parent = file.getParent();
        if (parent == null) {
            return null;
        }
        return new FileNameAdapter(parent);
    }

    @Override
    public boolean isFile() throws FileSystemException {
        return file.isRegularFile();
    }

    @Override
    public FileType getType() {
        return file.isDirectory() ? FileType.FOLDER : FileType.FILE;
    }

    @Override
    public String getFriendlyURI() {
        return file.getPublicUri();
    }
}
