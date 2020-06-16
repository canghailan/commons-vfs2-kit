package cc.whohow.fs.io;

import cc.whohow.fs.Command;
import cc.whohow.fs.File;

public class Move<F1 extends File<?, F1>, F2 extends File<?, F2>> implements Command<F2> {
    protected final Copy<F1, F2> copy;

    public Move(Copy<F1, F2> copy) {
        this.copy = copy;
    }

    @Override
    public F2 call() throws Exception {
        F2 file = copy.call();
        copy.getSource().delete();
        return file;
    }
}
