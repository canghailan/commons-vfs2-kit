package cc.whohow.vfs.provider.aliyun.oss;

import com.aliyuncs.regions.Endpoint;
import com.aliyuncs.regions.InternalEndpointsParser;
import com.aliyuncs.regions.ProductDomain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <a href="https://www.alibabacloud.com/help/zh/doc-detail/31837.htm">OSS开通Region和Endpoint对照表</a>
 */
public class AliyunOSSEndpoints {
    private static final String DEFAULT_ENDPOINT = "oss.aliyuncs.com";
    private static final String DEFAULT_INTERNAL_ENDPOINT = "oss-internal.aliyuncs.com";
    private static final Pattern ENDPOINT = Pattern.compile("^(?<sub>.+)\\.aliyuncs\\.com$");
    private static volatile CompletableFuture<Long> PING = CompletableFuture.supplyAsync(() -> ping(DEFAULT_INTERNAL_ENDPOINT));

    public static String getDefaultEndpoint() {
        return DEFAULT_ENDPOINT;
    }

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

    private static boolean isIntranet() {
        return PING.join() > 0;
    }

    public static String getEndpoint(String endpoint) {
        return isIntranet() ? getIntranetEndpoint(endpoint) : getExtranetEndpoint(endpoint);
    }

    public static String getExtranetEndpoint(String endpoint) {
        String sub = getSub(endpoint);
        if (sub.endsWith("-internal")) {
            return sub.substring(0, sub.length() - "-internal".length()) + ".aliyuncs.com";
        } else {
            return endpoint;
        }
    }

    public static String getIntranetEndpoint(String endpoint) {
        String sub = getSub(endpoint);
        if (sub.endsWith("-internal")) {
            return endpoint;
        } else {
            return sub + "-internal.aliyuncs.com";
        }
    }

    private static String getSub(String endpoint) {
        Matcher matcher = ENDPOINT.matcher(endpoint);
        if (matcher.matches()) {
            return matcher.group("sub");
        }
        throw new IllegalArgumentException(endpoint);
    }

    private static long ping(String address) {
        return ping(address, 1000);
    }

    private static long ping(String address, int timeout) {
        long timestamp = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, 80), timeout);
            return System.currentTimeMillis() - timestamp;
        } catch (IOException ignore) {
            return -1L;
        }
    }
}
