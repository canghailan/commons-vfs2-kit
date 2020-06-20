package cc.whohow.fs;

/**
 * 文件监听事件
 */
public interface FileWatchEvent<P extends Path, F extends File<P, F>> {
    /**
     * 类型
     */
    Kind kind();

    /**
     * 监听文件/文件夹
     */
    File<P, F> watchable();

    /**
     * 触发事件文件
     */
    File<P, F> file();

    enum Kind {
        /**
         * 新增
         */
        CREATE("+"),
        /**
         * 删除
         */
        DELETE("-"),
        /**
         * 修改
         */
        MODIFY("*"),
        /**
         * 无变化
         */
        NOT_MODIFIED("=");

        private final String symbol;

        Kind(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
