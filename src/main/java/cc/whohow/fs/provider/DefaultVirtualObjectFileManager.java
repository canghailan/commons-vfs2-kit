package cc.whohow.fs.provider;

import cc.whohow.fs.ObjectFile;
import cc.whohow.fs.ObjectFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultVirtualObjectFileManager implements ObjectFileManager {
    private static final Logger log = LogManager.getLogger(DefaultVirtualObjectFileManager.class);

    protected final Map<String, ObjectFileManager> objectFileManagers = new ConcurrentHashMap<>();

    public void loadProvider(ObjectFileManager objectFileManager) {
        objectFileManagers.put(objectFileManager.getScheme(), objectFileManager);
    }

    public Map<String, ObjectFileManager> getProviders() {
        return Collections.unmodifiableMap(objectFileManagers);
    }

    @Override
    public String getScheme() {
        return "vos"; // VirtualObjectStorage
    }

    @Override
    public ObjectFile get(CharSequence uri) {
        return getObjectFileManager(uri).get(uri);
    }

    @Override
    public void close() throws Exception {
        log.debug("close DefaultVirtualObjectFileManager");
        for (ObjectFileManager objectFileManager : objectFileManagers.values()) {
            try {
                objectFileManager.close();
            } catch (Throwable e) {
                log.warn("close ObjectFileManager error", e);
            }
        }
    }

    protected ObjectFileManager getObjectFileManager(CharSequence uri) {
        return objectFileManagers.get(getScheme(uri));
    }

    protected String getScheme(CharSequence uri) {
        return URI.create(uri.toString()).getScheme();
    }
}
