package cc.whohow.fs.watch;

import cc.whohow.fs.File;
import cc.whohow.fs.FileStream;
import cc.whohow.fs.FileWatchEvent;
import cc.whohow.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 聚合WatchKey，聚合有相同路径的监听任务，提高监听效率
 */
public class AggregatedPollingFileWatchKey<P extends Path, F extends File<P, F>, V> implements Runnable {
    private static final Logger log = LogManager.getLogger(AggregatedPollingFileWatchKey.class);
    protected final Function<F, V> diffKey;
    protected final NavigableMap<P, PollingFileWatchKey<P, F>> watchKeys = new ConcurrentSkipListMap<>();
    // 上次运行时的root和index
    protected volatile File<P, F> root;
    protected volatile Map<P, V> index;

    public AggregatedPollingFileWatchKey(Function<F, V> diffKey, File<P, F> root) {
        this.diffKey = diffKey;
        this.root = root;
    }

    public boolean isValid() {
        return !watchKeys.isEmpty();
    }

    public File<P, F> getRoot() {
        return watchKeys.firstEntry().getValue().watchable();
    }

    public synchronized PollingFileWatchKey<P, F> addListener(F file, Consumer<? super FileWatchEvent<P, F>> listener) {
        PollingFileWatchKey<P, F> watchKey = watchKeys.get(file.getPath());
        if (watchKey == null) {
            if (file.getPath().startsWith(root.getPath()) ||
                    root.getPath().startsWith(file.getPath())) {
                // 新增时，再次校验是否有公共路径
                watchKey = new PollingFileWatchKey<>(file);
                log.debug("add WatchKey: {}", watchKey);
                watchKeys.put(watchKey.watchable().getPath(), watchKey);
            } else {
                throw new IllegalArgumentException(file.toString());
            }
        }
        watchKey.addListener(listener);
        return watchKey;
    }

    public synchronized PollingFileWatchKey<P, F> removeListener(F file, Consumer<? super FileWatchEvent<P, F>> listener) {
        PollingFileWatchKey<P, F> watchKey = watchKeys.get(file.getPath());
        if (watchKey == null) {
            throw new IllegalArgumentException(file.toString());
        }
        watchKey.removeListener(listener);
        // 当所有监听都移除后，移除WatchKey
        if (!watchKey.isValid()) {
            log.debug("remove WatchKey: {}", watchKey);
            watchKeys.remove(watchKey.watchable().getPath());
        }
        return watchKey;
    }

    @Override
    public synchronized void run() {
        File<P, F> oldRoot = root;
        Map<P, V> oldIndex = index;
        File<P, F> newRoot = getRoot();
        Map<P, V> newIndex = buildIndex(newRoot);

        root = newRoot;
        index = newIndex;
        if (oldIndex == null) {
            return;
        }

        if (oldRoot.equals(newRoot)) {
            // 公共路径未变化，执行简单比对逻辑
            for (Map.Entry<P, V> e : newIndex.entrySet()) {
                V newValue = e.getValue();
                V oldValue = oldIndex.remove(e.getKey());
                if (oldValue == null) {
                    notify(FileWatchEvent.Kind.CREATE, e.getKey());
                } else if (!Objects.equals(newValue, oldValue)) {
                    notify(FileWatchEvent.Kind.MODIFY, e.getKey());
                }
            }
            for (Map.Entry<P, V> e : oldIndex.entrySet()) {
                notify(FileWatchEvent.Kind.DELETE, e.getKey());
            }
        } else {
            // 计算2次运行公共路径，只处理公共路径部分
            log.debug("root changed: {} -> {}", oldRoot, newRoot);
            P commonPath = oldRoot.getPath().startsWith(newRoot.getPath()) ?
                    oldRoot.getPath() : newRoot.getPath();
            for (Map.Entry<P, V> e : newIndex.entrySet()) {
                if (e.getKey().startsWith(commonPath)) {
                    V newValue = e.getValue();
                    V oldValue = oldIndex.remove(e.getKey());
                    if (oldValue == null) {
                        notify(FileWatchEvent.Kind.CREATE, e.getKey());
                    } else if (!Objects.equals(newValue, oldValue)) {
                        notify(FileWatchEvent.Kind.MODIFY, e.getKey());
                    }
                }
            }
            for (Map.Entry<P, V> e : oldIndex.entrySet()) {
                if (e.getKey().startsWith(commonPath)) {
                    notify(FileWatchEvent.Kind.DELETE, e.getKey());
                }
            }
        }
    }

    protected void notify(FileWatchEvent.Kind kind, P path) {
        F file = getFile(path);
        for (PollingFileWatchKey<P, F> watchKey : watchKeys.values()) {
            if (file.getPath().startsWith(watchKey.watchable().getPath())) {
                watchKey.accept(new ImmutableFileWatchEvent<>(kind, watchKey.watchable(), file));
            }
        }
    }

    protected F getFile(P path) {
        return root.getFileSystem().get(path);
    }

    protected Map<P, V> buildIndex(File<P, F> file) {
        try (FileStream<F> stream = file.tree()) {
            Map<P, V> index = new LinkedHashMap<>();
            for (F f : stream) {
                if (f.isRegularFile()) {
                    index.put(f.getPath(), diffKey.apply(f));
                }
            }
            return index;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
