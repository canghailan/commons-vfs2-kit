package cc.whohow.vfs.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class Ping implements LongSupplier, Supplier<Long> {
    private final String address;
    private final int timeout;

    public Ping(String address) {
        this(address, 1000);
    }

    public Ping(String address, int timeout) {
        this.address = address;
        this.timeout = timeout;
    }

    @Override
    public long getAsLong() {
        long timestamp = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, 80), timeout);
            return System.currentTimeMillis() - timestamp;
        } catch (IOException ignore) {
            return -1L;
        }
    }

    @Override
    public Long get() {
        return getAsLong();
    }
}
