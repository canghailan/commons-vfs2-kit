package cc.whohow.vfs;

import cc.whohow.vfs.path.PathBuilder;
import org.apache.commons.vfs2.FileName;

public class VirtualFileName implements FileNameImpl {
    private final String name;

    public VirtualFileName(String name) {
        this.name = name;
    }

    @Override
    public String getScheme() {
        return "vfs";
    }

    @Override
    public String getURI() {
        return name;
    }

    @Override
    public FileName getRoot() {
        return new VirtualFileName("/");
    }

    @Override
    public FileName getParent() {
        if ("/".equals(name)) {
            return null;
        }
        return new VirtualFileName(new PathBuilder(name).removeLast().endsWithSeparator(true).toString());
    }
}
