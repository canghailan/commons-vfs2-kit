package cc.whohow.vfs.type;

import java.io.InputStream;
import java.io.OutputStream;

public class UnsupportedType<T> implements DataType<T> {
    private static final UnsupportedType INSTANCE = new UnsupportedType();

    @SuppressWarnings("unchecked")
    public <TT> UnsupportedType<TT> get() {
        return INSTANCE;
    }

    @Override
    public T deserialize(InputStream stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serialize(OutputStream stream, T value) {
        throw new UnsupportedOperationException();
    }
}
