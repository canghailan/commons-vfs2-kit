package cc.whohow.fs;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 文件命令
 */
public interface FileCommand<R> extends Callable<R>, Supplier<R> {
    /**
     * 命令名
     */
    String getName();

    /**
     * 命令参数
     */
    String[] getArguments();

    /**
     * 文件系统
     */
    VirtualFileSystem getVirtualFileSystem();

    /**
     * 匹配度
     */
    int getMatchingScore();

    @Override
    default R get() {
        try {
            return call();
        } catch (Exception e) {
            throw UncheckedException.unchecked(e);
        }
    }

    final class MatchingScore {
        /**
         * 最低匹配度
         */
        public static final int UNMATCHED = -1;
        /**
         * 最低匹配度
         */
        public static final int LOW = 0;
        /**
         * 最高匹配度
         */
        public static final int HIGH = 100;
    }
}
