package cc.whohow.configuration.provider;

import cc.whohow.configuration.Configuration;

import java.nio.ByteBuffer;

public class FileConfiguration extends CacheableConfiguration<ByteBuffer> {
    public FileConfiguration(Configuration<ByteBuffer> source) {
        super(source);
    }

    @Override
    public ByteBuffer get() {
        return super.get().duplicate();
    }
}
