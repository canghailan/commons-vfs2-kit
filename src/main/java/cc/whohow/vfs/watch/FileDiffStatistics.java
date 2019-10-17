package cc.whohow.vfs.watch;

import java.util.function.Consumer;

public class FileDiffStatistics implements Consumer<FileDiffEntry<?>> {
    private long create = 0;
    private long delete = 0;
    private long modify = 0;
    private long notModified = 0;

    @Override
    public void accept(FileDiffEntry<?> fileDiffEntry) {
        switch (fileDiffEntry.getValue()) {
            case CREATE: {
                create++;
                break;
            }
            case DELETE: {
                delete++;
                break;
            }
            case MODIFY: {
                modify++;
                break;
            }
            case NOT_MODIFIED: {
                notModified++;
                break;
            }
            default: {
                throw new IllegalArgumentException(fileDiffEntry.getValue().toString());
            }
        }
    }

    public long getCreate() {
        return create;
    }

    public long getDelete() {
        return delete;
    }

    public long getModify() {
        return modify;
    }

    public long getNotModified() {
        return notModified;
    }

    public long getCount() {
        return getCreate() +
                getDelete() +
                getModify() +
                getNotModified();
    }

    @Override
    public String toString() {
        return "Create: " + getCreate() + "\n" +
                "Delete: " + getDelete() + "\n" +
                "Modify: " + getModify() + "\n" +
                "NotModified: " + getNotModified() + "\n" +
                "Count: " + getCount() + "\n";
    }
}
