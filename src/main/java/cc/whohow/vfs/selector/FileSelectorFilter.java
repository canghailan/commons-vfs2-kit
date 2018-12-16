package cc.whohow.vfs.selector;

import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

import java.lang.reflect.UndeclaredThrowableException;

public class FileSelectorFilter implements FileFilter {
    private final FileSelector fileSelector;

    public FileSelectorFilter(FileSelector fileSelector) {
        this.fileSelector = fileSelector;
    }

    @Override
    public boolean accept(FileSelectInfo fileInfo) {
        try {
            return fileSelector.includeFile(fileInfo);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
