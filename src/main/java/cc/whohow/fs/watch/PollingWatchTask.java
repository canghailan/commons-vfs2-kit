package cc.whohow.fs.watch;

import cc.whohow.fs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 文件监听轮询任务
 */
public class PollingWatchTask<P extends Path, F extends GenericFile<P, F>, V> implements Runnable {
    private static final Logger log = LogManager.getLogger(PollingWatchTask.class);
    protected final Function<F, V> compareKey;
    /**
     * 监听集合，一个可变的（其他线程控制），多线程安全集合
     */
    protected final Collection<PollingFileWatchKey<P, F>> watchKeys;
    /**
     * 监听事件通知
     */
    protected final BiConsumer<FileEvent.Kind, P> onWatchEvent;
    /**
     * 原分组索引
     */
    protected volatile NavigableMap<P, NavigableMap<P, PollingFileWatchKey<P, F>>> oldRoot;
    /**
     * 新分组索引
     */
    protected volatile NavigableMap<P, NavigableMap<P, PollingFileWatchKey<P, F>>> newRoot;
    /**
     * 原文件索引
     */
    protected volatile Map<P, V> oldIndex;
    /**
     * 新文件索引
     */
    protected volatile Map<P, V> newIndex;

    public PollingWatchTask(Function<F, V> compareKey,
                            Collection<PollingFileWatchKey<P, F>> watchKeys,
                            BiConsumer<FileEvent.Kind, P> onWatchEvent) {
        this.compareKey = compareKey;
        this.watchKeys = watchKeys;
        this.onWatchEvent = onWatchEvent;
    }

    @Override
    public synchronized void run() {
        oldRoot = newRoot;
        oldIndex = newIndex;

        // 构建新分组索引
        newRoot = buildRoot(watchKeys);
        // 构建新文件索引
        newIndex = buildIndex(newRoot.values());

        // 第一次运行
        if (oldRoot == null) {
            return;
        }

        if (newRoot.keySet().equals(oldRoot.keySet())) {
            // 分组索引未改变，简单模式
            for (Map.Entry<P, V> e : newIndex.entrySet()) {
                V newValue = e.getValue();
                V oldValue = oldIndex.remove(e.getKey());
                if (oldValue == null) {
                    onWatchEvent.accept(FileEvent.Kind.CREATE, e.getKey());
                } else if (!Objects.equals(newValue, oldValue)) {
                    onWatchEvent.accept(FileEvent.Kind.MODIFY, e.getKey());
                }
            }
            for (Map.Entry<P, V> e : oldIndex.entrySet()) {
                onWatchEvent.accept(FileEvent.Kind.DELETE, e.getKey());
            }
        } else {
            // 分组索引改变，需注意监听范围扩大、缩小导致的新增、删除事件误报
            for (Map.Entry<P, V> e : newIndex.entrySet()) {
                V newValue = e.getValue();
                V oldValue = oldIndex.remove(e.getKey());
                if (oldValue == null) {
                    if (startsWithAny(e.getKey(), oldRoot.keySet())) {
                        // 文件在原监听范围内，非误报
                        onWatchEvent.accept(FileEvent.Kind.CREATE, e.getKey());
                    }
                } else if (!Objects.equals(newValue, oldValue)) {
                    onWatchEvent.accept(FileEvent.Kind.MODIFY, e.getKey());
                }
            }
            for (Map.Entry<P, V> e : oldIndex.entrySet()) {
                if (startsWithAny(e.getKey(), newRoot.keySet())) {
                    // 文件在新监听范围内，非误报
                    onWatchEvent.accept(FileEvent.Kind.DELETE, e.getKey());
                }
            }
        }
    }

    protected NavigableMap<P, NavigableMap<P, PollingFileWatchKey<P, F>>> buildRoot(
            Collection<PollingFileWatchKey<P, F>> watchKeys) {
        List<NavigableMap<P, PollingFileWatchKey<P, F>>> groups = new ArrayList<>();
        for (PollingFileWatchKey<P, F> watchKey : watchKeys) {
            P path = watchKey.watchable().getPath();
            NavigableMap<P, PollingFileWatchKey<P, F>> group = getWatchKeyGroup(groups, path);
            if (group == null) {
                group = new TreeMap<>();
                groups.add(group);
            }
            group.put(path, watchKey);
        }

        NavigableMap<P, NavigableMap<P, PollingFileWatchKey<P, F>>> root = new TreeMap<>();
        for (NavigableMap<P, PollingFileWatchKey<P, F>> group : groups) {
            root.put(group.firstKey(), group);
        }
        return root;
    }

    protected NavigableMap<P, PollingFileWatchKey<P, F>> getWatchKeyGroup(
            Collection<NavigableMap<P, PollingFileWatchKey<P, F>>> groups, Path path) {
        for (NavigableMap<P, PollingFileWatchKey<P, F>> group : groups) {
            P groupPath = group.firstKey();
            if (path.startsWith(groupPath) || groupPath.startsWith(path)) {
                return group;
            }
        }
        return null;
    }

    protected Map<P, V> buildIndex(Collection<NavigableMap<P, PollingFileWatchKey<P, F>>> root) {
        Map<P, V> index = new LinkedHashMap<>();
        for (NavigableMap<P, PollingFileWatchKey<P, F>> group : root) {
            F watchable = group.firstEntry().getValue().watchable();
            try (FileStream<F> tree = watchable.tree()) {
                for (F file : tree) {
                    if (file.isRegularFile()) {
                        index.put(file.getPath(), compareKey.apply(file));
                    }
                }
            } catch (Exception e) {
                log.warn("watch ERROR", e);
                throw UncheckedException.unchecked(e);
            }
        }
        return index;
    }

    protected boolean startsWithAny(P path, Collection<P> paths) {
        return paths.stream().anyMatch(path::startsWith);
    }
}
