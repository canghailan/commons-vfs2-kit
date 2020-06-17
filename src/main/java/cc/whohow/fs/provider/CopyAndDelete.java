package cc.whohow.fs.provider;

import cc.whohow.fs.Copy;
import cc.whohow.fs.File;
import cc.whohow.fs.Move;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CopyAndDelete<F1 extends File<?, F1>, F2 extends File<?, F2>> implements Move<F1, F2> {
    private static final Logger log = LogManager.getLogger(CopyAndDelete.class);
    protected final Copy<F1, F2> copy;

    public CopyAndDelete(Copy<F1, F2> copy) {
        this.copy = copy;
    }

    @Override
    public F2 call() throws Exception {
        log.trace("move {} -> {}", getSource(), getTarget());
        F2 file = copy.call();
        copy.getSource().delete();
        return file;
    }

    @Override
    public F1 getSource() {
        return copy.getSource();
    }

    @Override
    public F2 getTarget() {
        return copy.getTarget();
    }

    @Override
    public String toString() {
        return "move " + getSource() + " " + getTarget();
    }
}
