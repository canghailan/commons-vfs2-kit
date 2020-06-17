package cc.whohow.fs.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class Ping implements LongSupplier, Supplier<Long> {
    private static final Logger log = LogManager.getLogger(Ping.class);
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
            time = System.currentTimeMillis() - timestamp;
            log.trace("ping {} {}ms", address, time);
        } catch (IOException ignore) {
            time = -1;
            log.trace("ping {} timeout", address);
        }
        return time;
    }

    @Override
    public Long get() {
        return getAsLong();
    }
}
