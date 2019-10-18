package cc.whohow.vfs.watch;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

public class FileDiffStatistics implements Consumer<FileDiffEntry<?>> {
    private LongAdder create = new LongAdder();
    private LongAdder delete = new LongAdder();
    private LongAdder modify = new LongAdder();
    private LongAdder notModified = new LongAdder();

    @Override
    public void accept(FileDiffEntry<?> fileDiffEntry) {
        switch (fileDiffEntry.getValue()) {
            case CREATE: {
                create.increment();
                break;
            }
            case DELETE: {
                delete.increment();
                break;
            }
            case MODIFY: {
                modify.increment();
                break;
            }
            case NOT_MODIFIED: {
                notModified.increment();
                break;
            }
            default: {
                throw new IllegalArgumentException(fileDiffEntry.getValue().toString());
            }
        }
    }

    public long getCreate() {
        return create.longValue();
    }

    public long getDelete() {
        return delete.longValue();
    }

    public long getModify() {
        return modify.longValue();
    }

    public long getNotModified() {
        return notModified.longValue();
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
