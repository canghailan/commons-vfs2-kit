package cc.whohow.vfs.operations;

public class DefaultMoveOperation extends AbstractFileOperation<Move.Options, Object> implements Move {
    private final Copy copy;
    private final Remove remove;

    public DefaultMoveOperation() {
        this(new DefaultCopyOperation(), new DefaultRemoveOperation());
    }

    public DefaultMoveOperation(Copy copy, Remove remove) {
        this.copy = copy;
        this.remove = remove;
    }

    @Override
    public Object apply(Options options) {
        copy.apply(new Copy.Options(options.getSource(), options.getDestination(), options.getOptions()));
        remove.apply(options.getSource());
        return null;
    }
}
