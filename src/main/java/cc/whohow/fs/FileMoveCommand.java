package cc.whohow.fs;

/**
 * 剪切，返回结果文件/目录
 */
public interface FileMoveCommand extends FileCommand<File<? extends Path, ? extends File<?, ?>>> {
    @Override
    default String getName() {
        return "move";
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
