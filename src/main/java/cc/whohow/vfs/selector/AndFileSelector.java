package cc.whohow.vfs.selector;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

public class AndFileSelector implements FileSelector {
    private final FileSelector[] fileSelectors;

    public static FileSelector of(FileSelector... fileSelectors) {
        return new AndFileSelector(fileSelectors);
    }

    private AndFileSelector(FileSelector... fileSelectors) {
        this.fileSelectors = fileSelectors;
    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        for (FileSelector fileSelector : fileSelectors) {
            if (!fileSelector.includeFile(fileInfo)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        for (FileSelector fileSelector : fileSelectors) {
            if (!fileSelector.traverseDescendents(fileInfo)) {
                return false;
            }
        }
        return true;
    }
}
