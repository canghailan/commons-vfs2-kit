package cc.whohow.fs;

/**
 * 对象存储
 */
public interface ObjectFileManager extends AutoCloseable {
    ObjectFile get(CharSequence uri);
}
