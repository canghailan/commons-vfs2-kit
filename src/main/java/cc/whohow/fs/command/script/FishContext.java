package cc.whohow.fs.command.script;

import cc.whohow.fs.command.FileShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.Bindings;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FishContext implements Bindings {
    private static final Logger log = LogManager.getLogger(FishContext.class);
    protected final FileShell fish;
    protected final Map<String, Object> cache = new ConcurrentHashMap<>();
    protected final Map<String, Object> keyValues = new ConcurrentHashMap<>();

    public FishContext(FileShell fish) {
        this.fish = fish;
    }

    @Override
    public Object put(String name, Object value) {
        log.trace("put({}, {})", name, value);
        return keyValues.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        log.trace("putAll({})", toMerge);
        keyValues.putAll(toMerge);
    }

    @Override
    public void clear() {
        log.trace("clear");
        keyValues.clear();
    }

    @Override
    public Set<String> keySet() {
        log.trace("keySet");
        return keyValues.keySet();
    }

    @Override
    public Collection<Object> values() {
        log.trace("values");
        return keyValues.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        log.trace("entrySet");
        return keyValues.entrySet();
    }

    @Override
    public int size() {
        log.trace("size");
        return keyValues.size();
    }

    @Override
    public boolean isEmpty() {
        log.trace("isEmpty");
        return keyValues.isEmpty();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean containsKey(Object key) {
        log.trace("containsKey({})", key);
        return fish.getCommands().contains(key) || keyValues.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        log.trace("containsValue({})", value);
        return keyValues.containsValue(value);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public Object get(Object key) {
        log.trace("get({})", key);
        if (fish.getCommands().contains(key)) {
            return cache.computeIfAbsent(key.toString(), this::newCommand);
        } else {
            return keyValues.get(key);
        }
    }

    @Override
    public Object remove(Object key) {
        log.trace("remove({})", key);
        return keyValues.remove(key);
    }

    protected Object newCommand(String name) {
        return new FishCommand(fish.newCommand(name));
    }
}
