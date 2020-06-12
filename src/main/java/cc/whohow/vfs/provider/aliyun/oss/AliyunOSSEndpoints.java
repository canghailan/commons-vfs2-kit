package cc.whohow.vfs.provider.aliyun.oss;

import cc.whohow.fs.net.Ping;
import com.aliyuncs.regions.Endpoint;
import com.aliyuncs.regions.InternalEndpointsParser;
import com.aliyuncs.regions.ProductDomain;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <a href="https://www.alibabacloud.com/help/zh/doc-detail/31837.htm">OSS开通Region和Endpoint对照表</a>
 */
public class AliyunOSSEndpoints {
    private static final String ENDPOINT_SUFFIX = ".aliyuncs.com";
    private static final String INTERNAL_ENDPOINT_SUFFIX = "-internal.aliyuncs.com";
    private static final String DEFAULT_ENDPOINT = "oss.aliyuncs.com";
    private static final String DEFAULT_INTERNAL_ENDPOINT = "oss-internal.aliyuncs.com";
    private static final Map<String, CompletableFuture<Long>> PING = new ConcurrentHashMap<>();

    /**
     * 默认公网endpoint
     */
    public static String getDefaultEndpoint() {
        return DEFAULT_ENDPOINT;
    }

    /**
     * 默认公网endpoint
     */
    public static String getDefaultInternalEndpoint() {
        return DEFAULT_INTERNAL_ENDPOINT;
    }

    /**
     * 获取所有endpoint
     */
    public static Set<String> getEndpoints() {
        Set<String> endpoints = new TreeSet<>();
        try {
            for (Endpoint endpoint : new InternalEndpointsParser().getEndpoints()) {
                for (ProductDomain productDomain : endpoint.getProductDomains()) {
                    if ("oss".equalsIgnoreCase(productDomain.getProductName())) {
                        endpoints.add(productDomain.getDomianName());
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return endpoints;
    }

    /**
     * 自动根据网络环境选择endpoint
     */
    public static String getEndpoint(String endpoint) {
        // 优先内网
        String intranet = getIntranetEndpoint(endpoint);
        if (ping(intranet).join() >= 0) {
            return intranet;
        }
        // 默认公网
        return getExtranetEndpoint(endpoint);
    }

    /**
     * 获取公网endpoint
     */
    public static String getExtranetEndpoint(String endpoint) {
        if (endpoint.endsWith(INTERNAL_ENDPOINT_SUFFIX)) {
            return endpoint.substring(0, endpoint.length() - INTERNAL_ENDPOINT_SUFFIX.length()) + ENDPOINT_SUFFIX;
        }
        return endpoint;
    }

    /**
     * 获取内网endpoint
     */
    public static String getIntranetEndpoint(String endpoint) {
        if (endpoint.endsWith(INTERNAL_ENDPOINT_SUFFIX)) {
            return endpoint;
        }
        if (endpoint.endsWith(ENDPOINT_SUFFIX)) {
            return endpoint.substring(0, endpoint.length() - ENDPOINT_SUFFIX.length()) + INTERNAL_ENDPOINT_SUFFIX;
        }
        return endpoint;
    }

    /**
     * 获取区域
     */
    public static String getRegion(String endpoint) {
        if (endpoint.endsWith(INTERNAL_ENDPOINT_SUFFIX)) {
            return endpoint.substring(0, endpoint.length() - INTERNAL_ENDPOINT_SUFFIX.length());
        }
        if (endpoint.endsWith(ENDPOINT_SUFFIX)) {
            return endpoint.substring(0, endpoint.length() - ENDPOINT_SUFFIX.length());
        }
        return null;
    }

    /**
     * 是否内网
     */
    public static boolean isInternal(String endpoint) {
        return endpoint.endsWith(INTERNAL_ENDPOINT_SUFFIX);
    }

    /**
     * ping
     */
    private static CompletableFuture<Long> ping(String address) {
        return PING.computeIfAbsent(address, AliyunOSSEndpoints::pingAsync);
    }

    /**
     * pingAsync
     */
    private static CompletableFuture<Long> pingAsync(String address) {
        return CompletableFuture.supplyAsync(new Ping(address));
    }
}
