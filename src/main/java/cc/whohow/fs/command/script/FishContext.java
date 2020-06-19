package cc.whohow.fs.command.script;

import cc.whohow.fs.command.FileShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.Bindings;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FishContext implements Bindings {
    private static final Logger log = LogManager.getLogger(FishContext.class);
    protected final FileShell fish;
    protected final Map<String, Object> cache = new ConcurrentHashMap<>();
    protected final Map<String, Object> global = new ConcurrentHashMap<>();

    public FishContext(FileShell fish) {
        this.fish = fish;
        this.global.put("VFS", fish.getVirtualFileSystem());
        this.global.put("PWD", Paths.get(".").toAbsolutePath().normalize().toUri().toString());
    }

    @Override
    public Object put(String name, Object value) {
        log.trace("put({}, {})", name, value);
        return global.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        log.trace("putAll({})", toMerge);
        global.putAll(toMerge);
    }

    @Override
    public void clear() {
        log.trace("clear");
        global.clear();
    }

    @Override
    public Set<String> keySet() {
        log.trace("keySet");
        return global.keySet();
    }

    @Override
    public Collection<Object> values() {
        log.trace("values");
        return global.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        log.trace("entrySet");
        return global.entrySet();
    }

    @Override
    public int size() {
        log.trace("size");
        return global.size();
    }

    @Override
    public boolean isEmpty() {
        log.trace("isEmpty");
        return global.isEmpty();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean containsKey(Object key) {
        log.trace("containsKey({})", key);
        return fish.getCommands().contains(key) || global.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        log.trace("containsValue({})", value);
        return global.containsValue(value);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public Object get(Object key) {
        log.trace("get({})", key);
        if (fish.getCommands().contains(key)) {
            return cache.computeIfAbsent(key.toString(), this::newCommand);
        } else {
            return global.get(key);
        }
    }

    @Override
    public Object remove(Object key) {
        log.trace("remove({})", key);
        return global.remove(key);
    }

    protected Object newCommand(String name) {
        return new FishCommand(fish, name);
    }
}
