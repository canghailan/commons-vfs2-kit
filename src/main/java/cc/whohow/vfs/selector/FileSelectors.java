package cc.whohow.vfs.selector;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.Selectors;

import java.lang.reflect.UndeclaredThrowableException;

public class FileSelectors {
    public static boolean include(FileSelector selector, FileObject file) {
        try {
            return selector == Selectors.SELECT_SELF ||
                    selector == Selectors.SELECT_ALL ||
                    selector == Selectors.SELECT_SELF_AND_CHILDREN ||
                    selector.includeFile(new ImmutableFileSelectInfo<>(file, file, 0));
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
