package cc.whohow.fs;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Copy<F1, F2> extends Supplier<CompletableFuture<F2>> {
    F1 getSource();

    F2 getTarget();
}
