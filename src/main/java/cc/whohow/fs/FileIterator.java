package cc.whohow.fs;

import java.io.Closeable;
import java.util.Iterator;

/**
 * 文件遍历器，使用完后需关闭
 */
public interface FileIterator<F> extends Iterator<F>, Closeable {
}
