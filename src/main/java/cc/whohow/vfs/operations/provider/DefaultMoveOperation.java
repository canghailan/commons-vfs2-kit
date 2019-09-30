package cc.whohow.vfs.operations.provider;

import cc.whohow.vfs.operations.AbstractFileOperation;
import cc.whohow.vfs.operations.Copy;
import cc.whohow.vfs.operations.Move;

public class DefaultMoveOperation extends AbstractFileOperation<Move.Options, Object> implements Move {
    private final DefaultCopyOperation copy = new DefaultCopyOperation();
    private final DefaultRemoveOperation remove = new DefaultRemoveOperation();

    @Override
    public Object apply(Options options) {
        copy.apply(new Copy.Options(options.getSource(), options.getDestination(), options.getOptions()));
        remove.apply(options.getSource());
        return null;
    }
}
