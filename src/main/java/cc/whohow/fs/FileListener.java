package cc.whohow.fs;

import java.util.EventListener;

@FunctionalInterface
public interface FileListener extends EventListener {
    void handleEvent(FileEvent event) throws Exception;
}
