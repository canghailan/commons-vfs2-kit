package cc.whohow.fs;

import cc.whohow.fs.util.UncheckedCloseable;

import java.io.Closeable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 文件流（集合），使用完后需关闭，不保证可重复遍历
 */
public interface FileStream<F> extends Iterable<F>, Closeable {
    /**
     * 转为Stream，请勿同时操作FileStream、Stream，不保证同时操作的正确性
     */
    default Stream<F> stream() {
        return StreamSupport.stream(spliterator(), false)
                .onClose(new UncheckedCloseable(this));
    }
}
