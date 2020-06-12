package cc.whohow.fs;

public interface Provider extends AutoCloseable {
    default String getName() {
        return getClass().getName();
    }

    void initialize(VirtualFileSystem vfs, File<?, ?> context) throws Exception;
}
