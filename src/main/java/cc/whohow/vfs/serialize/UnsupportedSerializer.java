package cc.whohow.vfs.serialize;

import java.io.InputStream;
import java.io.OutputStream;

public class UnsupportedSerializer<T> implements Serializer<T> {
    private static final UnsupportedSerializer INSTANCE = new UnsupportedSerializer();

    @SuppressWarnings("unchecked")
    public <TT> UnsupportedSerializer<TT> get() {
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
