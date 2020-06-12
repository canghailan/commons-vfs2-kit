package cc.whohow.fs;

/**
 * 拷贝，返回结果文件/目录
 */
public interface FileCopyCommand extends FileCommand<File<? extends Path, ? extends File<?, ?>>> {
    @Override
    default String getName() {
        return "copy";
    }

    /**
     * 源文件/目录
     */
    default File<?, ?> getSource() {
        return getVirtualFileSystem().get(getArguments()[1]);
    }

    /**
     * 目标文件/目录
     */
    default File<?, ?> getDestination() {
        return getVirtualFileSystem().get(getArguments()[2]);
    }
}
