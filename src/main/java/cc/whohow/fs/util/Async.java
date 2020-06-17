package cc.whohow.fs.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Async<V> extends Supplier<CompletableFuture<V>> {
}
