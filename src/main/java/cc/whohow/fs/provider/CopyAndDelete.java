package cc.whohow.fs.provider;

import cc.whohow.fs.Copy;
import cc.whohow.fs.File;
import cc.whohow.fs.Move;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class CopyAndDelete<F1 extends File<?, F1>, F2 extends File<?, F2>> implements Move<F1, F2> {
    private static final Logger log = LogManager.getLogger(CopyAndDelete.class);
    protected final Copy<F1, F2> copy;

    public CopyAndDelete(Copy<F1, F2> copy) {
        this.copy = copy;
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
    public CompletableFuture<F2> get() {
        log.trace("move {} -> {}", getSource(), getTarget());
        return copy.get().thenApply(this::deleteSource);
    }

    protected F2 deleteSource(F2 result) {
        copy.getSource().delete();
        return result;
    }

    @Override
    public String toString() {
        return "move " + getSource() + " " + getTarget();
    }
}
