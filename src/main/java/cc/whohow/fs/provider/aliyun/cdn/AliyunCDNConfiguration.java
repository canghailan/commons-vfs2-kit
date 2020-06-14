package cc.whohow.fs.provider.aliyun.cdn;

import java.time.Duration;

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
}
