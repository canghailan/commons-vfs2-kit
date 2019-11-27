package cc.whohow.vfs.log;

import org.apache.commons.logging.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LogProxy implements InvocationHandler {
    private final Log log;
    private final Object target;

    public LogProxy(Log log, Object target) {
        this.log = log;
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(Log log, T target, Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), interfaces, new LogProxy(log, target));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object r = method.invoke(target, args);
        if (log.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(method.getName()).append("(");
            for (Object arg : args) {
                buffer.append(arg).append(", ");
            }
            if (args.length > 0) {
                buffer.setLength(buffer.length() - ", ".length());
            }
            buffer.append(") -> ");
            buffer.append(r);
//        log.info(buffer);
            log.debug(buffer);
        }
        return r;
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
