package cc.whohow.fs;

import java.io.Closeable;
import java.util.Iterator;

/**
 * 文件遍历器
 *
 * @param <F>
 */
public interface FileIterator<F> extends Iterator<F>, Closeable {
}
