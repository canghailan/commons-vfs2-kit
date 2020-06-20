package cc.whohow.fs;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 剪切/移动/重命名
 */
public interface Move<F1, F2> extends Supplier<CompletableFuture<F2>> {
    /**
     * 源文件、文件夹
     */
    F1 getSource();

    /**
     * 目标文件、文件夹
     */
    F2 getTarget();
}
