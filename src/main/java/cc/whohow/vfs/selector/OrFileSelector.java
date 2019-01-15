package cc.whohow.vfs.selector;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

public class OrFileSelector implements FileSelector {
    private final FileSelector[] fileSelectors;

    private OrFileSelector(FileSelector... fileSelectors) {
        this.fileSelectors = fileSelectors;
    }

    public static FileSelector of(FileSelector... fileSelectors) {
        return new OrFileSelector(fileSelectors);
    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        for (FileSelector fileSelector : fileSelectors) {
            if (fileSelector.includeFile(fileInfo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        for (FileSelector fileSelector : fileSelectors) {
            if (fileSelector.traverseDescendents(fileInfo)) {
                return true;
            }
        }
        return false;
    }
}
