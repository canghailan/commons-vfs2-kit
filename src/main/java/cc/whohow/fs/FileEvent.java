package cc.whohow.fs;

/**
 * 文件监听事件
 */
public interface FileEvent {
    /**
     * 类型
     */
    Kind kind();

    /**
     * 触发事件文件
     */
    File file();

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
