package cc.whohow.fs.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class Ping implements LongSupplier, Supplier<Long> {
    private final InetSocketAddress address;
    private final int timeout;
    private long time = Long.MIN_VALUE;

    public Ping(String address) {
        this(address, 1000);
    }

    public Ping(String address, int timeout) {
        this(new InetSocketAddress(address, 80), timeout);
    }

    public Ping(InetSocketAddress address, int timeout) {
        this.address = address;
        this.timeout = timeout;
    }

    @Override
    public long getAsLong() {
        if (time != Long.MIN_VALUE) {
            return time;
        }
        try (Socket socket = new Socket()) {
            long timestamp = System.currentTimeMillis();
            socket.connect(address, timeout);
            return time = System.currentTimeMillis() - timestamp;
        } catch (IOException ignore) {
            return -1L;
        }
    }

    @Override
    public Long get() {
        return getAsLong();
    }
}
