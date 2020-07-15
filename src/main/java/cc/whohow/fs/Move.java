package cc.whohow.fs;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 剪切/移动/重命名
 */
public interface Move<F1 extends File, F2 extends File> extends Callable<F2> {
    /**
     * 源文件、文件夹
     */
    F1 getSource();

    /**
     * 目标文件、文件夹
     */
    F2 getTarget();

    default F2 callUnchecked() {
        try {
            return call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    default CompletableFuture<F2> callAsync(ExecutorService executor) {
        return CompletableFuture.supplyAsync(this::callUnchecked, executor);
    }
}
