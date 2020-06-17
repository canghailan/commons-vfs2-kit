package cc.whohow.fs.provider.aliyun.cdn;

import java.time.Duration;
import java.util.Objects;

public class AliyunCDNConfiguration {
    private final String origin;
    private final String cdn;
    private final String type;
    private final String key;
    private final Duration ttl;

    public AliyunCDNConfiguration(String origin, String cdn) {
        this(origin, cdn, null, null, null);
    }

    public AliyunCDNConfiguration(String origin, String cdn, String type, String key, Duration ttl) {
        this.origin = origin;
        this.cdn = cdn;
        this.type = type;
        this.key = key;
        this.ttl = ttl;
    }

    public String getOrigin() {
        return origin;
    }

    public String getCdn() {
        return cdn;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public Duration getTtl() {
        return ttl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AliyunCDNConfiguration) {
            AliyunCDNConfiguration that = (AliyunCDNConfiguration) o;
            return origin.equals(that.origin) &&
                    cdn.equals(that.cdn) &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(key, that.key) &&
                    Objects.equals(ttl, that.ttl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, cdn, type, key, ttl);
    }

    @Override
    public String toString() {
        return "AliyunCDNConfiguration{" +
                "origin='" + origin + '\'' +
                ", cdn='" + cdn + '\'' +
                ", type='" + type + '\'' +
                ", key='" + key + '\'' +
                ", ttl=" + ttl +
                '}';
    }
}
