package cc.whohow.fs;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

@FunctionalInterface
public interface Command<R> extends Callable<R>, Supplier<R> {
    default R get() {
        try {
            return call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }
}
